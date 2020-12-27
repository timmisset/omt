package com.misset.opp.omt.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.support.OMTBlockEntrySup;
import org.jetbrains.annotations.NotNull;

public abstract class OMTModelItemBlockSupImpl extends OMTLabelledElementImpl implements OMTBlockEntrySup {
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
}
