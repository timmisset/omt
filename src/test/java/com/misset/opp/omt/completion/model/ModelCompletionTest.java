package com.misset.opp.omt.completion.model;

import com.misset.opp.omt.completion.OMTCompletionTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class ModelCompletionTest extends OMTCompletionTestSuite {

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setName("ModelCompletionTest");
        super.setUp();
    }

    @AfterEach
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
}

