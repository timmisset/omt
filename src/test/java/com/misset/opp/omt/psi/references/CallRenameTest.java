package com.misset.opp.omt.psi.references;

import com.intellij.psi.PsiFile;
import com.misset.opp.omt.psi.support.OMTCall;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CallRenameTest extends RenameTest {

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("CallRenameTest");
        super.setUp(OMTCall.class);
        setOntologyModel();
    }

    @Test
    void renamesCallAndQueryName() {
        String content = "queries:|\n" +
                "   DEFINE QUERY query => '';\n" +
                "   DEFINE QUERY anotherQuery => <caret>query;";

        renameElement(content, "newQuery");

        assertEquals("queries:|\n" +
                "   DEFINE QUERY newQuery => '';\n" +
                "   DEFINE QUERY anotherQuery => newQuery;", myFixture.getEditor().getDocument().getText());
    }

    @Test
    void renamesCallAndImportedQueryName() {
        final PsiFile importedFile = addFile("imported.omt", "" +
                "queries: |\n" +
                "   DEFINE QUERY query => '';");
        String content = "" +
                "import:\n" +
                "   ./imported.omt:\n" +
                "   - query\n" +
                "queries:|\n" +
                "   DEFINE QUERY anotherQuery => <caret>query;";

        renameElement(content, "newQuery");

        assertEquals("" +
                "import:\n" +
                "   ./imported.omt:\n" +
                "   -   newQuery\n" +
                "queries:|\n" +
                "   DEFINE QUERY anotherQuery => newQuery;", myFixture.getEditor().getDocument().getText());
        assertEquals("queries: |\n" +
                "   DEFINE QUERY newQuery => '';", myFixture.getDocument(importedFile).getText());
    }

}
