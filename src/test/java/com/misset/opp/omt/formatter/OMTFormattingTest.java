package com.misset.opp.omt.formatter;

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
    @Override
    public void setUp() throws Exception {
        super.setName("OMTEnterTypedHandlerTest");
        super.setUp();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////
    // SPACING
    // ////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void testSpacingSpaceAroundAssignmentOperators() {
        assertFormattingApplied("$variable =='a'", "$variable == 'a'",
                psiFile -> setLanguageSettings(psiFile,
                        commonCodeStyleSettings -> commonCodeStyleSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS = true)
        );
    }

    @Test
    void spacesBeforePrefixIri() {
        String unformatted = "prefixes:\n" +
                " abc: <http://www.test.com>";
        String formatted = "prefixes:\n" +
                "    abc:    <http://www.test.com>";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void spacesBeforePrefixIriMultiple() {
        String unformatted = "prefixes:\n" +
                " abc: <http://www.test.com>\n" +
                " def: <http://www.test.com>";
        String formatted = "prefixes:\n" +
                "    abc:    <http://www.test.com>\n" +
                "    def:    <http://www.test.com>";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void spacesBeforePrefixIriMultipleWithLongPrefix() {
        String unformatted = "prefixes:\n" +
                " abc: <http://www.test.com>\n" +
                " defghijkl: <http://www.test.com>";
        String formatted = "prefixes:\n" +
                "    abc:          <http://www.test.com>\n" +
                "    defghijkl:    <http://www.test.com>";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testIndentationBlocks() {
        String unformatted = "model:\n" +
                " Activiteit: !Activity\n" +
                "  title: 'TEST'\n" +
                "\n" +
                " Procedure: !Procedure\n" +
                "  onRun: |\n" +
                "    'test';\n" +
                "    'test2';\n";
        String formatted = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        title: 'TEST'\n" +
                "\n" +
                "    Procedure: !Procedure\n" +
                "        onRun: |\n" +
                "            'test';\n" +
                "            'test2';\n";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testIndentationBlocksModelWithSpecificBlock() {
        String unformatted = "model:\n" +
                " Verklaring: !Activity\n" +
                "  title: Verklaring opnemen\n" +
                "\n" +
                "  queries: |\n" +
                "    DEFINE QUERY query => '';";
        String formatted = "model:\n" +
                "    Verklaring: !Activity\n" +
                "        title: Verklaring opnemen\n" +
                "\n" +
                "        queries: |\n" +
                "            DEFINE QUERY query => '';";
        assertFormattingApplied(unformatted, formatted);
    }


    @Test
    void testScalarValue() {
        String unformatted = "model:\n" +
                "    mijnActiviteit: !Activity\n" +
                "        payload:\n" +
                "            payloadItem:\n" +
                "            $variable / functie() / EXISTS\n" + // <-- scalar value
                "";
        String formatted = "model:\n" +
                "    mijnActiviteit: !Activity\n" +
                "        payload:\n" +
                "            payloadItem:\n" +
                "                $variable / functie() / EXISTS\n" + // <-- scalar value
                "";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testIndentationPrefixes() {
        String unformatted = "prefixes:\n" +
                " pol:    <http://enter.your/iri/>\n";
        String formatted = "prefixes:\n" +
                "    pol:    <http://enter.your/iri/>\n";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testIndentationDefinedQueries() {
        String unformatted = "queries: |\n" +
                " DEFINE QUERY query => 'test';\n" +
                "  DEFINE QUERY query2 => 'test';\n";
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query => 'test';\n" +
                "    DEFINE QUERY query2 => 'test';";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testIndentationSubQuery() {
        String unformatted = "queries: |\n" +
                " DEFINE QUERY query => \n" +
                "  ('test');\n";
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query =>\n" +
                "        ('test');\n";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testIndentationFilter() {
        String unformatted = "queries: |\n" +
                "    DEFINE QUERY query => 'a'\n" +
                "        [\n" +
                "            . == 'test'\n" +
                "        ];";
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query => 'a'\n" +
                "        [\n" +
                "            . == 'test'\n" +
                "        ];";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testIndentationQueryPaths() {
        String unformatted = "queries: |\n" +
                "    DEFINE QUERY query($param) =>\n" +
                "        'test'\n" +
                "            /   CHOOSE\n" +
                "                WHEN 'a == a' => 'a' /\n" +
                "                    FIRST\n" +
                "                OTHERWISE => null\n" +
                "                END ;";
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query($param) =>\n" +
                "        'test'\n" +
                "        /   CHOOSE\n" +
                "                WHEN 'a == a' => 'a' /\n" +
                "                    FIRST\n" +
                "                OTHERWISE => null\n" +
                "            END ;";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testInterpolatedString() {
        String unformatted = "queries: |\n" +
                "    DEFINE QUERY query() => \n" +
                "        MAP(`{\"a\": \"${b}\",\n" +
                "              \"c\": \"${d}\",\n" +
                "              \"d\": \"${e}\",\n" +
                "              }`) / CAST(JSON);";
        String formatted = "queries: |\n" +
                "    DEFINE QUERY query() =>\n" +
                "        MAP(`{\"a\": \"${b}\",\n" +
                "             \"c\": \"${d}\",\n" +
                "             \"d\": \"${e}\",\n" +
                "             }`) / CAST(JSON);";
        // the formatting is aligned on the first occurance of the String which starts with: {
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void testLeadingComment() {
        String unformatted = "model:\n" +
                "    // Something about the comment\n" +
                "    Activiteit: !Activity\n";
        String formatted = "model:\n" +
                "    // Something about the comment\n" +
                "    Activiteit: !Activity";
        assertFormattingApplied(unformatted, formatted);
    }

    private void setLanguageSettings(PsiFile file, Consumer<CommonCodeStyleSettings> languageSettings) {
        languageSettings.accept(CodeStyle.getLanguageSettings(file));
    }

    private void assertFormattingApplied(String unformatted, String formatted) {
        assertFormattingApplied(unformatted, formatted,
                psiFile -> setLanguageSettings(psiFile,
                        commonCodeStyleSettings ->
                                commonCodeStyleSettings.getIndentOptions().INDENT_SIZE = 4));
    }

    private void assertFormattingApplied(String unformatted, String formatted, Consumer<PsiFile> preformattingFile) {
        final PsiFile psiFile = myFixture.configureByText("test.omt", unformatted);
        preformattingFile.accept(psiFile);
        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(myFixture.getProject());
        WriteCommandAction.runWriteCommandAction(getProject(),
                () -> codeStyleManager.reformatText(psiFile,
                        ContainerUtil.newArrayList(myFixture.getFile().getTextRange())
                ));
        assertEquals(formatted.trim(), myFixture.getEditor().getDocument().getText().trim());
    }

}
