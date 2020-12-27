package com.misset.opp.omt.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.OMTModelItemTypeElement;
import com.misset.opp.omt.psi.support.OMTModelItemBlockSup;
import org.jetbrains.annotations.NotNull;

public abstract class OMTModelItemBlockSupImpl extends OMTLabelledElementImpl implements OMTModelItemBlockSup {
    public OMTModelItemBlockSupImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiElement getLabel() {
        return getNode().getPsi(OMTModelItemBlock.class).getModelItemLabel().getPropertyLabel();
    }

    @Override
    public OMTBlock getBlock() {
        return getNode().getPsi(OMTModelItemBlock.class).getIndentedBlock();
    }

    @Override
    public String getType() {
        final OMTModelItemTypeElement modelItemTypeElement = getNode().getPsi(OMTModelItemBlock.class).getModelItemLabel().getModelItemTypeElement();
        return modelItemTypeElement.getText().substring(1); // return type without flag token
    }
}
