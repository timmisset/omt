package com.misset.opp.omt.psi.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTCommandsBlock;
import com.misset.opp.omt.psi.support.OMTDefinedBlock;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public abstract class OMTCommandsBlockSupImpl extends OMTSpecificBlockSupImpl implements OMTDefinedBlock {

    public OMTCommandsBlockSupImpl(@NotNull ASTNode node) {
        super(node);
    }

    @NotNull
    @Override
    public List<OMTDefinedStatement> getStatements() {
        return getNode().getPsi(OMTCommandsBlock.class)
                .getDefineCommandStatementList().stream().map(statement -> (OMTDefinedStatement) statement).collect(Collectors.toList());
    }
}
