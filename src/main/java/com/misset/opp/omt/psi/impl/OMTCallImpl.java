package com.misset.opp.omt.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTCommandCall;
import com.misset.opp.omt.psi.OMTElementFactory;
import com.misset.opp.omt.psi.OMTOperatorCall;
import com.misset.opp.omt.psi.support.OMTCall;
import org.jetbrains.annotations.NotNull;

public abstract class OMTCallImpl extends OMTMemberNamedElementImpl implements OMTCall {

    public OMTCallImpl(@NotNull ASTNode node) {
        super(node);
    }

    @NotNull
    @Override
    public String getName() {
        String name = getFirstChild().getText();
        return name.startsWith("@") ? name.substring(1) : name;
    }

    @Override
    public PsiElement setName(@NotNull String newName) {
        PsiElement replacement = isCommandCall() ?
                OMTElementFactory.createCommand(getProject(), newName) :
                OMTElementFactory.createOperator(getProject(), newName);

        if (replacement != null) {
            replace(replacement);
        }
        return replacement;
    }

    @Override
    @NotNull
    public PsiElement getNameIdentifier() {
        return getNode().getPsi().getFirstChild();
    }

    @Override
    public boolean isCommandCall() {
        return this instanceof OMTCommandCall;
    }

    @Override
    public boolean isOperatorCall() {
        return this instanceof OMTOperatorCall;
    }

    @Override
    public boolean canCallOperator() {
        return isOperatorCall();
    }

    @Override
    public boolean canCallCommand() {
        return isCommandCall();
    }


}
