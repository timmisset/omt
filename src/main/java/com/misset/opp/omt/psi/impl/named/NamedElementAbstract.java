package com.misset.opp.omt.psi.impl.named;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public abstract class NamedElementAbstract<T extends PsiElement> extends ASTWrapperPsiElement {
    public NamedElementAbstract(@NotNull ASTNode node) {
        super(node);
    }

    protected T getPsi() {
        return (T) getNode().getPsi();
    }
}
