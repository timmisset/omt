package com.misset.opp.omt.psi.impl.named;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.named.OMTMemberNamedElement;
import org.jetbrains.annotations.NotNull;

public abstract class OMTMemberNamedElementAbstract extends ASTWrapperPsiElement implements OMTMemberNamedElement {
    public OMTMemberNamedElementAbstract(@NotNull ASTNode node) {
        super(node);
    }

    protected PsiElement getPsi() {
        return getNode().getPsi();
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        PsiReference reference = getReference();
        return reference == null ? new PsiReference[0] : new PsiReference[] { reference };
    }
}
