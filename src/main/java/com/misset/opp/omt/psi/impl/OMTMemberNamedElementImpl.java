package com.misset.opp.omt.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.named.OMTMemberNamedElement;
import com.misset.opp.omt.psi.references.MemberReference;
import com.misset.opp.omt.psi.util.MemberUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OMTMemberNamedElementImpl extends ASTWrapperPsiElement implements OMTMemberNamedElement {
    public OMTMemberNamedElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    protected static MemberUtil memberUtil = MemberUtil.SINGLETON;

    private PsiElement getPsi() {
        return getNode().getPsi();
    }

    private NamedMemberType type;

    @Override
    public NamedMemberType getType() {
        if (type == null) {
            type = memberUtil.getNamedMemberType(getPsi());
        }
        return type;
    }

    @Nullable
    @Override
    public PsiReference getReference() {
        NamedMemberType namedMemberType = getType();
        switch (namedMemberType) {
            case DefineName:
                return toReference((OMTDefineName) getPsi());
            case ImportingMember:
            case ExportingMember:
                return toReference((OMTMember) getPsi());
            case CommandCall:
                return toReference((OMTCommandCall) getPsi());
            case OperatorCall:
                return toReference((OMTOperatorCall) getPsi());
            case ModelItem:
                return toReference((OMTModelItemLabel) getPsi());

            default:
                return null;
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
        return new MemberReference(defineName, property, getType());
    }
    private PsiReference toReference(OMTMember member) {
        TextRange property = member.getNameIdentifier().getTextRangeInParent();
        return new MemberReference(member, property, getType());
    }

    private PsiReference toReference(OMTOperatorCall operatorCall) {
        TextRange property = operatorCall.getFirstChild().getTextRangeInParent();
        return new MemberReference(operatorCall, property, getType());
    }

    private PsiReference toReference(OMTCommandCall commandCall) {
        TextRange property = new TextRange(1, commandCall.getFirstChild().getText().length());
        return new MemberReference(commandCall, property, getType());
    }

    private PsiReference toReference(OMTModelItemLabel modelItemLabel) {
        TextRange propertyLabelTextRange = TextRange.from(modelItemLabel.getNameIdentifier().getStartOffsetInParent(), modelItemLabel.getName().length());
        return new MemberReference(modelItemLabel, propertyLabelTextRange, getType());
    }
}
