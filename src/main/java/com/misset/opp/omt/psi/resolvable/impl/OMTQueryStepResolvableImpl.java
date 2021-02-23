package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTQueryFilter;
import com.misset.opp.omt.psi.OMTQueryStep;
import com.misset.opp.omt.psi.OMTTypes;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.misset.opp.util.UtilManager.getQueryUtil;
import static com.misset.opp.util.UtilManager.getRDFModelUtil;

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
    public boolean isType() {
        return getCurieElement() != null &&
                getRDFModelUtil().isTypePredicate(getCurieElement().getAsResource());
    }

    @Override
    public List<Resource> resolveToResource() {
        return resolveToResource(true);
    }

    @Override
    public List<Resource> resolveToResource(boolean filter) {
        List<Resource> resources = new ArrayList<>();
        if (getConstantValue() != null) {
            resources = getConstantValue().resolveToResource();
        } else if (getVariable() != null) {
            resources = getVariable().getType();
        } else if (getCurieElement() != null) {
            // a type is a value not an instance of a class, return an empty list, will be caught by annotator
            if (getQueryUtil().isPreviousStepAType(this)) {
                return new ArrayList<>();
            }
            List<Resource> previousStep = getQueryUtil().getPreviousStepResources(this);
            if (canLookBack() && !previousStep.isEmpty()) {
                return getRDFModelUtil().listObjectsWithSubjectPredicate(previousStep, getCurieElement().getAsResource());
            }
            return getCurieElement().resolveToResource();
        } else if (getOperatorCall() != null) {
            return getOperatorCall().resolveToResource();
        } else if (getNegatedStep() != null) {
            return Collections.singletonList(getRDFModelUtil().getBooleanType());
        }
        return filter ? filter(resources) : resources;
    }

    @Override
    public boolean canLookBack() {
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
