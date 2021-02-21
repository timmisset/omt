package com.misset.opp.omt.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTParameterType;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.named.OMTCurie;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static util.UtilManager.getRDFModelUtil;

public abstract class OMTParameterTypeSupImpl extends ASTWrapperPsiElement implements OMTCurie {
    public OMTParameterTypeSupImpl(@NotNull ASTNode node) {
        super(node);
    }

    private boolean hasCurie() {
        return getNode().getPsi(OMTParameterType.class).getNamespacePrefix() != null;
    }

    @Override
    public PsiElement getPrefix() {
        return hasCurie() ? getNode().getPsi(OMTParameterType.class).getNamespacePrefix() : null;
    }

    @Override
    public String getPrefixName() {
        return hasCurie() ? getPrefix().getText().replace(":", "") : null;
    }

    @Override
    public boolean isDefinedByPrefix(OMTPrefix prefix) {
        return hasCurie() && prefix.getNamespacePrefix().getName().equals(getPrefixName());
    }

    @Override
    public Resource getAsResource() {
        if (hasCurie() && getContainingFile() != null) {
            return getRDFModelUtil().getResource(
                    ((OMTFile) getContainingFile()).curieToIri(getText().trim())
            );
        } else {
            final String type = getFirstChild().getText().trim();
            return getRDFModelUtil().getPrimitiveTypeAsResource(type);
        }
    }

    @Override
    public List<Resource> resolveToResource() {
        return Collections.singletonList(getAsResource());
    }
}
