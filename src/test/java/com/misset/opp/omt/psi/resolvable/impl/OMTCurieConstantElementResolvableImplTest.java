package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTCurieConstantElement;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.impl.OMTCurieConstantElementImpl;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import static org.mockito.Mockito.*;

class OMTCurieConstantElementResolvableImplTest extends OMTTestSuite {

    OMTCurieConstantElement curieConstantElement;

    @BeforeEach
    public void setUp() {
        curieConstantElement = mock(OMTCurieConstantElementImpl.class, InvocationOnMock::callRealMethod);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void canBeInstantiated() {
        curieConstantElement = new OMTCurieConstantElementImpl(mock(ASTNode.class));
        assertNotNull(curieConstantElement);
    }

    @Test
    void resolveToResourceReturnsCurieAsResolved() {
        final OMTCurieElement curieElement = mock(OMTCurieElement.class);
        final Resource curieAsResource = mock(Resource.class);
        doReturn(curieElement).when(curieConstantElement).getCurieElement();
        doReturn(curieAsResource).when(curieElement).getAsResource();
        doAnswer(invocation -> invocation.getArgument(0)).when(curieConstantElement).filter(anyList());
        assertContainsElements(curieConstantElement.resolveToResource(), curieAsResource);
    }

}
