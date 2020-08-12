package com.misset.opp.omt.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.OMTImportSource;
import com.misset.opp.omt.psi.named.OMTVariableNamedElement;
import com.misset.opp.omt.psi.references.ImportReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OMTImportNamedElementImpl extends ASTWrapperPsiElement implements OMTVariableNamedElement {
    public OMTImportNamedElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    public PsiReference getReference() {
        return toReference((OMTImportSource) getNode().getPsi());
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        return new PsiReference[]{toReference((OMTImportSource) getNode().getPsi())};
    }

    private PsiReference toReference(OMTImportSource importSource) {
        TextRange property = new TextRange(0, importSource.getText().length());
        return new ImportReference(importSource, property);
    }
}
