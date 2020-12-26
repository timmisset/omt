package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.util.PsiImplUtil;
import org.jetbrains.annotations.NotNull;

public class ModelItemReference extends MemberReference {
    public ModelItemReference(@NotNull PsiElement member, TextRange textRange) {
        super(member, textRange);
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        return PsiImplUtil.setName((OMTModelItemLabel) super.myElement, newElementName);
    }
}
