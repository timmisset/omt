package com.misset.opp.omt.psi.references;

import com.misset.opp.omt.psi.OMTNamespacePrefix;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PrefixRenameTest extends RenameTest {

    @Override
    @AfterAll
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    @BeforeAll
    protected void setUp() throws Exception {
        super.setName("PrefixRenameTest");
        super.setUp(OMTNamespacePrefix.class);
        setOntologyModel();
    }

    @Test
    void renamesPrefixAndUsages() {
        String content = "prefixes:\n" +
                "   abc<caret>: <http://abc/>\n" +
                "\n" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       onStart: |\n" +
                "           @LOG(/abc:def);\n" +
                "";

        renameElement(content, "aabc");
        assertEquals("prefixes:\n" +
                "    aabc:    <http://abc/>\n" +
                "\n" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       onStart: |\n" +
                "           @LOG(/aabc:def);\n", myFixture.getEditor().getDocument().getText());
    }

}
