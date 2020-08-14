package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTCommandCall;
import com.misset.opp.omt.psi.OMTOperatorCall;
import com.misset.opp.omt.psi.impl.CallCallableMismatchException;

public interface OMTCallable {

    int getMinExpected();

    int getMaxExpected();

    void validateSignature(OMTCommandCall call) throws CallCallableMismatchException;

    void validateSignature(OMTOperatorCall call) throws CallCallableMismatchException;

    boolean isOperator();

    boolean isCommand();

    String[] getParameters();

    String getName();

    PsiElement getElement();
}
