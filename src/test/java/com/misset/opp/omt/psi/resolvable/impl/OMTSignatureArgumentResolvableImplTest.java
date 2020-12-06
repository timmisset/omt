package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTCommandBlock;
import com.misset.opp.omt.psi.OMTResolvableValue;
import com.misset.opp.omt.psi.OMTSignatureArgument;
import com.misset.opp.omt.psi.impl.OMTSignatureArgumentImpl;
import com.misset.opp.omt.psi.util.MemberUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class OMTSignatureArgumentResolvableImplTest extends OMTTestSuite {

    @Mock
    MemberUtil memberUtil;

    OMTSignatureArgument signatureArgument;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setUtilMock(memberUtil);
        signatureArgument = mock(OMTSignatureArgumentImpl.class, InvocationOnMock::callRealMethod);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void canBeInstantiated() {
        signatureArgument = new OMTSignatureArgumentImpl(mock(ASTNode.class));
        assertNotNull(signatureArgument);
    }

    @Test
    void resolveToResourceCommandBlock() {
        final List<Object> commandBlockResources = Collections.emptyList();
        final OMTCommandBlock commandBlock = mock(OMTCommandBlock.class);
        doReturn(commandBlock).when(signatureArgument).getCommandBlock();
        doReturn(commandBlockResources).when(commandBlock).resolveToResource();
        assertSame(commandBlockResources, signatureArgument.resolveToResource());
    }

    @Test
    void resolveToResourceResolvableValue() {
        final OMTResolvableValue resolvableValue = mock(OMTResolvableValue.class);
        doReturn(null).when(signatureArgument).getCommandBlock();
        doReturn(resolvableValue).when(signatureArgument).getResolvableValue();
        signatureArgument.resolveToResource();

        verify(resolvableValue).resolveToResource();
    }
}
