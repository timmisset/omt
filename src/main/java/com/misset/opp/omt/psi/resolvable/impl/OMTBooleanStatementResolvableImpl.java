package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTBooleanStatement;
import com.misset.opp.omt.psi.impl.OMTQueryImpl;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.getRDFModelUtil;

public abstract class OMTBooleanStatementResolvableImpl extends OMTQueryImpl implements OMTBooleanStatement {

    private static final String BOOLEAN = "boolean";

    public OMTBooleanStatementResolvableImpl(@NotNull ASTNode node) {
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
    public List<Resource> resolveToResource(boolean lookBack) {
        return resolveToResource();
    }

    @Override
    public List<Resource> filter(List<Resource> resources) {
        // applies filtering on all steps and returns the one with the fewest options
        return getQueryList().stream()
                .map(query -> query.filter(resources))
                .min(Comparator.comparingInt(List::size))
                .orElse(resources);
    }
}