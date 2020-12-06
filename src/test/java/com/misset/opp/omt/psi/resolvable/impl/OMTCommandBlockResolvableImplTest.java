package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTResolvableValue;
import com.misset.opp.omt.psi.OMTReturnStatement;
import com.misset.opp.omt.psi.impl.OMTCommandBlockImpl;
import com.misset.opp.omt.util.RDFModelUtil;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OMTCommandBlockResolvableImplTest extends OMTTestSuite {

    @Mock
    OMTReturnStatement omtReturnStatement;

    @Mock
    RDFModelUtil rdfModelUtil;

    List<Resource> anyResourceAsList = Collections.singletonList(mock(Resource.class));

    @Mock
    OMTResolvableValue omtResolvableValue;

    OMTCommandBlockImpl commandBlock;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setUtilMock(rdfModelUtil);
        doReturn(anyResourceAsList).when(rdfModelUtil).getAnyTypeAsList();
        commandBlock = mock(OMTCommandBlockImpl.class, InvocationOnMock::callRealMethod);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void canBeInstantiated() {
        commandBlock = new OMTCommandBlockImpl(mock(ASTNode.class));
        assertNotNull(commandBlock);
    }

    @Test
    void resolveToResourceReturnsAnyWhenNoReturnStatement() {
        final MockedStatic<PsiTreeUtil> psiTreeUtilMock = getPsiTreeUtilMock();
        psiTreeUtilMock.when(() -> PsiTreeUtil.findChildOfType(eq(commandBlock), eq(OMTReturnStatement.class))).thenReturn(null);
        assertSame(anyResourceAsList, commandBlock.resolveToResource());
    }

    @Test
    void resolveToResourceReturnsAnyWhenNoResolvableValue() {
        final MockedStatic<PsiTreeUtil> psiTreeUtilMock = getPsiTreeUtilMock();
        psiTreeUtilMock.when(() -> PsiTreeUtil.findChildOfType(eq(commandBlock), eq(OMTReturnStatement.class))).thenReturn(omtReturnStatement);
        doReturn(null).when(omtReturnStatement).getResolvableValue();
        assertSame(anyResourceAsList, commandBlock.resolveToResource());
    }

    @Test
    void resolveToResourceReturnsResolvableValue() {
        final MockedStatic<PsiTreeUtil> psiTreeUtilMock = getPsiTreeUtilMock();
        psiTreeUtilMock.when(() -> PsiTreeUtil.findChildOfType(eq(commandBlock), eq(OMTReturnStatement.class))).thenReturn(omtReturnStatement);
        doReturn(omtResolvableValue).when(omtReturnStatement).getResolvableValue();
        doReturn(anyResourceAsList).when(omtResolvableValue).resolveToResource();
        assertSame(anyResourceAsList, commandBlock.resolveToResource());
        verify(omtResolvableValue, times(1)).resolveToResource();
    }

}
