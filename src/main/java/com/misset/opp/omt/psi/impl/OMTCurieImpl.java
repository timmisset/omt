package com.misset.opp.omt.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.named.OMTCurie;
import com.misset.opp.omt.psi.resolvable.impl.OMTCurieElementResolvableImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.omt.psi.util.UtilManager.getProjectUtil;

public abstract class OMTCurieImpl extends OMTCurieElementResolvableImpl implements OMTCurie {

    public OMTCurieImpl(@NotNull ASTNode node) {
        super(node);
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
    public boolean isDefinedByPrefix(OMTPrefix prefix) {
        return prefix.getNamespacePrefix().getName().equals(getPrefixName());
    }

    @Override
    public Resource getAsResource() {
        String resolvedIri = String.format("%s%s",
                ((OMTFile) getContainingFile()).getPrefixIri(getPrefixName()),
                getPrefix().getNextSibling().getText()
        );
        Model ontologyModel = getProjectUtil().getOntologyModel();
        return ontologyModel.getResource(resolvedIri);
    }
}
