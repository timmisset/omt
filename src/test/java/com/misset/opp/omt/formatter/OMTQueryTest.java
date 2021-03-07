package com.misset.opp.omt.formatter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OMTQueryTest extends OMTFormattingTest {
    @BeforeAll
    @Override
    public void setUp() throws Exception {
        super.setName("OMTQueryTest");
        super.setUp();
    }

    @AfterAll
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void testQueryShouldKeepIndentedQueryStepIntact() {
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query => $username / ont:property /\n" +
                "        ont:property2";
        assertFormattingShouldStayIntact(formatted);
    }

    @Test
    void testQueryShouldKeepStartingCompletelyAtNextLineIndentedQueryStepIntact() {
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query =>\n" +
                "        $username / ont:property / ont:property2";
        assertFormattingShouldStayIntact(formatted);
    }

    @Test
    void testQueryShouldKeepIndentedQueryStepFilterAtNewLineIntact() {
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query => $username / ont:property\n" +
                "        [\n" +
                "            ont:property2\n" +
                "        ]";
        assertFormattingShouldStayIntact(formatted);
    }

    @Test
    void testQueryShouldSetIndentedQueryStepFilterAtNewLine() {
        String unformatted = "queries: |\n" +
                "    DEFINE QUERY query => $username / ont:property\n" +
                "            [\n" +
                "            ont:property2\n" +
                "        ]";
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query => $username / ont:property\n" +
                "        [\n" +
                "            ont:property2\n" +
                "        ]";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testQueryShouldKeepIndentedSubQueryStepIntact() {
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query =>\n" +
                "        ($username / ont:property) / ont:property2";
        assertFormattingShouldStayIntact(formatted);
    }

    @Test
    void testQueryShouldKeepIndentedSubQueryWithSlashStepIntact() {
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query => .\n" +
                "        / ($username / ont:property) / ont:property2";
        assertFormattingShouldStayIntact(formatted);
    }

    @Test
    void testQueryShouldKeepIndentedSubQueryWithMultipleSlashStepIntact() {
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query => .\n" +
                "        / ($username / ont:property)\n" +
                "        / ont:property2\n" +
                "        / ont:property3";
        assertFormattingShouldStayIntact(formatted);
    }

    @Test
    void testQueryShouldIndentSubQueryStep() {
        String unformatted = "queries: |\n" +
                "    DEFINE QUERY query =>\n" +
                "    ($username / ont:property) / ont:property2";
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query =>\n" +
                "        ($username / ont:property) / ont:property2";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testQueryShouldSetChooseAtNewLine() {
        String unformatted = "queries: |\n" +
                "    DEFINE QUERY query => $username / CHOOSE \n" +
                "        WHEN . == 1 => 1\n" +
                "        WHEN . == 2 => 2\n" +
                "        OTHERWISE => 3\n" +
                "        END;";
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query => $username /\n" +
                "        CHOOSE\n" +
                "            WHEN . == 1 => 1\n" +
                "            WHEN . == 2 => 2\n" +
                "            OTHERWISE => 3\n" +
                "        END;";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testQueryShouldNotDoubleIndentOnForwardSlash() {
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query() =>\n" +
                "        $username\n" +
                "            / ont:someProperty;";
        assertFormattingShouldStayIntact(formatted);
    }

    @Test
    void testQueryShouldNotDoubleIndentOnMultipleForwardSlash() {
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query() =>\n" +
                "        $username\n" +
                "            / ont:someProperty\n" +
                "            / ont:someProperty;";
        assertFormattingShouldStayIntact(formatted);
    }

}
