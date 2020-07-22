package com.misset.opp.omt.psi;

import java.util.Objects;

public class OMTParameter {

    private String name;
    private boolean required;
    private OMTVariableValue defaultValue;

    // TODO:
    // private TypeOfParameter

    public OMTParameter(OMTVariable variable) {
        name = variable.getText();
        required = true;
        defaultValue = null;
    }

    public OMTParameter(OMTVariableAssignment variableAssignment) {
        name = variableAssignment.getVariable().getText();
        required = false;
        defaultValue = variableAssignment.getVariableAssignmentValue() != null ? variableAssignment.getVariableAssignmentValue().getVariableValue() : null;
    }

    public boolean isRequired() { return required; }
    public String getName() { return name; }

}
