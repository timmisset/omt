package com.misset.opp.omt.psi.annotations;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class QueryAnnotatorIT extends OMTAnnotationTest {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("QueryAnnotatorIT");
        super.setUp();
        setOntologyModel();
    }

    @Override
    @AfterEach
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
}
