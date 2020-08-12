package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;

public interface OMTExportMember {

    boolean isOperator();

    boolean isCommand();

    int expectedNumberOfParametersMin();

    int expectedNumberOfParametersMax();

    String[] getParameters();

    String getName();

    PsiElement getElement();

    PsiElement getResolvingElement();
}
