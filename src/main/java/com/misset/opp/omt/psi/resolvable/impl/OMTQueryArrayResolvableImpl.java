package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.OMTQueryArray;
import com.misset.opp.omt.psi.impl.OMTQueryImpl;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.util.UtilManager.getRDFModelUtil;

public abstract class OMTQueryArrayResolvableImpl extends OMTQueryImpl implements OMTQueryArray {

    public OMTQueryArrayResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean isBooleanType() {
        return false;
    }

    @Override
    public List<Resource> resolveToResource() {
        final List<Resource> resources = getQueryList().stream()
                .map(OMTQuery::resolveToResource)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return getRDFModelUtil().getDistinctResources(resources);
    }

    @Override
    public List<Resource> filter(List<Resource> resources) {
        return resources;
    }

}
