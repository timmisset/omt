package com.misset.opp.omt.formatter;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class OMTEnterTypedHandlerTest extends LightJavaCodeInsightFixtureTestCase {

    @Mock
    DataContext dataContext;

    OMTEnterTypedHandler omtEnterTypedHandler;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setName("OMTEnterTypedHandlerTest");
        super.setUp();

        MockitoAnnotations.initMocks(this);

        omtEnterTypedHandler = new OMTEnterTypedHandler();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void javaDocsStart() {
        String content = "/**<caret>";
        String documentText = configureHitEnterAndReturnDocumentText(content);
        assertEquals("/**\n" +
                "* ", documentText);
    }

    @Test
    void javaDocsContent() {
        String content = "/**\n" +
                "* test<caret>";
        String documentText = configureHitEnterAndReturnDocumentText(content);
        assertEquals("/**\n" +
                "* test\n" +
                "* ", documentText);
    }

    @Test
    void javaDocsContentClosed() {
        String content = "/**\n" +
                "* test<caret>\n" +
                "*/";
        String documentText = configureHitEnterAndReturnDocumentText(content);
        assertEquals("/**\n" +
                "* test\n" +
                "* \n" +
                "*/", documentText);
    }

    @Test
    void memberListAfterParent() {
        String content = "import:\n" +
                "    'import.omt':<caret>\n" +
                "";
        String documentText = configureHitEnterAndReturnDocumentText(content);
        assertEquals("import:\n" +
                "    'import.omt':\n" +
                //TODO: when indentation becomes configurable by the styling, this should be indented based on that setting
                "    -   \n", documentText);
    }

    @Test
    void postProcessEnter_AddsBulletForImport() {
        String content = "import:\n" +
                "    'import.omt':\n" +
                "       -   gebeurtenisLocatieRelatieSoortGraph<caret>\n" +
                "";
        String documentText = configureHitEnterAndReturnDocumentText(content);
        assertEquals("import:\n" +
                "    'import.omt':\n" +
                "       -   gebeurtenisLocatieRelatieSoortGraph\n" +
                "       -   \n", documentText);
    }

    @Test
        // TODO: testen
    void postProcessEnter_AddsBulletForImport2() {
        String content = "import:\n" +
                "    'import.omt':\n" +
                "       -   gebeurtenisLocatieRelatieSoortGraph<caret>\n" +
                "    'import2.omt':\n" +
                "       -   gebeurtenisLocatieRelatieSoortGraph\n" +
                "";
        String documentText = configureHitEnterAndReturnDocumentText(content);
        assertEquals("import:\n" +
                "    'import.omt':\n" +
                "       -   gebeurtenisLocatieRelatieSoortGraph\n" +
                "       -   \n" +
                "    'import2.omt':\n" +
                "       -   gebeurtenisLocatieRelatieSoortGraph\n", documentText);
    }

    @Test
    void postProcessEnter_AddsBulletForParameter() {
        String content = "model:\n" +
                "    test: !Activity\n" +
                "        params:\n" +
                "            -   $variable<caret>";
        String documentText = configureHitEnterAndReturnDocumentText(content);
        assertEquals("model:\n" +
                "    test: !Activity\n" +
                "        params:\n" +
                "            -   $variable\n" +
                "            -   ", documentText);
    }

    private String configureHitEnterAndReturnDocumentText(String content) {
        myFixture.configureByText("test.omt", content);
        myFixture.type('\n');
        return myFixture.getEditor().getDocument().getText();
    }
}
