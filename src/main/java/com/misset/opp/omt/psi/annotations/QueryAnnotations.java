package com.misset.opp.omt.psi.annotations;

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.*;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.intentions.query.MergeFiltersIntention.getMergeFilterIntention;
import static com.misset.opp.omt.psi.intentions.query.UnwrapIntention.getUnwrapIntention;
import static com.misset.opp.omt.psi.util.UtilManager.*;

public class QueryAnnotations {

    public void annotateQueryStep(OMTQueryStep step, AnnotationHolder holder) {
        // resolve the querystep to a class or type
        final List<Resource> resources = step
                .resolveToResource()
                .stream()
                .filter(resource -> getRDFModelUtil().isClassOrType(resource))
                .collect(Collectors.toList());

        // if none found, resolve it as a curie step
        if (resources.isEmpty()) {
            annotateQueryCurieElement(step, holder);
        }
        // throw warning for multiple filters on a single step
        if (step.getQueryFilterList().size() > 1) {
            annotateMultipleFilters(step, holder);
        }
        // annotate the subquery
        if (step instanceof OMTSubQuery) {
            annotateSubQuery((OMTSubQuery) step, holder);
        }

        // if the step is decorated with an asterix or plus, check if this is acceptable
        if (step.getStepDecorator() != null && step.getChildren()[0] instanceof OMTSubQuery) {
            final OMTSubQuery subQuery = (OMTSubQuery) step.getChildren()[0];
            if (!getQueryUtil().isDecoratable(subQuery.getQuery())) {
                holder.newAnnotation(HighlightSeverity.ERROR,
                        "Invalid decorator")
                        .range(step.getStepDecorator())
                        .create();
            }
        }

        holder.newAnnotation(
                HighlightSeverity.INFORMATION,
                String.format("Type(s): %s", ((OMTFile) step.getContainingFile()).resourcesAsTypes(resources))
        ).create();
    }

    private void annotateMultipleFilters(OMTQueryStep step, AnnotationHolder holder) {
        final List<OMTQueryFilter> queryFilters = step.getQueryFilterList();
        final OMTQueryFilter firstFilter = queryFilters.get(0);
        final OMTQueryFilter lastFilter = queryFilters.get(queryFilters.size() - 1);
        TextRange textRange = TextRange.create(firstFilter.getTextOffset(), lastFilter.getTextOffset() + lastFilter.getTextLength());
        holder.newAnnotation(
                HighlightSeverity.WARNING,
                "Multiple filter steps on the same step")
                .withFix(getMergeFilterIntention(step))
                .range(textRange)
                .create();
    }

    public void annotateSubQuery(OMTSubQuery subQuery, AnnotationHolder holder) {
        if (!getQueryUtil().isWrappableStep(subQuery)) {
            holder.newAnnotation(HighlightSeverity.WARNING,
                    "Unnecessary wrapping of statement")
                    .withFix(getUnwrapIntention(subQuery))
                    .create();
        }
    }

    public void annotateQueryCurieElement(OMTQueryStep step, AnnotationHolder holder) {
        List<Resource> previousStep = getQueryUtil().getPreviousStep(step);
        previousStep = previousStep.stream().filter(resource -> getRDFModelUtil().isClassOrType(resource)).collect(Collectors.toList());
        if (previousStep.isEmpty()) {
            return;
        } // no error when type resolving has failed
        if (step instanceof OMTCurieConstantElement) {
            return;
        }

        if (step instanceof OMTQueryReverseStep) {
            final OMTQueryReverseStep reverseStep = (OMTQueryReverseStep) step;
            final OMTCurieElement curieElement = reverseStep.getQueryStep() != null ?
                    reverseStep.getQueryStep().getCurieElement() : null;
            if (curieElement != null) {
                final List<Resource> resources = new ArrayList<>(getRDFModelUtil().listPredicatesForObjectClass(previousStep).keySet());
                validatePredicates(resources, curieElement, previousStep, holder, "REVERSE");
            }
        } else {
            final OMTCurieElement curieElement = step.getCurieElement();
            if (!(step.getParent() instanceof OMTQueryReverseStep)) {
                final List<Resource> resources = new ArrayList<>(getRDFModelUtil().listPredicatesForSubjectClass(previousStep).keySet());
                validatePredicates(resources, curieElement, previousStep, holder, "FORWARD");
            }
        }
    }

    private void validatePredicates(List<Resource> resources, OMTCurieElement curieElement, List<Resource> previousStep, AnnotationHolder holder, String direction) {

        boolean validPredicate = resources.stream().anyMatch(
                resource -> curieElement != null && curieElement.getAsResource() != null && curieElement.getAsResource().equals(resource)
        );
        if (!validPredicate) {
            annotateWrongPredicate(previousStep, curieElement, direction, holder);
        }
    }

