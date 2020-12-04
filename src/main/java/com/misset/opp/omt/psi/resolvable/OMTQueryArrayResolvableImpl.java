package com.misset.opp.omt.psi.resolvable;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.OMTQueryArray;
import com.misset.opp.omt.psi.impl.OMTQueryImpl;
import com.misset.opp.omt.psi.util.QueryUtil;
import com.misset.opp.omt.psi.util.TokenUtil;
import com.misset.opp.omt.util.ProjectUtil;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class OMTQueryArrayResolvableImpl extends OMTQueryImpl implements OMTQueryArray {

    private static final QueryUtil queryUtil = QueryUtil.SINGLETON;
    private static final TokenUtil tokenUtil = TokenUtil.SINGLETON;
    private static final ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    private static final String BOOLEAN = "boolean";

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
        return projectUtil.getRDFModelUtil().getDistinctResources(resources);
    }

    @Override
    public List<Resource> filter(List<Resource> resources) {
        return resources;
    }

    @Override
    public List<Resource> resolveToResource(boolean lookBack) {
        return resolveToResource();
    }
}
