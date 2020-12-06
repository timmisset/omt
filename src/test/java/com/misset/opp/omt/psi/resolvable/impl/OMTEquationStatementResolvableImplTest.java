package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.impl.OMTEquationStatementImpl;
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

class OMTEquationStatementResolvableImplTest extends OMTTestSuite {

    private static final Model model = ModelFactory.createDefaultModel();
    private static final String CLASSA = "http://classA";
    private static final String CLASSB = "http://classB";
    private static final String CLASSC = "http://classC";

    private final Resource TYPE_RESOURCE = rdfType(model);
    private final Resource BOOLEAN_RESOURCE = xsdBoolean(model);

    @Mock
    RDFModelUtil rdfModelUtil;

    @Mock
    OMTQuery leftHandQuery;

    @Mock
    OMTQuery rightHandQuery;

    List<OMTQuery> queryList;

    OMTEquationStatement equationStatement;

    List<Resource> resources;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setUtilMock(rdfModelUtil);
        equationStatement = mock(OMTEquationStatementImpl.class, InvocationOnMock::callRealMethod);

        queryList = Arrays.asList(leftHandQuery, rightHandQuery);
        doReturn(queryList).when(equationStatement).getQueryList();
        doReturn(Collections.singletonList(TYPE_RESOURCE)).when(leftHandQuery).resolveToResource();

        resources = Arrays.asList(
                model.createResource(CLASSA),
                model.createResource(CLASSB),
                model.createResource(CLASSC)
        );

        doReturn(true).when(rdfModelUtil).isTypePredicate(TYPE_RESOURCE);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void canBeInstantiated() {
        equationStatement = new OMTEquationStatementImpl(mock(ASTNode.class));
        assertNotNull(equationStatement);
    }

    @Test
    void isBooleanTypeReturnsTrue() {
        assertTrue(equationStatement.isBooleanType());
    }

    @Test
    void resolveToResourceReturnsBoolean() {
        doReturn(BOOLEAN_RESOURCE).when(rdfModelUtil).getPrimitiveTypeAsResource("boolean");
        final List<Resource> resources = equationStatement.resolveToResource();
        assertEquals(1, resources.size());
        assertContainsElements(resources, BOOLEAN_RESOURCE);
    }

    @Test
    void resolveToResourceWithLookbackReturnsBoolean() {
        doReturn(BOOLEAN_RESOURCE).when(rdfModelUtil).getPrimitiveTypeAsResource("boolean");
        final List<Resource> resources = equationStatement.resolveToResource(true);
        assertEquals(1, resources.size());
        assertContainsElements(resources, BOOLEAN_RESOURCE);
    }

    @Test
    void filterCallsResolveQueryWithoutLookbackWhenQueryPath() {
        OMTQueryPath queryPath = mock(OMTQueryPath.class);

        doReturn(new ArrayList<>()).when(queryPath).resolveToResource(eq(false));
        doReturn(Collections.singletonList(queryPath)).when(equationStatement).getQueryList();

        final List<Resource> filterResult = equationStatement.filter(resources);
        assertSame(filterResult, resources);
        verify(queryPath, times(1)).resolveToResource(eq(false));
    }

    @Test
    void filterReturnsInputWhenNotTypePredicate() {
        doReturn(false).when(rdfModelUtil).isTypePredicate(any());
        final List<Resource> filterResult = equationStatement.filter(resources);
        assertSame(filterResult, resources);
    }

    @Test
    void filterReturnsFilteredList() {
        doReturn(mock(OMTQueryFilter.class)).when(equationStatement).getParent();
        doReturn(Collections.singletonList(model.createResource(CLASSA))).when(rightHandQuery).resolveToResource();
        final List<Resource> filterResult = equationStatement.filter(resources);
        assertEquals(1, filterResult.size());
        assertContainsElements(filterResult, model.createResource(CLASSA));
    }

    @Test
    void filterReturnsFilteredNegatedList() {
        doReturn(mock(OMTNegatedStep.class)).when(equationStatement).getParent();
        doReturn(Collections.singletonList(model.createResource(CLASSA))).when(rightHandQuery).resolveToResource();
        final List<Resource> filterResult = equationStatement.filter(resources);
        assertEquals(2, filterResult.size());
        assertContainsElements(filterResult, model.createResource(CLASSB), model.createResource(CLASSC));
    }
}
