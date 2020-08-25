package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.exceptions.CallCallableMismatchException;
import com.misset.opp.omt.exceptions.NumberOfInputParametersMismatchException;

import java.util.List;

public interface OMTCallable {

    boolean hasParameters();

    int getMinExpected();

    int getMaxExpected();

    String[] getParameterNames();

    List<String> getLocalVariables();

    void validateSignature(OMTCall call) throws NumberOfInputParametersMismatchException, CallCallableMismatchException;

    boolean isOperator();

    boolean isCommand();

    boolean hasRest();

    String[] getParameters();

    String getName();

    PsiElement getElement();

    String htmlDescription();

    String shortDescription();
}
