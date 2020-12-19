package com.misset.opp.omt.psi.intentions.prefix;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.util.CurieUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class RegisterPrefixIntentionTest extends OMTTestSuite {

    private final String IRI = "http://ontologie/";

    @Mock
    CurieUtil curieUtil;

    @Mock
    OMTNamespacePrefix omtNamespacePrefix;

    IntentionAction intentionAction;

    @BeforeEach
    @Override
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        setUtilMock(curieUtil);
        intentionAction = RegisterPrefixIntention.getRegisterPrefixIntention(omtNamespacePrefix, IRI);
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
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
