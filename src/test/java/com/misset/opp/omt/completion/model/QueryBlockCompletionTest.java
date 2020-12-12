package com.misset.opp.omt.completion.model;

import com.misset.opp.omt.completion.OMTCompletionTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueryBlockCompletionTest extends OMTCompletionTestSuite {

    private static final String EXPECTED = "DEFINE QUERY yourQueryName => 'Hello world';";

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setName("QueryBlockCompletionTest");
        super.setUp();
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void queryBlockStart() {
        String content = "queries: |\n" +
                "   <caret>";
        assertCompletionContains(content, EXPECTED);
    }

    @Test
    void queryBlockAfterQuery() {
        String content = "queries: |\n" +
                "   DEFINE QUERY yourQueryName => 'test';\n" +
                "   <caret>";
        assertCompletionContains(content, EXPECTED);
    }

}
