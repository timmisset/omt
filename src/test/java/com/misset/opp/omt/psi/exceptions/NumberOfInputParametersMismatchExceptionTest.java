package com.misset.opp.omt.psi.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumberOfInputParametersMismatchExceptionTest {

    @Test
    void noParameters() {
        test("OPERATOR", 0, 0, 1, "OPERATOR expects 0 parameters, found 1");
    }
    @Test
    void oneParameter() {
        test("OPERATOR", 1, 1, 2, "OPERATOR expects 1 parameter, found 2");
    }
    @Test
    void betweenOneAndThreeParameters() {
        test("OPERATOR", 1, 3, 4, "OPERATOR expects between 1 and 3 parameters, found 4");
    }
    @Test
    void oneOrMoreParameters() {
        test("OPERATOR", 1, -1, 0, "OPERATOR expects 1 or more parameters, found 0");
    }

    void test(String name, int min, int max, int found, String expectedMessage) {
        assertEquals(expectedMessage, new NumberOfInputParametersMismatchException(
                name, min, max, found
        ).getMessage());
    }
}
