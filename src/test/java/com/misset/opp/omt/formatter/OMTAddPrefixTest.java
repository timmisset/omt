package com.misset.opp.omt.formatter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OMTAddPrefixTest extends OMTFormattingTest {
    @BeforeAll
    @Override
    public void setUp() throws Exception {
        super.setName("OMTAddPrefixTest");
        super.setUp();
    }

    @AfterAll
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
