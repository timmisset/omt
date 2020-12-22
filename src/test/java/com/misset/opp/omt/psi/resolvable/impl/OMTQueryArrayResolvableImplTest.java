package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.OMTQueryArray;
import com.misset.opp.omt.psi.impl.OMTQueryArrayImpl;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class OMTQueryArrayResolvableImplTest extends OMTTestSuite {

    private static final Model model = ModelFactory.createDefaultModel();
    private final Resource BOOLEAN_RESOURCE = xsdBoolean(model);
    private final Resource CLASSA = model.createResource("http://classA");
    private final Resource CLASSB = model.createResource("http://classB");

    @Mock
    RDFModelUtil rdfModelUtil;

    @Mock
    OMTQuery firstQuery;

    @Mock
    OMTQuery secondQuery;

    OMTQueryArray queryArray;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setUtilMock(rdfModelUtil);
        queryArray = mock(OMTQueryArrayImpl.class, InvocationOnMock::callRealMethod);

        doReturn(Collections.singletonList(CLASSA)).when(firstQuery).resolveToResource();
        doReturn(Collections.singletonList(CLASSB)).when(secondQuery).resolveToResource();
        doReturn(Arrays.asList(firstQuery, secondQuery)).when(queryArray).getQueryList();
        doAnswer(invocation -> invocation.getArgument(0)).when(rdfModelUtil).getDistinctResources(anyList());
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void canBeInstantiated() {
        queryArray = new OMTQueryArrayImpl(mock(ASTNode.class));
        assertNotNull(queryArray);
    }

    @Test
    void isBooleanTypeReturnsFalse() {
        assertFalse(queryArray.isBooleanType());
    }

    @Test
    void resolveToResourceReturnsCombinationOfQueries() {
        final List<Resource> resources = queryArray.resolveToResource();
        assertEquals(2, resources.size());
        assertContainsElements(resources, CLASSA, CLASSB);
        verify(rdfModelUtil, times(1)).getDistinctResources(anyList());
    }

    @Test
    void filterReturnsInput() {
        List<Resource> input = new ArrayList<>();
        assertSame(input, queryArray.filter(input));
    }
}
