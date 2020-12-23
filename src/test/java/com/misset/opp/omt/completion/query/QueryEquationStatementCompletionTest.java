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
    void equationStatementImplementation() {
        String content = "queries: |\n" +
                "   DEFINE QUERY myQuery => /ont:ClassC / ^rdf:type [rdf:type == <caret>]";
        final List<String> completionLookupElements = getCompletionLookupElements(withPrefixes(content));
        assertCompletionContainsGlobalVariables(completionLookupElements);
        assertContainsElements(completionLookupElements, "/ont:ClassCImpl", "/ont:ClassCImpl2");
    }

    @Test
    void equationStatementUnknownTypeShowsAllClasses() {
        String content = "queries: |\n" +
                "   DEFINE QUERY myQuery => $variable [rdf:type == <caret>]";
        final List<String> completionLookupElements = getCompletionLookupElements(withPrefixes(content));
        assertCompletionContainsGlobalVariables(completionLookupElements);
        assertCompletionContainsClasses(completionLookupElements, true);
    }

    @Test
    void equationStatementTypeCheck() {
        String content = "" +
                "queries:|\n" +
                "   DEFINE QUERY query => 'test';\n" +
                "   DEFINE QUERY query2 => 12;\n" +
                "\n" +
                "commands: |\n" +
                "   DEFINE COMMAND myCommand => {\n" +
                "       IF 'x' == <caret>\n" +
                "   }";
        final List<String> completionLookupElements = getCompletionLookupElements(withPrefixes(content));
        assertCompletionContainsGlobalVariables(completionLookupElements);
        assertContainsElements(completionLookupElements, "query");
        assertDoesntContain(completionLookupElements, "query2");
    }
}

