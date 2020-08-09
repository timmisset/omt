package com.misset.opp.omt.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.named.OMTVariableNamedElement;
import com.misset.opp.omt.psi.references.VariableReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OMTVariableNamedElementImpl extends ASTWrapperPsiElement implements OMTVariableNamedElement {
    public OMTVariableNamedElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    public PsiReference getReference() {
        return toReference((OMTVariable)getNode().getPsi());
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        return new PsiReference[] { toReference((OMTVariable)getNode().getPsi()) };
    }

    private PsiReference toReference(OMTVariable variable) {
        TextRange property = new TextRange(0, variable.getText().length());
        return new VariableReference(variable, property);
    }
}
