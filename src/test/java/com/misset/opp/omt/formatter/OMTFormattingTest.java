package com.misset.opp.omt.formatter;

import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.util.containers.ContainerUtil;
import com.misset.opp.omt.OMTTestSuite;

import java.util.function.Consumer;

public abstract class OMTFormattingTest extends OMTTestSuite {

    // the indentation size used by the test suite
    protected static final int INDENT_SIZE = 4;

    protected void setLanguageSettings(PsiFile file, Consumer<CommonCodeStyleSettings> languageSettings) {
        languageSettings.accept(CodeStyle.getLanguageSettings(file));
    }

    protected void assertFormattingApplied(String unformatted, String formatted) {
        assertFormattingApplied(unformatted, formatted,
                psiFile -> setLanguageSettings(psiFile,
                        commonCodeStyleSettings ->
                                commonCodeStyleSettings.getIndentOptions().INDENT_SIZE = INDENT_SIZE));
    }

    protected void assertFormattingApplied(String unformatted, String formatted, Consumer<PsiFile> preformattingFile) {
        final PsiFile psiFile = myFixture.configureByText("test.omt", unformatted);
        preformattingFile.accept(psiFile);
        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(myFixture.getProject());
        WriteCommandAction.runWriteCommandAction(getProject(),
                () -> codeStyleManager.reformatText(psiFile,
                        ContainerUtil.newArrayList(myFixture.getFile().getTextRange())
                ));
        assertEquals(formatted.trim(), myFixture.getEditor().getDocument().getText().trim());
    }

    protected String configureHitEnterAndReturnDocumentText(String content, Consumer<PsiFile> psiFile) {
        final PsiFile configuredFile = myFixture.configureByText("test.omt", content);
        psiFile.accept(configuredFile);
        myFixture.type('\n');
        return myFixture.getEditor().getDocument().getText();
    }

    protected String configureHitEnterAndReturnDocumentText(String content) {
        return configureHitEnterAndReturnDocumentText(content, file -> {
        });
    }
}
