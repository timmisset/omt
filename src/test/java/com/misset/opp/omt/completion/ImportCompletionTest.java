package com.misset.opp.omt.completion;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class ImportCompletionTest extends OMTCompletionTestSuite {

    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("ImportCompletionTest");
        super.setUp();

        setExportingFile();
    }

    private void setExportingFile() {
        String exportData = "queries: |\n" +
                "   DEFINE QUERY myQuery() => 'hello';\n" +
                "   DEFINE QUERY myQuery2() => 'hello';\n" +
                "";

        myFixture.addFileToProject("frontend/libs/exportFile.omt", exportData);
    }

    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void importAllMembers() {
        String content = "import:\n" +
                "   '@client/exportFile.omt':\n" +
                "   - <caret>\n";

        assertCompletionContains(content, "myQuery", "myQuery2");
    }

    @Test
    void importWithoutExistingImportMember() {
        String content = "import:\n" +
                "   '@client/exportFile.omt':\n" +
                "   - myQuery\n" +
                "   - <caret>\n";

        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "myQuery2");
        assertDoesntContain(completionLookupElements, "myQuery");
        assertEquals(1, completionLookupElements.size());
    }

}
