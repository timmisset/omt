package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;

public interface OMTLabelledElement extends OMTNamedElement {

    PsiElement getLabel();

}
