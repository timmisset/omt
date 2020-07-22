package com.misset.opp.omt.psi;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.exceptions.NumberOfInputParametersMismatchException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class OMTOperator {

    private PsiElement element;
    private String name;
    private boolean isStandAloneQuery;
    private List<OMTParameter> parameters;

    public OMTOperator(OMTDefineQueryStatement defineQueryStatement) {
        element = defineQueryStatement;
        name = defineQueryStatement.getDefineName().getText(); // DEFINE QUERY [NAME], WhiteSpace is included
        isStandAloneQuery = false;
        setParameters(defineQueryStatement.getDefineParam());
    }

    private void setParameters(OMTDefineParam defineParam) {
        if(defineParam == null) { return; }
        parameters = defineParam.getVariableList().stream()
                .map(OMTParameter::new)
                .collect(Collectors.toList());
    }

    public boolean canBeCalledBy(OMTOperatorCall operatorCall) {
        // TODO:
        // Check the signature of the call also
        return operatorCall.getFirstChild().getText().equals(name);
    }

    public void validateSignature(OMTOperatorCall operatorCall) throws NumberOfInputParametersMismatchException {
        int minExpected = (int)parameters.stream().filter(OMTParameter::isRequired).count();
        int maxExpected = parameters.size();
        int intputParameters = operatorCall.getSignature() != null ? operatorCall.getSignature().getVariableValueList().size() : 0;
        if(intputParameters < minExpected || intputParameters > maxExpected) {
            throw new NumberOfInputParametersMismatchException(name, minExpected, maxExpected, intputParameters);
        }
    }

    public boolean isStandAloneQuery() { return isStandAloneQuery; }
    public String getName() { return name; }
    public PsiElement getElement() { return element; }
    public List<OMTParameter> getParameters() { return parameters; }
}
