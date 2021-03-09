package com.misset.opp.omt;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/*
    The FindUsageProvider should not be tested using stubs, this is because the element selected for evaluation
    depends on the PsiTree and the first element that implements PsiNameIdentifierOwner is used
    this makes tests using mocks non-predictable when changes occur to the PsiTree
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OMTFindUsageProviderTest extends OMTTestSuite {

    private OMTFindUsageProvider omtFindUsageProvider;

    @BeforeAll()
    protected void setUp() throws Exception {
        super.setUp();
        omtFindUsageProvider = new OMTFindUsageProvider();
    }

    @AfterAll()
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void testGetWordsScannerReturnsDefaultWordScanner() {
        final WordsScanner wordsScanner = omtFindUsageProvider.getWordsScanner();
        assertTrue(wordsScanner instanceof DefaultWordsScanner);
    }

    @Test
    void testCanFindUsagesForDeclaredVariable() {
        String content = "commands: |\n" +
                "   DEFINE COMMAND command() => {\n" +
                "       VAR $<caret>variable = 1;\n" +
                "   }";
        testCanFindUsageFor(content, true);
    }

    @Test
    void testCanFindUsagesForDefineVariable() {
        String content = "commands: |\n" +
                "   DEFINE COMMAND command($<caret>variable) => {\n" +
                "       VAR $variable2 = $variable;\n" +
                "   }";
        testCanFindUsageFor(content, true);
    }

    @Test
    void testCanFindUsagesForDefineName() {
        String content = "commands: |\n" +
                "   DEFINE COMMAND com<caret>mand($variable) => {\n" +
                "       VAR $variable2 = $variable;\n" +
                "   }";
        testCanFindUsageFor(content, true);
    }

    @Test
    void testCanNotFindUsagesForUsageVariable() {
        String content = "commands: |\n" +
                "   DEFINE COMMAND command($variable) => {\n" +
                "       VAR $variable2 = $<caret>variable;\n" +
                "   }";
        testCanFindUsageFor(content, false);
    }

    @Test
    void testCanFindUsagesForModelItem() {
        String content = "model: \n" +
                "   Mijn<caret>Activiteit: !Activity\n" +
                "       onStart: |\n" +
                "           @SomeCall()";
        testCanFindUsageFor(content, true);
    }

    @Test
    void testCanNotFindUsagesForPropertyLabelWhichIsNotAModelItem() {
        String content = "model: \n" +
                "   MijnActiviteit: !Activity\n" +
                "       on<caret>Start: |\n" +
                "           @SomeCall()";
        testCanFindUsageFor(content, false);
    }

    @Test
    void testCanNotFindUsagesForCall() {
        String content = "model: \n" +
                "   MijnActiviteit: !Activity\n" +
                "       onStart: |\n" +
                "           @Some<caret>Call()";
        testCanFindUsageFor(content, false);
    }

    @Test
    void testCanFindUsageForPrefixDefinition() {
        String content = "prefixes: \n" +
                "   ab<caret>c:    <http://abc>\n" +
                "model: \n" +
                "   MijnActiviteit: !Activity\n" +
                "       onStart: |\n" +
                "           @LOG(/abc:someAttribute);";
        testCanFindUsageFor(content, true);
    }

    @Test
    void testCanNotFindUsageForPrefixUsage() {
        String content = "prefixes: \n" +
                "   abc:    <http://abc>\n" +
                "model: \n" +
                "   MijnActiviteit: !Activity\n" +
                "       onStart: |\n" +
                "           @LOG(/ab<caret>c:someAttribute);";
        testCanFindUsageFor(content, false);
    }

    @Test
    void testGetTypeReturnsVariableForVariable() {
        String content = "commands: |\n" +
                "   DEFINE COMMAND command() => {\n" +
                "       VAR $<caret>variable = 1;\n" +
                "   }";
        testGetType(content, "Variable");
    }

    @Test
    void testGetTypeReturnsCommandForCommand() {
        String content = "commands: |\n" +
                "   DEFINE COMMAND co<caret>mmand() => {\n" +
                "       VAR $variable = 1;\n" +
                "   }";
        testGetType(content, "Command");
    }

    @Test
    void testGetTypeReturnsQueryForQuery() {
        String content = "queries: |\n" +
                "   DEFINE QUERY qu<caret>ery() => '';";
        testGetType(content, "Query");
    }

    @Test
    void testGetTypeReturnsPrefixForPrefix() {
        String content = "prefixes: \n" +
                "   a<caret>bc:    <http://abc>\n";
        testGetType(content, "Prefix");
    }

    @Test
    void testGetTypeReturnsModelItemTypeActivity() {
        String content = "model: \n" +
                "   Mijn<caret>Activiteit: !Activity";
        testGetType(content, "Activity");
    }

    @Test
    void testGetTypeReturnsModelItemTypeProcedure() {
        String content = "model: \n" +
                "   Mijn<caret>Procedure: !Procedure";
        testGetType(content, "Procedure");
    }

    @Test
    void testGetTypeReturnsModelItemTypeComponent() {
        String content = "model: \n" +
                "   Mijn<caret>Component: !Component";
        testGetType(content, "Component");
    }

    @Test
    void testGetDescriptiveNameReturnsVariableName() {
        String content = "commands: |\n" +
                "   DEFINE COMMAND command() => {\n" +
                "       VAR $<caret>variable = 1;\n" +
                "   }";
        testGetDescriptiveName(content, "$variable");
    }

    @Test
    void testGetDescriptiveNameReturnsCommandName() {
        String content = "commands: |\n" +
                "   DEFINE COMMAND co<caret>mmand() => {\n" +
                "       VAR $variable = 1;\n" +
                "   }";
        testGetDescriptiveName(content, "command");
    }

    @Test
    void testGetDescriptiveNameReturnsPrefixName() {
        String content = "prefixes: \n" +
                "   a<caret>bc:    <http://abc>\n";
        testGetDescriptiveName(content, "abc");
    }

    @Test
    void testGetDescriptiveNameReturnsModelItemName() {
        String content = "model: \n" +
                "   Mijn<caret>Activiteit: !Activity";
        testGetDescriptiveName(content, "MijnActiviteit");
    }

    private void testCanFindUsageFor(String content, boolean exceptedResult) {
        myFixture.configureByText(getFileName(), content);
        getElementAtCaret(content, element -> assertEquals(exceptedResult, omtFindUsageProvider.canFindUsagesFor(element)), PsiNameIdentifierOwner.class, true);
    }

    private void testGetType(String content, String expectedType) {
        myFixture.configureByText(getFileName(), content);
        getElementAtCaret(content, element -> assertEquals(expectedType, omtFindUsageProvider.getType(element)), PsiNameIdentifierOwner.class, true);
    }

    private void testGetDescriptiveName(String content, String expectedDescription) {
        myFixture.configureByText(getFileName(), content);
        getElementAtCaret(content, element -> assertEquals(expectedDescription, omtFindUsageProvider.getDescriptiveName(element)), PsiNameIdentifierOwner.class, true);
    }
}
