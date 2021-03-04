package com.misset.opp.omt.psi.references;

import com.intellij.openapi.application.ReadAction;
import com.misset.opp.omt.psi.OMTCurieElement;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static com.misset.opp.util.UtilManager.getProjectUtil;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CurieReferenceIT extends ReferenceTest {

    @Override
    @BeforeAll
    protected void setUp() throws Exception {
        super.setName("CurieReferenceIT");
        super.setUp(OMTCurieElement.class);
        setOntologyModel();
        ReadAction.run(() -> getProjectUtil().loadOntologyModel(getProject(), true));
    }

    @Override
    @AfterAll
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
    void hasReferenceToTTLRDFPredicate() {
        String content = "queries: |\n" +
                "   DEFINE QUERY query => /ont:ClassA / ^rdf:type / ont:<caret>booleanProperty;";
        assertHasReference(withPrefixes(content));
    }

}
