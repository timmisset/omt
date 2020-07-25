package com.misset.opp.omt.psi;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.exceptions.NumberOfInputParametersMismatchException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OMTBuiltIn {

    private PsiElement element;
    private String name;

    private List<OMTParameter> parameters = new ArrayList<>();
    private int minExpected;
    private int maxExpected;

    public OMTBuiltIn(String name) {
        this.name = name;
        setExpected();
    }
    public OMTBuiltIn(String name, List<OMTParameter> params) {
        this.name = name;
        this.parameters = params;
        setExpected();
    }
    public OMTBuiltIn(PsiElement element, String name, OMTDefineParam params) {
        this.name = name;
        this.element = element;
        setParameters(params);
        setExpected();
    }
    private void setParameters(OMTDefineParam defineParam) {
        if(defineParam == null) { return; }
        parameters = defineParam.getVariableList().stream()
                .map(OMTParameter::new)
                .collect(Collectors.toList());
    }

    private boolean hasRest() {
        return parameters.stream().anyMatch(OMTParameter::isRest);
    }
    private void setExpected() {
        minExpected = (int)parameters.stream().filter(OMTParameter::isRequired).count();
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
        if(intputParameters < minExpected || (!hasRest() && intputParameters > maxExpected)) {
            throw new NumberOfInputParametersMismatchException(name, minExpected, maxExpected, intputParameters);
        }
    }
    public String getName() { return name; }
    public PsiElement getElement() { return element; }
    public List<OMTParameter> getParameters() { return parameters; }
}
