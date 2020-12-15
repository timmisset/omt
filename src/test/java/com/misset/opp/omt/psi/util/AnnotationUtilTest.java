package com.misset.opp.omt.psi.util;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.ExampleFiles;
import com.misset.opp.omt.psi.OMTVariable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Objects;

import static com.misset.opp.omt.psi.util.UtilManager.getAnnotationUtil;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AnnotationUtilTest extends OMTTestSuite {

    private ExampleFiles exampleFiles;
    @Mock
    AnnotationHolder annotationHolder;
    @Mock
    AnnotationBuilder annotationBuilder;
    PsiElement rootBlock;
    private OMTVariable unusedVariable;
    private OMTVariable usedVariableDeclaration;
    private OMTVariable undeclaredVariable;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setName("AnnotationUtilTest");
        super.setUp();

        MockitoAnnotations.initMocks(this);
        exampleFiles = new ExampleFiles(this, myFixture);
        rootBlock = exampleFiles.getActivityWithUndeclaredElements();
        ApplicationManager.getApplication().runReadAction(() -> {
            List<OMTVariable> variables = exampleFiles.getPsiElementsFromRootDocument(OMTVariable.class, rootBlock);
            variables.forEach(omtVariable ->
                    {
                        switch (Objects.requireNonNull(omtVariable.getName())) {
                            case "$undeclaredVariable":
                                undeclaredVariable = omtVariable;
                                break;
                            case "$unusedVariable":
                                unusedVariable = omtVariable;
                                break;
                            case "$usedVariable":
                                if (usedVariableDeclaration == null) {
                                    usedVariableDeclaration = omtVariable;
                                }
                                break;
                        }
                    }
            );
        });
        doReturn(annotationBuilder).when(annotationHolder).newAnnotation(any(), anyString());
        doReturn(annotationBuilder).when(annotationBuilder).range(any(PsiElement.class));
        doReturn(annotationBuilder).when(annotationBuilder).withFix(any(IntentionAction.class));
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void annotateUsageThrowsNotUsedAnnotation() {
        ApplicationManager.getApplication().runReadAction(() -> {
            try (MockedStatic<ReferencesSearch> searchMockedStatic = mockStatic(ReferencesSearch.class)) {
                final Query mock = mock(Query.class);
                doReturn(false).when(mock).anyMatch(any());
                searchMockedStatic.when(() -> ReferencesSearch.search(eq(unusedVariable))).thenReturn(mock);
                getAnnotationUtil().annotateUsage(unusedVariable, annotationHolder);
                verify(annotationHolder).newAnnotation(eq(HighlightSeverity.WARNING), eq("$unusedVariable is never used"));
                verify(annotationBuilder, times(1)).create();
            }
        });
    }

    @Test
    void annotateUsageDoesNotThrowUnusedAnnotation() {
        ApplicationManager.getApplication().runReadAction(() -> {
            try (MockedStatic<ReferencesSearch> searchMockedStatic = mockStatic(ReferencesSearch.class)) {
                final Query mock = mock(Query.class);
                doReturn(true).when(mock).anyMatch(any());
                searchMockedStatic.when(() -> ReferencesSearch.search(eq(unusedVariable))).thenReturn(mock);
                getAnnotationUtil().annotateUsage(unusedVariable, annotationHolder);
                verify(annotationBuilder, times(0)).create();
            }
        });
    }

    @Test
    void annotateOrigin() {
        ApplicationManager.getApplication().runReadAction(() -> {
            getAnnotationUtil().annotateOrigin(undeclaredVariable, annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.ERROR), eq("$undeclaredVariable is not declared"));
            verify(annotationBuilder, times(1)).create();
        });
    }
}
