package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;

public interface OMTAnnotationHolder extends PsiElement {

    boolean isAnnotated();

    void setAnnotated(boolean isAnnotated);

}
