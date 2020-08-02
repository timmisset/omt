package com.misset.opp.omt.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.exceptions.NumberOfInputParametersMismatchException;
import com.misset.opp.omt.psi.impl.OMTSignatureImpl;
import com.misset.opp.omt.psi.impl.OMTVariableValueImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

class OMTBuiltInTest {

    @Mock private PsiElement element;
    @Mock private OMTDefineParam omtDefineParam;
    @Mock private OMTVariable variable;
    @Mock private OMTParameter parameter;
    @Mock private OMTParameter optionalParameter;
    @Mock private OMTParameter restParameter;
    @Mock private OMTVariableValue value;
    @Mock private OMTSignature signature;
    @Mock private OMTOperatorCall call;

    private List<OMTParameter> parameterList;
    private List<OMTVariableValue> values;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        values = new ArrayList<>();

        doReturn("$var").when(variable).getText();
        doReturn(Arrays.asList(variable, variable, variable)).when(omtDefineParam).getVariableList();
        doReturn(values).when(signature).getVariableValueList();
        doReturn(signature).when(call).getSignature();

        doReturn(true).when(parameter).isRequired();
        doReturn(false).when(optionalParameter).isRequired();
        doReturn(true).when(restParameter).isRest();


        parameterList = Arrays.asList(parameter, parameter, parameter);

    }

    @Test
    void constructors() {
        assertEquals("test", new OMTBuiltIn("test", "").getName());
        assertEquals(parameterList, new OMTBuiltIn("test", parameterList, "").getParameters());

        OMTBuiltIn builtIn = new OMTBuiltIn(element, "test", omtDefineParam, "");
        assertEquals(3, builtIn.getParameters().size());
    }

    @Test
    void getMinExpected() {
        // all required
        assertEquals(0, new OMTBuiltIn("", "").getMinExpected());
        assertEquals(1, new OMTBuiltIn("", Arrays.asList(parameter), "").getMinExpected());
        assertEquals(1, new OMTBuiltIn("", Arrays.asList(parameter, restParameter), "").getMinExpected());

        // optional
        assertEquals(0, new OMTBuiltIn("", Arrays.asList(optionalParameter), "").getMinExpected());
    }

    @Test
    void getMaxExpected() {

        assertEquals(0, new OMTBuiltIn("", "").getMaxExpected());
        assertEquals(1, new OMTBuiltIn("", Arrays.asList(parameter), "").getMaxExpected());
        assertEquals(2, new OMTBuiltIn("", Arrays.asList(parameter, parameter), "").getMaxExpected());
        assertEquals(-1, new OMTBuiltIn("", Arrays.asList(parameter, restParameter), "").getMaxExpected());

        // optional
        assertEquals(0, new OMTBuiltIn("", Arrays.asList(optionalParameter), "").getMinExpected());
    }

    @Test
    void validateSignature_twoRequired() {
        OMTBuiltIn omtBuiltIn = new OMTBuiltIn("", Arrays.asList(parameter, parameter), "");

        // too few
        assertThrows(NumberOfInputParametersMismatchException.class, () -> omtBuiltIn.validateSignature(call));

        // sufficient
        values.addAll(Arrays.asList(value, value));
        assertDoesNotThrow(() -> omtBuiltIn.validateSignature(call));

        // too many
        values.add(value);
        assertThrows(NumberOfInputParametersMismatchException.class, () -> omtBuiltIn.validateSignature(call));
    }

    @Test
    void validateSignature_OneRequired_OneOptional() {
        OMTBuiltIn omtBuiltIn = new OMTBuiltIn("", Arrays.asList(parameter, optionalParameter), "");

        // too few
        assertThrows(NumberOfInputParametersMismatchException.class, () -> omtBuiltIn.validateSignature(call));

        // one and two
        values.addAll(Arrays.asList(value));
        assertDoesNotThrow(() -> omtBuiltIn.validateSignature(call));

        values.addAll(Arrays.asList(value));
        assertDoesNotThrow(() -> omtBuiltIn.validateSignature(call));

        // too many
        values.add(value);
        assertThrows(NumberOfInputParametersMismatchException.class, () -> omtBuiltIn.validateSignature(call));
    }

    @Test
    void validateSignature_OneRequired_OneRest() {
        OMTBuiltIn omtBuiltIn = new OMTBuiltIn("", Arrays.asList(parameter, restParameter), "");

        // too few
        assertThrows(NumberOfInputParametersMismatchException.class, () -> omtBuiltIn.validateSignature(call));

        // one to 5, all should pass
        for(int i = 0; i < 5; i++) {
            values.addAll(Arrays.asList(value));
            assertDoesNotThrow(() -> omtBuiltIn.validateSignature(call));
        }
    }

    @Test
    void getName() {
        assertEquals("name", new OMTBuiltIn("name", "").getName());
    }

    @Test
    void getElement() {
        assertEquals(element, new OMTBuiltIn(element, "name", omtDefineParam, "").getElement());
    }

    @Test
    void getParameters() {
        assertEquals(parameterList, new OMTBuiltIn("name", parameterList, "").getParameters());
    }
}
