package com.misset.opp.omt.psi;

import com.google.gson.JsonPrimitive;

import java.util.Objects;

public class OMTParameter {

    private String name;
    private boolean required;
    private OMTVariableValue defaultValue;
    private boolean rest;

    // TODO:
    // private TypeOfParameter

    // as parsed from builtIn tree
    public OMTParameter(JsonPrimitive primitive) {
        if(primitive.getAsString().startsWith("p.")) {
            name = primitive.getAsString();
            String typeOfParameter = name.substring(2);
            required = !typeOfParameter.startsWith("optional");
            rest = typeOfParameter.startsWith("rest");
        }
    }

    public OMTParameter(OMTVariable variable) {
        name = variable.getText();
        required = true;
        defaultValue = null;
    }
    public OMTParameter(OMTListItemParameter parameter) {
        name = parameter.getText();
        required = true;
        defaultValue = null;
    }

    public OMTParameter(OMTVariableAssignment variableAssignment) {
        name = variableAssignment.getVariable().getText();
        required = false;
        defaultValue = variableAssignment.getVariableAssignmentValue() != null ? variableAssignment.getVariableAssignmentValue().getVariableValue() : null;
    }

    public boolean isRequired() { return required; }
    public boolean isRest() { return rest; }
    public String getName() { return name; }

}
