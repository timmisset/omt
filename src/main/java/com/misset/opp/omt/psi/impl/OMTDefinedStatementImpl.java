package com.misset.opp.omt.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTDefineCommandStatement;
import com.misset.opp.omt.psi.OMTDefineQueryStatement;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import org.jetbrains.annotations.NotNull;

public abstract class OMTDefinedStatementImpl extends ASTWrapperPsiElement implements OMTDefinedStatement {

    public OMTDefinedStatementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean isQuery() {
        return this instanceof OMTDefineQueryStatement;
    }

    @Override
    public boolean isCommand() {
        return this instanceof OMTDefineCommandStatement;
    }

}
