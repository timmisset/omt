package com.misset.opp.omt.psi.references;

import com.misset.opp.omt.psi.OMTModelItemLabel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ModelItemRenameTest extends RenameTest {

    @Override
    @AfterAll
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    @BeforeAll
    protected void setUp() throws Exception {
        super.setName("ModelItemRenameTest");
        super.setUp(OMTModelItemLabel.class);
        setOntologyModel();
    }

    @Test
    void renamesPropertyLabel() {
        String content = "model:\n" +
                "    Acti<caret>viteit: !Activity";

        renameElement(content, "NieuweNaam");
        assertEquals("model:\n" +
                "    NieuweNaam: !Activity", myFixture.getEditor().getDocument().getText());
    }

    @Test
    void renamesPropertyLabelAndUsage() {
        String content = "model:\n" +
                "    Acti<caret>viteit: !Activity\n" +
                "\n" +
                "    AndereActiviteit: !Activity\n" +
                "        onStart: |\n" +
                "            @Activiteit();";

        renameElement(content, "NieuweNaam");
        assertEquals("model:\n" +
                "    NieuweNaam: !Activity\n" +
                "\n" +
                "    AndereActiviteit: !Activity\n" +
                "        onStart: |\n" +
                "            @NieuweNaam();", myFixture.getEditor().getDocument().getText());
    }

}
