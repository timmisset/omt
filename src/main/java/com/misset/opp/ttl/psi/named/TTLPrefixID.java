package com.misset.opp.ttl.psi.named;

import com.intellij.psi.PsiElement;

public interface TTLPrefixID extends PsiElement {
    String getPrefix();

    String getNamespace();
}
