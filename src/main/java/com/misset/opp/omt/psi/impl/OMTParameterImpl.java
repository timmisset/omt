package com.misset.opp.omt.psi.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.support.OMTParameter;

public class OMTParameterImpl implements OMTParameter {
    private static String REST = "rest";
    private static String OPTIONAL = "optional";

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
        variable = variableAssignment.getVariableList().get(0);
        defaultValue = variableAssignment.getVariableValue();
        name = variable.getName();
        required = true;
    }

    public OMTParameterImpl(OMTQuery query) {
        this(query.isQueryPath() ? (OMTQueryPath) query : null);
    }

    public OMTParameterImpl(OMTQueryPath queryPath) {
        if (queryPath == null || !(queryPath.getFirstChild().getFirstChild() instanceof OMTVariable)) {
            return;
        }
        variable = (OMTVariable) queryPath.getFirstChild().getFirstChild();
        name = variable.getName();
        required = false;
    }

    public OMTParameterImpl(JsonElement element, String name) {
        this.name = name;
        if (element.isJsonPrimitive()) {
            if (isParamIdentifier(element)) {
                parameterType = element.getAsString();
                parameterType = parameterType.substring(parameterType.indexOf(".") + 1);
            } else {
                parameterType = element.getAsString();
            }
            rest = parameterType.startsWith(REST);
            required = !rest && !isIgnoreParam(parameterType);
            return;
        }
        if (element.isJsonObject()) {
            JsonObject asObject = (JsonObject) element;
            parameterType = asObject.has("type") ? asObject.get("type").getAsString() : "Unknown type";
            rest = asObject.has(REST) && asObject.get(REST).getAsBoolean();
            required = !(asObject.has(OPTIONAL) && asObject.get(OPTIONAL).getAsBoolean());
            return;
        }
        throw new RuntimeException(String.format("Could not parse parameter information for: %s, name: %s",
                element.toString(), name));
    }

    private boolean isParamIdentifier(JsonElement element) {
        String asString = element.getAsString();
        return asString != null && (asString.startsWith("p.") || asString.startsWith("param."));
    }

    private boolean isIgnoreParam(String type) {
        return type.startsWith(OPTIONAL) || type.equals("ignoreCaseParam");
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
