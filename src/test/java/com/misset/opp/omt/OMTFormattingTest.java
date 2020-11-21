package com.misset.opp.omt;

import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.util.containers.ContainerUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class OMTFormattingTest extends LightJavaCodeInsightFixtureTestCase {

    @BeforeEach
    void setUpSuite() throws Exception {
        super.setName("OMTEnterTypedHandlerTest");
        super.setUp();
    }

    @AfterEach
    void tearDownSuite() throws Exception {
        super.tearDown();
    }

    @Test
    void testSpacingSpaceAroundAssignmentOperators() {
        assertFormattingApplied("$variable =='a'", "$variable == 'a'",
                psiFile -> setLanguageSettings(psiFile,
                        commonCodeStyleSettings -> commonCodeStyleSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS = true)
        );
    }

    @Test
    void testIdentionBlocks() {
        assertFormattingApplied(
                "model:\n" +
                        " Activiteit: !Activity\n" +
                        "  title: 'TEST'\n" +
                        "\n" +
                        " Procedure: !Procedure\n" +
                        "  onRun: |\n" +
                        "    'test';\n",
                "model:\n" +
                        "    Activiteit: !Activity\n" +
                        "        title: 'TEST'\n" +
                        "\n" +
                        "    Procedure: !Procedure\n" +
                        "        onRun: |\n" +
                        "            'test';\n",
                psiFile -> setLanguageSettings(psiFile,
                        commonCodeStyleSettings ->
                                commonCodeStyleSettings.getIndentOptions().CONTINUATION_INDENT_SIZE = 4));
    }

    private void setLanguageSettings(PsiFile file, Consumer<CommonCodeStyleSettings> languageSettings) {
        languageSettings.accept(CodeStyle.getLanguageSettings(file));
    }

    private void assertFormattingApplied(String unformatted, String formattedExpected, Consumer<PsiFile> preformattingFile) {
        final PsiFile psiFile = myFixture.configureByText("test.omt", unformatted);
        preformattingFile.accept(psiFile);
        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(myFixture.getProject());
        WriteCommandAction.runWriteCommandAction(getProject(),
                () -> codeStyleManager.reformatText(psiFile,
                        ContainerUtil.newArrayList(myFixture.getFile().getTextRange())
                ));
        assertEquals(formattedExpected.trim(), psiFile.getText().trim());
    }

}
