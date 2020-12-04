package com.misset.opp.omt.psi.resolvable;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTCommandBlock;
import com.misset.opp.omt.psi.OMTReturnStatement;
import com.misset.opp.omt.util.ProjectUtil;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class OMTCommandBlockResolvableImpl extends ASTWrapperPsiElement implements OMTCommandBlock {

    private static final ProjectUtil projectUtil = ProjectUtil.SINGLETON;

    public OMTCommandBlockResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<Resource> resolveToResource() {
        final OMTReturnStatement returnStatement = PsiTreeUtil.findChildOfType(this, OMTReturnStatement.class);
        if (returnStatement != null && returnStatement.getResolvableValue() != null) {
            return returnStatement.getResolvableValue().resolveToResource();
        }
        return projectUtil.getRDFModelUtil().getAnyTypeAsList();
    }
}
