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
        assertCompletionContains(content, "params:", "title:", "variables:");
    }

    @Test
    void modelNewEntryIndentation() {
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
    void modelNewEntryWithoutExistingKeys() {
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
        assertCompletionContains(content, "name:", "value:", "onChange:");

    }

    @Test
    void modelDestructedElementWithoutExistingKeys() {
        String content = "model:\n" +
                "   mijnActiviteit: !Activity\n" +
                "       title: Titel\n" +
                "       variables:\n" +
                "           -   name: $mijnVariabele\n" +
                "               <caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "value:", "onChange:");
        assertDoesntContain(completionLookupElements, "name:");
        assertCompletionDoesntContainBuiltinOperators(completionLookupElements);
    }
}

