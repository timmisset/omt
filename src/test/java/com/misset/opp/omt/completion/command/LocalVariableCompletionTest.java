package com.misset.opp.omt.completion.command;

import com.misset.opp.omt.completion.OMTCompletionTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class LocalVariableCompletionTest extends OMTCompletionTestSuite {

    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("LocalVariableCompletionTest");
        super.setUp();
    }

    @AfterEach
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
}
