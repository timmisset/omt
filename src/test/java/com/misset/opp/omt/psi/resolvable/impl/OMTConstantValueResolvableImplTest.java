package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTConstantValue;
import com.misset.opp.omt.psi.OMTResolvableValue;
import com.misset.opp.omt.psi.impl.OMTConstantValueImpl;
import com.misset.opp.omt.psi.util.TokenUtil;
import com.misset.opp.omt.util.ProjectUtil;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class OMTConstantValueResolvableImplTest extends OMTTestSuite {
    Model model;

    @Mock
    ProjectUtil projectUtil;
    @Mock
    TokenUtil tokenUtil;

    List<Resource> anyResourceAsList = Collections.singletonList(mock(Resource.class));

    @Mock
    OMTResolvableValue omtResolvableValue;

    OMTConstantValue constantValue;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setUtilMock(projectUtil);
        setUtilMock(tokenUtil);
        model = ModelFactory.createDefaultModel();
        doReturn(model).when(projectUtil).getOntologyModel();
        constantValue = mock(OMTConstantValueImpl.class, InvocationOnMock::callRealMethod);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void canBeInstantiated() {
        constantValue = new OMTConstantValueImpl(mock(ASTNode.class));
        assertNotNull(constantValue);
    }

    @Test
    void resolveToResourceReturnsEmptyListWhenNoTypedLiteral() {
        doReturn(null).when(tokenUtil).parseToTypedLiteral(eq(constantValue));
        assertEquals(Collections.EMPTY_LIST, constantValue.resolveToResource());
    }

    @Test
    void resolveToResourceReturnsString() {
        doReturn("string").when(tokenUtil).parseToTypedLiteral(eq(constantValue));
        assertEquals(XSD_STRING, constantValue.resolveToResource().get(0).toString());
    }

    @Test
    void resolveToResourceReturnsInteger() {
        doReturn(1).when(tokenUtil).parseToTypedLiteral(eq(constantValue));
        assertEquals(XSD_INTEGER, constantValue.resolveToResource().get(0).toString());
    }

    @Test
    void resolveToResourceReturnsDecimal() {
        doReturn(1.0).when(tokenUtil).parseToTypedLiteral(eq(constantValue));
        assertEquals(XSD_DOUBLE, constantValue.resolveToResource().get(0).toString());
    }

    @Test
    void resolveToResourceReturnsBoolean() {
        doReturn(true).when(tokenUtil).parseToTypedLiteral(eq(constantValue));
        assertEquals(XSD_BOOLEAN, constantValue.resolveToResource().get(0).toString());
    }
}
