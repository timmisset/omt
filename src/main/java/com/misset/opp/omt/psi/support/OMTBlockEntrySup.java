package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTBlock;

public interface OMTBlockEntrySup extends OMTLabelledElement {
    String getName();

    PsiElement getLabel();

    OMTBlock getBlock();
}
