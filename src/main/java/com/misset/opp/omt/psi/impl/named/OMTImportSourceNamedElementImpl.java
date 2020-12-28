package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.OMTElementFactory;
import com.misset.opp.omt.psi.OMTImportSource;
import com.misset.opp.omt.psi.named.OMTImportNamedElement;
import com.misset.opp.omt.psi.references.ImportSourceReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OMTImportSourceNamedElementImpl extends NameIdentifierOwnerImpl<OMTImportSource> implements OMTImportNamedElement {
    public OMTImportSourceNamedElementImpl(@NotNull ASTNode node) {
        super(node, OMTImportSource.class);
    }

    @Nullable
    @Override
    public PsiReference getReference() {
        TextRange property = new TextRange(0, getPsi().getText().length());
        return new ImportSourceReference(getPsi(), property);
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        return new PsiReference[]{getReference()};
    }

    @Override
    public PsiElement setName(@NotNull String name) {
        return replace(OMTElementFactory.createImportSource(getProject(), name));
    }

    @Override
    @NotNull
    public String getName() {
        return getText();
    }
}
