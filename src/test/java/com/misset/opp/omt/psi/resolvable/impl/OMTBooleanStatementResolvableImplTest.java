package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.impl.OMTBooleanStatementImpl;
import com.misset.opp.omt.util.RDFModelUtil;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OMTBooleanStatementResolvableImplTest extends OMTTestSuite {

    OMTBooleanStatementImpl booleanStatement;

    @BeforeEach
    public void setUp() {
        booleanStatement = mock(OMTBooleanStatementImpl.class, InvocationOnMock::callRealMethod);
    }

    @Test
    void canBeInstantiated() {
        booleanStatement = new OMTBooleanStatementImpl(mock(ASTNode.class));
        assertNotNull(booleanStatement);
    }

    @Test
    void isBooleanType() {
        assertTrue(booleanStatement.isBooleanType());
    }

    @Test
    void resolveToResource() throws Exception {
        RDFModelUtil modelUtil = mock(RDFModelUtil.class);
        setUtilMock(modelUtil);
        Resource resource = mock(Resource.class);

        doReturn(resource).when(modelUtil).getPrimitiveTypeAsResource(eq("boolean"));
        assertContainsElements(booleanStatement.resolveToResource(), resource);
        assertContainsElements(booleanStatement.resolveToResource(true), resource);
        super.tearDown();
    }

    @Test
    void filterReturnsListWithShortestResult() {

        Resource resource = mock(Resource.class);
        List<Resource> initialResources = new ArrayList<>();
        List<Resource> oneValueList = Collections.singletonList(resource);
        List<Resource> twoValuesList = Arrays.asList(resource, resource);

        OMTQuery oneValueQuery = mock(OMTQuery.class);
        OMTQuery twoValuesQuery = mock(OMTQuery.class);
        doReturn(oneValueList).when(oneValueQuery).filter(initialResources);
        doReturn(twoValuesList).when(twoValuesQuery).filter(initialResources);
        doReturn(Arrays.asList(oneValueQuery, twoValuesQuery)).when(booleanStatement).getQueryList();

        assertEquals(oneValueList, booleanStatement.filter(initialResources));

        verify(oneValueQuery).filter(initialResources);
        verify(twoValuesQuery).filter(initialResources);
    }

    @Test
    void filterReturnsInitialValuesWhenNoQueries() {
        List<Resource> initialResources = new ArrayList<>();
        doReturn(Collections.EMPTY_LIST).when(booleanStatement).getQueryList();

        assertEquals(initialResources, booleanStatement.filter(initialResources));
    }

}
