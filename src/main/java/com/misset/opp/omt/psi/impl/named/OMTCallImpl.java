package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.OMTCommandCall;
import com.misset.opp.omt.psi.OMTElementFactory;
import com.misset.opp.omt.psi.OMTOperatorCall;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.named.OMTCall;
import com.misset.opp.omt.psi.references.CallReference;
import com.misset.opp.omt.psi.support.OMTCallable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.misset.opp.util.UtilManager.getMemberUtil;

public abstract class OMTCallImpl extends MemberNamedElementImpl<OMTCall> implements OMTCall {

    public OMTCallImpl(@NotNull ASTNode node) {
        super(node, OMTCall.class);
    }

    @NotNull
    @Override
    public String getName() {
        String name = getFirstChild().getText();
        return name.startsWith("@") ? name.substring(1) : name;
    }

    @Override
    public PsiElement setName(@NotNull String newName) {
        OMTCall replacement = OMTElementFactory.createCall(
                getProject(),
                newName,
                getFlagSignature() != null ? getFlagSignature().getText() : "",
                getSignature() != null ? getSignature().getText() : "",
                getCallable().isCommand());
        replace(replacement);
        return replacement;
    }

    @Override
    @NotNull
    public PsiElement getNameIdentifier() {
        return getFirstChild();
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

    @Override
    public OMTCallable getCallable() {
        return getMemberUtil().getCallable(this);
    }

    @Nullable
    @Override
    public PsiReference getReference() {
        return new CallReference(getPsi(), getNameIdentifier().getTextRangeInParent());
    }

    @NotNull
    @Override
    public NamedMemberType getType() {
        return getCallable().isOperator() ? NamedMemberType.OperatorCall : NamedMemberType.CommandCall;
    }
}
