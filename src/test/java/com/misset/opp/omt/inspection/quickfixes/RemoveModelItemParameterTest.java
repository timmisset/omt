package com.misset.opp.omt.inspection.quickfixes;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.ReadAction;
import com.misset.opp.omt.formatter.OMTFormattingTest;
import com.misset.opp.omt.inspection.OMTCodeInspectionUnused;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RemoveModelItemParameterTest extends OMTFormattingTest {

    String content = "model:\n" +
            "    Activiteit: !Activity\n" +
            "        onStart: |\n" +
            "            @MijnProcedure('test', 'test2', 'test3');\n" +
            "\n" +
            "    MijnProcedure: !Procedure\n" +
            "        params:\n" +
            "        -   $param\n" +
            "        -   $param2\n" +
            "        -   $param3\n";

    List<IntentionAction> allQuickFixes;

    @BeforeAll
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setBuiltinAndModel();
        myFixture.enableInspections(OMTCodeInspectionUnused.class);
    }

    @BeforeEach
    protected void resetFixture() {
        allQuickFixes = getAllQuickFixes(content);
    }

    @AfterAll
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void testHasQuickFixes() {
        assertEquals(3, allQuickFixes.size());
        ReadAction.run(() -> allQuickFixes.forEach(
                intentionAction -> assertEquals("Remove parameter and refactor call signatures to this Procedure", intentionAction.getText())
        ));
    }

    @Test
    void testRemovesFirstParameterFromModelItemAndCall() {
        final IntentionAction intentionAction = allQuickFixes.get(0);
        String content = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        onStart: |\n" +
                "            @MijnProcedure('test2', 'test3');\n" +
                "\n" +
                "    MijnProcedure: !Procedure\n" +
                "        params:\n" +
                "        -   $param2\n" +
                "        -   $param3\n";

        invokeQuickFixIntention(intentionAction);
        assertFormattingApplied(getFile().getText(), content);
    }

    @Test
    void testRemovesMiddleParameterFromModelItemAndCall() {
        final IntentionAction intentionAction = allQuickFixes.get(1);
        String content = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        onStart: |\n" +
                "            @MijnProcedure('test', 'test3');\n" +
                "\n" +
                "    MijnProcedure: !Procedure\n" +
                "        params:\n" +
                "        -   $param\n" +
                "        -   $param3\n";

        invokeQuickFixIntention(intentionAction);
        assertFormattingApplied(getFile().getText(), content);
    }

    @Test
    void testRemovesLastParameterFromModelItemAndCall() {
        final IntentionAction intentionAction = allQuickFixes.get(2);
        String content = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        onStart: |\n" +
                "            @MijnProcedure('test', 'test2');\n" +
                "\n" +
                "    MijnProcedure: !Procedure\n" +
                "        params:\n" +
                "        -   $param\n" +
                "        -   $param2\n";

        invokeQuickFixIntention(intentionAction);
        assertFormattingApplied(getFile().getText(), content);
    }
}
