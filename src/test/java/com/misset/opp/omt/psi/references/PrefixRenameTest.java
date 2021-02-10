package com.misset.opp.omt.psi.references;

import com.misset.opp.omt.psi.OMTNamespacePrefix;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PrefixRenameTest extends RenameTest {

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    @BeforeEach
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
