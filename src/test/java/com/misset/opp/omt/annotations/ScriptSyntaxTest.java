package com.misset.opp.omt.annotations;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class ScriptSyntaxTest extends OMTAnnotationTest {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("SyntaxChecks");
        super.setUp();
        setOntologyModel();
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void testScriptContentShouldEndWithSemicolonThrowsError() {
        String content = "commands:|\n" +
                "   DEFINE COMMAND command => {\n" +
                "       VAR $x = ''\n" +
                "   }";
        myFixture.configureByText(getFileName(), content);
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEquals(1, highlighting.size());
        assertEquals("; expected", highlighting.get(0).getDescription());
    }

    @Test
    void testScriptContentCommandBlockWithoutSemicolon() {
        String content = "commands:|\n" +
                "   DEFINE COMMAND command => {\n" +
                "       VAR $x = '';\n" +
                "   }";
        myFixture.configureByText(getFileName(), content);
        assertEmpty(myFixture.doHighlighting(HighlightSeverity.ERROR));
    }

    @Test
    void testScriptContentNestedCommandBlockWithoutSemicolon() {
        String content = "commands:|\n" +
                "   DEFINE COMMAND command => {\n" +
                "       IF 1 == 2 { " +
                "           VAR $message = 'That\\'s very strange';" +
                "       }\n" +
                "   }";
        myFixture.configureByText(getFileName(), content);
        assertEmpty(myFixture.doHighlighting(HighlightSeverity.ERROR));
    }

    @Test
    void testScriptContentShouldEndWithSemicolonDoesNotThrowError() {
        String content = "commands:|\n" +
                "   DEFINE COMMAND command => {\n" +
                "       VAR $x = '';\n" +
                "   }";
        myFixture.configureByText(getFileName(), content);
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEquals(0, highlighting.size());
    }

    @Test
    void testScriptContentUnnecessarySemicolon() {
        String content = "commands:|\n" +
                "   DEFINE COMMAND command => {\n" +
                "       VAR $x = ''; ;\n" +
                "   }";
        myFixture.configureByText(getFileName(), content);
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.WARNING);
        assertTrue(highlighting.stream().anyMatch(highlightInfo -> highlightInfo.getDescription().equals("Unexpected character")));
    }

    @Test
    void testScriptQueryTypeShouldNotEndWithSemicolon() {
        String content = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        payload:\n" +
                "            payloadItem:\n" +
                "                value: |\n" +
                "                    'test';";
        myFixture.configureByText(getFileName(), withPrefixes(content));
        assertHasError("Query entry should not end with semicolon");
    }

    @Test
    void testScriptQueryTypeShouldNotEndWithSemicolonPasses() {
        String content = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        payload:\n" +
                "            payloadItem:\n" +
                "                value: |\n" +
                "                    'test'";
        myFixture.configureByText(getFileName(), withPrefixes(content));
        assertNoErrors();
    }
}
