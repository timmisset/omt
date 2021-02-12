package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTCommandBlock;
import com.misset.opp.omt.psi.OMTReturnStatement;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.misset.opp.omt.util.UtilManager.getRDFModelUtil;

public abstract class OMTCommandBlockResolvableImpl extends ASTWrapperPsiElement implements OMTCommandBlock {

    public OMTCommandBlockResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<Resource> resolveToResource() {
        final OMTReturnStatement returnStatement = PsiTreeUtil.findChildOfType(this, OMTReturnStatement.class);
        if (returnStatement != null && returnStatement.getResolvableValue() != null) {
            return returnStatement.getResolvableValue().resolveToResource();
        }
        return getRDFModelUtil().getAnyTypeAsList();
    }
}
