package com.misset.opp.omt.psi.references;

import com.misset.opp.omt.psi.named.OMTCall;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CallReferenceIT extends ReferenceTest {

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("CallReferenceIT");
        super.setUp(OMTCall.class);
        setOntologyModel();
    }

    @Test
    void testModelItemQueryInPayloadHasReference() {
        String content = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       queries: |\n" +
                "           DEFINE QUERY myQuery => '';\n" +
                "       payload:\n" +
                "            payloadItem:\n" +
                "                query: <caret>myQuery\n" +
                "                list: true";
        assertHasReference(content);
    }

    @Test
    void testModelItemQueryInPayloadHasNoReference() {
        String content = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       queries: |\n" +
                "           DEFINE QUERY myQuery => '';\n" +
                "       payload:\n" +
                "            payloadItem:\n" +
                "                query: <caret>Query\n" +
                "                list: true";
        assertHasNoReference(content);
        assertHasError("Query could not be resolved");
    }

    @Test
    void testQueryCannotReferenceItself() {
        String content = "queries: |\n" +
                "           DEFINE QUERY myQuery => <caret>myQuery;\n";
        assertHasNoReference(content);
        assertHasError("myQuery could not be resolved");
    }

    @Test
    void testQueryCanReferencePrecedingStatement() {
        String content = "queries: |\n" +
                "           DEFINE QUERY myQuery => 'myQuery';\n" +
                "           DEFINE QUERY myQuerySecond => <caret>myQuery;\n";
        assertHasReference(content);
        assertNoErrors();
    }

    @Test
    void testQueryCannotReferenceNextStatement() {
        String content = "queries: |\n" +
                "           DEFINE QUERY myQuery => <caret>myQuerySecond;\n" +
                "           DEFINE QUERY myQuerySecond => 'myQuery';\n";
        assertHasNoReference(content);
        assertHasError("myQuerySecond could not be resolved");
    }

    @Test
    void testQueryCannotReferenceItemInModel() {
        String content = "" +
                "queries: |" +
                "   DEFINE QUERY myQuery => <caret>myModelQuery;\n" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       queries: |\n" +
                "           DEFINE QUERY myModelQuery => '';\n";
        assertHasNoReference(content);
        assertHasError("myModelQuery could not be resolved");
    }

    @Test
    void testModelQueryCanReferenceRootQuery() {
        String content = "" +
                "queries: |" +
                "   DEFINE QUERY myQuery => 'myQuery';\n" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       queries: |\n" +
                "           DEFINE QUERY myModelQuery => <caret>myQuery;\n";
        assertHasReference(content);
        assertNoErrors();
    }

    @Test
    void testRootQueryCanReferenceImportedQuery() {
        String importedQuery = "" +
                "queries: |\n" +
                "   DEFINE QUERY importedQuery => '';\n";
        addFile("imported.omt", importedQuery);
        String content = "" +
                "import:\n" +
                "   ./imported.omt:\n" +
                "   - importedQuery\n" +
                "queries: |\n" +
                "   DEFINE QUERY myQuery => <caret>importedQuery;\n";
        assertHasReference(content);
        assertNoErrors();
    }

    @Test
    void testRootQueryCanReferenceDeferredImportedQuery() {
        // Whenever an OMT file imports a member, it is also available to any file that imports that file
        String importedQuery = "" +
                "queries: |\n" +
                "   DEFINE QUERY importedQuery => '';\n";
        addFile("imported.omt", importedQuery);
        String deferred = "" +
                "import:\n" +
                "   ./imported.omt:\n" +
                "   - importedQuery";
        addFile("deferred.omt", deferred);
        String content = "" +
                "import:\n" +
                "   ./deferred.omt:\n" +
                "   - importedQuery\n" +
                "queries: |\n" +
                "   DEFINE QUERY myQuery => <caret>importedQuery;\n";
        assertHasReference(content);
        assertNoErrors();
    }

    @Test
    void testRootQueryHasNoReferenceWhenImportedFileDoesNotExist() {
        String content = "" +
                "import:\n" +
                "   ./imported.omt:\n" +
                "   - importedQuery\n" +
                "queries: |\n" +
                "   DEFINE QUERY myQuery => <caret>importedQuery;\n";
        assertHasNoReference(content);
        assertHasError("importedQuery could not be resolved");
    }

    @Test
    void testRootCommandCanCallRootQuery() {
        String content = "" +
                "queries:|\n" +
                "   DEFINE QUERY myQuery => '';\n" +
                "\n" +
                "commands:|\n" +
                "   DEFINE COMMAND myCommand => {\n" +
                "       VAR $x = <caret>myQuery;\n" +
                "   }";
        assertHasReference(content);
    }

    @Test
    void testRootQueryHasNoReferenceWhenImportedMemberDoesNotExist() {
        String importedQuery = "" +
                "queries: |\n" +
                "   DEFINE QUERY importedQuery => '';\n";
        addFile("imported.omt", importedQuery);
        String content = "" +
                "import:\n" +
                "   ./imported.omt:\n" +
                "   - wronglyNamedMemberImport\n" +
                "queries: |\n" +
                "   DEFINE QUERY myQuery => <caret>wronglyNamedMemberImport;\n";
        assertHasNoReference(content);
        assertHasError("wronglyNamedMemberImport could not be resolved");
    }

    @Test
    void testModelItemsCanCallEachother() {
        String content = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       onStart:|\n" +
                "           <caret>@Procedure();\n" +
                "   Procedure: !Procedure\n" +
                "       onRun:|\n" +
                "           @Activiteit();\n";
        assertHasReference(content);
        assertNoErrors();
    }

    @Test
    void testModelItemsCanCallEachother2() {
        String content = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       onStart:|\n" +
                "           @Procedure();\n" +
                "   Procedure: !Procedure\n" +
                "       onRun:|\n" +
                "           <caret>@Activiteit();\n";
        assertHasReference(content);
        assertNoErrors();
    }

    @Test
    void testCanReferToImportedModelItem() {
        addFile("imported.omt", "" +
                "model:\n" +
                "   Procedure: !Procedure");
        String content = "" +
                "import:\n" +
                "   ./imported.omt:" +
                "   -   Procedure\n" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       onStart:|\n" +
                "           <caret>@Procedure();\n";
        assertHasReference(content);
        assertNoErrors();
    }
}
