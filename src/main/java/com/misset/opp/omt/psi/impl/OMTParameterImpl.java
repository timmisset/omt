package com.misset.opp.omt.psi.impl;

import com.google.gson.JsonPrimitive;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.support.OMTParameter;

public class OMTParameterImpl implements OMTParameter {
    private OMTVariable variable;
    private Object defaultValue;
    private boolean required;
    private boolean rest;
    private String name;

    public OMTParameterImpl(OMTVariable variable) {
        this(variable, true);
    }

    public OMTParameterImpl(OMTVariable variable, boolean required) {
        this(variable, null, required);
    }

    public OMTParameterImpl(OMTVariable variable, Object defaultValue) {
        this(variable, defaultValue, true);
    }

    public OMTParameterImpl(OMTVariable variable, Object defaultValue, boolean required) {
        this.variable = variable;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    public OMTParameterImpl(OMTParameterWithType parameterWithType) {
        variable = parameterWithType.getVariable();
        defaultValue = null;
        required = true;
    }

    public OMTParameterImpl(OMTVariableAssignment variableAssignment) {
        variable = variableAssignment.getVariable();
        defaultValue = variableAssignment.getVariableValue();
        required = true;
    }

    public OMTParameterImpl(OMTQueryPath queryPath) {
        if (!(queryPath.getFirstChild().getFirstChild() instanceof OMTVariable)) {
            throw new Error("OMTQueryPath must start with a variable to be parsed to a parameter");
        }
        variable = (OMTVariable) queryPath.getFirstChild().getFirstChild();
        required = false;
    }

    public OMTParameterImpl(JsonPrimitive primitive) {
        if (primitive.getAsString().startsWith("p.")) {
            name = primitive.getAsString();
            String typeOfParameter = name.substring(2);
            required = !typeOfParameter.startsWith("optional");
            rest = typeOfParameter.startsWith("rest");
        }
    }

    @Override
    public OMTVariable getVariable() {
        return variable;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean isRest() {
        return rest;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public OMTParameterType getType() {
        return null;
    }

    @Override
    public String getName() {
        return variable.getText();
    }
}
