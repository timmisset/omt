package com.misset.opp.omt.psi.references;

import com.misset.opp.omt.psi.OMTVariable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VariableReferenceIT extends ReferenceTest {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("VariableReferenceIT");
        super.setUp(OMTVariable.class);
        setBuiltinAndModel();
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void modelUsageHasReferenceTest() {
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       variables:\n" +
                "       - $test\n" +
                "       onStart: |\n" +
                "           @LOG($<caret>test);\n" +
                "";
        assertHasReference(content);
        assertNoErrors();
    }

    @Test
    void modelUsageHasUsagesTest() {
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       variables:\n" +
                "       - $<caret>test\n" +
                "       onStart: |\n" +
                "           @LOG($test);\n" +
                "           @LOG($test);\n" +
                "";
        assertHasUsages(content, 2);
        assertNoErrors();
    }

    @Test
    void modelUsageHasNoUsageWhenRedefinedTest() {
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       variables:\n" +
                "       - $<caret>test\n" +
                "       onStart: |\n" +
                "           VAR $test = 'test';\n" +
                "           @LOG($test);\n" +
                "";
        assertHasUsages(content, 0);
        assertNoErrors();
    }

    @Test
    void modelUsageHasNoUsageWhenRedefinedInScriptTest() {
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       onStart: |\n" +
                "           VAR $<caret>test = 'test';\n" +
                "           VAR $test = 'test';\n" +
                "           @LOG($test);\n" +
                "";
        assertHasUsages(content, 0);
        assertNoErrors();
    }

    @Test
    void modelUsageHasUsageWhenRedefinedInScriptTest() {
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       onStart: |\n" +
                "           VAR $test = 'test';\n" +
                "           VAR $test = 'test';\n" +
                "           VAR $test = 'test';\n" +
                "           VAR $<caret>test = 'test';\n" +
                "           @LOG($test);\n" +
                "";
        assertHasUsages(content, 1);
        assertNoErrors();
    }
}
