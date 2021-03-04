package com.misset.opp.omt.psi.references;

import com.misset.opp.omt.psi.OMTVariable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariableReferenceIT extends ReferenceTest {

    @Override
    @BeforeAll
    protected void setUp() throws Exception {
        super.setName("VariableReferenceIT");
        super.setUp(OMTVariable.class);
        setBuiltinAndModel();
    }

    @Override
    @AfterAll
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
                "           VAR $<caret>test = 'test';\n" +
                "           @LOG($test);\n" +
                "           VAR $test = 'test';\n" +
                "";
        assertHasUsages(content, 1);
        assertNoErrors();
    }

    @Test
    void modelUsageHasUsageWhenRedefinedInDifferentBlockTest() {
        String content = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        onStart: |\n" +
                "            IF 1 == 2 {\n" +
                "                VAR $<caret>x = 1;\n" +
                "                @LOG($x);\n" +
                "            }\n" +
                "            ELSE {\n" +
                "                VAR $x = 2;\n" +
                "                @LOG($x);\n" +
                "            }";
        assertHasUsages(content, 1);
        assertNoErrors();
    }

    @Test
    void modelUsageHasUsageWhenRedefinedInDifferentBlockTestSecondBlock() {
        String content = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        onStart: |\n" +
                "            IF 1 == 2 {\n" +
                "                VAR $x = 1;\n" +
                "                @LOG($x);\n" +
                "            }\n" +
                "            ELSE {\n" +
                "                VAR $<caret>x = 2;\n" +
                "                @LOG($x);\n" +
                "            }";
        assertHasUsages(content, 1);
        assertNoErrors();
    }

    @Test
    void modelUsageHasNoUsageWhenNoUsageInSameBlock() {
        String content = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        onStart: |\n" +
                "            IF 1 == 2 {\n" +
                "                VAR $<caret>x = 1;\n" +
                "            }\n" +
                "            ELSE {\n" +
                "                VAR $x = 2;\n" +
                "                @LOG($x);\n" +
                "            }";
        assertHasUsages(content, 0);
        assertNoErrors();
    }
}


