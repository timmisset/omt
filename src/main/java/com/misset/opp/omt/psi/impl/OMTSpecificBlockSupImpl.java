package com.misset.opp.omt.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTLeading;
import com.misset.opp.omt.psi.support.OMTBlockEntrySup;
import org.jetbrains.annotations.NotNull;

public abstract class OMTSpecificBlockSupImpl extends OMTLabelledElementImpl implements OMTBlockEntrySup {
    public OMTSpecificBlockSupImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiElement getLabel() {
        final PsiElement firstChild = getFirstChild();
        return firstChild instanceof OMTLeading ?
                PsiTreeUtil.nextVisibleLeaf(firstChild) :
                firstChild;
    }

    @Override
    public OMTBlock getBlock() {
        return null;
    }
}
