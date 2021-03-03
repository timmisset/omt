package com.misset.opp.omt.completion;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommandCompletionTest extends OMTCompletionTestSuite {

    @BeforeAll
    protected void setUp() throws Exception {
        super.setName("CommandCompletionTest");
        super.setUp();
    }

    @AfterAll
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void localVariableAvailableTest() {
        String content = "commands:|\n" +
                "   DEFINE COMMAND myCommand => {\n" +
                "       @FOREACH('', { <caret> });\n" +
                "   }";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertCompletionContainsBuiltinCommands(completionLookupElements);
        assertCompletionContainsGlobalVariables(completionLookupElements);
        assertCompletionNOTContainsBuiltinOperators(completionLookupElements);
        assertContainsElements(completionLookupElements, "$value", "$index");
    }

    @Test
    void localVariableNotAvailableTest() {
        String content = "commands:|\n" +
                "   DEFINE COMMAND myCommand => {\n" +
                "       <caret>\n" +
                "   }";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertCompletionContainsBuiltinCommands(completionLookupElements);
        assertCompletionContainsGlobalVariables(completionLookupElements);
        assertCompletionNOTContainsBuiltinOperators(completionLookupElements);
        assertDoesntContain(completionLookupElements, "$value", "$index");
    }

    @Test
    void scriptContentStart() {
        String content = "commands:|\n" +
                "   DEFINE COMMAND myCommand => {\n" +
                "       <caret>\n" +
                "   }";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertCompletionContainsBuiltinCommands(completionLookupElements);
        assertCompletionContainsGlobalVariables(completionLookupElements);
        assertCompletionNOTContainsBuiltinOperators(completionLookupElements);
    }

    @Test
    void scriptContentStartSecondLine() {
        String content = "commands:|\n" +
                "   DEFINE COMMAND myCommand => {\n" +
                "       VAR $x = 'a';\n" +
                "       <caret>\n" +
                "   }";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertCompletionContainsBuiltinCommands(completionLookupElements);
        assertCompletionContainsGlobalVariables(completionLookupElements);
        assertCompletionNOTContainsBuiltinOperators(completionLookupElements);
    }

    @Test
    void scriptContentStartSecondLineWithoutSemicolon() {
        // The semicolon closure of a scriptline with scriptcontent is not required by the parser
        // but it is required by the OMT language. Instead of throwing a parser error, continue as
        // if it isn't required and annotate the content with a missing semicolon error
        String content = "commands:|\n" +
                "   DEFINE COMMAND myCommand => {\n" +
                "       VAR $x = 'a'\n" +
                "       <caret>\n" +
                "   }";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertCompletionContainsBuiltinCommands(completionLookupElements);
        assertCompletionContainsGlobalVariables(completionLookupElements);
        assertCompletionNOTContainsBuiltinOperators(completionLookupElements);
    }

    @Test
    void signatureArgumentDefinedQuery() {
        // myQuery == string
        // mySecondQuery == number
        // callableQuery accepts a string input at the first argument
        // when using @callableQuery, only myQuery should be suggested as input for callableQuery argument 1
        String content = "" +
                "queries:|\n" +
                "   DEFINE QUERY myQuery => 'Hello';\n" +
                "   DEFINE QUERY mySecondQuery => 12;\n" +
                "   /**\n" +
                "   * @param $myParam (string)\n" +
                "   */\n" +
                "   DEFINE QUERY callableQuery($myParam) => $myParam;\n" +
                "\n" +
                "commands:|\n" +
                "   DEFINE COMMAND myCommand => {\n" +
                "       callableQuery(<caret>)\n" +
                "   }";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "myQuery");
        assertDoesntContain(completionLookupElements, "mySecondQuery");
        assertDoesntContain(completionLookupElements, "callableQuery"); // don't call self

    }

    @Test
    void signatureSecondArgumentDefinedQuery() {
        // myQuery == string
        // mySecondQuery == number
        // callableQuery accepts a string input at the first argument
        // when using @callableQuery, only myQuery should be suggested as input for callableQuery argument 1
        String content = "" +
                "queries:|\n" +
                "   DEFINE QUERY myQuery => 'Hello';\n" +
                "   DEFINE QUERY mySecondQuery => 12;\n" +
                "   /**\n" +
                "   * @param $myParam (string)\n" +
                "   * @param $myParam2 (integer)\n" +
                "   */\n" +
                "   DEFINE QUERY callableQuery($myParam, $myParam2) => $myParam;\n" +
                "\n" +
                "commands:|\n" +
                "   DEFINE COMMAND myCommand => {\n" +
                "       callableQuery('test', <caret>);\n" +
                "   }";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertDoesntContain(completionLookupElements, "myQuery");
        assertContainsElements(completionLookupElements, "mySecondQuery");
        assertDoesntContain(completionLookupElements, "callableQuery"); // don't call self

    }

}
