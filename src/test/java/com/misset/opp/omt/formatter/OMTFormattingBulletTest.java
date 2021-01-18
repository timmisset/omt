package com.misset.opp.omt.formatter;

import com.intellij.application.options.CodeStyle;
import com.misset.opp.omt.settings.OMTCodeStyleSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OMTFormattingBulletTest extends OMTFormattingTest {

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
    void afterImportLocation() {
        String content = "" +
                "import:\n" +
                "    '@client/test.omt':<caret>";
        String expected = "" +
                "import:\n" +
                "    '@client/test.omt':\n" +
                "    - ";
        assertBullet(content, expected, false);
    }

    @Test
    void afterImportLocationWithIndent() {
        String content = "" +
                "import:\n" +
                "    '@client/test.omt':<caret>";
        String expected = "" +
                "import:\n" +
                "    '@client/test.omt':\n" +
                "        - ";
        assertBullet(content, expected, true);
    }

    @Test
    void afterImportMemberWithIndent() {
        String content = "" +
                "import:\n" +
                "    '@client/test.omt':\n" +
                "        - memberA<caret>";
        String expected = "" +
                "import:\n" +
                "    '@client/test.omt':\n" +
                "        - memberA\n" +
                "        - ";
        assertBullet(content, expected, true);
    }

    @Test
    void afterImportMember() {
        String content = "" +
                "import:\n" +
                "    '@client/test.omt':\n" +
                "    - member<caret>";
        String expected = "" +
                "import:\n" +
                "    '@client/test.omt':\n" +
                "    - member\n" +
                "    - ";
        assertBullet(content, expected, false);
    }

    @Test
    void afterEmptySequenceItem() {
        String content = "" +
                "import:\n" +
                "    '@client/test.omt':\n" +
                "    - <caret>";
        String expected = "" +
                "import:\n" +
                "    '@client/test.omt':\n" +
                "      ";
        assertBullet(content, expected, false);
    }

    @Test
    void afterKnownSequenceBlockWithoutIndentation() {
        String content = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       params:<caret>";
        String expected = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       params:\n" +
                "       - ";
        assertBullet(content, expected, false);
    }

    @Test
    void afterKnownSequenceBlockWithIndentation() {
        String content = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       params:<caret>";
        String expected = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       params:\n" +
                "           - ";
        assertBullet(content, expected, true);
    }

    @Test
    void afterSequenceItem() {
        String content = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       params:\n" +
                "           - $param<caret>";
        String expected = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       params:\n" +
                "           - $param\n" +
                "           - ";
        assertBullet(content, expected, true);
    }

    @Test
    void notAfterDestructedSequenceItem() {
        String content = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       params:\n" +
                "           -   name: 'test'<caret>";
        String expected = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       params:\n" +
                "           -   name: 'test'\n" +
                "               ";
        assertBullet(content, expected, false);
    }

    @Test
    void sequenceSpacingAfterBullet() {
        String content = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       params:<caret>";
        String expected = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       params:\n" +
                "           -   ";
        assertBullet(content, expected, true, true);
    }

    private void assertBullet(String content, String expectedResult, boolean indentSequenceBullet) {
        assertBullet(content, expectedResult, indentSequenceBullet, false);
    }

    private void assertBullet(String content, String expectedResult, boolean indentSequenceBullet, boolean indentAfterBullet) {
        final String result = configureHitEnterAndReturnDocumentText(content, file -> {
            final OMTCodeStyleSettings customSettings = CodeStyle.getCustomSettings(file, OMTCodeStyleSettings.class);
            customSettings.INDENT_SEQUENCE_VALUE = indentSequenceBullet;
            customSettings.INDENT_AFTER_SEQUENCE_VALUE = indentAfterBullet;

            CodeStyle.getLanguageSettings(file).getIndentOptions().INDENT_SIZE = 4;
        });
        assertEquals(expectedResult, result);
    }

}
