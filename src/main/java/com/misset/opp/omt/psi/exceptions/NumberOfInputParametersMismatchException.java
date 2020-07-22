package com.misset.opp.omt.psi.exceptions;

public class NumberOfInputParametersMismatchException extends Exception {

    public NumberOfInputParametersMismatchException(String name, Integer minExpected, Integer maxExpected, Integer found) {
        super(String.format("%s expects %s %s, found %s",
                name,
                !minExpected.equals(maxExpected) ? String.format("between %s and %s", minExpected, maxExpected) : minExpected,
                minExpected == 1 ? "parameter" : "parameters",
                found));
    }
}
