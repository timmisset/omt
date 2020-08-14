package com.misset.opp.omt.external.util.builtIn;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTCommandCall;
import com.misset.opp.omt.psi.OMTOperatorCall;
import com.misset.opp.omt.psi.exceptions.NumberOfInputParametersMismatchException;
import com.misset.opp.omt.psi.support.OMTParameter;

import java.util.List;

public class BuiltInMember {

    private PsiElement element;
    private String name;
    private String typeOfOrigin;

    private List<OMTParameter> parameters;
    private int minExpected;
    private int maxExpected;

    public BuiltInMember(String name, List<OMTParameter> params, String typeOfOrigin) {
        this.name = name;
        this.typeOfOrigin = typeOfOrigin;
        this.parameters = params;
        setExpected();
    }

    private boolean hasRest() {
        return parameters.stream().anyMatch(OMTParameter::isRest);
    }

    private void setExpected() {
        minExpected = (int) parameters.stream().filter(OMTParameter::isRequired).count();
        maxExpected = hasRest() ? -1 : parameters.size();
    }

    public int getMinExpected() {
        return minExpected;
    }

    public int getMaxExpected() {
        return maxExpected;
    }

    public void validateSignature(OMTOperatorCall operatorCall) throws NumberOfInputParametersMismatchException {
        int intputParameters = operatorCall.getSignature() != null ? operatorCall.getSignature().getVariableValueList().size() : 0;
        if (intputParameters < minExpected || (!hasRest() && intputParameters > maxExpected)) {
            throw new NumberOfInputParametersMismatchException(name, minExpected, maxExpected, intputParameters);
        }
    }

    public void validateSignature(OMTCommandCall commandCall) throws NumberOfInputParametersMismatchException {
        int intputParameters = commandCall.getSignature() != null ? commandCall.getSignature().getVariableValueList().size() : 0;
        if (intputParameters < minExpected || (!hasRest() && intputParameters > maxExpected)) {
            throw new NumberOfInputParametersMismatchException(name, minExpected, maxExpected, intputParameters);
        }
    }

    public String getName() {
        return name;
    }

    public PsiElement getElement() {
        return element;
    }

    public List<OMTParameter> getParameters() {
        return parameters;
    }
}
