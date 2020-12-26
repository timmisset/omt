package com.misset.opp.omt.annotations;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.OMTImportSource;
import com.misset.opp.omt.psi.OMTMember;
import com.misset.opp.omt.psi.util.MemberUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ImportAnnotatorTest extends OMTAnnotationTest {

    private static final String MEMBER_NAME = "Member";
    private static final String FILENAME = "file.omt";

    @Mock
    OMTMember member;

    @Mock
    OMTImportSource importSource;

    @Mock
    MemberUtil memberUtil;

    @Mock
    PsiReference reference;

    ImportAnnotator importAnnotator;

    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("ImportAnnotatorTest");
        super.setUp();
        MockitoAnnotations.openMocks(this);

        setUtilMock(memberUtil);
        doReturn(true).when(memberUtil).isImportedMember(member);

        doReturn(reference).when(member).getReference();
        doReturn(MEMBER_NAME).when(member).getName();
        doReturn(MEMBER_NAME).when(member).getText();
        doReturn(FILENAME).when(importSource).getText();
        doReturn(reference).when(importSource).getReference();
        importAnnotator = new ImportAnnotator(getHolder());
    }

    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void annotateImportNoAnnotationWhenInvalidType() {
        importAnnotator.annotate(mock(PsiElement.class));
        verifyNoAnnotations();
    }

    @Test
    void annotateImportNoAnnotationWhenNotAnImportedMemberInvalidType() {
        doReturn(false).when(memberUtil).isImportedMember(member);
        importAnnotator.annotate(member);
        verifyNoAnnotations();
    }

    @Test
    void annotateMemberThrowsErrorWhenItHasNoReference() {
        mockIsReferenced(member, true);

        importAnnotator.annotate(member);
        verify(getHolder()).newAnnotation(eq(HighlightSeverity.ERROR), eq(MEMBER_NAME + " is not an exported member"));
    }

    @Test
    void annotateMemberThrowsNoErrorWhenItHasAReference() {
        doReturn(mock(PsiElement.class)).when(reference).resolve();
        mockIsReferenced(member, true);

        importAnnotator.annotate(member);
        verifyNoErrors();
    }

    @Test
    void annotateMemberThrowsWarningWhenNotUsed() {
        mockIsReferenced(member, false);
        importAnnotator.annotate(member);
        verify(getHolder()).newAnnotation(eq(HighlightSeverity.WARNING), eq(MEMBER_NAME + " is never used"));
        verify(getBuilder()).withFix(any(IntentionAction.class));
    }

    @Test
    void importSourceThrowsErrorWhenItHasNoReference() {
        importAnnotator.annotate(importSource);
        verify(getHolder()).newAnnotation(eq(HighlightSeverity.ERROR), eq(FILENAME + " could not be resolved to a file"));
    }

    @Test
    void importSourceThrowsNoErrorWhenItHasAReference() {
        doReturn(mock(PsiFile.class)).when(reference).resolve();
        importAnnotator.annotate(importSource);
        verifyNoErrors();
    }

    @Test
    void importSourceThrowsWeakWarningWhenUnnecessaryWrapping() {
        doReturn("'./somePath'").when(importSource).getName();
        importAnnotator.annotate(importSource);
        verify(getHolder(), times(1)).newAnnotation(HighlightSeverity.WEAK_WARNING, "Unnecessary wrapping of import statement");
    }

    @Test
    void importSourceThrowsNoWeakWarningWhenNoUnnecessaryWrapping() {
        doReturn("./somePath").when(importSource).getName();
        importAnnotator.annotate(importSource);
        verifyNoWeakWarnings();
    }
}
