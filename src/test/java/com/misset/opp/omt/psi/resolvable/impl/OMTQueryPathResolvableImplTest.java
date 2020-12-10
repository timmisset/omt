package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTNegatedStep;
import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.OMTQueryPath;
import com.misset.opp.omt.psi.OMTQueryStep;
import com.misset.opp.omt.psi.impl.OMTQueryPathImpl;
import com.misset.opp.omt.psi.util.TokenUtil;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class OMTQueryPathResolvableImplTest extends OMTTestSuite {

    private static final Model model = ModelFactory.createDefaultModel();
    private final Resource BOOLEAN_RESOURCE = xsdBoolean(model);
    private final Resource CLASSA = model.createResource("http://classA");
    private final Resource CLASSB = model.createResource("http://classB");

    @Mock
    RDFModelUtil rdfModelUtil;

    @Mock
    TokenUtil tokenUtil;

    @Mock
    OMTQuery firstQuery;

    @Mock
    OMTQuery secondQuery;

    OMTQueryPath queryPath;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setUtilMock(rdfModelUtil);
        setUtilMock(tokenUtil);
        queryPath = mock(OMTQueryPathImpl.class, InvocationOnMock::callRealMethod);

    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void canBeInstantiated() {
        queryPath = new OMTQueryPathImpl(mock(ASTNode.class));
        assertNotNull(queryPath);
    }

    @Test
    void isBooleanTypeReturnsTrueWhenStartsWithNegatedStep() {
        OMTQueryStep queryStep = mock(OMTQueryStep.class);
        doReturn(mock(OMTNegatedStep.class)).when(queryStep).getNegatedStep();
        doReturn(Collections.singletonList(queryStep)).when(queryPath).getQueryStepList();
        assertTrue(queryPath.isBooleanType());
    }

    @Test
    void isBooleanTypeReturnsTrueWhenEndsWithNotOperator() {
        OMTQueryStep queryStep = mock(OMTQueryStep.class);
        doReturn(true).when(tokenUtil).isNotOperator(any());
        doReturn(Arrays.asList(mock(OMTQueryStep.class), queryStep)).when(queryPath).getQueryStepList();
        doReturn(queryStep).when(queryPath).getLastChild();
        assertTrue(queryPath.isBooleanType());
    }

    @Test
    void resolveToResourceUsesLookback() {
        doReturn(null).when(queryPath).resolveToResource(anyBoolean());
        doReturn(Arrays.asList(mock(OMTQueryStep.class), mock(OMTQueryStep.class))).when(queryPath).getQueryStepList();
        queryPath.resolveToResource();
        verify(queryPath).resolveToResource(eq(true));
    }

    @Test
    void resolveToResourceReturnsEmptyListWhenNoSteps() {
        doReturn(mock(PsiElement.class)).when(queryPath).getParent();
        doReturn(Collections.emptyList()).when(queryPath).getQueryStepList();
        assertEquals(Collections.EMPTY_LIST, queryPath.resolveToResource());
    }

    @Test
    void resolveToResourceReturnsResourceListOfLastQueryStep() {
        List<Resource> firstStepResourceList = Collections.singletonList(CLASSA);
        List<Resource> secondStepResourceList = Collections.singletonList(CLASSB);
        OMTQueryStep firstStep = mock(OMTQueryStep.class);
        OMTQueryStep secondStep = mock(OMTQueryStep.class);

        doReturn(firstStepResourceList).when(firstStep).resolveToResource(anyBoolean());
        doReturn(secondStepResourceList).when(secondStep).resolveToResource(anyBoolean());
        doReturn(Arrays.asList(firstStep, secondStep)).when(queryPath).getQueryStepList();

        final List<Resource> resourceList = queryPath.resolveToResource();
        assertContainsElements(resourceList, CLASSB);
        assertEquals(1, resourceList.size());
    }

    @Test
    void filterReturnsEmptyListWhenNoQuerySteps() {
        doReturn(Collections.emptyList()).when(queryPath).getQueryStepList();
        assertEquals(Collections.EMPTY_LIST, queryPath.filter(Collections.singletonList(CLASSA)));
    }

    @Test
    void filterReturnsFilteredListWhenNegatedStep() {
        OMTQueryStep firstStep = mock(OMTQueryStep.class);
        OMTNegatedStep negatedStep = mock(OMTNegatedStep.class);
        doReturn(Collections.singletonList(firstStep)).when(queryPath).getQueryStepList();
        doReturn(negatedStep).when(firstStep).getNegatedStep();

        List<Resource> resourceList = Collections.singletonList(CLASSA);

        queryPath.filter(resourceList);
        verify(negatedStep).filter(eq(resourceList));
    }

    @Test
    void filterReturnsInput() {
        OMTQueryStep firstStep = mock(OMTQueryStep.class);
        doReturn(Collections.singletonList(firstStep)).when(queryPath).getQueryStepList();

        List<Resource> resourceList = Collections.singletonList(CLASSA);

        assertSame(resourceList, queryPath.filter(resourceList));
    }
}
