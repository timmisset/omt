package com.misset.opp.omt.formatter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OMTModelIndentationTest extends OMTFormattingTest {
    @BeforeAll
    @Override
    public void setUp() throws Exception {
        super.setName("OMTModelIndentationTest");
        super.setUp();
    }

    @AfterAll
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void testModelShouldKeepFormattingIntactModelAttributes() {
        String formatted = "" +
                "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        title: Mijn Activiteit";
        assertFormattingShouldStayIntact(formatted);
    }

    @Test
    void testCommentShouldKeepFormattingWithEOLIntactModelAttributes() {
        String formatted = "" +
                "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        # test\n" +
                "        title: Mijn Activiteit";
        assertFormattingShouldStayIntact(formatted);
    }

    @Test
    void testCommentShouldKeepFormattingWithMultilineCommentIntactModelAttributes() {
        String formatted = "" +
                "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        /*\n" +
                "        * Test\n" +
                "        */\n" +
                "        title: Mijn Activiteit";
        assertFormattingShouldStayIntact(formatted);
    }

    @Test
    void testCommentShouldKeepFormattingWithJavaDocsCommentIntactModelAttributes() {
        String formatted = "" +
                "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        /**\n" +
                "         * Test\n" +
                "         */\n" +
                "        title: Mijn Activiteit";
        assertFormattingShouldStayIntact(formatted);
    }

    @Test
    void testCommentShouldKeepFormattingWithEOLIntactBeforeDefinedStatement() {
        String formatted = "" +
                "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        queries: |\n" +
                "            # iets over deze query\n" +
                "            DEFINE QUERY query => '';";
        assertFormattingShouldStayIntact(formatted);
    }

    @Test
    void testCommentShouldKeepFormattingWithEOLIntactBetweenDefinedStatement() {
        String formatted = "" +
                "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        queries: |\n" +
                "            # iets over deze query\n" +
                "            DEFINE QUERY query => '';\n" +
                "            # iets over deze query\n" +
                "            DEFINE QUERY query => '';";
        assertFormattingShouldStayIntact(formatted);
    }

    @Test
    void testScriptShouldKeepFormattingWithScriptline() {
        String formatted = "" +
                "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        onRun: |\n" +
                "            VAR $myVariable = 'test';\n" +
                "            @LOG($myVariable);";
        assertFormattingShouldStayIntact(formatted);
    }
}
