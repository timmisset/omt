package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.OMTQueryStep;
import com.misset.opp.omt.psi.impl.OMTCurieElementImpl;
import com.misset.opp.omt.util.RDFModelUtil;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import static org.mockito.Mockito.*;

class OMTCurieElementResolvableImplTest extends OMTTestSuite {

    OMTCurieElement curieElement;

    @Mock
    Resource asResource;

    @Mock
    OMTQueryStep queryStep;

    @Mock
    OMTQuery query;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        curieElement = mock(OMTCurieElementImpl.class, InvocationOnMock::callRealMethod);
        doReturn(asResource).when(curieElement).getAsResource();
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void canBeInstantiated() {
        curieElement = new OMTCurieElementImpl(mock(ASTNode.class));
        assertNotNull(curieElement);
    }

    @Test
    void resolveToResourceReturnsAsResourceWhenNoQueryStep() {
        doReturn(null).when(curieElement).getParent();
        assertContainsElements(curieElement.resolveToResource(), asResource);
    }

    @Test
    void resolveToResourceReturnsAsResourceWhenNoQueryStepParent() {
        doReturn(queryStep).when(curieElement).getParent();
        doReturn(null).when(queryStep).getParent();
        assertContainsElements(curieElement.resolveToResource(), asResource);
    }

    @Test
    void resolveToResourceReturnsAsResourceWhenFirstStepInQuery() {
        doReturn(queryStep).when(curieElement).getParent();
        doReturn(query).when(queryStep).getParent();
        doReturn(queryStep).when(query).getFirstChild();
        assertContainsElements(curieElement.resolveToResource(), asResource);
    }

    @Test
    void resolveToResourceReturnsPredicateObjectsWhenNotFirstStepInQuery() {
        doReturn(queryStep).when(curieElement).getParent();
        doReturn(query).when(queryStep).getParent();
        doReturn(mock(OMTQueryStep.class)).when(query).getFirstChild();

        final RDFModelUtil rdfModelUtil = mock(RDFModelUtil.class);
        setUtilMock(rdfModelUtil);
        curieElement.resolveToResource();

        verify(rdfModelUtil, times(1)).getPredicateObjects(eq(asResource));
    }

}
