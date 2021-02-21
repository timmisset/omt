package com.misset.opp.omt.formatter;

import com.misset.opp.omt.psi.util.ImportUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static util.UtilManager.getImportUtil;

class OMTAddPrefixTest extends OMTFormattingTest {

    private static final String REST_OF_FILE_CONTENT = "commands: |\n" +
            "   DEFINE COMMAND command => {\n" +
            "       @Command()\n" +
            "   }";
    private static final String PREFIX = "ont";
    private static final String IRI = "http://ontologie";
    private static final ImportUtil importUtil = getImportUtil();
    private String existingImportBlock = "";

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setName("OMTAddPrefixTest");
        super.setUp();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void persistCorrectImport() {
        String formatted = "prefixes:\n" +
                "    ont:     <http://ontologie>\n" +
                "    ont2:    <http://ontologie2>\n";
        assertFormattingApplied(formatted, formatted);
    }

    @Test
    void addsBlankLineForAddedPrefix() {
        String unformatted = "prefixes:\n" +
                "    ont:     <http://ontologie>ont2:    <http://ontologie2>\n";
        String formatted = "prefixes:\n" +
                "    ont:     <http://ontologie>\n" +
                "    ont2:    <http://ontologie2>\n";
        assertFormattingApplied(unformatted, formatted);
    }

}
