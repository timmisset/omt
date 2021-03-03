package com.misset.opp.omt.annotations;

import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTParameterWithType;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.util.ModelUtil;
import com.misset.opp.omt.psi.util.VariableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class VariableAnnotatorTest extends OMTAnnotationTest {

    @Mock
    ModelUtil modelUtil;

    @Mock
    OMTVariable variable;

    @Mock
    PsiReference reference;

    @InjectMocks
    VariableAnnotator variableAnnotator;

    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("VariableAnnotatorTest");
        super.setUp();

        MockitoAnnotations.openMocks(this);

        setUtilMock(modelUtil);

        doReturn("$variable").when(variable).getName();
        doReturn("$variable").when(variable).getText();
        doReturn(false).when(variable).isGlobalVariable();
        doReturn(false).when(variable).isGlobalVariable();
        doReturn(false).when(variable).isDeclaredVariable();

        doReturn("").when(modelUtil).getEntryBlockLabel(eq(variable));
    }

    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void annotateVariableNoAnnotationWhenInvalidType() {
        variableAnnotator.annotate(mock(PsiElement.class));
        verifyNoAnnotations();
    }

    @Test
    void annotateVariable_GlobalVariable() {
        doReturn(true).when(variable).isGlobalVariable();
        variableAnnotator.annotate(variable);
        verify(getHolder()).newAnnotation(eq(HighlightSeverity.INFORMATION), eq("$variable is a global variable which is always available"));
        verify(getBuilder(), times(1)).create();
        verifyNoErrors();
    }

    @Test
    void annotateVariable_IgnoredVariable() {
        doReturn(true).when(variable).isIgnoredVariable();
        variableAnnotator.annotate(variable);
        verify(getHolder()).newAnnotation(eq(HighlightSeverity.INFORMATION), eq("$variable is used to indicate the variable ignored"));
        verify(getBuilder(), times(1)).create();
        verifyNoErrors();
    }

    @Test
    void annotateVariable_DeclaredVariableAnnotatesAsUntypedParameter() {
        doReturn(true).when(variable).isDeclaredVariable();
        setSearchReferenceMock(variable, query -> doReturn(true).when(query).anyMatch(any()));
        setPsiTreeUtilMockWhenThenReturn(() -> PsiTreeUtil.getParentOfType(eq(variable), eq(OMTParameterWithType.class)), null);
        doReturn("params").when(modelUtil).getEntryBlockLabel(eq(variable));

        variableAnnotator.annotate(variable);

        verify(getHolder()).newAnnotation(eq(HighlightSeverity.WEAK_WARNING), eq("Annotate parameter with a type"));
    }

    @Test
    void annotateVariable_DeclaredVariableNotAnnotatedAsUntypedWhenNotInParams() {
        doReturn(true).when(variable).isDeclaredVariable();
        setSearchReferenceMock(variable, query -> doReturn(true).when(query).anyMatch(any()));

        setPsiTreeUtilMockWhenThenReturn(() -> PsiTreeUtil.getParentOfType(eq(variable), eq(OMTParameterWithType.class)), null);
        doReturn("variables").when(modelUtil).getEntryBlockLabel(eq(variable));
        variableAnnotator.annotate(variable);
        verifyNoWeakWarnings();
    }

    @Test
    void annotateVariable_DeclaredVariableNotAnnotatedAsUntypedWhenWithType() {
        doReturn(true).when(variable).isDeclaredVariable();
        setSearchReferenceMock(variable, query -> doReturn(true).when(query).anyMatch(any()));
        setPsiTreeUtilMockWhenThenReturn(() -> PsiTreeUtil.getParentOfType(eq(variable), eq(OMTParameterWithType.class)), mock(OMTParameterWithType.class));

        doReturn("params").when(modelUtil).getEntryBlockLabel(eq(variable));
        variableAnnotator.annotate(variable);
        verifyNoWeakWarnings();
    }

    @Test
    void annotateVariable_LocalVariable() {
        HashMap<String, String> localVariables = new HashMap<>();
        localVariables.put(variable.getName(), "COMMAND");
        final VariableUtil variableUtil = mock(VariableUtil.class);
        setUtilMock(variableUtil);

        doReturn(localVariables).when(variableUtil).getLocalVariables(eq(variable));

        variableAnnotator.annotate(variable);

        verify(getHolder()).newAnnotation(eq(HighlightSeverity.INFORMATION), eq("$variable is locally available in COMMAND"));
    }

    @Test
    void annotateVariable_UsageVariableThrowsErrorWhenNoReference() {
        doReturn(reference).when(variable).getReference();

        variableAnnotator.annotate(variable);

        verify(getHolder()).newAnnotation(eq(HighlightSeverity.ERROR), eq("$variable is not declared"));
    }

}
