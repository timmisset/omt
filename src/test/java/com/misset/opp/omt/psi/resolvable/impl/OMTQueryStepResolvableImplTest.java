package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.impl.OMTQueryStepImpl;
import com.misset.opp.omt.psi.util.QueryUtil;
import com.misset.opp.omt.util.RDFModelUtil;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class OMTQueryStepResolvableImplTest extends OMTTestSuite {

    private static final Model model = ModelFactory.createDefaultModel();
    private final Resource CLASSA = model.createResource("http://classA");
    private final Resource CLASSB = model.createResource("http://classB");

    @Mock
    RDFModelUtil rdfModelUtil;

    @Mock
    QueryUtil queryUtil;

    @Mock
    OMTCurieElement curieElement;

    @Mock
    OMTQueryPath queryPath;

    OMTQueryStep queryStep;

    List<Resource> resourceList;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setUtilMock(rdfModelUtil);
        setUtilMock(queryUtil);
        resourceList = new ArrayList<>(Arrays.asList(CLASSA, CLASSB));
        queryStep = mock(OMTQueryStepImpl.class, InvocationOnMock::callRealMethod);
        doReturn(queryPath).when(queryStep).getParent();
//        doReturn(previousStep).when(queryUtil).getPreviousStep(queryStep);
//        doReturn(curieElement).when(queryStep).getCurieElement();
//        doAnswer(invocation -> invocation.getArgument(0)).when(queryStep).filter(anyList());
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void canBeInstantiated() {
        queryStep = new OMTQueryStepImpl(mock(ASTNode.class));
        assertNotNull(queryStep);
    }

    @Test
    void filterReturnsResourcesWhenNoFilters() {
        doReturn(Collections.emptyList()).when(queryStep).getQueryFilterList();
        assertSame(resourceList, queryStep.filter(resourceList));
    }

    @Test
    void filterReturnsResourcesWhenFilterHasNoQuery() {
        final OMTQueryFilter filter = mock(OMTQueryFilter.class);
        doReturn(Collections.singletonList(filter)).when(queryStep).getQueryFilterList();
        assertSame(resourceList, queryStep.filter(resourceList));
    }

    @Test
    void filterFiltersTheResources() {
        final OMTQueryFilter filter = mock(OMTQueryFilter.class);
        final OMTQuery query = mock(OMTQuery.class);
        doReturn(query).when(filter).getQuery();
        doAnswer(invocation -> invocation.getArgument(0)).when(query).filter(anyList());

        doReturn(Arrays.asList(filter, filter)).when(queryStep).getQueryFilterList(); // 2 filters in the step
        assertSame(resourceList, queryStep.filter(resourceList));
        verify(query, times(2)).filter(anyList()); // check both filters are processed
    }

    @Test
    void resolveToResourceCallsResolveToResourceWithLookbackAndFilter() {
        doReturn(null).when(queryStep).resolveToResource(anyBoolean(), anyBoolean());
        queryStep.resolveToResource();
        verify(queryStep).resolveToResource(eq(true), eq(true));
    }

    @Test
    void resolveToResourceReturnsConstantValue() {
        final OMTConstantValue constantValue = mock(OMTConstantValue.class);
        doReturn(resourceList).when(constantValue).resolveToResource();
        doReturn(constantValue).when(queryStep).getConstantValue();
        assertSame(resourceList, queryStep.resolveToResource(true, false));
    }

    @Test
    void resolveToResourceReturnsVariableType() {
        final OMTVariable variable = mock(OMTVariable.class);
        doReturn(resourceList).when(variable).getType();
        doReturn(null).when(queryStep).getConstantValue();
        doReturn(variable).when(queryStep).getVariable();
        assertSame(resourceList, queryStep.resolveToResource(true, false));
    }

    @Test
    void resolveToResourceReturnsCurieResolveToResource() {
        final OMTVariable variable = mock(OMTVariable.class);
        doReturn(resourceList).when(variable).getType();
        doReturn(null).when(queryStep).getConstantValue();
        doReturn(null).when(queryStep).getVariable();
        doReturn(curieElement).when(queryStep).getCurieElement();
        doReturn(resourceList).when(queryUtil).getPreviousStep(eq(queryStep));
        doReturn(Collections.singletonList(CLASSA)).when(curieElement).resolveToResource();

        final List<Resource> resources = queryStep.resolveToResource(false, false);
        assertEquals(1, resources.size());
        assertContainsElements(resources, CLASSA);
    }

    @Test
    void resolveToResourceReturnsCurieResolveToResourceWhenNoPreviousStep() {
        final OMTVariable variable = mock(OMTVariable.class);
        doReturn(resourceList).when(variable).getType();
        doReturn(null).when(queryStep).getConstantValue();
        doReturn(null).when(queryStep).getVariable();
        doReturn(curieElement).when(queryStep).getCurieElement();
        doReturn(Collections.emptyList()).when(queryUtil).getPreviousStep(eq(queryStep));
        doReturn(Collections.singletonList(CLASSA)).when(curieElement).resolveToResource();

        doReturn(0).when(queryStep).getTextOffset();
        doReturn(0).when(queryPath).getTextOffset();

        ASTNode leafNode = mock(ASTNode.class);
        PsiElement leafElement = mock(PsiElement.class);
        doReturn(leafNode).when(leafElement).getNode();
        doReturn(OMTTypes.FORWARD_SLASH).when(leafNode).getElementType();

        try (MockedStatic<PsiTreeUtil> mockedStatic = mockStatic(PsiTreeUtil.class)) {
            mockedStatic
                    .when(() -> PsiTreeUtil.prevLeaf(any(), eq(true)))
                    .thenReturn(leafElement);

            final List<Resource> resources = queryStep.resolveToResource(true, false);
            assertEquals(1, resources.size());
            assertContainsElements(resources, CLASSA);
        }
    }

    @Test
    void resolveToResourceReturnsSubjectPredicatesFromPreviousStep() {
        final OMTVariable variable = mock(OMTVariable.class);
        doReturn(resourceList).when(variable).getType();
        doReturn(null).when(queryStep).getConstantValue();
        doReturn(null).when(queryStep).getVariable();
        doReturn(curieElement).when(queryStep).getCurieElement();
        doReturn(resourceList).when(queryUtil).getPreviousStep(eq(queryStep));
        doReturn(Collections.singletonList(CLASSA)).when(curieElement).resolveToResource();
        doAnswer(invocation -> invocation.getArgument(0)).when(rdfModelUtil).listObjectsWithSubjectPredicate(anyList(), any());

        doReturn(10).when(queryStep).getTextOffset();
        doReturn(0).when(queryPath).getTextOffset();

        final List<Resource> resources = queryStep.resolveToResource(true, false);
        assertSame(resourceList, resources);
        verify(rdfModelUtil).listObjectsWithSubjectPredicate(eq(resourceList), any());
    }

    @Test
    void resolveToResourceReturnsOperatorCallReturnType() {
        final OMTVariable variable = mock(OMTVariable.class);
        final OMTOperatorCall operatorCall = mock(OMTOperatorCall.class);
        doReturn(resourceList).when(variable).getType();
        doReturn(null).when(queryStep).getConstantValue();
        doReturn(null).when(queryStep).getVariable();
        doReturn(null).when(queryStep).getCurieElement();
        doReturn(operatorCall).when(queryStep).getOperatorCall();
        doReturn(resourceList).when(operatorCall).resolveToResource();
        assertSame(resourceList, queryStep.resolveToResource(true, false));
    }
}
