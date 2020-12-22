package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.OMTSubQuery;
import com.misset.opp.omt.psi.impl.OMTSubQueryImpl;
import com.misset.opp.omt.psi.util.MemberUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import static org.mockito.Mockito.*;

class OMTSubQueryResolvableImplTest extends OMTTestSuite {

    @Mock
    MemberUtil memberUtil;

    @Mock
    OMTQuery query;

    OMTSubQuery subQuery;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setUtilMock(memberUtil);
        subQuery = mock(OMTSubQueryImpl.class, InvocationOnMock::callRealMethod);
        doReturn(query).when(subQuery).getQuery();
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void canBeInstantiated() {
        subQuery = new OMTSubQueryImpl(mock(ASTNode.class));
        assertNotNull(subQuery);
    }

    @Test
    void resolveToResourceDefersToQuery() {
        subQuery.resolveToResource();
        verify(query).resolveToResource();
    }
}
