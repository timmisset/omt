package com.misset.opp.omt.psi.references;

import com.misset.opp.omt.psi.OMTMember;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MemberReferenceIT extends ReferenceTest {

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("MemberReferenceIT");
        super.setUp(OMTMember.class);
        setBuiltinAndModel();
    }

    @Test
    void testExportFromModuleHasReference() {
        final String fileName = getFileName();
        addFile(fileName, "" +
                "model:\n" +
                "   Procedure: !Procedure");
        String content = String.format("" +
                "moduleName: ModuleNaam\n" +
                "import:\n" +
                "   ./%s:" +
                "   -   Procedure\n" +
                "export:\n" +
                "   - Pro<caret>cedure\n", fileName);
        assertHasReference(content);
        assertNoErrors();
    }

    @Test
    void testExportFromModuleHasNoUsage() {
        final String fileName = getFileName();
        addFile(fileName, "" +
                "model:\n" +
                "   Procedure: !Procedure");
        String content = String.format("" +
                "moduleName: ModuleNaam\n" +
                "import:\n" +
                "   ./%s:" +
                "   -   Procedure\n" +
                "export:\n" +
                "   - Pro<caret>cedure\n", fileName);
        assertHasUsages(content, 0);
        assertNoErrors();
    }

    @Test
    void testImportFromModuleHasExportUsage() {
        final String fileName = getFileName();
        addFile(fileName, "" +
                "model:\n" +
                "   Procedure: !Procedure");
        String content = String.format("" +
                "moduleName: ModuleNaam\n" +
                "import:\n" +
                "   ./%s:" +
                "   -   Pro<caret>cedure\n" +
                "export:\n" +
                "   - Procedure\n", fileName);
        assertHasUsages(content, 1);
        assertNoErrors();
    }
}
