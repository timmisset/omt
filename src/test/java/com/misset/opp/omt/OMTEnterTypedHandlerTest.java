package com.misset.opp.omt;

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
    void setUpSuite() throws Exception {
        super.setName("OMTEnterTypedHandlerTest");
        super.setUp();

        MockitoAnnotations.initMocks(this);

        omtEnterTypedHandler = new OMTEnterTypedHandler();
    }

    @AfterEach
    void tearDownSuite() throws Exception {
        super.tearDown();
    }

    @Test
    void postProcessEnter_AddsBulletForImport() {
        String content = "import:\n" +
                "    'import.omt':\n" +
                "       -   gebeurtenisLocatieRelatieSoortGraph<caret>\n" +
                "";
        myFixture.configureByText("test.omt", content);
        myFixture.type('\n');
        assertEquals("import:\n" +
                "    'import.omt':\n" +
                "       -   gebeurtenisLocatieRelatieSoortGraph\n" +
                "       -   \n", myFixture.getFile().getText());
    }

    @Test
    void postProcessEnter_AddsBulletForImport2() {
        String content = "import:\n" +
                "    'import.omt':\n" +
                "       -   gebeurtenisLocatieRelatieSoortGraph<caret>\n" +
                "    'import2.omt':\n" +
                "       -   gebeurtenisLocatieRelatieSoortGraph\n" +
                "";
        myFixture.configureByText("test.omt", content);
        myFixture.type('\n');
        assertEquals("import:\n" +
                "    'import.omt':\n" +
                "       -   gebeurtenisLocatieRelatieSoortGraph\n" +
                "       -   \n" +
                "    'import2.omt':\n" +
                "       -   gebeurtenisLocatieRelatieSoortGraph\n", myFixture.getFile().getText());
    }

    @Test
    void postProcessEnter_AddsBulletForParameter() {
        String content = "model:\n" +
                "    test: !Activity\n" +
                "        params:\n" +
                "            -   $variable<caret>";
        myFixture.configureByText("test.omt", content);
        myFixture.type('\n');
        assertEquals("model:\n" +
                "    test: !Activity\n" +
                "        params:\n" +
                "            -   $variable\n" +
                "            -   ", myFixture.getFile().getText());
    }
}
