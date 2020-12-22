package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTCurieConstantElement;
import com.misset.opp.omt.psi.OMTQueryFilter;
import com.misset.opp.omt.psi.OMTQueryStep;
import com.misset.opp.omt.psi.OMTTypes;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.getQueryUtil;
import static com.misset.opp.omt.psi.util.UtilManager.getRDFModelUtil;

public abstract class OMTQueryStepResolvableImpl extends ASTWrapperPsiElement implements OMTQueryStep {

    public OMTQueryStepResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<Resource> filter(List<Resource> resources) {
        for (OMTQueryFilter filter : getQueryFilterList()) {
            resources = filter.getQuery() != null ? filter.getQuery().filter(resources) : resources;
        }
        return resources;
    }

    @Override
    public List<Resource> resolveToResource() {
        return resolveToResource(true);
    }

    @Override
    public List<Resource> resolveToResource(boolean filter) {
        // steps that do not include preceeding info
        List<Resource> resources = new ArrayList<>();
        if (getConstantValue() != null) {
            resources = getConstantValue().resolveToResource();
        } else if (getVariable() != null) {
            resources = getVariable().getType();
        } else if (getCurieElement() != null) {
            List<Resource> previousStep = getQueryUtil().getPreviousStep(this);
            if (canLookBack() && !previousStep.isEmpty()) {
                return getRDFModelUtil().listObjectsWithSubjectPredicate(previousStep, getCurieElement().getAsResource());
            }
            return getCurieElement().resolveToResource();
        } else if (getOperatorCall() != null) {
            return getOperatorCall().resolveToResource();
        }
        return filter ? filter(resources) : resources;
    }

    protected boolean canLookBack() {
        if (this instanceof OMTCurieConstantElement) {
            return false;
        }
        if (!firstStepInParent()) {
            return true;
        }
        final PsiElement prevLeaf = PsiTreeUtil.prevVisibleLeaf(this);

        return prevLeaf == null || prevLeaf.getNode().getElementType() != OMTTypes.FORWARD_SLASH;

    }

    protected boolean firstStepInParent() {
        return getParent() != null && getTextOffset() == getParent().getTextOffset();
    }

}
