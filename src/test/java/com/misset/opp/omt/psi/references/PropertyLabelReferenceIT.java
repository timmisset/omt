package com.misset.opp.omt.psi.references;

import com.misset.opp.omt.psi.OMTPropertyLabel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PropertyLabelReferenceIT extends ReferenceTest {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("PropertyLabelReferenceIT");
        super.setUp(OMTPropertyLabel.class);
        setOntologyModel();
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void hasReferenceToPrefixes() {
        String content = "" +
                "moduleName: ModuleNaam\n" +
                "prefixes:\n" +
                "    xsd:    <http://www.w3.org/2001/XMLSchema#>\n" +
                "declare:\n" +
                "    ModuleNaam:\n" +
                "        ActiviteitNaam:\n" +
                "            type: Activity\n" +
                "            params:\n" +
                "            -   xsd<caret>:string";
        assertHasReference(content);
    }

}
