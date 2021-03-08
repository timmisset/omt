package com.misset.opp.omt.psi.impl;

import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTVariable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OMTVariableImplTest extends OMTTestSuite {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("OMTVariableImplTest");
        super.setUp();
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void isReadOnly() {
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       variables:\n" +
                "       -   name: $test\n" +
                "           readonly: true\n" +
                "       onStart: |\n" +
                "           @LOG($<caret>test);";
        getElementAtCaret(content, element -> assertTrue(element.isReadOnly()), OMTVariable.class, true);
    }

    @Test
    void isNotReadOnly() {
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       variables:\n" +
                "       -   $test\n" +
                "       onStart: |\n" +
                "           @LOG($<caret>test);";
        getElementAtCaret(content, element -> assertFalse(element.isReadOnly()), OMTVariable.class, true);
    }

    @Test
    void isNotReadOnlyDestructed() {
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       variables:\n" +
                "       -   name: $test\n" +
                "           readonly: false\n" +
                "       onStart: |\n" +
                "           @LOG($<caret>test);";
        getElementAtCaret(content, element -> assertFalse(element.isReadOnly()), OMTVariable.class, true);
    }
}
