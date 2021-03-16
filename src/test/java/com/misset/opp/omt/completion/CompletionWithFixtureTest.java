package com.misset.opp.omt.completion;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Arrays;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CompletionWithFixtureTest extends OMTCompletionTestSuite {

    private static final List<String> ALL_MODEL_ITEM_TYPES = Arrays.asList("!Activity", "!Component", "!Procedure", "!StandaloneQuery", "!Ontology");

    @BeforeAll
    protected void setUp() throws Exception {
        super.setName("CompletionWithFixtureTest");
        super.setUp();

        setExportingFile();
    }

    @AfterAll
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

    @Test
    void scriptContentStart() {
        String content = "commands:|\n" +
                "   DEFINE COMMAND myCommand => {\n" +
                "       <caret>\n" +
                "   }";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertCompletionContainsBuiltinCommands(completionLookupElements);
        assertCompletionContainsGlobalVariables(completionLookupElements);
        assertCompletionNOTContainsBuiltinOperators(completionLookupElements);
    }

    @Test
    void scriptContentStartSecondLine() {
        String content = "commands:|\n" +
                "   DEFINE COMMAND myCommand => {\n" +
                "       VAR $x = 'a';\n" +
                "       <caret>\n" +
                "   }";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertCompletionContainsBuiltinCommands(completionLookupElements);
        assertCompletionContainsGlobalVariables(completionLookupElements);
        assertCompletionNOTContainsBuiltinOperators(completionLookupElements);
    }

    @Test
    void scriptContentStartSecondLineWithoutSemicolon() {
        // The semicolon closure of a scriptline with scriptcontent is not required by the parser
        // but it is required by the OMT language. Instead of throwing a parser error, continue as
        // if it isn't required and annotate the content with a missing semicolon error
        String content = "commands:|\n" +
                "   DEFINE COMMAND myCommand => {\n" +
                "       VAR $x = 'a'\n" +
                "       <caret>\n" +
                "   }";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertCompletionContainsBuiltinCommands(completionLookupElements);
        assertCompletionContainsGlobalVariables(completionLookupElements);
        assertCompletionNOTContainsBuiltinOperators(completionLookupElements);
    }

    @Test
    void signatureArgumentDefinedQuery() {
        // myQuery == string
        // mySecondQuery == number
        // callableQuery accepts a string input at the first argument
        // when using @callableQuery, only myQuery should be suggested as input for callableQuery argument 1
        String content = "" +
                "queries:|\n" +
                "   DEFINE QUERY myQuery => 'Hello';\n" +
                "   DEFINE QUERY mySecondQuery => 12;\n" +
                "   /**\n" +
                "   * @param $myParam (string)\n" +
                "   */\n" +
                "   DEFINE QUERY callableQuery($myParam) => $myParam;\n" +
                "\n" +
                "commands:|\n" +
                "   DEFINE COMMAND myCommand => {\n" +
                "       callableQuery(<caret>)\n" +
                "   }";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "myQuery");
        assertDoesntContain(completionLookupElements, "mySecondQuery");
        assertDoesntContain(completionLookupElements, "callableQuery"); // don't call self

    }

    @Test
    void signatureSecondArgumentDefinedQuery() {
        // myQuery == string
        // mySecondQuery == number
        // callableQuery accepts a string input at the first argument
        // when using @callableQuery, only myQuery should be suggested as input for callableQuery argument 1
        String content = "" +
                "queries:|\n" +
                "   DEFINE QUERY myQuery => 'Hello';\n" +
                "   DEFINE QUERY mySecondQuery => 12;\n" +
                "   /**\n" +
                "   * @param $myParam (string)\n" +
                "   * @param $myParam2 (integer)\n" +
                "   */\n" +
                "   DEFINE QUERY callableQuery($myParam, $myParam2) => $myParam;\n" +
                "\n" +
                "commands:|\n" +
                "   DEFINE COMMAND myCommand => {\n" +
                "       callableQuery('test', <caret>);\n" +
                "   }";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertDoesntContain(completionLookupElements, "myQuery");
        assertContainsElements(completionLookupElements, "mySecondQuery");
        assertDoesntContain(completionLookupElements, "callableQuery"); // don't call self

    }

    // used by the import completion tests
    private void setExportingFile() {
        String exportData = "queries: |\n" +
                "   DEFINE QUERY myQuery => 'hello';\n" +
                "   DEFINE QUERY myQuery2 => 'hello';\n" +
                "\n" +
                "model:\n" +
                "   MijnActiviteit: !Activity\n" +
                "       queries:\n" +
                "           DEFINE QUERY myQuery3 => 'hello';\n" +
                "   MijnProcedure: !Procedure\n" +
                "   MijnComponent: !Component\n" +
                "";
        myFixture.addFileToProject("frontend/libs/exportFile.omt", exportData);
    }

    @Test
    void importAllMembers() {
        String content = "import:\n" +
                "   '@client/exportFile.omt':\n" +
                "   - <caret>\n";

        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "myQuery", "myQuery2", "MijnActiviteit", "MijnProcedure", "MijnComponent");
        assertDoesntContain(completionLookupElements, "myQuery3");
    }

    @Test
    void importWithoutExistingImportMember() {
        String content = "import:\n" +
                "   '@client/exportFile.omt':\n" +
                "   - myQuery\n" +
                "   - <caret>\n";

        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "myQuery2");
        assertDoesntContain(completionLookupElements, "myQuery");
    }

    @Test
    void modelNewEntry() {
        String content = "model:\n" +
                "   mijnActiviteit: !Activity\n" +
                "       <caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "params:", "title:", "variables:");
        assertCompletionNOTContainsBuiltinOperators(completionLookupElements);
    }

    @Test
    void modelNewEntryOnIndentedBlock() {
        String content = "model:\n" +
                "   mijnActiviteit: !Activity\n" +
                "       graphs:\n" +
                "           <caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "edit:", "live:");
        assertCompletionNOTContainsBuiltinOperators(completionLookupElements);
    }

    @Test
    void modelNewEntryAfterIndentedBlock() {
        String content = "model:\n" +
                "   mijnActiviteit: !Activity\n" +
                "       graphs:\n" +
                "       <caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "params:", "title:", "variables:");
        assertDoesntContain(completionLookupElements, "edit:", "live:");
        assertCompletionNOTContainsBuiltinOperators(completionLookupElements);
    }

    @Test
    void modelNewEntryNoIndentation() {
        String content = "model:\n" +
                "   mijnActiviteit: !Activity\n" +
                "   <caret>";
        assertCompletionNotContains(content, "params:", "title:", "variables:");
    }

    @Test
    void modelNewEntryPartiallyEntered() {
        String content = "model:\n" +
                "   mijnActiviteit: !Activity\n" +
                "       pa<caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "params:");
        assertDoesntContain(completionLookupElements, "title:", "variables:");
    }

    @Test
    void modelNewEntryWithExistingKeys() {
        String content = "model:\n" +
                "   mijnActiviteit: !Activity\n" +
                "       title: Titel\n" +
                "       <caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "params:", "variables:");
        assertDoesntContain(completionLookupElements, "title:");
    }

    @Test
    void modelDestructedElement() {
        String content = "model:\n" +
                "   mijnActiviteit: !Activity\n" +
                "       title: Titel\n" +
                "       variables:\n" +
                "           - <caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "name:", "value:", "onChange:");
        assertCompletionNOTContainsBuiltinOperators(completionLookupElements);
    }

    @Test
    void modelDestructedElementInVariableAssignment() {
        String content = "model:\n" +
                "   mijnActiviteit: !Activity\n" +
                "       title: Titel\n" +
                "       variables:\n" +
                "           - $variable = <caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertDoesntContain(completionLookupElements, "name:", "value:", "onChange:");
        assertCompletionContainsBuiltinOperators(completionLookupElements);
        assertCompletionNOTContainsBuiltinCommands(completionLookupElements);
    }

    @Test
    void modelDestructedElementWithExistingKeys() {
        String content = "model:\n" +
                "   mijnActiviteit: !Activity\n" +
                "       title: Titel\n" +
                "       variables:\n" +
                "           -   name: $mijnVariabele\n" +
                "               <caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "value:", "onChange:");
        assertDoesntContain(completionLookupElements, "name:");
        assertCompletionNOTContainsBuiltinOperators(completionLookupElements);
    }

    @Test
    void commandBlockStart() {
        String content = "commands: |\n" +
                "   <caret>";
        assertCompletionContains(content, "DEFINE COMMAND yourCommandName => { RETURN true; }");
    }

    @Test
    void commandBlockAfterCommand() {
        String content = "commands: |\n" +
                "   DEFINE COMMAND yourCommandName => { RETURN true; }\n" +
                "   <caret>";
        assertCompletionContains(content, "DEFINE COMMAND yourCommandName => { RETURN true; }");
    }

    @Test
    void modelItemTypesWithoutFlag() {
        String content = "model:\n" +
                "   mijnActiviteit: <caret>";
        assertCompletionSameContents(content, ALL_MODEL_ITEM_TYPES);
    }

    @Test
    void modelItemTypesWithExistingFlag() {
        String content = "model:\n" +
                "   mijnActiviteit: !<caret>";
        assertCompletionSameContents(content, ALL_MODEL_ITEM_TYPES);
    }

    @Test
    void modelItemTypesWithPartialAutoCompletes() {
        String content = "model:\n" +
                "   mijnActiviteit: !Act<caret>";
        assertCompletionAutocompleted(content, "model:\n" +
                "   mijnActiviteit: !Activity");
    }

    @Test
    void modelItemTypesWithExistingTypeCaretAtFlag() {
        String content = "model:\n" +
                "   mijnActiviteit: !<caret>Activity";
        assertCompletionSameContents(content, ALL_MODEL_ITEM_TYPES);
    }

    @Test
    void testNewDocument() {
        setReasons();
        String content = "<caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "queries:", "commands:", "moduleName:", "model:");
    }

    @Test
    void testFilterExisting() {
        setReasons();
        String content = "queries: |\n" +
                "   DEFINE QUERY query => '';\n" +
                "\n" +
                "<caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "commands:", "moduleName:", "model:");
    }

    @Test
    void testModule() {
        setReasons();
        String content = "moduleName: Mijn module\n" +
                "\n" +
                "<caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "commands:", "prefixes:");
        assertDoesntContain(completionLookupElements, "model:");
    }

    @Test
    void testModel() {
        setReasons();
        String content = "<caret>\n" +
                "\n" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       title: Mijn titel";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "commands:", "prefixes:");
        assertDoesntContain(completionLookupElements, "module:");
    }

    @Test
    void queryBlockStart() {
        String content = "queries: |\n" +
                "   <caret>";
        assertCompletionContains(content, "DEFINE QUERY yourQueryName => 'Hello world';");
    }

    @Test
    void queryBlockAfterQuery() {
        String content = "queries: |\n" +
                "   DEFINE QUERY yourQueryName => 'test';\n" +
                "   <caret>";
        assertCompletionContains(content, "DEFINE QUERY yourQueryName => 'Hello world';");
    }

    @Test
    void modelReasonProperty() {
        setReasons();
        String content = "model:\n" +
                "   mijnActiviteit: !Activity\n" +
                "       reason: <caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "Naam", "Naam2");
    }

    @Test
    void parameterTypeInParameter() {
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       params:\n" +
                "       - $paramA (<caret>)";

        final List<String> completionLookupElements = getCompletionLookupElements(withPrefixes(content));
        assertCompletionContainsClasses(completionLookupElements, false);
    }

    @Test
    void parameterTypeInParameterPrefixKnown() {
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       params:\n" +
                "       - $paramA (ont:<caret>)";

        final List<String> completionLookupElements = getCompletionLookupElements(withPrefixes(content));
        assertCompletionContainsClasses(completionLookupElements, false);
    }

    @Test
    void parameterTypeInParameterAnnotation() {
        String content = "/**\n" +
                " * @param $myParam (<caret>)\n" +
                " */\n" +
                "queries:|\n" +
                "   DEFINE QUERY query($myParam) => 'test';";

        final List<String> completionLookupElements = getCompletionLookupElements(withPrefixes(content));
        assertCompletionContainsClasses(completionLookupElements, false);
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
