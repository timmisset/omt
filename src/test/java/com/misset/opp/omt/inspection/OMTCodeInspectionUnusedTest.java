package com.misset.opp.omt.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.OMTVariableAssignment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class OMTCodeInspectionUnusedTest extends OMTInspectionTest {

    OMTCodeInspectionUnused omtCodeInspectionUnused;

    @Mock
    ProblemsHolder problemsHolder;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        omtCodeInspectionUnused = Mockito.spy(new OMTCodeInspectionUnused());
        doReturn(false).when(omtCodeInspectionUnused).isTestFile(problemsHolder);
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void getDescription() {
        // having no description will throw an error in IntelliJ
        assertNotNull("UnusedDeclarations", omtCodeInspectionUnused.getStaticDescription());
    }

    @Test
    void returnEmptyWhenTestFile() {
        doReturn(true).when(omtCodeInspectionUnused).isTestFile(problemsHolder);
        final PsiElementVisitor psiElementVisitor = omtCodeInspectionUnused.buildVisitor(problemsHolder, false);
        assertEquals(PsiElementVisitor.EMPTY_VISITOR, psiElementVisitor);
    }

    @Test
    void testOMTNamespacePrefixRegistersWarningWhenPrefixIsUnused() {
        final OMTNamespacePrefix namespacePrefix = mock(OMTNamespacePrefix.class);
        final OMTPrefix prefix = mock(OMTPrefix.class);
        doReturn(prefix).when(namespacePrefix).getParent();
        doReturn("Prefix").when(namespacePrefix).getText();
        setUnusedMock(namespacePrefix, true);

        final PsiElementVisitor psiElementVisitor = omtCodeInspectionUnused.buildVisitor(problemsHolder, false);
        psiElementVisitor.visitElement(namespacePrefix);

        verify(problemsHolder).registerProblem(eq(namespacePrefix), eq("Prefix is never used"), eq(ProblemHighlightType.LIKE_UNUSED_SYMBOL), any(LocalQuickFix.class));
    }

    @Test
    void testOMTNamespacePrefixRegistersNoWarningWhenPrefixIsUsed() {
        final OMTNamespacePrefix namespacePrefix = mock(OMTNamespacePrefix.class);
        final OMTPrefix prefix = mock(OMTPrefix.class);
        doReturn(prefix).when(namespacePrefix).getParent();
        doReturn("Prefix").when(namespacePrefix).getText();
        setUnusedMock(namespacePrefix, false);

        final PsiElementVisitor psiElementVisitor = omtCodeInspectionUnused.buildVisitor(problemsHolder, false);
        psiElementVisitor.visitElement(namespacePrefix);

        verify(problemsHolder, never()).registerProblem(eq(namespacePrefix), eq("Prefix is never used"), eq(ProblemHighlightType.LIKE_UNUSED_SYMBOL), any(LocalQuickFix.class));
    }

    @Test
    void testOMTNamespacePrefixRegistersNoWarningWhenNotAPrefix() {
        final OMTNamespacePrefix namespacePrefix = mock(OMTNamespacePrefix.class);
        doReturn(null).when(namespacePrefix).getParent();

        verifyNoWarning(namespacePrefix);
    }

    @Test
    void testOMTVariableRegistersNoWarningWhenNotADeclaredVariable() {
        final OMTVariable variable = mock(OMTVariable.class);
        doReturn(false).when(variable).isDeclaredVariable();

        verifyNoWarning(variable);
    }

    @Test
    void testOMTVariableRegistersNoWarningWhenIgnoredVariable() {
        final OMTVariable variable = mock(OMTVariable.class);
        doReturn(true).when(variable).isDeclaredVariable();
        doReturn(true).when(variable).isIgnoredVariable();

        verifyNoWarning(variable);
    }

    @Test
    void testOMTVariableRegistersNoWarningWhenUsedVariable() {
        final OMTVariable variable = mock(OMTVariable.class);
        doReturn(true).when(variable).isDeclaredVariable();
        doReturn(false).when(variable).isIgnoredVariable();
        setUnusedMock(variable, false);

        verifyNoWarning(variable);
    }

    @Test
    void testOMTVariableRegistersRenameIntentionWhenAdjacentVariable() {
        final PsiElement unusedVariable = getUnusedVariable();
        final OMTVariableAssignment variableAssignment = mock(OMTVariableAssignment.class);
        doReturn(variableAssignment).when(unusedVariable).getParent();

        setPsiTreeUtilMockWhenThenReturn(() -> PsiTreeUtil.getNextSiblingOfType(eq(unusedVariable), eq(OMTVariable.class)), mock(OMTVariable.class));
        ArgumentCaptor<LocalQuickFix> localQuickFixesArgumentCaptor = ArgumentCaptor.forClass(LocalQuickFix.class);

        visit(unusedVariable);

        verify(problemsHolder).registerProblem(
                eq(unusedVariable), anyString(), any(ProblemHighlightType.class), localQuickFixesArgumentCaptor.capture()
        );
        final LocalQuickFix localQuickFix = localQuickFixesArgumentCaptor.getValue();
        assertEquals("Rename", localQuickFix.getFamilyName());

    }

    @Test
    void testOMTVariableRegistersRemoveIntentionWhenNoAdjacentVariable() {
        final PsiElement unusedVariable = getUnusedVariable();
        final OMTVariableAssignment variableAssignment = mock(OMTVariableAssignment.class);
        doReturn(variableAssignment).when(unusedVariable).getParent();

        setPsiTreeUtilMockWhenThenReturn(() -> PsiTreeUtil.getNextSiblingOfType(eq(unusedVariable), eq(OMTVariable.class)), null);
        ArgumentCaptor<LocalQuickFix> localQuickFixesArgumentCaptor = ArgumentCaptor.forClass(LocalQuickFix.class);

        visit(unusedVariable);

        verify(problemsHolder).registerProblem(
                eq(unusedVariable), anyString(), any(ProblemHighlightType.class), localQuickFixesArgumentCaptor.capture()
        );
        final LocalQuickFix localQuickFix = localQuickFixesArgumentCaptor.getValue();
        assertEquals("Remove", localQuickFix.getFamilyName());
    }

    private PsiElement getUnusedVariable() {
        final OMTVariable variable = mock(OMTVariable.class);
        doReturn(true).when(variable).isDeclaredVariable();
        doReturn(false).when(variable).isIgnoredVariable();
        setUnusedMock(variable, true);
        return variable;
    }

    void visit(PsiElement element) {
        final PsiElementVisitor psiElementVisitor = omtCodeInspectionUnused.buildVisitor(problemsHolder, false);
        psiElementVisitor.visitElement(element);
    }

    void verifyNoWarning(PsiElement element) {
        final PsiElementVisitor psiElementVisitor = omtCodeInspectionUnused.buildVisitor(problemsHolder, false);
        psiElementVisitor.visitElement(element);
        verify(problemsHolder, never()).registerProblem(any(), anyString(), any(), any());
    }

    void setUnusedMock(PsiElement element, boolean unused) {
        setSearchReferenceMock(element, query -> doReturn(!unused).when(query).anyMatch(any()));
    }

}
