package com.misset.opp.omt.formatter;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.psi.util.ImportUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static util.UtilManager.getImportUtil;

class OMTAddImportTest extends OMTFormattingTest {

    private static final String REST_OF_FILE_CONTENT = "commands: |\n" +
            "   DEFINE COMMAND command => {\n" +
            "       @Command()\n" +
            "   }";
    private static final String IMPORT_SOURCE = "@client/importSource.omt";
    private static final String IMPORT_MEMBER = "member";
    private static final ImportUtil importUtil = getImportUtil();
    private String existingImportBlock = "";

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setName("OMTAddImportTest");
        super.setUp();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void persistCorrectImport() {
        String formatted = "import:\n" +
                "    '@client/importSource.omt':\n" +
                "    -   member\n";
        assertFormattingApplied(formatted, formatted);
    }

    @Test
    void bulletCorrectionOnlyImport() {
        String unformatted = "import:\n" +
                "    '@client/importSource.omt':\n" +
                "        -   member\n" +
                "\n";
        String formatted = "import:\n" +
                "    '@client/importSource.omt':\n" +
                "    -   member";
        assertFormattingApplied(unformatted, formatted);
    }

    @Test
    void addFirstImportToFile() {
        String formatted = "import:\n" +
                "    '@client/importSource.omt':\n" +
                "    -   member";
        addImportMember(formatted);
    }

    @Test
    void addFirstImportToEmptyBlock() {
        existingImportBlock = "import:";
        String formatted = "import:\n" +
                "    '@client/importSource.omt':\n" +
                "    -   member";
        addImportMember(formatted);
    }

    @Test
    void addMemberToExistingImport() {
        existingImportBlock = "import:\n" +
                "    '@client/importSource.omt':\n" +
                "    -   existingMember";
        String formatted = "import:\n" +
                "    '@client/importSource.omt':\n" +
                "    -   existingMember\n" +
                "    -   member";
        addImportMember(formatted);
    }

    @Test
    void addImportAndMemberToExistingImportBlock() {
        existingImportBlock = "import:\n" +
                "    '@client/otherSource.omt':\n" +
                "    -    existingMember";
        String formatted = "import:\n" +
                "    '@client/otherSource.omt':\n" +
                "    -   existingMember\n" +
                "    '@client/importSource.omt':\n" +
                "    -   member";
        addImportMember(formatted);
    }

    private void addImportMember(String formatted) {
        String content = String.format("%s\n" +
                "\n" +
                "%s", existingImportBlock, REST_OF_FILE_CONTENT);
        String expectedResult = String.format("%s\n" +
                "\n" +
                "%s", formatted, REST_OF_FILE_CONTENT);

        final PsiFile psiFile = myFixture.configureByText(getFileName(), content);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> importUtil.addImportMemberToBlock(psiFile, IMPORT_SOURCE, IMPORT_MEMBER));

        assertEquals(expectedResult.trim(), myFixture.getEditor().getDocument().getText().trim());
    }
}
