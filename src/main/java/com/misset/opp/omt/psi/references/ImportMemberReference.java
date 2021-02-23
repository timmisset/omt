package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.misset.opp.omt.psi.OMTMember;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.util.UtilManager.getImportUtil;

public class ImportMemberReference extends MemberReference<OMTMember> {
    public ImportMemberReference(@NotNull OMTMember member, TextRange textRange) {
        super(member, textRange);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        return toResolveResult(getImportUtil().resolveImportMember(myElement).orElse(myElement));
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        return myElement.setName(newElementName);
    }
}
