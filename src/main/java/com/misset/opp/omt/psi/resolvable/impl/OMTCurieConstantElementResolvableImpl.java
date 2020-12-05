package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTCurieConstantElement;
import com.misset.opp.omt.psi.impl.OMTQueryStepImpl;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class OMTCurieConstantElementResolvableImpl extends OMTQueryStepImpl implements OMTCurieConstantElement {

    public OMTCurieConstantElementResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<Resource> resolveToResource() {
        if (getCurieElement() == null) {
            return new ArrayList<>();
        }
        return filter(Collections.singletonList(getCurieElement().getAsResource()));
    }

}
