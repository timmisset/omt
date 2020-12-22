package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTSubQuery;
import com.misset.opp.omt.psi.impl.OMTQueryStepImpl;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class OMTSubQueryResolvableImpl extends OMTQueryStepImpl implements OMTSubQuery {

    public OMTSubQueryResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<Resource> resolveToResource() {
        return getQuery().resolveToResource();
    }
}
