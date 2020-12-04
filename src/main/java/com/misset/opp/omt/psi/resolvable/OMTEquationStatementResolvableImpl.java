package com.misset.opp.omt.psi.resolvable;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTEquationStatement;
import com.misset.opp.omt.psi.OMTNegatedStep;
import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.OMTQueryPath;
import com.misset.opp.omt.psi.impl.OMTQueryImpl;
import com.misset.opp.omt.psi.util.QueryUtil;
import com.misset.opp.omt.psi.util.TokenUtil;
import com.misset.opp.omt.util.ProjectUtil;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class OMTEquationStatementResolvableImpl extends OMTQueryImpl implements OMTEquationStatement {

    private static final QueryUtil queryUtil = QueryUtil.SINGLETON;
    private static final TokenUtil tokenUtil = TokenUtil.SINGLETON;
    private static final ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    private static final String BOOLEAN = "boolean";

    public OMTEquationStatementResolvableImpl(@NotNull ASTNode node) {
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
        final OMTQuery query = getQueryList().get(0);
        final List<Resource> leftHand = query instanceof OMTQueryPath ? query.resolveToResource(false) : query.resolveToResource();

        if (!leftHand.isEmpty() && projectUtil.getRDFModelUtil().isTypePredicate(leftHand.get(0))) {
            // [rdf:type == ...]
            // now filter the resources based on the type
            PsiElement parent = getParent();
            boolean isNegated = parent instanceof OMTNegatedStep;
            List<Resource> rightHand = getQueryList().get(1).resolveToResource();
            final List<String> resourcesToCheck = rightHand.stream().map(Resource::toString).collect(Collectors.toList());
            return resources.stream().filter(
                    resource -> isNegated != resourcesToCheck.contains(resource.toString())
            ).collect(Collectors.toList());
        }
        return resources;
    }
}
