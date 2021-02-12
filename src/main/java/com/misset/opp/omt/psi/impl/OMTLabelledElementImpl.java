package com.misset.opp.omt.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.support.OMTLabelledElement;
import org.jetbrains.annotations.NotNull;

public abstract class OMTLabelledElementImpl extends ASTWrapperPsiElement implements OMTLabelledElement {

    public OMTLabelledElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    public PsiElement getLabel() {
        return getNode().getPsi();
    }

    @Override
    public String getName() {
        String propertyLabelText = getLabel().getText();
        return propertyLabelText.endsWith(":") ?
                propertyLabelText.substring(0, propertyLabelText.length() - 1) :
                propertyLabelText;
    }
}
