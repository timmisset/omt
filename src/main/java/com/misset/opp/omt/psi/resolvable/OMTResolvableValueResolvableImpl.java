package com.misset.opp.omt.psi.resolvable;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTResolvableValue;
import com.misset.opp.omt.psi.support.OMTCallable;
import com.misset.opp.omt.psi.util.MemberUtil;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public abstract class OMTResolvableValueResolvableImpl extends ASTWrapperPsiElement implements OMTResolvableValue {

    private static final MemberUtil memberUtil = MemberUtil.SINGLETON;

    public OMTResolvableValueResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<Resource> resolveToResource() {
        if (getQuery() != null) {
            return getQuery().resolveToResource();
        } else {
            final OMTCallable callable = memberUtil.getCallable(Objects.requireNonNull(getCommandCall()));
            return callable.getReturnType();
        }
    }
}
