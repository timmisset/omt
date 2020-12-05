package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTSignatureArgument;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public abstract class OMTSignatureArgumentResolvableImpl extends ASTWrapperPsiElement implements OMTSignatureArgument {
    public OMTSignatureArgumentResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<Resource> resolveToResource() {
        if (getCommandBlock() != null) {
            return getCommandBlock().resolveToResource();
        }
        return Objects.requireNonNull(getResolvableValue()).resolveToResource();
    }
}
