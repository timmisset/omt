package com.misset.opp.omt.psi.references;

import com.misset.opp.omt.inspection.OMTCodeInspectionUnused;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NamespacePrefixReferenceIT extends ReferenceTest {

    @Override
    @BeforeAll
    protected void setUp() throws Exception {
        super.setName("NamespacePrefixReferenceIT");
        super.setUp(OMTNamespacePrefix.class);
        setOntologyModel();
        myFixture.enableInspections(OMTCodeInspectionUnused.class);
    }

    @Override
    @AfterAll
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void modelUsageHasValidReferenceTest() {
        String content = "prefixes:\n" +
                "   ont: <http://ontologie/>\n" +
                "\n" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       onStart: |\n" +
                "           /ont<caret>:ClassA;\n" +
                "";
        assertHasReference(content);
        assertNoErrors();
    }

    @Test
    void modelUsageHasNoValidReferenceTest() {
        String content = "prefixes:\n" +
                "   not: <http://ontologie/>\n" +
                "\n" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       onStart: |\n" +
                "           /ont<caret>:ClassA;\n" +
                "";
        assertHasNoReference(content);
        assertHasError("Unknown prefix");
        assertHasWarning("not: is never used");
    }

    @Test
    void modelUsageAndDeclareHasValidReferenceTest() {
        String content =
                "model:\n" +
                        "   Activiteit: !Activity\n" +
                        "       prefixes:\n" +
                        "           ont: <http://ontologie/>\n" +
                        "       onStart: |\n" +
                        "           /ont<caret>:ClassA;\n" +
                        "";
        assertHasReference(content);
        assertNoErrors();
    }

    @Test
    void modelUsageAndDeclareOvershadowsRootblockTest() {
        String content = "prefixes:\n" +
                "   ont<caret>: <http://ontologie/>\n" + // <-- should be no reference to this prefix
                "\n" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       prefixes:\n" +
                "           ont: <http://ontologie/>\n" + // <-- due to the model overshadowing it
                "       onStart: |\n" +
                "           /ont:ClassA;\n" +
                "";
        assertHasUsages(content, 0);
        assertHasWarning("ont: is never used");
    }

    @Test
    void modelUsageAndDeclareShadowHasReference() {
        String content = "prefixes:\n" +
                "   ont: <http://ontologie/>\n" +
                "\n" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       prefixes:\n" +
                "           ont<caret>: <http://ontologie/>\n" + // <-- overshadowing prefix should be referenced to
                "       onStart: |\n" +
                "           /ont:ClassA;\n" +
                "";
        assertHasUsages(content, 1);
        assertHasWarning("ont: is never used"); // <-- the root one
    }

    @Test
    void modelHasMultipleUsages() {
        String content = "prefixes:\n" +
                "   ont<caret>: <http://ontologie/>\n" +
                "\n" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       onStart: |\n" +
                "           /ont:ClassA;\n" +
                "           /ont:ClassA;\n" +
                "";
        assertHasUsages(content, 2);
        assertNoErrors();
    }

    @Test
    void testScalarValueCurieHasReferenceToPrefixes() {
        String content = "" +
                "moduleName: ModuleNaam\n" +
                "prefixes:\n" +
                "    xsd:    <http://www.w3.org/2001/XMLSchema#>\n" +
                "declare:\n" +
                "    ModuleNaam:\n" +
                "        ActiviteitNaam:\n" +
                "            type: Activity\n" +
                "            params:\n" +
                "            -   xsd<caret>:string\n";
        assertHasReference(content);
    }

}
