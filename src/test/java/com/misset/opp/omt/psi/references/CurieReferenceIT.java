package com.misset.opp.omt.psi.references;

import com.misset.opp.omt.psi.OMTCurieElement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CurieReferenceIT extends ReferenceTest {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("CurieReferenceIT");
        super.setUp(OMTCurieElement.class);
        setOntologyModel();
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void hasReferenceToTTLModel() {
        String content = "queries: |\n" +
                "   DEFINE QUERY query => /ont:<caret>ClassA;";
        assertHasReference(withPrefixes(content));
    }

}
