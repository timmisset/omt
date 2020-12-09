package com.misset.opp.omt.completion;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class QueryStepCompletionTest extends OMTCompletionTestSuite {

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setName("QueryStepCompletionTest");
        super.setUp();
        setOntologyModel();
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void firstQueryStep() {
        String content = "queries: |\n" +
                "   DEFINE QUERY myQuery => <caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertCompletionContainsClasses(completionLookupElements);
        assertCompletionContainsGlobalVariables(completionLookupElements);
    }
}

