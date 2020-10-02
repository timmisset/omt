package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.exceptions.CallCallableMismatchException;
import com.misset.opp.omt.exceptions.IncorrectFlagException;
import com.misset.opp.omt.exceptions.NumberOfInputParametersMismatchException;

import java.util.List;

public interface OMTCallable {

    boolean hasFlags();

    List<String> getFlags();

    boolean hasParameters();

    int getMinExpected();

    int getMaxExpected();

    String[] getParameterNames();

    List<String> getLocalVariables();

    void setHTMLDescription(String description);

    void validateSignature(OMTCall call) throws NumberOfInputParametersMismatchException, CallCallableMismatchException, IncorrectFlagException;

    boolean isOperator();

    boolean isCommand();

    boolean hasRest();

    String[] getParameters();

    String getName();

    PsiElement getElement();

    String htmlDescription();

    String shortDescription();
}
