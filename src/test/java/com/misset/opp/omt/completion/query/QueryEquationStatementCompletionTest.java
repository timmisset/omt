package com.misset.opp.omt.completion.query;

import com.misset.opp.omt.completion.OMTCompletionTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class QueryEquationStatementCompletionTest extends OMTCompletionTestSuite {

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setName("QueryFilterStepCompletionTest");
        super.setUp();
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void equationStatement() {
        String content = "queries: |\n" +
                "   DEFINE QUERY myQuery => /ont:ClassC [rdf:type == <caret>]";
        final List<String> completionLookupElements = getCompletionLookupElements(withPrefixes(content));
        assertCompletionContainsBuiltinOperators(completionLookupElements);
        assertCompletionContainsGlobalVariables(completionLookupElements);
        assertContainsElements(completionLookupElements, "ont:ClassCImpl", "ont:ClassCImpl");
    }

}

