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
    void testIndentationBlocks() {
        String text = "model:\n" +
                " Activiteit: !Activity\n" +
                "  title: 'TEST'\n" +
                "\n" +
                " Procedure: !Procedure\n" +
                "  onRun: |\n" +
                "    'test';\n" +
                "    'test2';\n";
        String expectedText = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        title: 'TEST'\n" +
                "\n" +
                "    Procedure: !Procedure\n" +
                "        onRun: |\n" +
                "            'test';\n" +
                "            'test2';\n";
        assertFormattingApplied(text, expectedText,
                psiFile -> setLanguageSettings(psiFile,
                        commonCodeStyleSettings ->
                                commonCodeStyleSettings.getIndentOptions().INDENT_SIZE = 4));
    }

    @Test
        // TODO: testen
    void testScalarValue() {
        assertFormattingApplied("model:\n" +
                        "    mijnActiviteit: !Activity\n" +
                        "        payload:\n" +
                        "            payloadItem:\n" +
                        "            $variable / functie() / EXISTS\n" + // <-- scalar value
                        "", "model:\n" +
                        "    mijnActiviteit: !Activity\n" +
                        "        payload:\n" +
                        "            payloadItem:\n" +
                        "                $variable / functie() / EXISTS\n" + // <-- scalar value
                        "",
                psiFile -> setLanguageSettings(psiFile,
                        commonCodeStyleSettings ->
                                commonCodeStyleSettings.getIndentOptions().INDENT_SIZE = 4));
    }

    @Test
    void testIndentationPrefixes() {
        assertFormattingApplied(
                "prefixes:\n" +
                        " pol:    <http://enter.your/iri/>\n",
                "prefixes:\n" +
                        "    pol:    <http://enter.your/iri/>\n",
                psiFile -> setLanguageSettings(psiFile,
                        commonCodeStyleSettings ->
                                commonCodeStyleSettings.getIndentOptions().INDENT_SIZE = 4));
    }

    @Test
    void testIndentationDefinedQueries() {
        assertFormattingApplied(
                "queries: |\n" +
                        " DEFINE QUERY query => 'test';\n" +
                        "  DEFINE QUERY query2 => 'test';\n",
                "queries: |\n" +
                        "    DEFINE QUERY query => 'test';\n" +
                        "    DEFINE QUERY query2 => 'test';",
                psiFile -> setLanguageSettings(psiFile,
                        commonCodeStyleSettings ->
                                commonCodeStyleSettings.getIndentOptions().INDENT_SIZE = 4));
    }

    @Test
    void testIndentationSubQuery() {
        assertFormattingApplied(
                "queries: |\n" +
                        " DEFINE QUERY query => \n" +
                        "  ('test');\n",
                "queries: |\n" +
                        "    DEFINE QUERY query =>\n" +
                        "        ('test');\n",
                psiFile -> setLanguageSettings(psiFile,
                        commonCodeStyleSettings ->
                                commonCodeStyleSettings.getIndentOptions().INDENT_SIZE = 4));
    }

    @Test
    void testIndentationFilter() {
        String text = "queries: |\n" +
                "    DEFINE QUERY query => 'a'\n" +
                "        [\n" +
                "            . == 'test'\n" +
                "        ];";
        String expectedString = "queries: |\n" +
                "    DEFINE QUERY query => 'a'\n" +
                "        [\n" +
                "            . == 'test'\n" +
                "        ];";
        assertFormattingApplied(text, expectedString,
                psiFile -> setLanguageSettings(psiFile,
                        commonCodeStyleSettings ->
                                commonCodeStyleSettings.getIndentOptions().INDENT_SIZE = 4));
    }

    @Test
    void testIndentationQueryPaths() {
        String text = "queries: |\n" +
                "    DEFINE QUERY query($param) =>\n" +
                "        'test'\n" +
                "            /   CHOOSE\n" +
                "                WHEN 'a == a' => 'a' /\n" +
                "                    FIRST\n" +
                "                OTHERWISE => null\n" +
                "                END ;";
        String expected = "queries: |\n" +
                "    DEFINE QUERY query($param) =>\n" +
                "        'test'\n" +
                "        /   CHOOSE\n" +
                "                WHEN 'a == a' => 'a' /\n" +
                "                    FIRST\n" +
                "                OTHERWISE => null\n" +
                "            END ;";
        assertFormattingApplied(text, expected,
                psiFile -> setLanguageSettings(psiFile,
                        commonCodeStyleSettings ->
                                commonCodeStyleSettings.getIndentOptions().INDENT_SIZE = 4));
    }

    @Test
    void testInterpolatedString() {
        String text = "queries: |\n" +
                "    DEFINE QUERY query() => \n" +
                "        MAP(`{\"a\": \"${b}\",\n" +
                "              \"c\": \"${d}\",\n" +
                "              \"d\": \"${e}\",\n" +
                "              }`) / CAST(JSON);";
        String expectedString = "queries: |\n" +
                "    DEFINE QUERY query() =>\n" +
                "        MAP(`{\"a\": \"${b}\",\n" +
                "             \"c\": \"${d}\",\n" +
                "             \"d\": \"${e}\",\n" +
                "             }`) / CAST(JSON);";
        // the formatting is aligned on the first occurance of the String which starts with: {
        assertFormattingApplied(text, expectedString,
                psiFile -> setLanguageSettings(psiFile,
                        commonCodeStyleSettings ->
                                commonCodeStyleSettings.getIndentOptions().INDENT_SIZE = 4));
    }

    @Test
    void testLeadingComment() {
        String text = "model:\n" +
                "    // Something about the comment\n" +
                "    Activiteit: !Activity\n";
        String expectedString = "model:\n" +
                "    // Something about the comment\n" +
                "    Activiteit: !Activity";
        assertFormattingApplied(text, expectedString,
                psiFile -> setLanguageSettings(psiFile,
                        commonCodeStyleSettings ->
                                commonCodeStyleSettings.getIndentOptions().INDENT_SIZE = 4));
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
