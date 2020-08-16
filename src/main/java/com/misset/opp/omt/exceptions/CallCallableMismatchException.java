package com.misset.opp.omt.exceptions;

import com.misset.opp.omt.psi.impl.OMTCallableImpl;
import com.misset.opp.omt.psi.support.OMTCall;

public class CallCallableMismatchException extends Throwable {
    public CallCallableMismatchException(OMTCallableImpl omtCallable, OMTCall call) {
        super(String.format("%s is a %s and cannot be called using %s",
                omtCallable.getName(),
                omtCallable.isCommand() ? "Command" : "Operator",
                call.getName()
        ));
    }
}
