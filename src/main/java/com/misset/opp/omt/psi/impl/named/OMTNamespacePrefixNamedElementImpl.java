package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.OMTElementFactory;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.named.OMTNamespacePrefixNamedElement;
import com.misset.opp.omt.psi.references.NamespacePrefixReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A curie prefix is the prefix part of the prefix (prefix == prefix:iri)
 */
public abstract class OMTNamespacePrefixNamedElementImpl extends NameIdentifierOwnerImpl<OMTNamespacePrefix> implements OMTNamespacePrefixNamedElement {
    public OMTNamespacePrefixNamedElementImpl(@NotNull ASTNode node) {
        super(node, OMTNamespacePrefix.class);
    }

    @Nullable
    @Override
    public PsiReference getReference() {
        if (getParent() instanceof OMTCurieElement) {
            return new NamespacePrefixReference(getPsi(), getNameIdentifier().getTextRangeInParent());
        }
        return null;
    }

    @Override
    @NotNull
    public String getName() {
        return getNameIdentifier().getText();
    }

    @NotNull
    @Override
    public PsiElement getNameIdentifier() {
        return getPsi().getFirstChild();
    }

    @Override
    public PsiElement setName(@NotNull String name) {
        OMTNamespacePrefix replacement = OMTElementFactory.createNamespacePrefix(getProject(), name);
        return replace(replacement);
    }

    @Override
    @NotNull
    // A prefix and its usage (as curies) can only exist in the same file
    public SearchScope getUseScope() {
        return GlobalSearchScope.fileScope(getContainingFile());
    }
}
