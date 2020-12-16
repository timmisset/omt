package com.misset.opp.omt.psi.annotations;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.misset.opp.omt.OMTTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class QuerySyntaxTest extends OMTTestSuite {

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
    void testDefinedQueryStatementShouldEndWithSemicolonThrowsError() {
        String content = "queries:|\n" +
                "   DEFINE QUERY query => ''";
        myFixture.configureByText(getFileName(), content);
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEquals(1, highlighting.size());
        assertEquals("; expected", highlighting.get(0).getDescription());
    }

    @Test
    void testDefinedQueryStatementShouldEndWithSemicolonDoesNotThrowError() {
        String content = "queries:|\n" +
                "   DEFINE QUERY query => ''; ";
        myFixture.configureByText(getFileName(), content);
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEmpty(highlighting);
    }

    @Test
    void testQueryWithEmptyStep() {
        String content = "queries:|\n" +
                "   DEFINE QUERY query => $variable / / ont:test;";
        myFixture.configureByText(getFileName(), content);
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertTrue(highlighting.stream().anyMatch(
                highlightInfo -> highlightInfo.getDescription().equals("Unexpected character")
        ));
    }

    @Test
    void testUnnecessarySemicolon() {
        String content = "queries:|\n" +
                "   DEFINE QUERY query => 'test';;";
        myFixture.configureByText(getFileName(), content);
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.WARNING);
        assertTrue(highlighting.stream().anyMatch(
                highlightInfo -> highlightInfo.getDescription().equals("Unexpected character")
        ));
    }

}
