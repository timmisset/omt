package com.misset.opp.omt.inspection.quickfixes;

import com.misset.opp.omt.formatter.OMTFormattingTest;
import com.misset.opp.omt.inspection.OMTCodeInspectionUnused;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RemoveDefinedParameterTest extends OMTFormattingTest {

    @BeforeAll
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setBuiltinAndModel();
        myFixture.enableInspections(OMTCodeInspectionUnused.class);
    }

    @AfterAll
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void testHasQuickFixes() {
        String content = "queries: |\n" +
                "    DEFINE QUERY yourQueryName($param) => 'Hello world';\n" +
                "    DEFINE QUERY yourQueryName2 => yourQueryName('test');\n" +
                "    DEFINE QUERY yourQueryName3 => yourQueryName('test');";
        assertNotNull(getQuickFixIntention(content, "Remove parameter"));
    }

    @Test
    void testQueriesRemovesParameterAndCallArguments() {
        String content = "queries: |\n" +
                "    DEFINE QUERY yourQueryName($param) => 'Hello world';\n" +
                "    DEFINE QUERY yourQueryName2 => yourQueryName('test');\n" +
                "    DEFINE QUERY yourQueryName3 => yourQueryName('test');";
        invokeQuickFixIntention(content, "Remove parameter");
        assertFormattingApplied(getFile().getText(), "queries: |\n" +
                "    DEFINE QUERY yourQueryName => 'Hello world';\n" +
                "    DEFINE QUERY yourQueryName2 => yourQueryName;\n" +
                "    DEFINE QUERY yourQueryName3 => yourQueryName;");
    }

    @Test
    void testCommandsRemovesParameterAndCallArguments() {
        String content = "commands: |\n" +
                "    DEFINE COMMAND yourCommandName($param) => { RETURN 'Hello world'; }\n" +
                "    DEFINE COMMAND yourCommandName2 => { @yourCommandName('test'); }\n" +
                "    DEFINE COMMAND yourCommandName3 => { @yourCommandName('test'); }";
        invokeQuickFixIntention(content, "Remove parameter");
        assertFormattingApplied(getFile().getText(), "commands: |\n" +
                "    DEFINE COMMAND yourCommandName => { RETURN 'Hello world'; }\n" +
                "    DEFINE COMMAND yourCommandName2 => { @yourCommandName(); }\n" +
                "    DEFINE COMMAND yourCommandName3 => { @yourCommandName(); }");
    }

}
