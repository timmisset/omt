package com.misset.opp.omt.psi;

import java.util.List;

public class OMTOperator extends OMTBuiltIn {
    private boolean isStandAloneQuery;

    public OMTOperator(String name) {
        super(name);
    }

    public OMTOperator(String name, List<OMTParameter> params) {
        super(name, params);
    }
    public OMTOperator(OMTDefineQueryStatement defineQueryStatement) {
        super(defineQueryStatement, defineQueryStatement.getDefineName().getText(), defineQueryStatement.getDefineParam());
        isStandAloneQuery = false;
    }

    public boolean canBeCalledBy(OMTOperatorCall operatorCall) {
        return operatorCall.getFirstChild().getText().equals(getName());
    }
}
