package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayList;
import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.getRDFModelUtil;

public class QueryUtil {
    private static List<Resource> resolvePathPart(PsiElement part) {
        if (part != null) {
            if (part instanceof OMTQueryStep) {
                return ((OMTQueryStep) part).resolveToResource();
            }
            if (part instanceof OMTQueryPath) {
                return ((OMTQueryPath) part).resolveToResource();
            }
            if (part instanceof OMTEquationStatement) {
                return ((OMTEquationStatement) part).resolveToResource();
            }
        }
        return new ArrayList<>();
    }

    public boolean isWrappableStep(OMTSubQuery subQuery) {
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

    private boolean isDecorated(OMTQueryStep step) {
        return getDecorator(step) != null;
    }

    private OMTStepDecorator getDecorator(OMTQueryStep step) {
        if (step.getStepDecorator() != null) {
            return step.getStepDecorator();
        }
        return step.getParent() instanceof OMTQueryStep ? ((OMTQueryStep) step.getParent()).getStepDecorator() : null;
    }

    public boolean isDecoratable(OMTQuery step) {
        if (step instanceof OMTBooleanStatement) {
            return false;
        }
        return !(step instanceof OMTEquationStatement);
    }

    /**
     * Returns the previous step that is able to pass the type to the current step
     * /ont:ClassA / rdf:type / CURRENT_STEP                will be the result of rdf:type which will get the result of /ont:ClassA
     * /ont:ClassA / rdf:type / (CURRENT_STEP)*             will resolve to it's parent (Subquery) and then the previous step
     * $myVariable [rdf:type == CURRENT_STEP]               CURRENT_STEP is the start of it's own query path, will return the types of the step that contains the filter
     * $myVariable / SOME_OPERATOR(CURRENT_STEP)            Contained in a signature argument, cannot inherit types
     *
     * @param step
     * @return
     */
    public List<Resource> getPreviousStep(PsiElement step) {
        PsiElement previous = PsiImplUtil.getPreviousSibling(step, OMTQueryPath.class, OMTQueryStep.class);
        if (previous == null) {
            // retrieve the previous value via the parent
            final PsiElement container = PsiImplUtil.getParent(step, OMTSubQuery.class, OMTQueryFilter.class, OMTSignatureArgument.class);
            if (container instanceof OMTQueryFilter) {
                // resolve the filter
                return getPreviousStep((OMTQueryFilter) container);
            } else if (container instanceof OMTSubQuery) {
                // resolve the step before the subquery
                return getPreviousStep(container);
            }
            // OMTSignatureArgument doesn't inherit values from it's previous step
            return new ArrayList<>();
        }
        List<Resource> typesForStep = new ArrayList<>(resolvePathPart(previous));
        typesForStep.addAll(getRDFModelUtil().allSubClasses(typesForStep));
        return getRDFModelUtil().getDistinctResources(typesForStep);
    }

    public List<Resource> getPreviousStep(OMTQueryFilter filter) {
        final List<Resource> resources = ((OMTQueryStep) filter.getParent()).resolveToResource(true, false);
        resources.addAll(getRDFModelUtil().allSubClasses(resources));
        return getRDFModelUtil().getDistinctResources(resources);
    }

}
