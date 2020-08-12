package com.misset.opp.omt.exceptions;

public class UnknownMappingException extends Throwable {
    public UnknownMappingException(String mapping) {
        super(String.format("Unknown mapping %s", mapping));
    }
}
