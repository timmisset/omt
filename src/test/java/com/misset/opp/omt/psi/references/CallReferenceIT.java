package com.misset.opp.omt.psi.references;

import com.misset.opp.omt.psi.named.OMTCall;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CallReferenceIT extends ReferenceTest {

    @Override
    @AfterAll
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    @BeforeAll
    protected void setUp() throws Exception {
        super.setName("CallReferenceIT");
        super.setUp(OMTCall.class);
        setBuiltinAndModel();
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
    void testQueryDefinedInScript() {
        String content = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       onStart: |\n" +
                "           DEFINE QUERY query => 'myQuery';\n" +
                "           @LOG(<caret>query);";
        assertHasReference(content);
        assertNoErrors();
    }

    @Test
    void testRootQueryCanReferenceImportedQuery() {
        String importedQuery = "" +
                "queries: |\n" +
                "   DEFINE QUERY importedQuery => '';\n";
        String fileName = getFileName();
        addFile(fileName, importedQuery);
        String content = String.format("" +
                "import:\n" +
                "   ./%s:\n" +
                "   - importedQuery\n" +
                "queries: |\n" +
                "   DEFINE QUERY myQuery => <caret>importedQuery;\n", fileName);
        assertHasReference(content);
        assertNoErrors();
    }

    @Test
    void testRootQueryCanReferenceDeferredImportedQuery() {
        // Whenever an OMT file imports a member, it is also available to any file that imports that file
        String importedQuery = "" +
                "queries: |\n" +
                "   DEFINE QUERY importedQuery => '';\n";
        String importedFile = getFileName();
        addFile(importedFile, importedQuery);
        String deferred = String.format("" +
                "import:\n" +
                "   ./%s:\n" +
                "   - importedQuery", importedFile);

        String deferredFile = getFileName();
        addFile(deferredFile, deferred);
        String content = String.format("" +
                "import:\n" +
                "   ./%s:\n" +
                "   - importedQuery\n" +
                "queries: |\n" +
                "   DEFINE QUERY myQuery => <caret>importedQuery;\n", deferredFile);
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
        final String fileName = getFileName();
        String importedQuery = "" +
                "queries: |\n" +
                "   DEFINE QUERY importedQuery => '';\n";
        addFile(fileName, importedQuery);
        String content = String.format("" +
                "import:\n" +
                "   ./%s:\n" +
                "   - wronglyNamedMemberImport\n" +
                "queries: |\n" +
                "   DEFINE QUERY myQuery => <caret>wronglyNamedMemberImport;\n", fileName);
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
        final String fileName = getFileName();
        addFile(fileName, "" +
                "model:\n" +
                "   Procedure: !Procedure");
        String content = String.format("" +
                "import:\n" +
                "   ./%s:" +
                "   -   Procedure\n" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       onStart:|\n" +
                "           <caret>@Procedure();\n", fileName);
        assertHasReference(content);
        assertNoErrors();
    }

    @Test
    void testNoErrorOnModuleProcedureReferences() {
        final String fileName = getFileName();
        addFile(fileName, "" +
                "model:\n" +
                "   Procedure: !Procedure");
        String content = String.format("" +
                "moduleName: ModuleNaam\n" +
                "import:\n" +
                "   ./%s:" +
                "   -   Procedure\n" +
                "procedures:\n" +
                "   - Pro<caret>cedure\n", fileName);
        assertHasReference(content);
        assertNoErrors();
    }

}
