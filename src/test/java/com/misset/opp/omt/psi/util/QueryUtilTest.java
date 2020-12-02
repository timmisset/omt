package com.misset.opp.omt.psi.util;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.misset.opp.omt.OMTTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class QueryUtilTest extends OMTTestSuite {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setName("QueryUtilTest");
        super.setUp();

        setOntologyModel();
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void annotateQueryPathThrowsDuplicateForwardSlash() {
        String content = "queries: |\n" +
                " DEFINE QUERY query => 'a' / / 'b';";
        myFixture.configureByText("test.omt", content);
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEquals(1, highlighting.size());
        assertEquals("Unexpected token", highlighting.get(0).getDescription());
    }

    @Test
    void annotateQueryPathThrowsDuplicateForwardSlashTwice() {
        String content = "queries: |\n" +
                " DEFINE QUERY query => 'a' / / / 'b';";
        myFixture.configureByText("test.omt", content);
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEquals(2, highlighting.size());
        assertEquals("Unexpected token", highlighting.get(0).getDescription());
        assertEquals("Unexpected token", highlighting.get(1).getDescription());
    }

    @Test
    void annotateQueryPathThrowsDuplicateForwardSlashNoErrorForNested() {
        String content = "queries: |\n" +
                " DEFINE QUERY query => 'a' / ('a') / 'b';";
        myFixture.configureByText("test.omt", content);
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEquals(0, highlighting.size());
    }
}
