package com.misset.opp.ttl.psi.resolvable.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.misset.opp.ttl.psi.TTLIri;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.util.UtilManager.getRDFModelUtil;

public abstract class TTLIriImpl extends ASTWrapperPsiElement implements com.misset.opp.ttl.psi.resolvable.TTLIri, TTLIri {
    public TTLIriImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public Resource getAsResource() {
        return getPrefixedName() != null ?
                getPrefixedName().getAsResource() :
                getRDFModelUtil().createResource(getText());
    }

    @Override
    public String getResourceAsString() {
        return getPrefixedName() != null ?
                getPrefixedName().getAsResource().toString() :
                getText();
    }

    @Override
    public String getLocalName() {
        return getPrefixedName() != null ? getPrefixedName().getLocalName() : "";
    }

    @Override
    public String getPrefix() {
        return getPrefixedName() != null ? getPrefixedName().getPrefix() : "";
    }
}
