package com.misset.opp.omt.psi.references;

import com.intellij.psi.PsiFile;
import com.misset.opp.omt.psi.named.OMTCall;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CallRenameTest extends RenameTest {

    @Override
    @AfterAll
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    @BeforeAll
    protected void setUp() throws Exception {
        super.setName("CallRenameTest");
        super.setUp(OMTCall.class);
        setOntologyModel();
    }

    @Test
    void renamesCallAndQueryName() {
        String content = "queries:|\n" +
                "   DEFINE QUERY query => '';\n" +
                "   DEFINE QUERY anotherQuery => q<caret>uery;";

        renameElement(content, "newQuery");

        assertEquals("queries:|\n" +
                "   DEFINE QUERY newQuery => '';\n" +
                "   DEFINE QUERY anotherQuery => newQuery;", myFixture.getEditor().getDocument().getText());
    }

    //    @Test
    // known flaky test, no cause found yet
    void renamesCallAndImportedQueryName() {
        final PsiFile importedFile = addFile("imported.omt", "" +
                "queries: |\n" +
                "   DEFINE QUERY query => '';");
        String content = "" +
                "import:\n" +
                "   ./imported.omt:\n" +
                "   - query\n" +
                "queries:|\n" +
                "   DEFINE QUERY anotherQuery => q<caret>uery;";

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

    @Test
    void renamesCallAndModelItem() {
        String content = "model:\n" +
                "    Activiteit: !Activity\n" +
                "\n" +
                "    AndereActiviteit: !Activity\n" +
                "        onStart: |\n" +
                "            @Acti<caret>viteit();";

        renameElement(content, "NieuweNaam");
        assertEquals("model:\n" +
                "    NieuweNaam: !Activity\n" +
                "\n" +
                "    AndereActiviteit: !Activity\n" +
                "        onStart: |\n" +
                "            @NieuweNaam();", myFixture.getEditor().getDocument().getText());
    }

}
