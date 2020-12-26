package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTDefineName;
import com.misset.opp.omt.psi.util.PsiImplUtil;
import org.jetbrains.annotations.NotNull;

public class DefinedReference extends MemberReference {
    public DefinedReference(@NotNull PsiElement member, TextRange textRange) {
        super(member, textRange);
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        return PsiImplUtil.setName((OMTDefineName) super.myElement, newElementName);
    }
}
