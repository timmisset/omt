package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTDefineName;
import org.jetbrains.annotations.NotNull;

public class DefinedReference extends MemberReference<OMTDefineName> {
    public DefinedReference(@NotNull OMTDefineName member, TextRange textRange) {
        super(member, textRange);
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        return myElement.setName(newElementName);
    }
}
