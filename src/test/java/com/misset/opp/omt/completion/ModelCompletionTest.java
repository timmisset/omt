package com.misset.opp.omt.completion;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Arrays;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ModelCompletionTest extends OMTCompletionTestSuite {
    private static final List<String> ALL_MODEL_ITEM_TYPES = Arrays.asList("!Activity", "!Component", "!Procedure", "!StandaloneQuery", "!Ontology");

    @BeforeAll
    @Override
    protected void setUp() throws Exception {
        super.setName("ModelCompletionTest");
        super.setUp();
    }

    @AfterAll
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
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
}

