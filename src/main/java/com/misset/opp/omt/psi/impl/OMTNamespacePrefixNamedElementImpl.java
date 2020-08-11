package com.misset.opp.omt.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.named.OMTNamespacePrefixNamedElement;
import com.misset.opp.omt.psi.references.NamespacePrefixReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A curie prefix is the prefix part of the prefix (prefix == prefix:iri)
 */
public abstract class OMTNamespacePrefixNamedElementImpl extends ASTWrapperPsiElement implements OMTNamespacePrefixNamedElement {
    public OMTNamespacePrefixNamedElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    public PsiReference getReference() {
        OMTNamespacePrefix namespacePrefix = (OMTNamespacePrefix) getNode().getPsi();
        TextRange property = new TextRange(0, namespacePrefix.getText().length() - 1);
        return new NamespacePrefixReference(namespacePrefix, property);
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        PsiReference reference = getReference();
        return reference == null ? new PsiReference[0] : new PsiReference[] { reference };
    }
}