    private void annotateWrongPredicate(List<Resource> resources, OMTCurieElement curieElement, String direction, AnnotationHolder holder) {
        if (curieElement == null) {
            return;
        }
        holder.newAnnotation(
                HighlightSeverity.ERROR,
                String.format("%s is not a known %s-path predicate for type(s): %s",
                        curieElement.getAsResource(),
                        direction,
                        ((OMTFile) curieElement.getContainingFile()).resourcesAsTypes(resources)
                ))
                .create();
    }

    public void annotateAddToCollection(OMTAddToCollection addToCollection, AnnotationHolder holder) {
        final List<Resource> assignee = addToCollection.getQuery().resolveToResource();
        final List<Resource> value = addToCollection.getResolvableValue().resolveToResource();
        annotateAssignment(assignee, value, holder, addToCollection.getResolvableValue());
    }

    public void annotateRemoveFromCollection(OMTRemoveFromCollection removeFromCollection, AnnotationHolder holder) {
        final List<Resource> assignee = removeFromCollection.getQuery().resolveToResource();
        final List<Resource> value = removeFromCollection.getResolvableValue().resolveToResource();
        annotateAssignment(assignee, value, holder, removeFromCollection.getResolvableValue());
    }

    public void annotateAssignmentStatement(OMTAssignmentStatement assignmentStatement, AnnotationHolder holder) {
        final List<Resource> assignee = assignmentStatement.getQuery().resolveToResource();
        final List<Resource> value = assignmentStatement.getResolvableValue().resolveToResource();
        annotateAssignment(assignee, value, holder, assignmentStatement.getResolvableValue());
    }

    private void annotateAssignment(List<Resource> assignee, List<Resource> value, AnnotationHolder holder, PsiElement target) {
        assignee = getRDFModelUtil().appendAllSubclassesAndImplementations(assignee);
        value = getRDFModelUtil().appendAllSubclassesAndImplementations(value);
        final boolean compatibleTypes = getRDFModelUtil().validateType(assignee, value);
        if (!compatibleTypes) {
            setIncompatibleTypes(assignee, value, holder, target);
        }
    }

    public void annotateEquationStatement(OMTEquationStatement equationStatement, AnnotationHolder holder) {
        annotateAssignment(
                equationStatement.getQueryList().get(0).resolveToResource(),
                equationStatement.getQueryList().get(1).resolveToResource(),
                holder,
                equationStatement);
    }

    private void setIncompatibleTypes(List<Resource> leftHand, List<Resource> rightHand, AnnotationHolder holder, PsiElement target) {
        holder.newAnnotation(HighlightSeverity.ERROR,
                String.format("Incompatible types LEFT-HAND: %s, RIGHT-HAND %s",
                        leftHand.stream().map(Resource::getLocalName).sorted().collect(Collectors.joining(", ")),
                        rightHand.stream().map(Resource::getLocalName).sorted().collect(Collectors.joining(", ")))

        ).range(target).create();
    }

    public void annotateIfBlock(OMTIfBlock omtIfBlock, AnnotationHolder holder) {
        annotateBoolean(omtIfBlock.getQuery().resolveToResource(), holder, omtIfBlock.getQuery());
    }

    public void annotateBooleanStatement(OMTBooleanStatement booleanStatement, AnnotationHolder holder) {
        booleanStatement.getQueryList().forEach(
                query -> annotateBoolean(query.resolveToResource(), holder, query)
        );

    }

    public void annotateQueryPath(OMTQueryPath queryPath, AnnotationHolder holder) {
        final List<ASTNode> duplicateSiblings = getTokenFinderUtil().getDuplicateSiblings(queryPath.getNode(), OMTTypes.FORWARD_SLASH);
        duplicateSiblings.forEach(
                node -> holder.newAnnotation(HighlightSeverity.ERROR, "Unexpected token").range(node).create()
        );
    }

    private void annotateBoolean(List<Resource> valueType, AnnotationHolder holder, PsiElement range) {
        final Resource booleanType = getRDFModelUtil().getPrimitiveTypeAsResource("boolean");
        if (valueType == null || valueType.isEmpty()) {
            return;
        }
        if (valueType.stream().noneMatch(
                booleanType::equals
        )) {
            holder.newAnnotation(HighlightSeverity.ERROR,
                    String.format("Expected boolean, got %s",
                            valueType.stream().map(Resource::getLocalName).sorted().collect(Collectors.joining(", ")))
            ).range(range).create();
        }
    }
}
