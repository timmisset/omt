package com.misset.opp.ttl.psi.named.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.ttl.psi.impl.TTLDirectiveImpl;
import com.misset.opp.ttl.psi.named.TTLPrefixIDNamedElement;
import org.jetbrains.annotations.NotNull;

public abstract class TTLPrefixIDNamedElementImpl extends TTLDirectiveImpl implements TTLPrefixIDNamedElement {
    public TTLPrefixIDNamedElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    private PsiElement getPrefixElement() {
        return PsiTreeUtil.nextVisibleLeaf(getFirstChild());
    }

    private PsiElement getNamespaceElement() {
        return PsiTreeUtil.nextVisibleLeaf(getPrefixElement());
    }

    @Override
    public String getPrefix() {
        final String prefix = getPrefixElement().getText();
        return prefix.endsWith(":") ? prefix.substring(0, prefix.length() - 1) : prefix;
    }

    @Override
    public String getNamespace() {
        final String text = getNamespaceElement().getText();
        return text.substring(1, text.length() - 1);
    }
}
