package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.OMTElementFactory;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.named.OMTNamespacePrefixNamedElement;
import com.misset.opp.omt.psi.references.NamespacePrefixReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A curie prefix is the prefix part of the prefix (prefix == prefix:iri)
 */
public abstract class OMTNamespacePrefixNamedElementAbstract extends NamedElementAbstract<OMTNamespacePrefix> implements OMTNamespacePrefixNamedElement {
    public OMTNamespacePrefixNamedElementAbstract(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    public PsiReference getReference() {
        TextRange property = new TextRange(0, getPsi().getText().length() - 1);
        return new NamespacePrefixReference(getPsi(), property);
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        PsiReference reference = getReference();
        return reference == null ? new PsiReference[0] : new PsiReference[]{reference};
    }

    @Override
    public String getName() {
        String prefix = getPsi().getText();
        prefix = prefix.endsWith(":") ? prefix.substring(0, prefix.length() - 1) : prefix;

        return prefix;
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return null;
    }

    @Override
    public PsiElement setName(@NotNull String name) {
        OMTNamespacePrefix replacement = OMTElementFactory.createNamespacePrefix(getProject(), name);
        return replace(replacement);
    }
}
