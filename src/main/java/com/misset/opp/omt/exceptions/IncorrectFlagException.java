package com.misset.opp.omt.exceptions;

import java.util.List;

public class IncorrectFlagException extends Throwable {
    public IncorrectFlagException(String flagName, List<String> flags) {
        super(String.format("Incorrect flag '%s' used, acceptable flags are: %s",
                flagName,
                String.format("'%s'", String.join("', '", flags))));
    }
}
