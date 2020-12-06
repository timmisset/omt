package com.misset.opp.omt.psi.intentions.imports;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTImportSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.misset.opp.omt.psi.intentions.imports.UnwrapIntention.getUnwrapIntention;
import static org.mockito.Mockito.*;

class UnwrapIntentionTest extends OMTTestSuite {

    OMTImportSource importSource;
    IntentionAction unwrapIntention;

    @BeforeEach
    protected void setUp() {
        importSource = mock(OMTImportSource.class);
        unwrapIntention = getUnwrapIntention(importSource);
    }

    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void getText() {
        assertEquals("Unwrap", unwrapIntention.getText());
    }

    @Test
    void getFamilyName() {
        assertEquals("Import source", unwrapIntention.getFamilyName());
    }

    @Test
    void isAvailable() {
        assertTrue(unwrapIntention.isAvailable(mock(Project.class), mock(Editor.class), mock(PsiFile.class)));
    }

    @Test
    void invoke() {
        doReturn("'file.omt':").when(importSource).getName();
        unwrapIntention.invoke(mock(Project.class), mock(Editor.class), mock(PsiFile.class));
        verify(importSource).setName("file.omt:");
    }

    @Test
    void startInWriteAction() {
        assertTrue(unwrapIntention.startInWriteAction());
    }
}
