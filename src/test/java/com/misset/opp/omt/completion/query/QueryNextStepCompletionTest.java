package com.misset.opp.omt.completion.query;

import com.misset.opp.omt.completion.OMTCompletionTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class QueryNextStepCompletionTest extends OMTCompletionTestSuite {

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setName("QueryNextStepCompletionTest");
        super.setUp();
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void nextQueryStep() {
        String content = "queries: |\n" +
                "   DEFINE QUERY myQuery => /ont:ClassA / ^rdf:type / <caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(withPrefixes(content));
        assertCompletionContainsBuiltinOperators(completionLookupElements);
        assertCompletionDoesntGlobalVariables(completionLookupElements);
        assertContainsElements(completionLookupElements, "ont:booleanProperty", "ont:classProperty", "ont:stringProperty", "rdf:type");
    }

}

