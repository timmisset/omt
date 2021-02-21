package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTCurieConstantElement;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.impl.OMTQueryStepImpl;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static util.UtilManager.getRDFModelUtil;

public abstract class OMTCurieConstantElementResolvableImpl extends OMTQueryStepImpl implements OMTCurieConstantElement {

    public OMTCurieConstantElementResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<Resource> resolveToResource() {
        return filter(Collections.singletonList(getCurieElement().getAsResource()));
    }

    @Override
    public boolean isType() {
        return getRDFModelUtil().isClassOrType(getCurieElement().getAsResource());
    }

    @Override
    public boolean canLookBack() {
        return false;
    }

    @NotNull
    @Override
    public OMTCurieElement getCurieElement() {
        return Objects.requireNonNull(super.getCurieElement());
    }
}
