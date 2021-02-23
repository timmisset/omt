package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.OMTElementFactory;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.named.OMTCurie;
import com.misset.opp.omt.psi.references.CurieReference;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.misset.opp.util.UtilManager.getProjectUtil;

public abstract class OMTCurieElementImpl extends NameIdentifierOwnerImpl<OMTCurieElement> implements OMTCurie {
    public OMTCurieElementImpl(@NotNull ASTNode node) {
        super(node, OMTCurieElement.class);
    }

    @Nullable
    @Override
    public PsiReference getReference() {
        return new CurieReference(getPsi(), getNameIdentifier().getTextRangeInParent());
    }

    @NotNull
    @Override
    public PsiElement getNameIdentifier() {
        return getPsi().getLastChild();
    }

    @Override
    public String getName() {
        return getText().contains(":") ? getText().split(":")[1] : "";
    }

    @Override
    public PsiElement getPrefix() {
        return getFirstChild();
    }

    @Override
    public String getPrefixName() {
        return getPrefix().getText().replace(":", "");
    }

    @Override
    public PsiElement setName(@NotNull String name) {
        final PsiElement psiElement = OMTElementFactory.fromString(
                String.format("queries: |\n" +
                        "   DEFINE QUERY query => %s:%s;", getPrefixName(), name)
                , OMTCurie.class, getProject());
        return psiElement != null ? replace(psiElement) : this;
    }

    @Override
    public boolean isDefinedByPrefix(OMTPrefix prefix) {
        return prefix.getNamespacePrefix().getName().equals(getPrefixName());
    }

    @Override
    @Nullable
    public Resource getAsResource() {
        String resolvedIri = String.format("%s%s",
                ((OMTFile) getContainingFile()).getPrefixIri(getPrefixName()),
                getPrefix().getNextSibling().getText()
        );
        Model ontologyModel = getProjectUtil().getOntologyModel();
        if (ontologyModel == null) {
            return null;
        }
        return ontologyModel.getResource(resolvedIri);
    }
}
