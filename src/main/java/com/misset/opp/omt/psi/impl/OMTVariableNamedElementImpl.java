package com.misset.opp.omt.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.OMTDeclaredVariable;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.named.OMTVariableNamedElement;
import com.misset.opp.omt.psi.references.VariableReference;
import com.misset.opp.omt.psi.util.VariableUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

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
        TextRange property = new TextRange(0, variable.getText().length() + 1);
        return new VariableReference(variable, property);
    }
}
