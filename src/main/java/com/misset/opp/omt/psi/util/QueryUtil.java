package com.misset.opp.omt.psi.util;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.misset.opp.omt.external.util.rdf.RDFModelUtil;
import com.misset.opp.omt.psi.*;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayList;
import java.util.List;
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

        holder.newAnnotation(
                HighlightSeverity.INFORMATION,
                String.format("Type(s): %s", resourcesAsTypes(resources, (OMTFile) step.getContainingFile()))
        ).create();
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
                resource -> resource.getURI().equals(curieElement.getAsResource().toString())
        );
        if (!validPredicate) {
            setWrongPredicate(previousStep, curieElement, direction, holder);
        }
    }

    private String resourcesAsTypes(List<Resource> resources, OMTFile file) {
        return resources.stream()
                .map(file::resourceToCurie)
                .collect(Collectors.joining(", "));
    }

    private void setWrongPredicate(List<Resource> types, OMTCurieElement curieElement, String direction, AnnotationHolder holder) {
        holder.newAnnotation(
                HighlightSeverity.ERROR,
                String.format("%s is not a known %s-path predicate for type(s): %s",
                        curieElement.getAsResource(),
                        direction,
                        resourcesAsTypes(types, (OMTFile) curieElement.getContainingFile())
                ))
                .create();
    }
}
