package com.misset.opp.omt.psi.references;

import com.intellij.openapi.application.ReadAction;
import com.misset.opp.omt.psi.OMTCurieElement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.misset.opp.omt.util.UtilManager.getProjectUtil;

public class CurieReferenceIT extends ReferenceTest {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("CurieReferenceIT");
        super.setUp(OMTCurieElement.class);
        setOntologyModel();
        ReadAction.run(() -> getProjectUtil().loadOntologyModel(getProject(), true));
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void hasReferenceToTTLModelClass() {
        String content = "queries: |\n" +
                "   DEFINE QUERY query => /ont:<caret>ClassA;";
        assertHasReference(withPrefixes(content));
    }

    @Test
    void hasReferenceToTTLRDFType() {
        // provided by the rdf ontology part of the LNKD.tech plugin
        String content = "queries: |\n" +
                "   DEFINE QUERY query => /ont:ClassA / rdf:<caret>type;";
        assertHasReference(withPrefixes(content));
    }

    @Test
    void hasReferenceToTTLRDFPredicate() {
        String content = "queries: |\n" +
                "   DEFINE QUERY query => /ont:ClassA / ^rdf:type / ont:<caret>booleanProperty;";
        assertHasReference(withPrefixes(content));
    }

}
