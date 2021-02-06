package com.misset.opp.omt.completion.model;

import com.misset.opp.omt.completion.OMTCompletionTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class ModelRootCompletionTest extends OMTCompletionTestSuite {

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setName("ModelRootCompletionTest");
        super.setUp();
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
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

}

