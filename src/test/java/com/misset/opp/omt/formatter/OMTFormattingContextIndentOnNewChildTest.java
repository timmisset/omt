package com.misset.opp.omt.formatter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OMTFormattingContextIndentOnNewChildTest extends OMTFormattingTest {

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setName("OMTFormattingContextIndentOnNewChild");
        super.setUp();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void emptyDocument() {
        String content = "<caret>";
        String expected = "\n";
        assertIndentation(content, expected);
    }

    @Test
    void afterEntry() {
        String content = "" +
                "model:<caret>";
        String expected = "" +
                "model:\n" +
                "....";
        assertIndentation(content, expected);
    }

    @Test
    void afterBlankLine() {
        String content = "" +
                "model:\n" +
                "....<caret>";
        String expected = "" +
                "model:\n" +
                "....\n" +
                "....";
        assertIndentation(content, expected);
    }

    @Test
    void insideModelAfterIncomplete() {
        String content = "" +
                "model:\n" +
                "....Activiteit: !Activity<caret>";
        String expected = "" +
                "model:\n" +
                "....Activiteit: !Activity\n" +
                "........";
        assertIndentation(content, expected);
    }

    @Test
    void insideModelAfterScalar() {
        String content = "" +
                "model:\n" +
                "....Activiteit: !Activity\n" +
                "........title: mijnTitel<caret>";
        String expected = "" +
                "model:\n" +
                "....Activiteit: !Activity\n" +
                "........title: mijnTitel\n" +
                "........";
        assertIndentation(content, expected);
    }

    @Test
    void insideQueriesBlock() {
        String content = "" +
                "queries: |\n" +
                "....DEFINE QUERY query => 'test';<caret>";
        String expected = "" +
                "queries: |\n" +
                "....DEFINE QUERY query => 'test';\n" +
                "....";
        assertIndentation(content, expected);
    }

    @Test
    void insideCommandsBlock() {
        String content = "" +
                "commands: |\n" +
                "....DEFINE COMMAND command => {}<caret>";
        String expected = "" +
                "commands: |\n" +
                "....DEFINE COMMAND command => {}\n" +
                "....";
        assertIndentation(content, expected);
    }

    @Test
    void betweenQueriesInBlock() {
        String content = "" +
                "queries: |\n" +
                "....DEFINE QUERY query => 'test';<caret>\n" +
                "....DEFINE QUERY query2 => 'test';";
        String expected = "" +
                "queries: |\n" +
                "....DEFINE QUERY query => 'test';\n" +
                "....\n" +
                "....DEFINE QUERY query2 => 'test';";
        assertIndentation(content, expected);
    }

    @Test
    void commandBlock() {
        String content = "" +
                "commands: |\n" +
                "....DEFINE COMMAND command => \n" +
                "....{<caret>";
        String expected = "" +
                "commands: |\n" +
                "....DEFINE COMMAND command => \n" +
                "....{\n" +
                "........\n" +
                "....}";
        assertIndentation(content, expected);
    }

    private void assertIndentation(String content, String expectedResult) {
        content = content.replace(".", " ");
        expectedResult = expectedResult.replace(".", " ");
        final String result = configureHitEnterAndReturnDocumentText(content);
        assertEquals(expectedResult, result);
    }

}
