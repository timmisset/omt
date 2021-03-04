package com.misset.opp.omt.formatter;

import com.intellij.application.options.CodeStyle;
import com.misset.opp.omt.settings.OMTCodeStyleSettings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OMTBlankLineTest extends OMTFormattingTest {

    @BeforeAll
    @Override
    public void setUp() throws Exception {
        super.setName("OMTBlankLineTest");
        super.setUp();
    }

    @AfterAll
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void emptyLinesBetweenRootBlocks() {
        String content = "" +
                "prefixes:\n" +
                "    ont:    <http://ontologie#>\n" +
                "model:\n" +
                "    Activiteit: !Activity";
        String expected = "" +
                "prefixes:\n" +
                "    ont:    <http://ontologie#>\n" +
                "\n" +
                "model:\n" +
                "    Activiteit: !Activity";
        assertSpacing(content, expected);
    }

    @Test
    void emptyLinesBetweenRootBlocksWithComments() {
        String content = "" +
                "prefixes:\n" +
                "    ont:    <http://ontologie#>\n" +
                "\n" +
                "\n" +
                "#Some comment\n" +
                "model:\n" +
                "    Activiteit: !Activity";
        String expected = "" +
                "prefixes:\n" +
                "    ont:    <http://ontologie#>\n" +
                "\n" +
                "#Some comment\n" +
                "model:\n" +
                "    Activiteit: !Activity";
        assertSpacing(content, expected);
    }

    @Test
    void emptyLinesBetweenModelItems() {
        String content = "" +
                "model:\n" +
                "    Activiteit: !Activity\n" +
                "        title: mijnTitel\n" +
                "    Proc: !Procedure\n" +
                "        onRun: |\n" +
                "           RETURN true;";
        String expected = "" +
                "model:\n" +
                "    Activiteit: !Activity\n" +
                "        title: mijnTitel\n" +
                "\n" +
                "    Proc: !Procedure\n" +
                "        onRun: |\n" +
                "            RETURN true;";
        assertSpacing(content, expected);
    }

    private void assertSpacing(String content, String expectedResult) {
        assertFormattingApplied(content, expectedResult, file -> {
            final OMTCodeStyleSettings customSettings = CodeStyle.getCustomSettings(file, OMTCodeStyleSettings.class);
            customSettings.INDENT_SEQUENCE_VALUE = false;
            customSettings.INDENT_AFTER_SEQUENCE_VALUE = false;
            CodeStyle.getLanguageSettings(file).getIndentOptions().INDENT_SIZE = 4;
        });
    }

}
