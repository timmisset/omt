package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.misset.opp.omt.psi.CachedPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class NameIdentifierOwnerImpl<T extends PsiElement> extends CachedPsiElement implements PsiNameIdentifierOwner {
    private final Class<T> clazz;

    public NameIdentifierOwnerImpl(@NotNull ASTNode node, Class<T> clazz) {
        super(node);
        this.clazz = clazz;
    }

    protected T getPsi() {
        return clazz.cast(getNode().getPsi());
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return getPsi();
    }

}
