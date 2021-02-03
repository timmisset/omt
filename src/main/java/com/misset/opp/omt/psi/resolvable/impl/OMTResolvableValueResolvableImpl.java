package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.CachedPsiElement;
import com.misset.opp.omt.psi.OMTResolvableValue;
import com.misset.opp.omt.psi.support.OMTCallable;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static com.misset.opp.omt.util.UtilManager.getMemberUtil;

public abstract class OMTResolvableValueResolvableImpl extends CachedPsiElement implements OMTResolvableValue {

    public OMTResolvableValueResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<Resource> resolveToResource() {
        if (getQuery() == null) {
            final OMTCallable callable = getMemberUtil().getCallable(Objects.requireNonNull(getCommandCall()));
            return callable.getReturnType();
        }
        return getQuery().resolveToResource();
    }
}
