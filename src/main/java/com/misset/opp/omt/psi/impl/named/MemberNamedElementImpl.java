package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.named.OMTMemberNamedElement;
import org.jetbrains.annotations.NotNull;

public abstract class MemberNamedElementImpl<T extends PsiElement> extends NameIdentifierOwnerImpl<T> implements OMTMemberNamedElement {
    public MemberNamedElementImpl(@NotNull ASTNode node, Class<T> clazz) {
        super(node, clazz);
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        PsiReference reference = getReference();
        return reference == null ? new PsiReference[0] : new PsiReference[]{reference};
    }

    @Override
    @NotNull
    public String getName() {
        return getText();
    }

    @NotNull
    @Override
    public PsiElement getNameIdentifier() {
        return this;
    }
}
