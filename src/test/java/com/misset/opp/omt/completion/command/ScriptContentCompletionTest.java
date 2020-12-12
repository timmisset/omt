package com.misset.opp.omt.completion.command;

import com.misset.opp.omt.completion.OMTCompletionTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class ScriptContentCompletionTest extends OMTCompletionTestSuite {

    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("ScriptContentCompletionTest");
        super.setUp();
    }

    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
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
}
