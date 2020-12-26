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
        assertCompletionNOTContainsGlobalVariables(completionLookupElements);
        assertContainsElements(completionLookupElements, "ont:booleanProperty", "ont:classProperty", "ont:stringProperty", "rdf:type");
        assertDoesntContain(completionLookupElements, "^rdf:type");
    }

    @Test
    void nextQueryStepShowsReverseType() {
        String content = "queries: |\n" +
                "   DEFINE QUERY myQuery => /ont:ClassA / <caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(withPrefixes(content));
        assertCompletionContainsBuiltinOperators(completionLookupElements);
        assertCompletionNOTContainsGlobalVariables(completionLookupElements);
        assertContainsElements(completionLookupElements, "^rdf:type");
    }

    @Test
    void nextQueryStepPartial() {
        String content = "queries: |\n" +
                "   DEFINE QUERY myQuery => /ont:ClassA / ^rdf:type / ont:<caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(withPrefixes(content));
        assertCompletionNOTContainsBuiltinOperators(completionLookupElements);
        assertCompletionNOTContainsGlobalVariables(completionLookupElements);
        assertContainsElements(completionLookupElements, "ont:booleanProperty", "ont:classProperty", "ont:stringProperty");
        assertDoesntContain(completionLookupElements, "rdf:type", "^rdf:type");
    }

    @Test
    void nextQueryStepPartialReverse() {
        String content = "queries: |\n" +
                "   DEFINE QUERY myQuery => /ont:ClassA / ^<caret>";
        String completed = "queries: |\n" +
                "   DEFINE QUERY myQuery => /ont:ClassA / ^rdf:type";
        assertCompletionAutocompleted(withPrefixes(content), withPrefixes(completed));
    }

    @Test
    void nextQueryStepInsidePath() {
        String content = "queries: |\n" +
                "   DEFINE QUERY myQuery => /ont:ClassA / ^rdf:type / <caret> / ont:classProperty;";
        final List<String> completionLookupElements = getCompletionLookupElements(withPrefixes(content));
        assertCompletionContainsBuiltinOperators(completionLookupElements);
        assertCompletionNOTContainsGlobalVariables(completionLookupElements);
        assertContainsElements(completionLookupElements, "ont:booleanProperty", "ont:classProperty", "ont:stringProperty", "rdf:type");
        assertDoesntContain(completionLookupElements, "^rdf:type");
    }

    @Test
    void nextQueryStepInsidePathPartial() {
        String content = "queries: |\n" +
                "   DEFINE QUERY myQuery => /ont:ClassA / ^<caret> / ont:classProperty;";
        String completed = "queries: |\n" +
                "   DEFINE QUERY myQuery => /ont:ClassA / ^rdf:type / ont:classProperty;";
        assertCompletionAutocompleted(withPrefixes(content), withPrefixes(completed));
    }

    @Test
    void nextQueryStepInsidePathPartial2() {
        String content = "queries: |\n" +
                "   DEFINE QUERY myQuery => /ont:ClassA / ^rdf:<caret> / ont:classProperty;";
        String completed = "queries: |\n" +
                "   DEFINE QUERY myQuery => /ont:ClassA / ^rdf:type / ont:classProperty;";
        assertCompletionAutocompleted(withPrefixes(content), withPrefixes(completed));
    }

    @Test
    void nextQueryStepInsidePathWithCompleteStructure() {
        String content =
                "model:\n" +
                        "    AanpassenAangifte: !Activity\n" +
                        "        queries: |\n" +
                        "            DEFINE QUERY query($variable) =>\n" +
                        "                $variable / ont:classProperty / <caret> ont:stringProperty;\n" +
                        "";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertCompletionContainsBuiltinOperators(completionLookupElements);
    }

}

