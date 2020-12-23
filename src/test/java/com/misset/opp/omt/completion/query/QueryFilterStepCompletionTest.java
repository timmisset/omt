package com.misset.opp.omt.completion.query;

import com.misset.opp.omt.completion.OMTCompletionTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class QueryFilterStepCompletionTest extends OMTCompletionTestSuite {

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
    void queryFilterFirstStep() {
        String content = "queries: |\n" +
                "   DEFINE QUERY myQuery => /ont:ClassA [<caret>]";
        final List<String> completionLookupElements = getCompletionLookupElements(withPrefixes(content));
        assertCompletionContainsBuiltinOperators(completionLookupElements);
        assertCompletionNOTContainsBuiltinCommands(completionLookupElements);
        assertCompletionContainsGlobalVariables(completionLookupElements);
        assertContainsElements(completionLookupElements, "^rdf:type");
    }

    @Test
    void queryFilterNextStep() {
        String content = "queries: |\n" +
                "   DEFINE QUERY myQuery => /ont:ClassA [ont:booleanProperty / <caret>]";
        final List<String> completionLookupElements = getCompletionLookupElements(withPrefixes(content));
        assertCompletionContainsBuiltinOperators(completionLookupElements);
        assertCompletionNOTContainsBuiltinCommands(completionLookupElements);
        assertCompletionNOTContainsGlobalVariables(completionLookupElements);
        assertContainsElements(completionLookupElements, "^ont:booleanProperty");
    }

    @Test
    void queryFilterFirstStepEmptyFilter() {
        String content = "queries: |\n" +
                "    DEFINE QUERY yourQueryName => $variable [<caret>];";
        final List<String> completionLookupElements = getCompletionLookupElements(withPrefixes(content));
        assertContainsElements(completionLookupElements, "rdf:type");
    }
}

