package com.misset.opp.omt.psi.intentions.generic;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.util.CurieUtil;
import com.misset.opp.omt.psi.util.ImportUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class RemoveIntentionTest {

    @Mock
    CurieUtil curieUtil;
    @Mock
    ImportUtil importUtil;

    @InjectMocks
    RemoveIntention removeIntention;

    IntentionAction intentionAction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        intentionAction = this.removeIntention.getRemoveIntention(mock(PsiElement.class));
    }

    @Test
    void getRemoveIntentionNoTitle() {
        assertEquals("Remove", intentionAction.getText());
    }

    @Test
    void getRemoveIntentionText() {
        assertEquals("Remove item", removeIntention.getRemoveIntention(
                mock(PsiElement.class), "Remove item"
        ).getText());
    }

    @Test
    void getFamilyName() {
        assertEquals("Remove", intentionAction.getFamilyName());
    }

    @Test
    void isAvailable() {
        assertTrue(intentionAction.isAvailable(
                mock(Project.class),
                mock(Editor.class),
                mock(PsiFile.class)
        ));
    }

    @Test
    void startInWriteAction() {
        assertTrue(intentionAction.startInWriteAction());
    }

    @Test
    void invokeOMTNamespacePrefix() {
        final OMTNamespacePrefix namespacePrefix = mock(OMTNamespacePrefix.class);
        final OMTPrefix prefix = mock(OMTPrefix.class);
        final PsiFile psiFile = mock(PsiFile.class);
        doReturn(prefix).when(namespacePrefix).getParent();
        doReturn(psiFile).when(namespacePrefix).getContainingFile();

        intentionAction = removeIntention.getRemoveIntention(namespacePrefix);
        intentionAction.invoke(
                mock(Project.class),
                mock(Editor.class),
                mock(PsiFile.class));
        verify(prefix).delete();
    }

    @Test
    void invokeOMTNamespacePrefixNotPrefix() {
        final OMTNamespacePrefix namespacePrefix = mock(OMTNamespacePrefix.class);
        final PsiElement parent = mock(PsiElement.class);
        final PsiFile psiFile = mock(PsiFile.class);
        doReturn(parent).when(namespacePrefix).getParent();
        doReturn(psiFile).when(namespacePrefix).getContainingFile();

        intentionAction = removeIntention.getRemoveIntention(namespacePrefix);
        intentionAction.invoke(
                mock(Project.class),
                mock(Editor.class),
                mock(PsiFile.class));
        verify(parent, times(0)).delete();
    }

    @Test
    void invokeOMTMemberListItem() {
        final OMTMemberListItem memberListItem = mock(OMTMemberListItem.class);
        final PsiFile psiFile = mock(PsiFile.class);
        doReturn(psiFile).when(memberListItem).getContainingFile();

        intentionAction = removeIntention.getRemoveIntention(memberListItem);
        intentionAction.invoke(
                mock(Project.class),
                mock(Editor.class),
                mock(PsiFile.class));
        verify(memberListItem).delete();
    }

    @Test
    void invokeOMTVariable() {
        final OMTSequenceItem sequenceItem = mock(OMTSequenceItem.class);
        final OMTVariable variable = mock(OMTVariable.class);

        try (MockedStatic mocked = mockStatic(PsiTreeUtil.class)) {
            List<OMTSequenceItem> list = new ArrayList<>();
            list.add(sequenceItem);

            mocked.when(() -> PsiTreeUtil.collectParents(
                    eq(variable), eq(OMTSequenceItem.class), eq(false), any()
            )).thenReturn(list);

            intentionAction = removeIntention.getRemoveIntention(variable);
            intentionAction.invoke(
                    mock(Project.class),
                    mock(Editor.class),
                    mock(PsiFile.class));
            verify(sequenceItem).delete();
        }
    }

    @Test
    void invokeOMTVariableReturnsMoreThan1() {
        final OMTSequenceItem sequenceItem = mock(OMTSequenceItem.class);
        final OMTVariable variable = mock(OMTVariable.class);

        try (MockedStatic mocked = mockStatic(PsiTreeUtil.class)) {
            List<OMTSequenceItem> list = new ArrayList<>();
            list.add(sequenceItem);
            list.add(sequenceItem);

            mocked.when(() -> PsiTreeUtil.collectParents(
                    eq(variable), eq(OMTSequenceItem.class), eq(false), any()
            )).thenReturn(list);

            intentionAction = removeIntention.getRemoveIntention(variable);
            intentionAction.invoke(
                    mock(Project.class),
                    mock(Editor.class),
                    mock(PsiFile.class));
            verify(sequenceItem, times(0)).delete();
        }
    }
}
