package com.misset.opp.omt.inspection.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTCommandCall;
import com.misset.opp.omt.psi.OMTDeclareVariable;
import com.misset.opp.omt.psi.OMTScriptLine;
import com.misset.opp.omt.psi.OMTVariableAssignment;
import com.misset.opp.omt.psi.OMTVariableValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RemoveVariableAssignmentTest extends OMTTestSuite {
    @Mock
    OMTVariableValue variableValue;

    @Mock
    OMTVariableAssignment variableAssignment;

    @Mock
    OMTDeclareVariable declareVariable;

    @Mock
    OMTCommandCall commandCall;

    @Mock
    OMTScriptLine scriptLine;

    @BeforeEach
    protected void setUp() {
        MockitoAnnotations.openMocks(this);

        doReturn(declareVariable).when(variableAssignment).getParent();
        doReturn(variableValue).when(variableAssignment).getVariableValue();
    }

    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void getRemoveQuickFixForVariableAssignmentKeepingCommand() {
        doReturn(commandCall).when(variableValue).getCommandCall();

        final LocalQuickFix removeQuickFix = Remove.getRemoveQuickFix(variableAssignment);

        assertEquals("Remove", removeQuickFix.getFamilyName());
        assertEquals("Remove variable (keep command)", removeQuickFix.getName());

        removeQuickFix.applyFix(mock(Project.class), mock(ProblemDescriptor.class));

        verify(declareVariable).replace(eq(commandCall));
    }

    @Test
    void getRemoveQuickFixForVariableAssignmentRemoveEntireLine() {
        setPsiTreeUtilMockWhenThenReturn(
                () -> PsiTreeUtil.getParentOfType(eq(declareVariable), eq(OMTScriptLine.class))
                , scriptLine);
        doReturn(null).when(variableValue).getCommandCall();

        final LocalQuickFix removeQuickFix = Remove.getRemoveQuickFix(variableAssignment);

        assertEquals("Remove", removeQuickFix.getFamilyName());
        assertEquals("Remove variable assignment", removeQuickFix.getName());

        removeQuickFix.applyFix(mock(Project.class), mock(ProblemDescriptor.class));

        verify(scriptLine).delete();
    }

    @Test
    void getRemoveQuickFixForVariableAssignmentRemoveDeclareVariableWhenNoScriptlineReturned() {
        setPsiTreeUtilMockWhenThenReturn(
                () -> PsiTreeUtil.getParentOfType(eq(declareVariable), eq(OMTScriptLine.class))
                , null);
        doReturn(null).when(variableValue).getCommandCall();

        final LocalQuickFix removeQuickFix = Remove.getRemoveQuickFix(variableAssignment);
        removeQuickFix.applyFix(mock(Project.class), mock(ProblemDescriptor.class));

        verify(declareVariable).delete();
    }

}
