package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.OMTQueryFilter;
import com.misset.opp.omt.psi.resolvable.OMTQueryFilterResolvable;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.util.UtilManager.getRDFModelUtil;

public abstract class OMTQueryFilterResolvableImpl extends ASTWrapperPsiElement implements OMTQueryFilterResolvable, OMTQueryFilter {
    public OMTQueryFilterResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public OMTQuery getQuery() {
        return getQueryList().size() == 1 ? getQueryList().get(0) : null;
    }

    @Override
    public boolean isSubSelection() {
        return getQueryList().size() == 2 || (
                getQuery() != null &&
                        !getQuery().resolveToResource().isEmpty() &&
                        getRDFModelUtil().validateType(getQuery().resolveToResource(),
                                getRDFModelUtil().getPrimitiveTypeAsResourceList("number"))
        );
    }
}
