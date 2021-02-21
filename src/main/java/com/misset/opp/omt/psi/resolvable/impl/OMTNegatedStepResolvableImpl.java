package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTNegatedStep;
import com.misset.opp.omt.psi.impl.OMTQueryImpl;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static util.UtilManager.getRDFModelUtil;

public abstract class OMTNegatedStepResolvableImpl extends OMTQueryImpl implements OMTNegatedStep {

    private static final String BOOLEAN = "boolean";

    public OMTNegatedStepResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean isBooleanType() {
        return true;
    }

    @Override
    public List<Resource> resolveToResource() {
        return Collections.singletonList(getRDFModelUtil().getPrimitiveTypeAsResource(BOOLEAN));
    }

    @Override
    public List<Resource> filter(List<Resource> resources) {
        return getQuery().filter(resources);
    }

    @Override
    public boolean isType() {
        return false;
    }
}
