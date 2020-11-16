package com.misset.opp.omt.psi.intentions.prefix;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.util.CurieUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class RegisterPrefixIntentionTest {

    private final String IRI = "http://ontologie/";

    @Mock
    CurieUtil curieUtil;

    @Mock
    OMTNamespacePrefix omtNamespacePrefix;

    @InjectMocks
    RegisterPrefixIntention registerPrefixIntention;
    IntentionAction intentionAction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        intentionAction = this.registerPrefixIntention.getRegisterPrefixIntention(omtNamespacePrefix, IRI);
    }

    @Test
    void getText() {
        assertEquals("Register as http://ontologie/", intentionAction.getText());
    }

    @Test
    void getFamilyName() {
        assertEquals("Prefixes", intentionAction.getFamilyName());
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
    void invoke() {
        doReturn("ont").when(omtNamespacePrefix).getName();
        intentionAction.invoke(mock(Project.class),
                mock(Editor.class),
                mock(PsiFile.class));
        verify(curieUtil).addPrefixToBlock(omtNamespacePrefix, "ont", IRI);
    }

    @Test
    void startInWriteAction() {
        assertTrue(intentionAction.startInWriteAction());
    }
}
