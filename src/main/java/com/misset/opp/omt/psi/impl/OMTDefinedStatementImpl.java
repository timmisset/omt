package com.misset.opp.omt.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.CachedPsiElement;
import com.misset.opp.omt.psi.OMTDefineCommandStatement;
import com.misset.opp.omt.psi.OMTDefineQueryStatement;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.omt.psi.OMTTypes.DEFINE_START;

public abstract class OMTDefinedStatementImpl extends CachedPsiElement implements OMTDefinedStatement {

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

    @Override
    public PsiElement getDefineLabel() {
        return findChildByType(DEFINE_START);
    }

}
