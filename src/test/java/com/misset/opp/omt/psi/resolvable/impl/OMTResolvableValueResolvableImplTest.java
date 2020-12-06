package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTCommandCall;
import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.OMTResolvableValue;
import com.misset.opp.omt.psi.impl.OMTResolvableValueImpl;
import com.misset.opp.omt.psi.support.OMTCallable;
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

class OMTResolvableValueResolvableImplTest extends OMTTestSuite {

    @Mock
    MemberUtil memberUtil;

    OMTResolvableValue resolvableValue;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setUtilMock(memberUtil);
        resolvableValue = mock(OMTResolvableValueImpl.class, InvocationOnMock::callRealMethod);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void canBeInstantiated() {
        resolvableValue = new OMTResolvableValueImpl(mock(ASTNode.class));
        assertNotNull(resolvableValue);
    }

    @Test
    void resolveToResourceQuery() {
        final List<Object> queryResources = Collections.emptyList();
        final OMTQuery query = mock(OMTQuery.class);
        doReturn(query).when(resolvableValue).getQuery();
        doReturn(queryResources).when(query).resolveToResource();
        assertSame(queryResources, resolvableValue.resolveToResource());
    }

    @Test
    void resolveToResourceCommandCallable() {
        final OMTCommandCall commandCall = mock(OMTCommandCall.class);
        final OMTCallable callable = mock(OMTCallable.class);
        final List<Object> callableResources = Collections.emptyList();

        doReturn(callable).when(memberUtil).getCallable(eq(commandCall));
        doReturn(commandCall).when(resolvableValue).getCommandCall();
        doReturn(null).when(resolvableValue).getQuery();

        doReturn(callableResources).when(callable).getReturnType();

        assertSame(callableResources, resolvableValue.resolveToResource());
    }
}
