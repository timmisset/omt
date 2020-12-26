package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.misset.opp.omt.psi.OMTMember;
import com.misset.opp.omt.psi.util.PsiImplUtil;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.omt.psi.util.UtilManager.getMemberUtil;

public class ExportMemberReference extends MemberReference {
    public ExportMemberReference(@NotNull PsiElement member, TextRange textRange) {
        super(member, textRange);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        return toResolveResult(getMemberUtil().getDeclaringMemberFromImport(myElement, ((OMTMember) myElement).getName())
                .orElse(myElement));
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        return PsiImplUtil.setName((OMTMember) super.myElement, newElementName);
    }
}
