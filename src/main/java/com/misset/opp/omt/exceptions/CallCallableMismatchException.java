package com.misset.opp.omt.exceptions;

import com.misset.opp.omt.psi.named.OMTCall;
import com.misset.opp.omt.psi.support.OMTCallable;

public class CallCallableMismatchException extends Throwable {
    public CallCallableMismatchException(OMTCallable omtCallable, OMTCall call) {
        super(String.format("%s is a %s and cannot be called using %s",
                omtCallable.getName(),
                omtCallable.isCommand() ? "Command" : "Operator",
                call.getName()
        ));
    }
}
