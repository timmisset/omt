package com.misset.opp.omt.psi.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTQueriesBlock;
import com.misset.opp.omt.psi.support.OMTDefinedBlock;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public abstract class OMTQueriesBlockSupImpl extends OMTSpecificBlockSupImpl implements OMTDefinedBlock {

    public OMTQueriesBlockSupImpl(@NotNull ASTNode node) {
        super(node);
    }

    @NotNull
    @Override
    public List<OMTDefinedStatement> getStatements() {
        return getNode().getPsi(OMTQueriesBlock.class)
                .getDefineQueryStatementList().stream().map(statement -> (OMTDefinedStatement) statement).collect(Collectors.toList());
    }
}
