package com.misset.opp.omt.completion.model;

import com.misset.opp.omt.completion.OMTCompletionTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandBlockCompletionTest extends OMTCompletionTestSuite {

    private static final String EXPECTED = "DEFINE COMMAND yourCommandName => { RETURN true; }";

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setName("CommandBlockCompletionTest");
        super.setUp();
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void commandBlockStart() {
        String content = "commands: |\n" +
                "   <caret>";
        assertCompletionContains(content, EXPECTED);
    }

    @Test
    void commandBlockAfterCommand() {
        String content = "commands: |\n" +
                "   DEFINE COMMAND yourCommandName => { RETURN true; }\n" +
                "   <caret>";
        assertCompletionContains(content, EXPECTED);
    }

}
