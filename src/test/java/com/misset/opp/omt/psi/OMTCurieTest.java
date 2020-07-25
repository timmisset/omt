package com.misset.opp.omt.psi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OMTCurieTest {

    @Mock private OMTCurieElement curieElement;
    @Mock private OMTCurieConstantElement curieConstantElement;
    @Mock private OMTPrefix omtPrefix;
    @Mock private OMTCuriePrefix omtCuriePrefix;

    private OMTCurie fromCurie;
    private OMTCurie fromCurieConstant;
    private static String prefix = "dat:";
    private static String value = "item";
    private static String curie = String.format("%s%s", prefix, value);
    private static String curieConstant = String.format("/%s%s", prefix, value);

    @BeforeEach()
    void setUp() {
        MockitoAnnotations.initMocks(this);
        fromCurie = new OMTCurie(curieElement);
        fromCurieConstant = new OMTCurie(curieConstantElement);

        doReturn(curie).when(curieElement).getText();
        doReturn(curieConstant).when(curieConstantElement).getText();
    }
    @Test
    void isConstant() {
        assertFalse(fromCurie.isConstant());
        assertTrue(fromCurieConstant.isConstant());
    }

    @Test
    void getElement() {
        assertEquals(curieElement, fromCurie.getElement());
        assertEquals(curieConstantElement, fromCurieConstant.getElement());
    }

    @Test
    void getCuriePrefix() {
        assertEquals(prefix, fromCurie.getCuriePrefix());
        assertEquals(prefix, fromCurieConstant.getCuriePrefix());
    }

    @Test
    void isDefinedByPrefix() {
        doReturn(omtCuriePrefix).when(omtPrefix).getCuriePrefix();
        doReturn(prefix).when(omtCuriePrefix).getText();

        assertTrue(fromCurie.isDefinedByPrefix(omtPrefix));
        assertTrue(fromCurieConstant.isDefinedByPrefix(omtPrefix));
    }

    @Test
    void isNotDefinedByPrefix() {
        doReturn(omtCuriePrefix).when(omtPrefix).getCuriePrefix();
        doReturn("somethingElse:").when(omtPrefix).getText();

        assertFalse(fromCurie.isDefinedByPrefix(omtPrefix));
        assertFalse(fromCurieConstant.isDefinedByPrefix(omtPrefix));
    }

    @Test
    void getText() {
        doReturn("curie").when(curieElement).getText();
        doReturn("curie constant").when(curieConstantElement).getText();

        assertEquals("curie", fromCurie.getText());
        assertEquals("curie constant", fromCurieConstant.getText());
    }

    @Test
    void isPrefixDefinition() {
        doReturn(mock(OMTPrefix.class)).when(curieElement).getParent();
        assertTrue(fromCurie.isPrefixDefinition());
    }

    @Test
    void isNotPrefixDefinition() {
        doReturn(mock(OMTQueryStep.class)).when(curieElement).getParent();
        assertFalse(fromCurie.isPrefixDefinition());
    }
}
