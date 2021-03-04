package com.misset.opp.omt.formatter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockitoAnnotations;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OMTEnterTypedJavaDocsTest extends OMTFormattingTest {

    @BeforeAll
    @Override
    public void setUp() throws Exception {
        super.setName("OMTEnterTypedJavaDocsTest");
        super.setUp();

        MockitoAnnotations.openMocks(this);
    }

    @AfterAll
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void javaDocsCreateBlock() {
        String content = "" +
                "/**<caret>";
        String expected = "" +
                "/**\n" +
                ".*.\n" +
                ".*/";
        assertSmartCompletion(content, expected);
    }

    @Test
    void javaDocsInsideBlock() {
        String content = "" +
                "/**\n" +
                ".* Some statement<caret>\n" +
                ".*/";
        String expected = "" +
                "/**\n" +
                ".* Some statement\n" +
                ".*.\n" +
                ".*/";
        assertSmartCompletion(content, expected);
    }

    @Test
    void javaDocsAfterBlock() {
        String content = "" +
                "/**\n" +
                ".* Some statement\n" +
                ".*/<caret>";
        String expected = "" +
                "/**\n" +
                ".* Some statement\n" +
                ".*/\n" +
                "";
        assertSmartCompletion(content, expected);
    }

    private void assertSmartCompletion(String content, String expectedResult) {
        content = content.replace(".", " ");
        expectedResult = expectedResult.replace(".", " ");
        final String result = configureHitEnterAndReturnDocumentText(content);
        assertEquals(expectedResult, result);
    }

}
