package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.misset.opp.omt.psi.named.OMTCall;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.util.UtilManager.getMemberUtil;

public class CallReference extends MemberReference<OMTCall> {
    public CallReference(@NotNull OMTCall member, TextRange textRange) {
        super(member, textRange);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        return declaringMemberToResolveResult(getMemberUtil().getDeclaringMember(myElement).orElse(null));
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        return myElement.setName(newElementName);
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        final PsiElement declaringMember = getMemberUtil().getDeclaringMember(myElement).orElse(null);
        return declaringMember != null &&
                (declaringMember.equals(element) || element.equals(isReferenceToTarget(declaringMember)));
    }
}
