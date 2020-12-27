package com.misset.opp.omt.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTParameterWithType;
import com.misset.opp.omt.psi.support.OMTParameterWithTypeSup;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.getVariableUtil;

public abstract class OMTParameterWithTypeSupImpl extends ASTWrapperPsiElement implements OMTParameterWithTypeSup {
    public OMTParameterWithTypeSupImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<Resource> getType() {
        return getVariableUtil().getType(getNode().getPsi(OMTParameterWithType.class));
    }
}
