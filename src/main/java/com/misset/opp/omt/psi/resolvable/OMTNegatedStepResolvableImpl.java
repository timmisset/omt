package com.misset.opp.omt.psi.resolvable;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTNegatedStep;
import com.misset.opp.omt.psi.impl.OMTQueryImpl;
import com.misset.opp.omt.psi.util.QueryUtil;
import com.misset.opp.omt.psi.util.TokenUtil;
import com.misset.opp.omt.util.ProjectUtil;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public abstract class OMTNegatedStepResolvableImpl extends OMTQueryImpl implements OMTNegatedStep {

    private static final QueryUtil queryUtil = QueryUtil.SINGLETON;
    private static final TokenUtil tokenUtil = TokenUtil.SINGLETON;
    private static final ProjectUtil projectUtil = ProjectUtil.SINGLETON;
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
        return Collections.singletonList(projectUtil.getRDFModelUtil().getPrimitiveTypeAsResource(BOOLEAN));
    }

    @Override
    public List<Resource> resolveToResource(boolean lookBack) {
        return resolveToResource();
    }

    @Override
    public List<Resource> filter(List<Resource> resources) {
        return resources;
    }
}
