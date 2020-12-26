package com.misset.opp.omt.completion.command;

import com.misset.opp.omt.completion.OMTCompletionTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class SignatureArgumentCompletionTest extends OMTCompletionTestSuite {

    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("VariableAssignmentCompletionTest");
        super.setUp();
    }

    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
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
