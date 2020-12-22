package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTNegatedStep;
import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.impl.OMTNegatedStepImpl;
import com.misset.opp.omt.util.RDFModelUtil;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

class OMTNegatedStepResolvableImplTest extends OMTTestSuite {

    private static final Model model = ModelFactory.createDefaultModel();
    private final Resource BOOLEAN_RESOURCE = xsdBoolean(model);

    @Mock
    RDFModelUtil rdfModelUtil;

    OMTNegatedStep negatedStep;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setUtilMock(rdfModelUtil);
        negatedStep = mock(OMTNegatedStepImpl.class, InvocationOnMock::callRealMethod);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void canBeInstantiated() {
        negatedStep = new OMTNegatedStepImpl(mock(ASTNode.class));
        assertNotNull(negatedStep);
    }

    @Test
    void isBooleanTypeReturnsTrue() {
        assertTrue(negatedStep.isBooleanType());
    }

    @Test
    void resolveToResourceReturnsBoolean() {
        doReturn(BOOLEAN_RESOURCE).when(rdfModelUtil).getPrimitiveTypeAsResource("boolean");
        final List<Resource> resources = negatedStep.resolveToResource();
        assertEquals(1, resources.size());
        assertContainsElements(resources, BOOLEAN_RESOURCE);
    }

    @Test
    void filterCallsFilterOnQuery() {
        final OMTQuery query = mock(OMTQuery.class);
        doReturn(query).when(negatedStep).getQuery();
        List<Resource> input = new ArrayList<>();
        negatedStep.filter(input);

        verify(query).filter(eq(input));
    }
}
