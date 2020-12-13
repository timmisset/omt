package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTQueryStep;
import com.misset.opp.omt.psi.OMTSignature;
import com.misset.opp.omt.psi.OMTSignatureArgument;
import com.misset.opp.omt.psi.impl.OMTOperatorCallImpl;
import com.misset.opp.omt.psi.support.OMTCallable;
import com.misset.opp.omt.psi.util.MemberUtil;
import com.misset.opp.omt.psi.util.QueryUtil;
import com.misset.opp.omt.util.RDFModelUtil;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OMTCallResolvableImplTest extends OMTTestSuite {

    @Mock
    OMTCallable callable;

    @Mock
    OMTQueryStep queryStep;

    @Mock
    MemberUtil memberUtil;

    @Mock
    QueryUtil queryUtil;

    OMTOperatorCallImpl operatorCall;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setUtilMock(memberUtil);
        setUtilMock(queryUtil);

        operatorCall = mock(OMTOperatorCallImpl.class, InvocationOnMock::callRealMethod);
        doReturn(queryStep).when(operatorCall).getParent();
        doReturn(callable).when(memberUtil).getCallable(eq(operatorCall));
        doAnswer(invocation -> invocation.getArgument(0)).when(queryStep).filter(anyList());
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void canBeInstantiated() {
        operatorCall = new OMTOperatorCallImpl(mock(ASTNode.class));
        assertNotNull(operatorCall);
    }

    @Test
    void resolveToResourceReturnsPreviousStepWhenNoCallable() {
        doReturn(null).when(memberUtil).getCallable(eq(operatorCall));
        List<Resource> resources = new ArrayList<>();
        doReturn(resources).when(queryUtil).getPreviousStep(Mockito.eq(queryStep));

        assertSame(resources, operatorCall.resolveToResource());
    }

    @Test
    void resolveToResourceReturnsFirstArgumentType() {
        List<Resource> resources = new ArrayList<>();
        doReturn("CAST").when(operatorCall).getName();
        doReturn(resources).when(operatorCall).getFirstArgumentType();

        assertSame(resources, operatorCall.resolveToResource());
    }

    @Test
    void resolveToResourceCombinesTypes() {
        List<Resource> previousStep = Collections.singletonList(mock(Resource.class));
        List<Resource> firstArgumentType = Collections.singletonList(mock(Resource.class));
        doReturn("IF_EMPTY").when(operatorCall).getName();
        doReturn(firstArgumentType).when(operatorCall).getFirstArgumentType();
        doReturn(previousStep).when(queryUtil).getPreviousStep(eq(queryStep));

        final List<Resource> combinedResources = operatorCall.resolveToResource();

        assertEquals(2, combinedResources.size());
    }

    @Test
    void resolveToResourceReturnsPreviousStepWhenAny() {
        List<Resource> previousStep = Collections.singletonList(mock(Resource.class));
        doReturn("SOME_OPERATOR_CALL").when(operatorCall).getName();
        doReturn(previousStep).when(queryUtil).getPreviousStep(eq(queryStep));
        doReturn(true).when(callable).returnsAny();

        assertSame(previousStep, operatorCall.resolveToResource());
    }

    @Test
    void resolveToResourceReturnsReturnTypeWhenNotAny() {
        List<Resource> previousStep = Collections.singletonList(mock(Resource.class));
        List<Resource> callableReturnType = Collections.singletonList(mock(Resource.class));
        doReturn("SOME_OPERATOR_CALL").when(operatorCall).getName();
        doReturn(previousStep).when(queryUtil).getPreviousStep(eq(queryStep));
        doReturn(false).when(callable).returnsAny();
        doReturn(callableReturnType).when(callable).getReturnType();

        assertSame(callableReturnType, operatorCall.resolveToResource());
    }

    @Test
    void getFirstArgumentTypeReturnsAnyWhenNoSignature() {
        List<Resource> anyAsList = Collections.singletonList(mock(Resource.class));
        final RDFModelUtil rdfModelUtil = mock(RDFModelUtil.class);
        setUtilMock(rdfModelUtil);

        doReturn(anyAsList).when(rdfModelUtil).getAnyTypeAsList();
        doReturn(null).when(operatorCall).getSignature();

        assertSame(anyAsList, operatorCall.getFirstArgumentType());
    }

    @Test
    void getFirstArgumentTypeReturnsAnyWhenSignatureIsNotResolvable() {
        List<Resource> anyAsList = Collections.singletonList(mock(Resource.class));

        final RDFModelUtil rdfModelUtil = mock(RDFModelUtil.class);
        final OMTSignature omtSignature = mock(OMTSignature.class);
        final OMTSignatureArgument omtSignatureArgument = mock(OMTSignatureArgument.class);

        setUtilMock(rdfModelUtil);

        doReturn(anyAsList).when(rdfModelUtil).getAnyTypeAsList();
        doReturn(omtSignature).when(operatorCall).getSignature();
        doReturn(Collections.singletonList(omtSignatureArgument)).when(omtSignature).getSignatureArgumentList();
        doReturn(Collections.EMPTY_LIST).when(omtSignatureArgument).resolveToResource();

        assertSame(anyAsList, operatorCall.getFirstArgumentType());
    }

    @Test
    void getFirstArgumentTypeReturnsResolvedResourcesForFirstArgument() {
        List<Resource> anyAsList = Collections.singletonList(mock(Resource.class));
        List<Resource> firstArgumentResources = Collections.singletonList(mock(Resource.class));

        final RDFModelUtil rdfModelUtil = mock(RDFModelUtil.class);
        final OMTSignature omtSignature = mock(OMTSignature.class);
        final OMTSignatureArgument omtSignatureArgument = mock(OMTSignatureArgument.class);

        setUtilMock(rdfModelUtil);

        doReturn(anyAsList).when(rdfModelUtil).getAnyTypeAsList();
        doReturn(omtSignature).when(operatorCall).getSignature();
        doReturn(Collections.singletonList(omtSignatureArgument)).when(omtSignature).getSignatureArgumentList();
        doReturn(firstArgumentResources).when(omtSignatureArgument).resolveToResource();

        assertSame(firstArgumentResources, operatorCall.getFirstArgumentType());
    }
}