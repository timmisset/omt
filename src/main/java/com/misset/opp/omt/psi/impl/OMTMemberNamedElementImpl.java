package com.misset.opp.omt.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.named.OMTVariableNamedElement;
import com.misset.opp.omt.psi.references.MemberReference;
import com.misset.opp.omt.psi.references.VariableReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OMTMemberNamedElementImpl extends ASTWrapperPsiElement implements OMTVariableNamedElement {
    public OMTMemberNamedElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    private NamedMemberType getNamedMemberType() {
        if(getPsi() instanceof OMTOperatorCall) { return NamedMemberType.OperatorCall; }
        if(getPsi() instanceof OMTDefineName) { return NamedMemberType.DefineName; }
        if(getPsi() instanceof OMTCommandCall) { return NamedMemberType.CommandCall; }
        if(getPsi() instanceof OMTMember && getPsi().getParent().getParent() instanceof OMTImport) { return NamedMemberType.ImportingMember; }
        return null;
    }
    private PsiElement getPsi() { return getNode().getPsi(); }

    @Nullable
    @Override
    public PsiReference getReference() {
        NamedMemberType namedMemberType = getNamedMemberType();
        if(namedMemberType == null) { return null; }
        switch (namedMemberType) {
            case DefineName: return toReference((OMTDefineName) getPsi());
            case ImportingMember: return toReference((OMTMember) getPsi());
            case CommandCall: return toReference((OMTCommandCall) getPsi());
            case OperatorCall: return toReference((OMTOperatorCall) getPsi());

            default: return null;
        }
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        PsiReference reference = getReference();
        return reference == null ? new PsiReference[0] : new PsiReference[] { reference };
    }
    private PsiReference toReference(OMTDefineName defineName) {
        TextRange property = defineName.getTextRangeInParent();
        return new MemberReference(defineName, property, getNamedMemberType());
    }
    private PsiReference toReference(OMTMember member) {
        TextRange property = new TextRange(0, member.getText().length());
        return new MemberReference(member, property, getNamedMemberType());
    }
    private PsiReference toReference(OMTOperatorCall operatorCall) {
        TextRange property = operatorCall.getFirstChild().getTextRangeInParent();
        return new MemberReference(operatorCall, property, getNamedMemberType());
    }
    private PsiReference toReference(OMTCommandCall commandCall) {
        TextRange property = new TextRange(1, commandCall.getFirstChild().getText().length());
        return new MemberReference(commandCall, property, getNamedMemberType());
    }
}
