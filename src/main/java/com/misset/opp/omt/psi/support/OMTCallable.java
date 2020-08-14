package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.exceptions.NumberOfInputParametersMismatchException;
import com.misset.opp.omt.psi.impl.CallCallableMismatchException;

import java.util.List;

public interface OMTCallable {

    int getMinExpected();

    int getMaxExpected();

    List<String> getLocalVariables();

    void validateSignature(OMTCall call) throws CallCallableMismatchException, NumberOfInputParametersMismatchException;

    boolean isOperator();

    boolean isCommand();

    boolean hasRest();

    String[] getParameters();

    String getName();

    PsiElement getElement();

    String htmlDescription();

    String shortDescription();
}
