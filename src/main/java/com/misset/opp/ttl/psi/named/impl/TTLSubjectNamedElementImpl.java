package com.misset.opp.ttl.psi.named.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.misset.opp.omt.psi.impl.named.NameIdentifierOwnerImpl;
import com.misset.opp.ttl.psi.TTLElementFactory;
import com.misset.opp.ttl.psi.TTLSubject;
import com.misset.opp.ttl.psi.named.TTLSubjectNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.misset.opp.util.UtilManager.getTTLUtil;

public abstract class TTLSubjectNamedElementImpl extends NameIdentifierOwnerImpl<TTLSubjectNamedElement> implements TTLSubjectNamedElement, com.misset.opp.ttl.psi.TTLSubject {

    public TTLSubjectNamedElementImpl(@NotNull ASTNode node) {
        super(node, TTLSubjectNamedElement.class);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return getIri();
    }

    @Override
    @NlsSafe
    @Nullable
    public String getName() {
        return getIri() != null ? getIri().getLocalName() : null;
    }

    @Override
    public TTLSubject setName(@NlsSafe @NotNull String name) {
        TTLSubject from = this;
        TTLSubject to = (TTLSubject) replace(
                TTLElementFactory.getSubject(
                        getProject(),
                        getIri() != null ? getIri().getPrefix() : "", // keep the same prefix
                        name)                                         // only set the local name
        );
        getTTLUtil().renameSubject(from, to);
        return to;
    }

    @Override
    public String getResourceAsString() {
        return getIri() != null ? getIri().getResourceAsString() : null;
    }

    @Override
    public @NotNull SearchScope getUseScope() {
        return GlobalSearchScope.allScope(getProject());
    }
}
