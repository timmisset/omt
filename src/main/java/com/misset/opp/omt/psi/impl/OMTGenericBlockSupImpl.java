package com.misset.opp.omt.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTGenericBlock;
import com.misset.opp.omt.psi.support.OMTBlockEntrySup;
import org.jetbrains.annotations.NotNull;

public abstract class OMTGenericBlockSupImpl extends OMTLabelledElementImpl implements OMTBlockEntrySup {
    public OMTGenericBlockSupImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiElement getLabel() {
        return getNode().getPsi(OMTGenericBlock.class).getPropertyLabel();
    }

    @Override
    public OMTBlock getBlock() {
        return getNode().getPsi(OMTGenericBlock.class).getIndentedBlock();
    }

}
