package com.misset.opp.omt.formatter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OMTFormattingReformattingTest extends OMTFormattingTest {

    @BeforeAll
    @Override
    public void setUp() throws Exception {
        super.setName("OMTFormattingReformattingTest");
        super.setUp();
    }

    @AfterAll
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////
    // SPACING
    // ////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void testSpacingSpaceAroundAssignmentOperators() {
        String unformatted = "commands: |\n" +
                "    DEFINE COMMAND command =>{}";
        String formatted = "commands: |\n" +
                "    DEFINE COMMAND command => {}";
        assertFormattingApplied(unformatted, formatted,
                psiFile -> setLanguageSettings(psiFile,
                        commonCodeStyleSettings -> commonCodeStyleSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS = true)
        );
    }

    @Test
    void spacesBeforePrefixIri() {
        String unformatted = "prefixes:\n" +
                " abc: <http://www.test.com>";
        String formatted = "prefixes:\n" +
                "    abc:    <http://www.test.com>";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void spacesBeforePrefixIriMultiple() {
        String unformatted = "prefixes:\n" +
                " abc: <http://www.test.com>\n" +
                " def: <http://www.test.com>";
        String formatted = "prefixes:\n" +
                "    abc:    <http://www.test.com>\n" +
                "    def:    <http://www.test.com>";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void spacesBeforePrefixIriMultipleWithLongPrefix() {
        String unformatted = "prefixes:\n" +
                " abc: <http://www.test.com>\n" +
                " defghijkl: <http://www.test.com>";
        String formatted = "prefixes:\n" +
                "    abc:          <http://www.test.com>\n" +
                "    defghijkl:    <http://www.test.com>";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testIndentationBlocks() {
        String unformatted = "model:\n" +
                " Activiteit: !Activity\n" +
                "  title: 'TEST'\n" +
                "\n" +
                " Procedure: !Procedure\n" +
                "  onRun: |\n" +
                "    'test';\n" +
                "    'test2';\n";
        String formatted = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        title: 'TEST'\n" +
                "\n" +
                "    Procedure: !Procedure\n" +
                "        onRun: |\n" +
                "            'test';\n" +
                "            'test2';\n";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testIndentationBlocksModelWithSpecificBlock() {
        String unformatted = "model:\n" +
                " Verklaring: !Activity\n" +
                "  title: Verklaring opnemen\n" +
                "\n" +
                "  queries: |\n" +
                "    DEFINE QUERY query => '';";
        String formatted = "model:\n" +
                "    Verklaring: !Activity\n" +
                "        title: Verklaring opnemen\n" +
                "\n" +
                "        queries: |\n" +
                "            DEFINE QUERY query => '';";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testScalarValue() {
        String unformatted = "model:\n" +
                "    mijnActiviteit: !Activity\n" +
                "        payload:\n" +
                "            payloadItem:\n" +
                "            $variable / functie() / EXISTS\n" + // <-- scalar value
                "";
        String formatted = "model:\n" +
                "    mijnActiviteit: !Activity\n" +
                "        payload:\n" +
                "            payloadItem:\n" +
                "                $variable / functie() / EXISTS\n" + // <-- scalar value
                "";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testIndentationPrefixes() {
        String unformatted = "prefixes:\n" +
                " pol:    <http://enter.your/iri/>\n";
        String formatted = "prefixes:\n" +
                "    pol:    <http://enter.your/iri/>\n";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testIndentationDefinedQueries() {
        String unformatted = "queries: |\n" +
                " DEFINE QUERY query => 'test';\n" +
                "  DEFINE QUERY query2 => 'test';\n";
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query => 'test';\n" +
                "    DEFINE QUERY query2 => 'test';";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testIndentationSubQuery() {
        String unformatted = "queries: |\n" +
                " DEFINE QUERY query => \n" +
                "  ('test');\n";
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query =>\n" +
                "        ('test');\n";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testIndentationFilter() {
        String unformatted = "queries: |\n" +
                "    DEFINE QUERY query => 'a'\n" +
                "        [\n" +
                "            . == 'test'\n" +
                "        ];";
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query => 'a'\n" +
                "        [\n" +
                "            . == 'test'\n" +
                "        ];";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testIndentationQueryPaths() {
        String unformatted = "queries: |\n" +
                "    DEFINE QUERY query($param) =>\n" +
                "        'test' /\n" +
                "               CHOOSE\n" +
                "                WHEN 'a == a' => 'a' /\n" +
                "                    FIRST\n" +
                "                OTHERWISE => null\n" +
                "                END ;";
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query($param) =>\n" +
                "        'test' /\n" +
                "            CHOOSE\n" +
                "                WHEN 'a == a' => 'a' /\n" +
                "                    FIRST\n" +
                "                OTHERWISE => null\n" +
                "            END ;";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testInterpolatedString() {
        String unformatted = "queries: |\n" +
                "    DEFINE QUERY query() => \n" +
                "        MAP(`{\"a\": \"${b}\",\n" +
                "              \"c\": \"${d}\",\n" +
                "              \"d\": \"${e}\",\n" +
                "              }`) / CAST(JSON);";
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query() =>\n" +
                "        MAP(`{\"a\": \"${b}\",\n" +
                "             \"c\": \"${d}\",\n" +
                "             \"d\": \"${e}\",\n" +
                "             }`) / CAST(JSON);";
        // the formatting is aligned on the first occurance of the String which starts with: {
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testLeadingComment() {
        String unformatted = "model:\n" +
                "    // Something about the comment\n" +
                "    Activiteit: !Activity\n";
        String formatted = "model:\n" +
                "    // Something about the comment\n" +
                "    Activiteit: !Activity";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testCommandBlocks() {
        String unformatted = "commands: |\n" +
                "    DEFINE COMMAND yourCommandName => { RETURN 'Hello world'; }\n" +
                "    DEFINE COMMAND yourCommandName2 => { @yourCommandName(); }\n";
        String formatted = "commands: |\n" +
                "    DEFINE COMMAND yourCommandName => {\n" +
                "        RETURN 'Hello world';\n" +
                "    }\n" +
                "    DEFINE COMMAND yourCommandName2 => {\n" +
                "        @yourCommandName();\n" +
                "    }\n";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testModelAndScalarCommentIndentationShouldStayIntact() {
        String formatted = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        #comment for model property\n" +
                "        onDone: |\n" +
                "            # comment line 1\n" +
                "            # comment line 2\n" +
                "            @LOG('do something');\n";
        assertFormattingShouldStayIntact(formatted);
    }

    @Test
    void testModelAndScalarWithoutDecoratorCommentIndentationShouldStayIntact() {
        String formatted = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        #comment for model property\n" +
                "        onDone:\n" +
                "            # comment line 1\n" +
                "            # comment line 2\n" +
                "            @LOG('do something');\n";
        assertFormattingShouldStayIntact(formatted);
    }

    @Test
    void testSequenceItemIndentationShouldStayIntact() {
        String formatted = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        variables:\n" +
                "        -   $mijnVariabel";
        assertFormattingShouldStayIntact(formatted);
    }

    @Test
    void testSequenceItemWithTagIndentationShouldStayIntact() {
        String formatted = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        variables:\n" +
                "        -   !Ref $mijnVariabel";
        assertFormattingShouldStayIntact(formatted);
    }
}
