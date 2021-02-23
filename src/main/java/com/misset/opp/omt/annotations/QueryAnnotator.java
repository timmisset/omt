package com.misset.opp.omt.annotations;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTAddToCollection;
import com.misset.opp.omt.psi.OMTAssignmentStatement;
import com.misset.opp.omt.psi.OMTBooleanStatement;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.OMTDefineQueryStatement;
import com.misset.opp.omt.psi.OMTEquationStatement;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTQueryReverseStep;
import com.misset.opp.omt.psi.OMTQueryStep;
import com.misset.opp.omt.psi.OMTRemoveFromCollection;
import com.misset.opp.omt.psi.OMTSubQuery;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.misset.opp.omt.intentions.query.MergeFiltersIntention.getMergeFilterIntention;
import static com.misset.opp.omt.intentions.query.UnwrapIntention.getUnwrapIntention;
import static com.misset.opp.util.UtilManager.getQueryUtil;
import static com.misset.opp.util.UtilManager.getRDFModelUtil;

public class QueryAnnotator extends AbstractAnnotator {

    private OMTQueryStep step;

    public QueryAnnotator(AnnotationHolder holder) {
        super(holder);
    }

    public void annotate(PsiElement element) {
        if (element instanceof OMTDefineQueryStatement) {
            annotate((OMTDefineQueryStatement) element);
        } else if (element instanceof OMTAddToCollection) {
            annotate((OMTAddToCollection) element);
        } else if (element instanceof OMTRemoveFromCollection) {
            annotate((OMTRemoveFromCollection) element);
        } else if (element instanceof OMTAssignmentStatement) {
            annotate((OMTAssignmentStatement) element);
        } else if (element instanceof OMTEquationStatement) {
            annotate((OMTEquationStatement) element);
        } else if (element instanceof OMTBooleanStatement) {
            annotate((OMTBooleanStatement) element);
        } else if (element instanceof OMTQueryStep) {
            annotate((OMTQueryStep) element);
        }
    }

    private void annotate(OMTQueryStep step) {
        this.step = step;
        // resolve the querystep to a class or type
        final List<Resource> resources = step
                .resolveToResource()
                .stream()
                .filter(resource -> getRDFModelUtil().isClassOrType(resource))
                .collect(Collectors.toList());

        // if none found, resolve it as a curie step
        if (resources.isEmpty()) {
            annotateQueryCurieElement();
        }
        // throw warning for multiple filters on a single step
        if (step.getQueryFilterList().size() > 1) {
            annotateMultipleFilters();
        }
        // annotate the subquery
        if (step instanceof OMTSubQuery) {
            annotateSubQuery();
        }

        // if the step is decorated with an asterix or plus, check if this is acceptable
        if (step.getStepDecorator() != null && step.getChildren()[0] instanceof OMTSubQuery) {
            final OMTSubQuery subQuery = (OMTSubQuery) step.getChildren()[0];
            if (!getQueryUtil().isDecoratable(subQuery.getQuery())) {
                setError("Invalid decorator");
            }
        }

        final String message = String.format("Type(s): %s", ((OMTFile) step.getContainingFile()).resourcesAsTypes(resources));
        setInformation(message);
    }

    private void annotateMultipleFilters() {
        setWarning("Multiple filter steps on the same step",
                annotationBuilder -> annotationBuilder.withFix(getMergeFilterIntention(step)));
    }

    private void annotateSubQuery() {
        if (!getQueryUtil().isWrappableStep((OMTSubQuery) step)) {
            setWarning("Unnecessary wrapping of statement",
                    annotationBuilder -> annotationBuilder.withFix(getUnwrapIntention((OMTSubQuery) step)));
        }
    }

    private void annotate(OMTAddToCollection addToCollection) {
        final List<Resource> assignee = addToCollection.getQuery().resolveToResource();
        final List<Resource> value = addToCollection.getResolvableValue().resolveToResource();
        annotateAssignment(assignee, value);
    }

    private void annotate(OMTRemoveFromCollection removeFromCollection) {
        final List<Resource> assignee = removeFromCollection.getQuery().resolveToResource();
        final List<Resource> value = removeFromCollection.getResolvableValue().resolveToResource();
        annotateAssignment(assignee, value);
    }

    private void annotate(OMTAssignmentStatement assignmentStatement) {
        final List<Resource> assignee = assignmentStatement.getQuery().resolveToResource();
        final List<Resource> value = assignmentStatement.getResolvableValue().resolveToResource();
        annotateAssignment(assignee, value);
    }

    private void annotate(OMTEquationStatement equationStatement) {
        annotateAssignment(
                equationStatement.getQueryList().get(0).resolveToResource(),
                equationStatement.getQueryList().get(1).resolveToResource()
        );
    }

    private void setIncompatibleTypes(List<Resource> leftHand, List<Resource> rightHand) {
        final String message = String.format("Incompatible types LEFT-HAND: %s, RIGHT-HAND %s",
                leftHand.stream().map(Resource::getLocalName).sorted().collect(Collectors.joining(", ")),
                rightHand.stream().map(Resource::getLocalName).sorted().collect(Collectors.joining(", ")));
        setError(message);
    }

    private void annotate(OMTBooleanStatement booleanStatement) {
        booleanStatement.getQueryList().forEach(
                query ->
                        annotateBoolean(query.resolveToResource(), query.getText())
        );
    }

    private void annotate(OMTDefineQueryStatement statement) {
        if (!statement.getText().trim().endsWith(";")) {
            setError("; expected");
        }
    }

    private void annotateAssignment(List<Resource> assignee, List<Resource> value) {
        assignee = getRDFModelUtil().appendAllSubclassesAndImplementations(assignee);
        value = getRDFModelUtil().appendAllSubclassesAndImplementations(value);
        final boolean compatibleTypes = getRDFModelUtil().validateType(assignee, value);
        if (!compatibleTypes) {
            setIncompatibleTypes(assignee, value);
        }
    }

    private void validatePredicates(List<Resource> resources, @NotNull OMTCurieElement curieElement, List<Resource> previousStep, String direction) {
        if (resources.stream().noneMatch(
                resource -> resource.equals(curieElement.getAsResource())
        )) {
            annotateWrongPredicate(previousStep, curieElement, direction);
        }
    }

    private void annotateWrongPredicate(List<Resource> resources, @NotNull OMTCurieElement curieElement, String direction) {
        final String message = String.format("%s is not a known %s-path predicate for type(s): %s",
                curieElement.getAsResource(),
                direction,
                ((OMTFile) curieElement.getContainingFile()).resourcesAsTypes(resources)
        );
        setError(message);
    }

    private void annotateQueryCurieElement() {

        List<Resource> previousStep = getQueryUtil().getPreviousStepResources(step);
        if (previousStep.contains(getRDFModelUtil().getOwlThing())) {
            return;
        } // accept all from any
        previousStep = previousStep.stream().filter(resource -> getRDFModelUtil().isClassOrType(resource)).collect(Collectors.toList());
        final OMTCurieElement curieElement = step.getCurieElement();
        if (previousStep.isEmpty() || curieElement == null) {
            return;
        }

        if (step instanceof OMTQueryReverseStep) {
            final List<Resource> resources = new ArrayList<>(getRDFModelUtil().listPredicatesForObjectClass(previousStep).keySet());
            validatePredicates(resources, curieElement, previousStep, "REVERSE");
        } else {
            final List<Resource> resources = new ArrayList<>(getRDFModelUtil().listPredicatesForSubjectClass(previousStep).keySet());
            validatePredicates(resources, curieElement, previousStep, "FORWARD");
        }
    }
}
