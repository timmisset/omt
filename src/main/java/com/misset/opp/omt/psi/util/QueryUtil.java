package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTBooleanStatement;
import com.misset.opp.omt.psi.OMTEquationStatement;
import com.misset.opp.omt.psi.OMTIfBlock;
import com.misset.opp.omt.psi.OMTNegatedStep;
import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.OMTQueryArray;
import com.misset.opp.omt.psi.OMTQueryFilter;
import com.misset.opp.omt.psi.OMTQueryPath;
import com.misset.opp.omt.psi.OMTQueryStep;
import com.misset.opp.omt.psi.OMTSignatureArgument;
import com.misset.opp.omt.psi.OMTStepDecorator;
import com.misset.opp.omt.psi.OMTSubQuery;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static com.misset.opp.util.UtilManager.getRDFModelUtil;

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
        if (PsiTreeUtil.getParentOfType(subQuery, OMTIfBlock.class) != null) {
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
     * /ont:ClassA / rdf:type / (CURRENT_STEP)*             will resolve to it's parent (SubQuery) and then the previous step
     * $myVariable [rdf:type == CURRENT_STEP]               CURRENT_STEP is the start of it's own query path, will return the types of the step that contains the filter
     * $myVariable / SOME_OPERATOR(CURRENT_STEP)            Contained in a signature argument, cannot inherit types
     */
    @NotNull
    public List<Resource> getPreviousStepResources(@Nullable PsiElement step) {
        PsiElement previous = getPreviousSibling(step, OMTQueryPath.class, OMTQueryStep.class);
        if (previous == null) {
            // retrieve the previous value via the parent
            final PsiElement container = getParent(step, OMTSubQuery.class, OMTQueryFilter.class, OMTSignatureArgument.class);
            if (container instanceof OMTQueryFilter) {
                // resolve the filter
                return getPreviousStepResources((OMTQueryFilter) container);
            } else if (container instanceof OMTSubQuery) {
                // resolve the step before the SubQuery
                return getPreviousStepResources(container);
            }
            // OMTSignatureArgument doesn't inherit values from it's previous step
            return new ArrayList<>();
        }
        List<Resource> typesForStep = new ArrayList<>(resolvePathPart(previous));
        typesForStep.addAll(getRDFModelUtil().allSubClasses(typesForStep));
        return getRDFModelUtil().getDistinctResources(typesForStep);
    }

    public List<Resource> getPreviousStepResources(OMTQueryFilter filter) {
        final List<Resource> resources = ((OMTQueryStep) filter.getParent()).resolveToResource(false);
        resources.addAll(getRDFModelUtil().allSubClasses(resources));
        return getRDFModelUtil().getDistinctResources(resources);
    }

    public boolean isPreviousStepAType(OMTQueryStep step) {
        OMTQueryStep previous = (OMTQueryStep) getPreviousSibling(step, OMTQueryStep.class);
        if (previous == null) {
            final OMTQueryFilter filter = PsiTreeUtil.getParentOfType(step, OMTQueryFilter.class);
            return filter != null && ((OMTQueryStep) filter.getParent()).isType();
        }
        return previous.isType();
    }

    @SafeVarargs
    private PsiElement getPreviousSibling(PsiElement element, Class<? extends PsiElement>... ofTypes) {
        return getElementOrContinueWith(element, PsiElement::getPrevSibling, ofTypes);
    }

    @SafeVarargs
    private PsiElement getParent(PsiElement element, Class<? extends PsiElement>... ofTypes) {
        return getElementOrContinueWith(element, PsiElement::getParent, ofTypes);
    }

    @SafeVarargs
    private PsiElement getElementOrContinueWith(PsiElement element, UnaryOperator<PsiElement> continueWith, Class<? extends PsiElement>... ofTypes) {
        if (element == null) {
            return null;
        }
        PsiElement continueWithElement = continueWith.apply(element);
        while (continueWithElement != null && !isAssignableFrom(continueWithElement, ofTypes)) {
            continueWithElement = continueWith.apply(continueWithElement);
        }
        return continueWithElement;
    }

    @SafeVarargs
    private boolean isAssignableFrom(PsiElement element, Class<? extends PsiElement>... ofTypes) {
        for (Class<? extends PsiElement> clazz : ofTypes) {
            if (clazz.isAssignableFrom(element.getClass())) {
                return true;
            }
        }
        return false;
    }

}
