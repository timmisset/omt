package com.misset.opp.omt.psi.util;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.external.util.rdf.RDFModelUtil;
import com.misset.opp.omt.psi.*;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class QueryUtil {
    public static final QueryUtil SINGLETON = new QueryUtil();
    private static final ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    private RDFModelUtil rdfModelUtil;

    private RDFModelUtil getRDFModel() {
        if (rdfModelUtil == null || !rdfModelUtil.isLoaded()) {
            rdfModelUtil = new RDFModelUtil(projectUtil.getOntologyModel());
        }
        return rdfModelUtil;
    }

    public void annotateQueryStep(OMTQueryStep step, AnnotationHolder holder) {
        final List<Resource> resources = step
                .resolveToResource()
                .stream()
                .filter(resource -> getRDFModel().isClassOrType(resource))
                .collect(Collectors.toList());
        if (resources.isEmpty()) {
            validateQueryCurieElement(step, holder);
        }
        // annotate the subquery
        if (step instanceof OMTSubQuery) {
            annotateSubQuery((OMTSubQuery) step, holder);
        }

        // if the step is decorated with an asterix or plus, check if this is acceptable
        if (step.getStepDecorator() != null && step.getChildren()[0] instanceof OMTSubQuery) {
            final OMTSubQuery subQuery = (OMTSubQuery) step.getChildren()[0];
            if (!isDecoratable(subQuery.getQuery())) {
                holder.newAnnotation(HighlightSeverity.ERROR,
                        "Invalid decorator")
                        .range(step.getStepDecorator())
                        .create();
            }
        }

        holder.newAnnotation(
                HighlightSeverity.INFORMATION,
                String.format("Type(s): %s", resourcesAsTypes(resources, (OMTFile) step.getContainingFile()))
        ).create();
    }

    public void annotateSubQuery(OMTSubQuery subQuery, AnnotationHolder holder) {
        if (!wrappableStep(subQuery)) {
            holder.newAnnotation(HighlightSeverity.WARNING,
                    "Unnecessary wrapping of statement")
                    .create();
        }
    }

    private boolean wrappableStep(OMTSubQuery subQuery) {
        if (isDecorated(subQuery)) {
            return true;
        }
        if (subQuery.getQuery() instanceof OMTBooleanStatement ||
                subQuery.getQuery() instanceof OMTQueryArray) {
            return true;
        }
        if (PsiTreeUtil.findFirstParent(subQuery, parent -> parent instanceof OMTIfBlock) != null) {
            return true;
        }
        return subQuery.getParent() instanceof OMTQueryPath && subQuery.getParent().getParent() instanceof OMTNegatedStep;
    }

    private boolean isDecoratable(OMTQuery step) {
        if (step instanceof OMTBooleanStatement) {
            return false;
        }
        if (step instanceof OMTEquationStatement) {
            return false;
        }
        return true;
    }

    private boolean isDecorated(OMTQueryStep step) {
        return getDecorator(step) != null;
    }

    private OMTStepDecorator getDecorator(OMTQueryStep step) {
        if (step.getStepDecorator() != null) {
            return step.getStepDecorator();
        }
        return step.getParent() instanceof OMTQueryStep ? ((OMTQueryStep) step.getParent()).getStepDecorator() : null;
    }

    public void validateQueryCurieElement(OMTQueryStep step, AnnotationHolder holder) {
        List<Resource> previousStep = PsiImplUtil.getPreviousStep(step);
        previousStep = previousStep.stream().filter(resource -> getRDFModel().isClassOrType(resource)).collect(Collectors.toList());
        if (previousStep.isEmpty()) {
            return;
        } // no error when type resolving has failed
        if (step instanceof OMTCurieConstantElement) {
            return;
        }

        if (step instanceof OMTQueryReverseStep) {
            final OMTQueryReverseStep reverseStep = (OMTQueryReverseStep) step;
            final OMTCurieElement curieElement = reverseStep.getQueryStep().getCurieElement();
            if (curieElement != null) {
                final List<Resource> resources = new ArrayList<>(getRDFModel().listPredicatesForObjectClass(previousStep).keySet());
                validatePredicates(resources, curieElement, previousStep, holder, "REVERSE");
            }
        } else {
            final OMTCurieElement curieElement = step.getCurieElement();
            if (!(step.getParent() instanceof OMTQueryReverseStep)) {
                final List<Resource> resources = new ArrayList<>(getRDFModel().listPredicatesForSubjectClass(previousStep).keySet());
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

    private String resourcesAsTypes(List<Resource> resources, OMTFile file) {
        return resources.stream()
                .map(file::resourceToCurie)
                .collect(Collectors.joining(", "));
    }

    private void annotateWrongPredicate(List<Resource> types, OMTCurieElement curieElement, String direction, AnnotationHolder holder) {
        if (curieElement == null) {
            return;
        }
        holder.newAnnotation(
                HighlightSeverity.ERROR,
                String.format("%s is not a known %s-path predicate for type(s): %s",
                        curieElement.getAsResource(),
                        direction,
                        resourcesAsTypes(types, (OMTFile) curieElement.getContainingFile())
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

    private void annotateAssignment(List<Resource> assignee, List<Resource> value, AnnotationHolder holder, PsiElement range) {
        validateType(assignee, value,
                (acceptableTypes, argumentTypes) ->
                        holder.newAnnotation(HighlightSeverity.ERROR,
                                String.format("Incorrect type, %s expected but value is of type: %s",
                                        acceptableTypes.stream().map(Resource::getLocalName).sorted().collect(Collectors.joining(", ")),
                                        argumentTypes.stream().map(Resource::getLocalName).sorted().collect(Collectors.joining(", ")))

                        ).range(range).create());
    }

    public void annotateEquationStatement(OMTEquationStatement equationStatement, AnnotationHolder holder) {
        final List<Resource> leftHand = equationStatement.getQueryList().get(0).resolveToResource();
        final List<Resource> rightHand = equationStatement.getQueryList().get(1).resolveToResource();
        validateType(leftHand, rightHand,
                (acceptableTypes, argumentTypes) ->
                        holder.newAnnotation(HighlightSeverity.ERROR,
                                String.format("Incompatible types LEFT-HAND: %s, RIGHT-HAND %s",
                                        acceptableTypes.stream().map(Resource::getLocalName).sorted().collect(Collectors.joining(", ")),
                                        argumentTypes.stream().map(Resource::getLocalName).sorted().collect(Collectors.joining(", ")))

                        ).create());
    }

    public void validateType(Resource resource, List<Resource> sender, BiConsumer<List<Resource>, List<Resource>> onFail) {
        validateType(Collections.singletonList(resource), sender, onFail);
    }

    /**
     * Validate if the type of the sender is acceptable by the receiver
     *
     * @param receiver
     * @param sender
     * @return
     */
    public void validateType(
            List<Resource> receiver,
            List<Resource> sender,
            BiConsumer<List<Resource>, List<Resource>> onFail) {
        final RDFModelUtil rdfModelUtil = projectUtil.getRDFModelUtil();
        List<Resource> acceptableTypes = new ArrayList<>(receiver);
        acceptableTypes.addAll(rdfModelUtil.allSubClasses(receiver));
        List<Resource> argumentTypes = new ArrayList<>(sender);
        argumentTypes.addAll(rdfModelUtil.allSubClasses(argumentTypes));
        argumentTypes = rdfModelUtil.getDistinctResources(rdfModelUtil.getClasses(argumentTypes));

        if (argumentTypes.stream().anyMatch(
                resource -> rdfModelUtil.getPrimitiveTypeAsResource("int").equals(resource) ||
                        rdfModelUtil.getPrimitiveTypeAsResource("decimal").equals(resource)
        )) {
            acceptableTypes.add(rdfModelUtil.getPrimitiveTypeAsResource("int"));
            acceptableTypes.add(rdfModelUtil.getPrimitiveTypeAsResource("decimal"));
        }

        if (acceptableTypes.isEmpty() ||
                argumentTypes.isEmpty() ||
                acceptableTypes.stream().noneMatch(rdfModelUtil::isClassOrType) ||
                argumentTypes.stream().noneMatch(rdfModelUtil::isClassOrType) ||
                argumentTypes.contains(rdfModelUtil.getAnyType())) {
            // when the argument type cannot be resolved to specific class or type it's resolved to any
            // which means we cannot validate the call
            return;
        }
        for (Resource argumentType : argumentTypes) {
            if (acceptableTypes.contains(argumentType)) {
                return; // acceptable type found
            }
        }
        onFail.accept(acceptableTypes, argumentTypes);
    }


}
