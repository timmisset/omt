package com.misset.opp.omt.annotations;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AnnotationsWithFixtureTest extends OMTAnnotationTest {

    @Override
    @BeforeAll
    protected void setUp() throws Exception {
        super.setName("AnnotationsWithFixtureTest");
        super.setUp();
        setOntologyModel();
    }

    @Override
    @AfterAll
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void rdfTypeThrowsUnknownPredicateWhenCalledOnType() {
        String query = "/ont:ClassA / rdf:type";
        myFixture.configureByText(getFileName(), queryWithPrefixes(query));
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertTrue(
                highlighting.stream().anyMatch(
                        highlightInfo -> highlightInfo.getDescription().endsWith("type is not a known FORWARD-path predicate for type(s): ont:ClassA")
                )
        );
    }

    @Test
    void rdfTypeThrowsNoUnknownPredicateWhenCalledOnInstance() {
        String query = "/ont:ClassA / ^rdf:type / rdf:type";
        myFixture.configureByText(getFileName(), queryWithPrefixes(query));
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEmpty(highlighting);
    }

    @Test
    void reverseRdfTypeThrownsUnknownPredicateWhenCalledOnInstance() {
        String query = "/ont:ClassA / ^rdf:type / ^rdf:type";
        myFixture.configureByText(getFileName(), queryWithPrefixes(query));
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertTrue(
                highlighting.stream().anyMatch(
                        highlightInfo -> highlightInfo.getDescription().endsWith("type is not a known REVERSE-path predicate for type(s): ont:ClassA")
                )
        );
    }

    @Test
    void reverseRdfTypeThrownsNoUnknownPredicateWhenCalledOnInstance() {
        String query = "/ont:ClassA / ^rdf:type";
        myFixture.configureByText(getFileName(), queryWithPrefixes(query));
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEmpty(highlighting);
    }

    @Test
    void testQuerySyntaxDefinedQueryStatementShouldEndWithSemicolonThrowsError() {
        String content = "queries:|\n" +
                "   DEFINE QUERY query => ''";
        myFixture.configureByText(getFileName(), content);
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEquals(1, highlighting.size());
        assertEquals("; expected", highlighting.get(0).getDescription());
    }

    @Test
    void testQuerySyntaxDefinedQueryStatementShouldEndWithSemicolonDoesNotThrowError() {
        String content = "queries:|\n" +
                "   DEFINE QUERY query => ''; ";
        myFixture.configureByText(getFileName(), content);
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEmpty(highlighting);
    }

    @Test
    void testQuerySyntaxQueryWithEmptyStep() {
        String content = "queries:|\n" +
                "   DEFINE QUERY query => $variable / / ont:test;";
        myFixture.configureByText(getFileName(), content);
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertTrue(highlighting.stream().anyMatch(
                highlightInfo -> highlightInfo.getDescription().equals("Unexpected character")
        ));
    }

    @Test
    void testQuerySyntaxUnnecessarySemicolon() {
        String content = "queries:|\n" +
                "   DEFINE QUERY query => 'test';;";
        myFixture.configureByText(getFileName(), content);
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.WARNING);
        assertTrue(highlighting.stream().anyMatch(
                highlightInfo -> highlightInfo.getDescription().equals("Unexpected character")
        ));
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
