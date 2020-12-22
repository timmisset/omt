package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.impl.OMTQueryImpl;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.util.UtilManager.getRDFModelUtil;

public abstract class OMTEquationStatementResolvableImpl extends OMTQueryImpl implements OMTEquationStatement {

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
        return Collections.singletonList(getRDFModelUtil().getPrimitiveTypeAsResource(BOOLEAN));
    }

    @Override
    public List<Resource> filter(List<Resource> resources) {
        final List<Resource> compareTo = new ArrayList<>();

        // check if either the left-side or right-side of the equation is a type filter (rdf:type)
        if (isTypeFilter(getQueryList().get(0))) {
            compareTo.addAll(getQueryList().get(1).resolveToResource());
        } else if (isTypeFilter(getQueryList().get(1))) {
            compareTo.addAll(getQueryList().get(0).resolveToResource());
        }
        if (!compareTo.isEmpty()) {
            // [rdf:type == ...]
            // now filter the resources based on the type
            PsiElement parent = getParent();
            boolean isNegated = parent instanceof OMTNegatedStep;
            return resources.stream().filter(
                    resource -> isNegated != compareTo.contains(resource)
            ).collect(Collectors.toList());
        }
        return resources;
    }

    private boolean isTypeFilter(OMTQuery query) {
        if (!(query instanceof OMTQueryPath)) {
            return false;
        }
        final OMTQueryPath queryPath = (OMTQueryPath) query;
        final OMTCurieElement curieElement = queryPath.getQueryStepList().size() == 1 &&
                queryPath.getQueryStepList().get(0).getCurieElement() != null ? queryPath.getQueryStepList().get(0).getCurieElement() : null;
        return curieElement != null && getRDFModelUtil().isTypePredicate(curieElement.getAsResource());
    }
}
