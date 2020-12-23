package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.OMTQueryReverseStep;
import com.misset.opp.omt.psi.impl.OMTQueryReverseStepImpl;
import com.misset.opp.omt.psi.util.QueryUtil;
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
import java.util.List;

import static org.mockito.Mockito.*;

class OMTQueryReverseStepResolvableImplTest extends OMTTestSuite {

    private static final Model model = ModelFactory.createDefaultModel();
    private final Resource CLASSA = model.createResource("http://classA");
    private final Resource CLASSB = model.createResource("http://classB");

    @Mock
    RDFModelUtil rdfModelUtil;

    @Mock
    QueryUtil queryUtil;

    @Mock
    OMTCurieElement curieElement;

    OMTQueryReverseStep queryReverseStep;

    List<Resource> previousStep;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setUtilMock(rdfModelUtil);
        setUtilMock(queryUtil);
        previousStep = new ArrayList<>(Arrays.asList(CLASSA, CLASSB));
        queryReverseStep = mock(OMTQueryReverseStepImpl.class, InvocationOnMock::callRealMethod);
        doReturn(previousStep).when(queryUtil).getPreviousStepResources(queryReverseStep);
        doReturn(curieElement).when(queryReverseStep).getCurieElement();
        doAnswer(invocation -> invocation.getArgument(0)).when(queryReverseStep).filter(anyList());
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void canBeInstantiated() {
        queryReverseStep = new OMTQueryReverseStepImpl(mock(ASTNode.class));
        assertNotNull(queryReverseStep);
    }

    @Test
    void resolveToResourceCallsResolveToResourceWithFilter() {
        doReturn(null).when(queryReverseStep).resolveToResource(anyBoolean());
        queryReverseStep.resolveToResource();
        verify(queryReverseStep).resolveToResource(eq(true));
    }

    @Test
    void resolveToResourceReturnsPreviousStepWhenNoCurieElement() {
        doReturn(null).when(queryReverseStep).getCurieElement();
        assertContainsElements(queryReverseStep.resolveToResource(false), CLASSA, CLASSB); // for coverage, run with false
    }

    @Test
    void resolveToResourceDoesntAddSuperclassesForRDFTypePredicateAndReturnsPredicatesObjectMatches() {
        doReturn(true).when(rdfModelUtil).isTypePredicate(any());
        doReturn(previousStep).when(rdfModelUtil).allSuperClasses(previousStep);
        doReturn(previousStep).when(rdfModelUtil).getPredicateSubjects(any());
        doReturn(previousStep).when(rdfModelUtil).listSubjectsWithPredicateObjectClass(any(), anyList());

        queryReverseStep.resolveToResource(true);

        // no calls to add superclasses
        verify(rdfModelUtil, times(0)).allSuperClasses(eq(previousStep));
        // resources is not empty (previousStep is set), should call listSubjectsWithPredicateObjectClass
        verify(rdfModelUtil).listSubjectsWithPredicateObjectClass(any(), anyList());
    }

    @Test
    void resolveToResourceDoesntAddSuperclassesForRDFTypePredicateAndReturnsPredicatesMatchesOnly() {
        doReturn(true).when(rdfModelUtil).isTypePredicate(any());
        previousStep.clear();
        doReturn(previousStep).when(rdfModelUtil).allSuperClasses(previousStep);
        doReturn(previousStep).when(rdfModelUtil).getPredicateSubjects(any());
        doReturn(previousStep).when(rdfModelUtil).listSubjectsWithPredicateObjectClass(any(), anyList());

        queryReverseStep.resolveToResource(false); // for coverage, run with false

        // no calls to add superclasses
        verify(rdfModelUtil, times(0)).allSuperClasses(eq(previousStep));
        // resources is not empty (previousStep is set), should call listSubjectsWithPredicateObjectClass
        verify(rdfModelUtil).getPredicateSubjects(any());
    }

    @Test
    void resolveToResourceAddsSuperclassesAndReturnsPredicatesMatchesOnly() {
        doReturn(false).when(rdfModelUtil).isTypePredicate(any());
        doReturn(previousStep).when(rdfModelUtil).allSuperClasses(previousStep);
        doReturn(previousStep).when(rdfModelUtil).getPredicateSubjects(any());
        doReturn(previousStep).when(rdfModelUtil).listSubjectsWithPredicateObjectClass(any(), anyList());

        queryReverseStep.resolveToResource(false);

        // no calls to add superclasses
        verify(rdfModelUtil).allSuperClasses(eq(previousStep));
        // resources is not empty (previousStep is set), should call listSubjectsWithPredicateObjectClass
        verify(rdfModelUtil).listSubjectsWithPredicateObjectClass(any(), anyList());
    }
}
