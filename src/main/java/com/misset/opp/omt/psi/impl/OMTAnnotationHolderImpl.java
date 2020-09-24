package com.misset.opp.omt.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.support.OMTAnnotationHolder;
import org.jetbrains.annotations.NotNull;

public abstract class OMTAnnotationHolderImpl extends ASTWrapperPsiElement implements OMTAnnotationHolder {
    private boolean annotated;

    public OMTAnnotationHolderImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean isAnnotated() {
        return annotated;
    }

    @Override
    public void setAnnotated(boolean isAnnotated) {
        annotated = isAnnotated;
    }
}
