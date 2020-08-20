package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTDefineName;

public interface OMTDefinedStatement extends PsiElement {

    boolean isCommand();

    boolean isQuery();

    OMTDefineName getDefineName();

}
