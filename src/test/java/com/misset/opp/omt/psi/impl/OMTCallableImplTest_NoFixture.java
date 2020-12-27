package com.misset.opp.omt.psi.impl;

import com.misset.opp.omt.exceptions.CallCallableMismatchException;
import com.misset.opp.omt.exceptions.IncorrectFlagException;
import com.misset.opp.omt.exceptions.NumberOfInputParametersMismatchException;
import com.misset.opp.omt.psi.OMTFlagSignature;
import com.misset.opp.omt.psi.OMTSignature;
import com.misset.opp.omt.psi.named.OMTCall;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class OMTCallableImplTest_NoFixture {
    private OMTCallableImpl getCallable(boolean asCommand) {
        final OMTCallableImpl callable = mock(OMTCallableImpl.class, InvocationOnMock::callRealMethod);
        doReturn(asCommand).when(callable).isCommand();
        doReturn(!asCommand).when(callable).isOperator();
        doReturn(false).when(callable).hasRest();
        doReturn(0).when(callable).getMinExpected();
        doReturn(1).when(callable).getMaxExpected();
        doReturn(Arrays.asList("flag")).when(callable).getFlags();
        return callable;
    }

    @Test
    void validateSignatureThrowsNothing() {
        final OMTCallableImpl callable = getCallable(true);
        final OMTCall call = mock(OMTCall.class);
        final OMTSignature signature = mock(OMTSignature.class);
        final OMTFlagSignature flagSignature = mock(OMTFlagSignature.class);
        final List<OMTSignature> signatureList = mock(List.class);

        doReturn(true).when(call).canCallCommand();
        doReturn(signature).when(call).getSignature();
        doReturn(signatureList).when(signature).getSignatureArgumentList();
        doReturn(0).when(signatureList).size();
        doReturn(flagSignature).when(call).getFlagSignature();
        doReturn("!flag").when(flagSignature).getText();

        assertDoesNotThrow(() -> callable.validateSignature(call));
    }

    @Test
    void validateSignatureThrowsIncorrectFlagException() {
        final OMTCallableImpl callable = getCallable(true);
        final OMTCall call = mock(OMTCall.class);
        final OMTSignature signature = mock(OMTSignature.class);
        final OMTFlagSignature flagSignature = mock(OMTFlagSignature.class);
        final List<OMTSignature> signatureList = mock(List.class);

        doReturn(true).when(call).canCallCommand();
        doReturn(signature).when(call).getSignature();
        doReturn(signatureList).when(signature).getSignatureArgumentList();
        doReturn(1).when(signatureList).size();
        doReturn(flagSignature).when(call).getFlagSignature();
        doReturn("!invalidFlag").when(flagSignature).getText();

        assertThrows(IncorrectFlagException.class, () -> callable.validateSignature(call));
    }

    @Test
    void validateSignatureThrowsNumberOfInputParametersMismatchExceptionWhenTooManyParameters() {
        final OMTCallableImpl callable = getCallable(true);
        final OMTCall call = mock(OMTCall.class);
        final OMTSignature signature = mock(OMTSignature.class);
        final List<OMTSignature> signatureList = mock(List.class);

        doReturn(true).when(call).canCallCommand();
        doReturn(signature).when(call).getSignature();

        doReturn(signatureList).when(signature).getSignatureArgumentList();
        doReturn(2).when(signatureList).size();
        doReturn(1).when(callable).getMinExpected();
        doReturn(1).when(callable).getMaxExpected();
        assertThrows(NumberOfInputParametersMismatchException.class, () -> callable.validateSignature(call));
    }

    @Test
    void validateSignatureThrowsNumberOfInputParametersMismatchExceptionWhenTooFewParameters() {
        final OMTCallableImpl callable = getCallable(true);
        final OMTCall call = mock(OMTCall.class);
        final OMTSignature signature = mock(OMTSignature.class);
        final List<OMTSignature> signatureList = mock(List.class);
        doReturn(true).when(call).canCallCommand();
        doReturn(signature).when(call).getSignature();

        doReturn(signatureList).when(signature).getSignatureArgumentList();
        doReturn(0).when(signatureList).size();
        doReturn(1).when(callable).getMinExpected();
        doReturn(1).when(callable).getMaxExpected();
        assertThrows(NumberOfInputParametersMismatchException.class, () -> callable.validateSignature(call));
    }

    @Test
    void validateSignatureThrowsCallCallableMismatchExceptionCommand() {
        final OMTCall call = mock(OMTCall.class);
        doReturn(false).when(call).canCallCommand();
        assertThrows(CallCallableMismatchException.class, () -> getCallable(true).validateSignature(call));
    }

    @Test
    void validateSignatureThrowsCallCallableMismatchExceptionOperator() {
        final OMTCall call = mock(OMTCall.class);
        doReturn(false).when(call).canCallOperator();
        assertThrows(CallCallableMismatchException.class, () -> getCallable(false).validateSignature(call));
    }

    @Test
    void setName() {
        final OMTCallableImpl callable = mock(OMTCallableImpl.class, InvocationOnMock::callRealMethod);
        callable.setName("name");
        assertEquals("name", callable.getName());
        callable.setName("name:");
        assertEquals("name", callable.getName());
    }
}
