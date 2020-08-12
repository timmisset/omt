package com.misset.opp.omt.psi.impl;

import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.support.OMTParameter;

public class OMTParameterImpl implements OMTParameter {
    private OMTVariable variable;
    private Object defaultValue;
    private boolean isRequired;

    public OMTParameterImpl(OMTVariable variable) {
        this(variable, true);
    }

    public OMTParameterImpl(OMTVariable variable, boolean isRequired) {
        this(variable, null, isRequired);
    }

    public OMTParameterImpl(OMTVariable variable, Object defaultValue) {
        this(variable, defaultValue, true);
    }

    public OMTParameterImpl(OMTVariable variable, Object defaultValue, boolean isRequired) {
        this.variable = variable;
        this.defaultValue = defaultValue;
        this.isRequired = isRequired;
    }

    public OMTParameterImpl(OMTParameterWithType parameterWithType) {
        variable = parameterWithType.getVariable();
        defaultValue = null;
        isRequired = true;
    }

    public OMTParameterImpl(OMTVariableAssignment variableAssignment) {
        variable = variableAssignment.getVariable();
        defaultValue = variableAssignment.getVariableValue();
        isRequired = true;
    }

    public OMTParameterImpl(OMTQueryPath queryPath) {
        if (!(queryPath.getFirstChild().getFirstChild() instanceof OMTVariable)) {
            throw new Error("OMTQueryPath must start with a variable to be parsed to a parameter");
        }
        variable = (OMTVariable) queryPath.getFirstChild().getFirstChild();
        isRequired = false;
    }

    @Override
    public OMTVariable getVariable() {
        return variable;
    }

    @Override
    public boolean isRequired() {
        return isRequired;
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
