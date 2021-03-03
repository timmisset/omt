package com.misset.opp.omt.completion;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QueryCompletionTest extends OMTCompletionTestSuite {

    @BeforeAll
    @Override
    protected void setUp() throws Exception {
        super.setName("QueryCompletionTest");
        super.setUp();
    }

    @AfterAll
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
        assertCompletionContains(withPrefixes(content), "^rdf:type", "^rdfs:subClassOf");
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
        assertCompletionContains(withPrefixes(content), "^rdf:type", "^rdfs:subClassOf");
    }

    @Test
    void nextQueryStepInsidePathPartial2() {
        String content = "queries: |\n" +
                "   DEFINE QUERY myQuery => /ont:ClassA / ^rdf:<caret> / ont:classProperty;";
        assertCompletionContains(withPrefixes(content), "^rdf:type", "^rdfs:subClassOf");
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

    @Test
    void firstQueryStep() {
        String content = "queries: |\n" +
                "   DEFINE QUERY myQuery => <caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertCompletionContainsClasses(completionLookupElements, true);
        assertCompletionContainsGlobalVariables(completionLookupElements);
        assertCompletionContainsBuiltinOperators(completionLookupElements);
    }

    @Test
    void firstQueryStepHasInputParameter() {
        String content = "queries: |\n" +
                "   DEFINE QUERY myQuery($myParam) => <caret>";
        assertCompletionContains(content, "$myParam");
    }

    @Test
    void firstQueryStepHasModelParameterAndVariable() {
        String content = "model:\n" +
                "   MijnActiviteit: !Activity\n" +
                "       queries: |\n" +
                "           DEFINE QUERY myQuery => <caret>;\n" +
                "       params:\n" +
                "           - $myParam\n" +
                "       variables:\n" +
                "           - $myVariable\n";
        assertCompletionContains(content, "$myParam", "$myVariable");
    }

    @Test
    void sequencePreviousQuery() {
        String content = "queries: |\n" +
                "   DEFINE QUERY previousQuery => 'I should be suggested';\n" +
                "   DEFINE QUERY query => <caret>;\n" +
                "   DEFINE QUERY nextQuery => 'I should NOT be suggested';\n";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "previousQuery");
        assertDoesntContain(completionLookupElements, "nextQuery");
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
                "   DEFINE QUERY myQuery => /ont:ClassA / ^rdf:type [ont:booleanProperty / <caret>]";
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

