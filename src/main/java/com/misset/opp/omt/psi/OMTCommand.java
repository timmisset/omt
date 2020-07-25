package com.misset.opp.omt.psi;

import java.util.List;

public class OMTCommand extends OMTBuiltIn {
    private boolean isStandAloneQuery;

    public OMTCommand(String name) {
        super(name);
    }

    public OMTCommand(String name, List<OMTParameter> params) {
        super(name, params);
    }
    public OMTCommand(OMTDefineCommandStatement defineCommandStatement) {
        super(defineCommandStatement, defineCommandStatement.getDefineName().getText(), defineCommandStatement.getDefineParam());
    }

    public boolean canBeCalledBy(OMTOperatorCall operatorCall) {
        return operatorCall.getFirstChild().getText().equals(getName());
    }
}
