package com.misset.opp.omt.psi.impl.named;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class NamedElementAbstract<T extends PsiElement> extends ASTWrapperPsiElement implements PsiNameIdentifierOwner {
    public NamedElementAbstract(@NotNull ASTNode node) {
        super(node);
    }

    protected T getPsi() {
        return (T) getNode().getPsi();
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return getPsi();
    }

    @Override
    public String getName() {
        return getText();
    }
}
