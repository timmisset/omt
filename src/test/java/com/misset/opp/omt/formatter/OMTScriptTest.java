package com.misset.opp.omt.formatter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OMTScriptTest extends OMTFormattingTest {
    @BeforeAll
    @Override
    public void setUp() throws Exception {
        super.setName("OMTScriptTest");
        super.setUp();
    }

    @AfterAll
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void testScriptShouldNotHaveLineFeedAroundELSE() {
        String unformatted = "" +
                "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        onRun: |\n" +
                "            IF 1 == 2 {\n" +
                "                @LOG('1');\n" +
                "            }\n" +
                "            ELSE\n" +
                "            {\n" +
                "                @LOG('2');\n" +
                "            }";
        String formatted = "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        onRun: |\n" +
                "            IF 1 == 2 {\n" +
                "                @LOG('1');\n" +
                "            } ELSE {\n" +
                "                @LOG('2');\n" +
                "            }";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testScriptShouldNotHaveLineBetweenIFBlockAndCommandBlock() {
        String unformatted = "" +
                "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        onRun: |\n" +
                "            IF 1 == 2 \n" +
                "            {\n" +
                "                @LOG('1');\n" +
                "            }\n";
        String formatted = "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        onRun: |\n" +
                "            IF 1 == 2 {\n" +
                "                @LOG('1');\n" +
                "            }";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testScriptShouldHaveLineFeedAtCommandBlockStartAndEnd() {
        String unformatted = "" +
                "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        onRun: |\n" +
                "            IF 1 == 2 { @LOG('1'); }";
        String formatted = "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        onRun: |\n" +
                "            IF 1 == 2 {\n" +
                "                @LOG('1');\n" +
                "            }";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testScriptSignatureArgumentIndentationShouldStayIntact() {
        String formatted = "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        onRun: |\n" +
                "            IF 1 == 2 {\n" +
                "                @LOG(\n" +
                "                    '1',\n" +
                "                    '2'\n" +
                "                );\n" +
                "            }";
        assertFormattingShouldStayIntact(formatted);
    }

}
