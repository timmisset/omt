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
    void scriptUsageHasNoUsageWhenRedefinedInScriptTest() {
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
    void scriptUsageHasUsageWhenRedefinedInScriptTest() {
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
    void modelUsageHasNoUsageWhenOvershadowByScript() {
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       variables:\n" +
                "       -   $<caret>param\n" +
                "       onStart: |\n" +
                "           VAR $param = 'test';\n" +
                "           @LOG($param);\n" +
                "";
        assertHasUsages(content, 0);
    }

    @Test
    void modelUsageHasNoUsageWhenOvershadowByModel() {
        String content = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        variables:\n" +
                "        - $<caret>param\n" +
                "\n" +
                "        actions:\n" +
                "            myAction:\n" +
                "                params:\n" +
                "                - $param\n" +
                "\n" +
                "                onSelect: |\n" +
                "                    @LOG($param);";
        assertHasUsages(content, 0);
    }

    @Test
    void modelUsageHasUsageWhenOvershadowingModel() {
        String content = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        variables:\n" +
                "        - $param\n" +
                "\n" +
                "        actions:\n" +
                "            myAction:\n" +
                "                params:\n" +
                "                - $<caret>param\n" +
                "\n" +
                "                onSelect: |\n" +
                "                    @LOG($param);";
        assertHasUsages(content, 1);
    }

    @Test
    void scriptUsageHasUsageWhenOvershadowingModel() {
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       variables:\n" +
                "       -   $param\n" +
                "       onStart: |\n" +
                "           VAR $<caret>param = 'test';\n" +
                "           @LOG($param);\n" +
                "";
        assertHasUsages(content, 1);
    }

    @Test
    void scriptUsageHasUsageWhenRedefinedInDifferentBlockTest() {
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
    void scriptUsageHasUsageWhenRedefinedInDifferentBlockTestSecondBlock() {
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
    void scriptUsageHasNoUsageWhenNoUsageInSameBlock() {
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

    @Test
    void variablesDeclaredInsideScriptBlockHasUsage() {
        String content = "commands: |\n" +
                "    DEFINE COMMAND doSomething($param1, $param2) => {\n" +
                "        IF ($param1 / ont:someProperty / EXISTS) {\n" +
                "            VAR $<caret>firstDeclaredVariable = $param1 / ont:someProperty;\n" +
                "            VAR $secondDeclaredVariable = $firstDeclaredVariable / ont:anotherProperty / CAST(IRI);\n" +
                "        }" +
                "    }";
        assertHasUsages(content, 1);
    }

    @Test
    void variablesDeclaredInsideScriptBlockHasNoUsage() {
        String content = "commands: |\n" +
                "    DEFINE COMMAND doSomething($param1, $param2) => {\n" +
                "        IF ($param1 / ont:someProperty / EXISTS) {\n" +
                "            VAR $firstDeclaredVariable = $param1 / ont:someProperty;\n" +
                "            VAR $<caret>secondDeclaredVariable = $firstDeclaredVariable / ont:anotherProperty / CAST(IRI);\n" +
                "        }" +
                "    }";
        assertHasUsages(content, 0);
    }

    @Test
    void variablesDeclaredInsideScriptBlockIsDeclared() {
        String content = "commands: |\n" +
                "    DEFINE COMMAND doSomething($param1, $param2) => {\n" +
                "        IF ($param1 / ont:someProperty / EXISTS) {\n" +
                "            VAR $firstDeclaredVariable = $param1 / ont:someProperty;\n" +
                "            VAR $secondDeclaredVariable = $<caret>firstDeclaredVariable / ont:anotherProperty / CAST(IRI);\n" +
                "        }" +
                "    }";
        assertHasReference(content);
    }

    @Test
    void variablesDeclaredInsideScriptBlockIsNotDeclared() {
        String content = "commands: |\n" +
                "    DEFINE COMMAND doSomething($param1, $param2) => {\n" +
                "        IF ($param1 / ont:someProperty / EXISTS) {\n" +
                "            VAR $firstDeclaredVariable = $param1 / ont:someProperty;\n" +
                "            VAR $secondDeclaredVariable = $<caret>firstDeclaredVariableWithTypo / ont:anotherProperty / CAST(IRI);\n" +
                "        }" +
                "    }";
        assertHasNoReference(content);
    }

    @Test
    void variablesDeclaredInsideDefinedStatementHasNoUsage() {
        String content = "queries: |\n" +
                "    DEFINE QUERY someQuery($<caret>param) => '';";
        assertHasUsages(content, 0);
    }

    @Test
    void variablesDeclaredInsideDefinedStatementHasUsage() {
        String content = "queries: |\n" +
                "    DEFINE QUERY someQuery($<caret>param) => $param;";
        assertHasUsages(content, 1);
    }

    @Test
    void variablesDeclaredInsideDefinedStatementHasUsageWhenAnotherQueryHasSameParam() {
        String content = "queries: |\n" +
                "    DEFINE QUERY someQuery($<caret>param) => $param;\n" +
                "    DEFINE QUERY someQuery2($param) => '';\n";
        assertHasUsages(content, 1);
    }

    @Test
    void variablesDeclaredInsideDefinedStatementHasNoUsageWhenAnotherQueryHasSameParam() {
        String content = "queries: |\n" +
                "    DEFINE QUERY someQuery($param) => $param;\n" +
                "    DEFINE QUERY someQuery2($<caret>param) => '';\n";
        assertHasUsages(content, 0);
    }

    @Test
    void variablesDeclaredInsideDefinedStatementInsideModelHasNoUsage() {
        String content = "queries: |\n" +
                "    DEFINE QUERY someQuery($<caret>param) => '';";
        assertHasUsages(inModel(content), 0);
    }

    @Test
    void variablesDeclaredInsideDefinedStatementInsideModelHasUsage() {
        String content = "queries: |\n" +
                "    DEFINE QUERY someQuery($<caret>param) => $param;";
        assertHasUsages(inModel(content), 1);
    }

    @Test
    void variablesDeclaredInsideDefinedStatementInsideModelHasUsageWhenAnotherQueryHasSameParam() {
        String content = "queries: |\n" +
                "    DEFINE QUERY someQuery($<caret>param) => $param;\n" +
                "    DEFINE QUERY someQuery2($param) => '';\n";
        assertHasUsages(inModel(content), 1);
    }

    @Test
    void variablesDeclaredInsideDefinedStatementInsideModelHasNoUsageWhenAnotherQueryHasSameParam() {
        String content = "queries: |\n" +
                "    DEFINE QUERY someQuery($param) => $param;\n" +
                "    DEFINE QUERY someQuery2($<caret>param) => '';\n";
        assertHasUsages(inModel(content), 0);
    }
}


