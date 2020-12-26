package com.misset.opp.omt.annotations;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.OMTTestSuite;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OMTAnnotationTest extends OMTTestSuite {

    AnnotationHolder holder;

    AnnotationBuilder builder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        holder = mock(AnnotationHolder.class);          // do not replace this with MockitoAnnotations setup, the inheriting class setup will overwrite them
        builder = mock(AnnotationBuilder.class);        // when they also use MockitoAnnotations and reset the returns to null
        doReturn(builder).when(holder).newAnnotation(any(HighlightSeverity.class), anyString());
        doReturn(builder).when(builder).withFix(any(IntentionAction.class));
        doReturn(builder).when(builder).range(any(PsiElement.class));
        doReturn(builder).when(builder).range(any(ASTNode.class));
        doReturn(builder).when(builder).range(any(TextRange.class));
        doReturn(builder).when(builder).tooltip(anyString());
    }

    protected AnnotationHolder getHolder() {
        return holder;
    }

    protected AnnotationBuilder getBuilder() {
        return builder;
    }

    /**
     * Mocks if this element is referenced to. Meaning any other PsiElement resolves its reference to this element
     */
    protected void mockIsReferenced(PsiElement element, boolean isReferenced) {
        setSearchReferenceMock(element, query ->
                doReturn(isReferenced).when(query).anyMatch(any())
        );
    }

    protected void verifyInfo(String info) {
        verify(holder).newAnnotation(eq(HighlightSeverity.INFORMATION), eq(info));
    }

    protected void verifyNoWeakWarnings() {
        verify(holder, times(0)).newAnnotation(eq(HighlightSeverity.WEAK_WARNING), anyString());
    }

    protected void verifyNoErrors() {
        verify(holder, times(0)).newAnnotation(eq(HighlightSeverity.ERROR), anyString());
    }

    protected void verifyError(String error) {
        verify(holder).newAnnotation(eq(HighlightSeverity.ERROR), eq(error));
    }

    protected void verifyNoWarnings() {
        verify(holder, times(0)).newAnnotation(eq(HighlightSeverity.WARNING), anyString());
    }

    protected void verifyNoAnnotations() {
        verify(holder, times(0)).newAnnotation(any(), anyString());
    }

    protected void verifyWarning(String warning) {
        verify(holder).newAnnotation(eq(HighlightSeverity.WARNING), eq(warning));
    }
}
