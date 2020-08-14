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
    private OMTParameterType type;
    private String parameterType;

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
        name = variable.getName();
        this.required = required;
    }

    public OMTParameterImpl(OMTParameterWithType parameterWithType) {
        variable = parameterWithType.getVariable();
        type = parameterWithType.getParameterType();
        defaultValue = null;
        name = variable.getName();
        required = true;
    }

    public OMTParameterImpl(OMTVariableAssignment variableAssignment) {
        variable = variableAssignment.getVariable();
        defaultValue = variableAssignment.getVariableValue();
        name = variable.getName();
        required = true;
    }

    public OMTParameterImpl(OMTQueryPath queryPath) {
        if (!(queryPath.getFirstChild().getFirstChild() instanceof OMTVariable)) {
            throw new Error("OMTQueryPath must start with a variable to be parsed to a parameter");
        }
        variable = (OMTVariable) queryPath.getFirstChild().getFirstChild();
        name = variable.getName();
        required = false;
    }

    public OMTParameterImpl(JsonPrimitive primitive, String name) {
        if (primitive.getAsString().startsWith("p.")) {
            this.name = name;
            parameterType = primitive.getAsString().substring(2);
            required = !parameterType.startsWith("optional");
            rest = parameterType.startsWith("rest");
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
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    private String getParameterTypeDesc() {
        if (type != null) {
            return type.getText();
        }
        if (parameterType != null) {
            return parameterType;
        }
        return null;
    }

    @Override
    public String describe() {
        return String.format("%s%s%s%s%s",
                isRest() ? "..." : "",
                getName(),
                getParameterTypeDesc() != null ? String.format(" (%s) ", getParameterTypeDesc()) : "",
                isRequired() ? " (required) " : "",
                defaultValue != null ? defaultValue.toString() : "");
    }
}
