package com.misset.opp.ttl.psi.named.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.misset.opp.omt.psi.impl.named.NameIdentifierOwnerImpl;
import com.misset.opp.ttl.psi.TTLElementFactory;
import com.misset.opp.ttl.psi.TTLIri;
import com.misset.opp.ttl.psi.TTLObject;
import com.misset.opp.ttl.psi.named.TTLObjectNamedElement;
import com.misset.opp.ttl.psi.references.ObjectReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.misset.opp.util.UtilManager.getTTLUtil;

public abstract class TTLObjectNamedElementImpl extends NameIdentifierOwnerImpl<TTLObjectNamedElement> implements TTLObject {

    public TTLObjectNamedElementImpl(@NotNull ASTNode node) {
        super(node, TTLObjectNamedElement.class);
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
    public TTLObject setName(@NlsSafe @NotNull String name) {
        return (TTLObject) replace(
                TTLElementFactory.getObject(
                        getProject(),
                        getIri() != null ? getIri().getPrefix() : "", // keep the same prefix
                        name)                                         // only set the local name
        );
    }

    @Override
    @Nullable
    public PsiReference getReference() {
        final TTLIri iri = getIri();
        if (iri == null || !getTTLUtil().hasSubject(iri)) {
            return null;
        }
        return new ObjectReference(getPsi(), iri.getTextRangeInParent());
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
