package com.misset.opp.omt.psi;

import java.util.List;

public class OMTCommand extends OMTBuiltIn {
    private boolean isStandAloneQuery;

    public OMTCommand(String name, String typeOfOrigin) {
        super(name, typeOfOrigin);
    }

    public OMTCommand(String name, List<OMTParameter> params, String typeOfOrigin) {
        super(name, params, typeOfOrigin);
    }
    public OMTCommand(OMTDefineCommandStatement defineCommandStatement) {
        super(  defineCommandStatement,
                defineCommandStatement.getDefineName().getText(), defineCommandStatement.getDefineParam(),
                "Defined Command"
        );
    }

    public boolean canBeCalledBy(OMTOperatorCall operatorCall) {
        return operatorCall.getFirstChild().getText().equals(getName());
    }
}
