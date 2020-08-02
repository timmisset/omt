package com.misset.opp.omt.psi;

import java.util.List;

public class OMTOperator extends OMTBuiltIn {
    private boolean isStandAloneQuery;

    public OMTOperator(String name, String typeOfOrigin) {
        super(name, typeOfOrigin);
    }

    public OMTOperator(String name, List<OMTParameter> params, String typeOfOrigin) {
        super(name, params, typeOfOrigin);
    }
    public OMTOperator(OMTDefineQueryStatement defineQueryStatement) {
        super(defineQueryStatement,
                defineQueryStatement.getDefineName().getText(), defineQueryStatement.getDefineParam(),
                "Defined Query"
        );
        isStandAloneQuery = false;
    }


    public boolean canBeCalledBy(OMTOperatorCall operatorCall) {
        return operatorCall.getFirstChild().getText().equals(getName());
    }
}
