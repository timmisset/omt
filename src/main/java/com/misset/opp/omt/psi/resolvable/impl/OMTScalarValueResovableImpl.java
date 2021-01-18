package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTScalarValue;
import com.misset.opp.omt.psi.resolvable.OMTScalarValueResolvable;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.getRDFModelUtil;

public abstract class OMTScalarValueResovableImpl extends ASTWrapperPsiElement implements OMTScalarValueResolvable, OMTScalarValue {
    public OMTScalarValueResovableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<Resource> resolveToResource() {
        if (getQuery() != null) {
            return getQuery().resolveToResource();
        } else if (getStringEntry() != null) {
            return Collections.singletonList(getRDFModelUtil().getStringType());
        }
        return Collections.emptyList();
    }
}
