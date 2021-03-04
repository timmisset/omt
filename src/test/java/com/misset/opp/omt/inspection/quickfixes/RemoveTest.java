package com.misset.opp.omt.inspection.quickfixes;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.ReadAction;
import com.misset.opp.omt.formatter.OMTFormattingTest;
import com.misset.opp.omt.inspection.OMTCodeInspectionUnused;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RemoveTest extends OMTFormattingTest {

    @BeforeAll
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setBuiltinAndModel();
        myFixture.enableInspections(OMTCodeInspectionUnused.class);
    }

    @AfterAll
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    String removeModelParameterContent = "model:\n" +
            "    Activiteit: !Activity\n" +
            "        onStart: |\n" +
            "            @MijnProcedure('test', 'test2', 'test3');\n" +
            "\n" +
            "    MijnProcedure: !Procedure\n" +
            "        params:\n" +
            "        -   $param\n" +
            "        -   $param2\n" +
            "        -   $param3\n";

    @Test
    void testRemoveDefinedParameterHasQuickFixes() {
        String content = "queries: |\n" +
                "    DEFINE QUERY yourQueryName($param) => 'Hello world';\n" +
                "    DEFINE QUERY yourQueryName2 => yourQueryName('test');\n" +
                "    DEFINE QUERY yourQueryName3 => yourQueryName('test');";
        assertNotNull(getQuickFixIntention(content, "Remove parameter and refactor call signatures to this Query"));
    }

    @Test
    void testRemoveDefinedParameterRemovesParameterAndCallArgumentsInQueries() {
        String content = "queries: |\n" +
                "    DEFINE QUERY yourQueryName($param) => 'Hello world';\n" +
                "    DEFINE QUERY yourQueryName2 => yourQueryName('test');\n" +
                "    DEFINE QUERY yourQueryName3 => yourQueryName('test');";
        invokeQuickFixIntention(content, "Remove parameter and refactor call signatures to this Query");
        assertFormattingApplied(getFile().getText(), "queries: |\n" +
                "    DEFINE QUERY yourQueryName => 'Hello world';\n" +
                "    DEFINE QUERY yourQueryName2 => yourQueryName;\n" +
                "    DEFINE QUERY yourQueryName3 => yourQueryName;");
    }

    @Test
    void testRemoveDefinedParameterRemovesParameterAndCallArgumentsInCommands() {
        String content = "commands: |\n" +
                "    DEFINE COMMAND yourCommandName($param) => { RETURN 'Hello world'; }\n" +
                "    DEFINE COMMAND yourCommandName2 => { @yourCommandName('test'); }\n" +
                "    DEFINE COMMAND yourCommandName3 => { @yourCommandName('test'); }";
        invokeQuickFixIntention(content, "Remove parameter and refactor call signatures to this Command");
        assertFormattingApplied(getFile().getText(), "commands: |\n" +
                "    DEFINE COMMAND yourCommandName => { RETURN 'Hello world'; }\n" +
                "    DEFINE COMMAND yourCommandName2 => { @yourCommandName(); }\n" +
                "    DEFINE COMMAND yourCommandName3 => { @yourCommandName(); }");
    }

    @Test
    void testRemoveModelParameterHasQuickFixes() {
        List<IntentionAction> allQuickFixes = getAllQuickFixes(removeModelParameterContent);
        assertEquals(3, allQuickFixes.size());
        ReadAction.run(() -> allQuickFixes.forEach(
                intentionAction -> assertEquals("Remove parameter and refactor call signatures to this Procedure", intentionAction.getText())
        ));
    }

    @Test
    void testRemoveModelParameterRemovesFirstParameterFromModelItemAndCall() {
        final IntentionAction intentionAction = getAllQuickFixes(removeModelParameterContent).get(0);
        String content = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        onStart: |\n" +
                "            @MijnProcedure('test2', 'test3');\n" +
                "\n" +
                "    MijnProcedure: !Procedure\n" +
                "        params:\n" +
                "        -   $param2\n" +
                "        -   $param3\n";

        invokeQuickFixIntention(intentionAction);
        assertFormattingApplied(getFile().getText(), content);
    }

    @Test
    void testRemoveModelParameterRemovesMiddleParameterFromModelItemAndCall() {
        final IntentionAction intentionAction = getAllQuickFixes(removeModelParameterContent).get(1);
        String content = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        onStart: |\n" +
                "            @MijnProcedure('test', 'test3');\n" +
                "\n" +
                "    MijnProcedure: !Procedure\n" +
                "        params:\n" +
                "        -   $param\n" +
                "        -   $param3\n";

        invokeQuickFixIntention(intentionAction);
        assertFormattingApplied(getFile().getText(), content);
    }

    @Test
    void testRemoveModelParameterRemovesLastParameterFromModelItemAndCall() {
        final IntentionAction intentionAction = getAllQuickFixes(removeModelParameterContent).get(2);
        String content = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        onStart: |\n" +
                "            @MijnProcedure('test', 'test2');\n" +
                "\n" +
                "    MijnProcedure: !Procedure\n" +
                "        params:\n" +
                "        -   $param\n" +
                "        -   $param2\n";

        invokeQuickFixIntention(intentionAction);
        assertFormattingApplied(getFile().getText(), content);
    }

    @Test
    void testRemoveDefinedStatement() {
        String content = "queries: |\n" +
                "DEFINE QUERY query => 'myQuery';\n" +
                "DEFINE QUERY query2 => query;";
        invokeQuickFixIntention(content, "Remove Query");
        assertFormattingApplied(getFile().getText(), "queries: |\n" +
                "    DEFINE QUERY query => 'myQuery';"
        );
    }

    @Test
    void testRemoveLastDefinedStatementRemovesBlock() {
        String content = "queries: |\n" +
                "DEFINE QUERY query => 'myQuery';\n" +
                "\n" +
                "model:\n" +
                "   MijnActiviteit: !Activity\n" +
                "       onRun: |\n" +
                "           @LOG('something');";
        invokeQuickFixIntention(content, "Remove Query");
        assertFormattingApplied(getFile().getText(), "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        onRun: |\n" +
                "            @LOG('something');"
        );
    }

    @Test
    void testRemovePrefixRemovesPrefix() {
        String content = "prefixes:\n" +
                "   abc:    <http://iri>\n" +
                "   def:    <http://iri2>\n" +
                "\n" +
                "model:\n" +
                "   MijnActiviteit: !Activity\n" +
                "       onRun: |\n" +
                "           @LOG(/abc:value);";
        invokeQuickFixIntention(content, "Remove prefix");
        assertFormattingApplied(getFile().getText(), "prefixes:\n" +
                "    abc:    <http://iri>\n" +
                "\n" +
                "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        onRun: |\n" +
                "            @LOG(/abc:value);"
        );
    }

    @Test
    void testRemovePrefixRemovesPrefixBlock() {
        String content = "prefixes:\n" +
                "   def:    <http://iri2>\n" +
                "\n" +
                "model:\n" +
                "   MijnActiviteit: !Activity\n";
        invokeQuickFixIntention(content, "Remove prefix block");
        assertFormattingApplied(getFile().getText(), "model:\n" +
                "    MijnActiviteit: !Activity"
        );
    }

    @Test
    void testRemoveVariableAssignmentRemovesEntireLine() {
        String content = "commands: |\n" +
                "   DEFINE COMMAND command => { \n" +
                "       VAR $x = 'test';\n" +
                "   }";
        invokeQuickFixIntention(content, "Remove variable assignment");
        assertFormattingApplied(getFile().getText(), "commands: |\n" +
                "    DEFINE COMMAND command => {\n" +
                "\n" +
                "    }"
        );
    }

    @Test
    void testRemoveVariableAssignmentRemovesVariableKeepsCommand() {
        String content = "commands: |\n" +
                "   DEFINE COMMAND command => { \n" +
                "       VAR $x = @LOG('test');\n" +
                "   }";
        invokeQuickFixIntention(content, "Remove variable (keep command)");
        assertFormattingApplied(getFile().getText(), "commands: |\n" +
                "    DEFINE COMMAND command => {\n" +
                "        @LOG('test');\n" +
                "    }"
        );
    }
}
