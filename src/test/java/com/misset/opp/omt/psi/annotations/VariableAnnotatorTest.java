package com.misset.opp.omt.psi.annotations;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTParameterType;
import com.misset.opp.omt.psi.OMTParameterWithType;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.OMTVariableAssignment;
import com.misset.opp.omt.psi.util.AnnotationUtil;
import com.misset.opp.omt.psi.util.ModelUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class VariableAnnotatorTest extends OMTTestSuite {

    @Mock
    AnnotationUtil annotationUtil;

    @Mock
    ModelUtil modelUtil;

    @Mock
    OMTVariable variable;

    @InjectMocks
    VariableAnnotator variableAnnotator;

    @Mock
    AnnotationHolder annotationHolder;

    @Mock
    AnnotationBuilder annotationBuilder;

    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("VariableAnnotatorTest");
        super.setUp();

        MockitoAnnotations.openMocks(this);

        setUtilMock(annotationUtil);
        setUtilMock(modelUtil);

        doReturn("$variable").when(variable).getName();
        doReturn(false).when(variable).isGlobalVariable();
        doReturn(false).when(variable).isGlobalVariable();
        doReturn(false).when(variable).isDeclaredVariable();

        doReturn(annotationBuilder).when(annotationHolder).newAnnotation(any(HighlightSeverity.class), anyString());
        doReturn(annotationBuilder).when(annotationBuilder).withFix(any(IntentionAction.class));

        doReturn("").when(modelUtil).getEntryBlockLabel(eq(variable));
    }

    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void annotateVariable_GlobalVariable() {
        doReturn(true).when(variable).isGlobalVariable();
        variableAnnotator.annotateVariable(variable, annotationHolder);
        verify(annotationHolder).newAnnotation(eq(HighlightSeverity.INFORMATION), eq("$variable is a global variable which is always available"));
        verify(annotationBuilder, times(1)).create();
        verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.ERROR), anyString());
    }

    @Test
    void annotateVariable_IgnoredVariable() {
        doReturn(true).when(variable).isIgnoredVariable();
        variableAnnotator.annotateVariable(variable, annotationHolder);
        verify(annotationHolder).newAnnotation(eq(HighlightSeverity.INFORMATION), eq("$variable is used to indicate the variable ignored"));
        verify(annotationBuilder, times(1)).create();
        verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.ERROR), anyString());
    }

    @Test
    void annotateVariable_DeclaredVariableSuggestsRenameTo$_WhenNotUsed() {
        doReturn(true).when(variable).isDeclaredVariable();
        doReturn(annotationBuilder).when(annotationUtil).annotateUsageGetBuilder(eq(variable), eq(annotationHolder));
        doReturn(mock(OMTVariableAssignment.class)).when(variable).getParent();

        try (MockedStatic<PsiTreeUtil> psiTreeUtilMockedStatic = mockStatic(PsiTreeUtil.class)) {
            // when there is a sibling variable in the same assignment statement
            psiTreeUtilMockedStatic.when(() -> PsiTreeUtil.getNextSiblingOfType(eq(variable), eq(OMTVariable.class)))
                    .thenReturn(mock(OMTVariable.class));
            variableAnnotator.annotateVariable(variable, annotationHolder);
            verify(annotationUtil, times(1)).annotateUsageGetBuilder(eq(variable), eq(annotationHolder));

            ArgumentCaptor<IntentionAction> intentionActionArgumentCaptor = ArgumentCaptor.forClass(IntentionAction.class);
            verify(annotationBuilder, times(1)).withFix(intentionActionArgumentCaptor.capture());
            assertEquals("Rename to $_", intentionActionArgumentCaptor.getValue().getText());
            verify(annotationBuilder, times(1)).create();
        }
    }

    @Test
    void annotateVariable_DeclaredVariableDoesNOTSuggestRenameTo$_WhenNoNextSibling() {
        doReturn(true).when(variable).isDeclaredVariable();
        doReturn(annotationBuilder).when(annotationUtil).annotateUsageGetBuilder(eq(variable), eq(annotationHolder));
        doReturn(mock(OMTVariableAssignment.class)).when(variable).getParent();

        try (MockedStatic<PsiTreeUtil> psiTreeUtilMockedStatic = mockStatic(PsiTreeUtil.class)) {
            // when there is a sibling variable in the same assignment statement
            psiTreeUtilMockedStatic.when(() -> PsiTreeUtil.getNextSiblingOfType(eq(variable), eq(OMTVariable.class)))
                    .thenReturn(null);
            variableAnnotator.annotateVariable(variable, annotationHolder);
            verify(annotationUtil, times(1)).annotateUsageGetBuilder(eq(variable), eq(annotationHolder));

            ArgumentCaptor<IntentionAction> intentionActionArgumentCaptor = ArgumentCaptor.forClass(IntentionAction.class);
            verify(annotationBuilder, times(0)).withFix(intentionActionArgumentCaptor.capture());
            verify(annotationBuilder, times(1)).create();
        }
    }

    @Test
    void annotateVariable_DeclaredVariableDoesNOTSuggestRenameTo$_WhenNotInAssignment() {
        doReturn(true).when(variable).isDeclaredVariable();
        doReturn(annotationBuilder).when(annotationUtil).annotateUsageGetBuilder(eq(variable), eq(annotationHolder));
        doReturn(null).when(variable).getParent();

        variableAnnotator.annotateVariable(variable, annotationHolder);
        verify(annotationUtil, times(1)).annotateUsageGetBuilder(eq(variable), eq(annotationHolder));

        ArgumentCaptor<IntentionAction> intentionActionArgumentCaptor = ArgumentCaptor.forClass(IntentionAction.class);
        verify(annotationBuilder, times(0)).withFix(intentionActionArgumentCaptor.capture());
        verify(annotationBuilder, times(1)).create();
    }

    @Test
    void annotateVariable_DeclaredVariableAnnotatesAsUntypedParameter() {
        doReturn(true).when(variable).isDeclaredVariable();
        doReturn(null).when(annotationUtil).annotateUsageGetBuilder(eq(variable), eq(annotationHolder));

        try (MockedStatic<PsiTreeUtil> psiTreeUtilMockedStatic = mockStatic(PsiTreeUtil.class)) {
            psiTreeUtilMockedStatic.when(
                    () -> PsiTreeUtil.getParentOfType(eq(variable), eq(OMTParameterType.class))
            ).thenReturn(null);

            doReturn("params").when(modelUtil).getEntryBlockLabel(eq(variable));
            variableAnnotator.annotateVariable(variable, annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.WEAK_WARNING), eq("Annotate parameter with a type"));
        }
    }

    @Test
    void annotateVariable_DeclaredVariableNotAnnotatedAsUntypedWhenNotInParams() {
        doReturn(true).when(variable).isDeclaredVariable();
        doReturn(null).when(annotationUtil).annotateUsageGetBuilder(eq(variable), eq(annotationHolder));

        try (MockedStatic<PsiTreeUtil> psiTreeUtilMockedStatic = mockStatic(PsiTreeUtil.class)) {
            psiTreeUtilMockedStatic.when(
                    () -> PsiTreeUtil.getParentOfType(eq(variable), eq(OMTParameterWithType.class))
            ).thenReturn(null);

            doReturn("variables").when(modelUtil).getEntryBlockLabel(eq(variable));
            variableAnnotator.annotateVariable(variable, annotationHolder);
            verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.WEAK_WARNING), eq("Annotate parameter with a type"));
        }
    }

    @Test
    void annotateVariable_DeclaredVariableNotAnnotatedAsUntypedWhenWithType() {
        doReturn(true).when(variable).isDeclaredVariable();
        doReturn(null).when(annotationUtil).annotateUsageGetBuilder(eq(variable), eq(annotationHolder));

        try (MockedStatic<PsiTreeUtil> psiTreeUtilMockedStatic = mockStatic(PsiTreeUtil.class)) {
            psiTreeUtilMockedStatic.when(
                    () -> PsiTreeUtil.getParentOfType(eq(variable), eq(OMTParameterWithType.class))
            ).thenReturn(mock(OMTParameterWithType.class));

            doReturn("params").when(modelUtil).getEntryBlockLabel(eq(variable));
            variableAnnotator.annotateVariable(variable, annotationHolder);
            verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.WEAK_WARNING), eq("Annotate parameter with a type"));
        }
    }

}
