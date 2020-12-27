package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.exceptions.CallCallableMismatchException;
import com.misset.opp.omt.exceptions.IncorrectFlagException;
import com.misset.opp.omt.exceptions.IncorrectSignatureArgument;
import com.misset.opp.omt.exceptions.NumberOfInputParametersMismatchException;
import com.misset.opp.omt.psi.OMTSignatureArgument;
import com.misset.opp.omt.psi.named.OMTCall;
import org.apache.jena.rdf.model.Resource;

import java.util.HashMap;
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

    void validateSignatureArgument(int index, OMTSignatureArgument argument) throws IncorrectSignatureArgument;

    boolean acceptsArgument(int index, OMTSignatureArgument argument);

    boolean acceptsArgument(int index, List<Resource> resources);

    OMTParameter getParameter(int index);

    Resource getParameterType(int index);

    List<Resource> getAcceptableArgumentType(int index);

    String htmlDescription();

    String shortDescription();

    String getAsSuggestion();

    void setName(String name);

    List<Resource> getReturnType();

    String getCallableType();

    HashMap<String, Resource> getCallArgumentTypes();

    boolean returnsAny();
}
