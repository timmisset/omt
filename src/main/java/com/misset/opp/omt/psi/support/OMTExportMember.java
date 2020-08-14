package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;

public interface OMTExportMember extends OMTCallable {

    PsiElement getResolvingElement();
}
