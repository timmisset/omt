package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import org.jetbrains.annotations.NotNull;

public class ModelItemReference extends MemberReference<OMTModelItemLabel> {
    public ModelItemReference(@NotNull OMTModelItemLabel member, TextRange textRange) {
        super(member, textRange);
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        return myElement.setName(newElementName);
    }
}
