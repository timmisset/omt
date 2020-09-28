package com.misset.opp.omt.psi.util;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.psi.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CurieUtilTest extends LightJavaCodeInsightFixtureTestCase {

    @Mock
    AnnotationHolder annotationHolder;
    @Mock
    AnnotationBuilder annotationBuilder;

    private static final CurieUtil curieUtil = CurieUtil.SINGLETON;
    private final ExampleFiles exampleFiles = new ExampleFiles(this);
    PsiElement rootBlock;

    private OMTNamespacePrefix abcDeclared;
    private OMTNamespacePrefix abcUsage;
    private OMTNamespacePrefix def;
    private OMTNamespacePrefix ghi;

    @BeforeEach
    void setUpSuite() throws Exception {
        super.setName("CurieUtilTest");
        super.setUp();

        MockitoAnnotations.initMocks(this);

        ApplicationManager.getApplication().runReadAction(() -> {
            rootBlock = exampleFiles.getActivityWithImportsPrefixesParamsVariablesGraphsPayload();
        });

        doReturn(annotationBuilder).when(annotationHolder).newAnnotation(any(), anyString());
        doReturn(annotationBuilder).when(annotationBuilder).range(any(PsiElement.class));
        doReturn(annotationBuilder).when(annotationBuilder).withFix(any(IntentionAction.class));
    }

    @AfterEach
    void tearDownSuite() throws Exception {
        super.tearDown();
    }

    @Test
    void testGetDefinedByPrefixForOMTParameterType() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTParameterType parameterType = exampleFiles.getPsiElementFromRootDocument(OMTParameterType.class, rootBlock);
            Optional<OMTPrefix> definedByPrefix = curieUtil.getDefinedByPrefix(parameterType);

            assertTrue(definedByPrefix.isPresent());
            assertEquals(definedByPrefix.get().getNamespacePrefix().getName(), parameterType.getNamespacePrefix().getName());
        });
    }

    @Test
    void testGetDefinedByPrefixForOMTCurieElement() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCurieElement curieElement = exampleFiles.getPsiElementFromRootDocument(OMTCurieElement.class, rootBlock);
            Optional<OMTPrefix> definedByPrefix = curieUtil.getDefinedByPrefix(curieElement);

            assertTrue(definedByPrefix.isPresent());
            assertEquals(definedByPrefix.get().getNamespacePrefix().getName(), curieElement.getNamespacePrefix().getName());
        });
    }

    @Test
    void testGetDefinedByPrefixOMTCurieElementReturnsEmpty() {
        assertEquals(Optional.empty(), curieUtil.getDefinedByPrefix(null, mock(OMTCurieElement.class)));
    }

    @Test
    void testGetDefinedByPrefixOMTParameterTypeReturnsEmpty() {
        assertEquals(Optional.empty(), curieUtil.getDefinedByPrefix(null, mock(OMTParameterType.class)));
    }

    @Test
    void getPrefixBlockReturnsExisting() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCurieElement curieElement = exampleFiles.getPsiElementFromRootDocument(OMTCurieElement.class, rootBlock);
            OMTPrefixBlock prefixBlock = exampleFiles.getPsiElementFromRootDocument(OMTPrefixBlock.class, rootBlock);
            OMTPrefixBlock returnedPrefixBlock = curieUtil.getPrefixBlock(curieElement);
            assertEquals(prefixBlock, returnedPrefixBlock);
        });
    }

    @Test
    void getPrefixBlockReturnsExistingWhenNew() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCurieElement curieElement = exampleFiles.getPsiElementFromRootDocument(OMTCurieElement.class, rootBlock);
            OMTPrefixBlock prefixBlock = exampleFiles.getPsiElementFromRootDocument(OMTPrefixBlock.class, rootBlock);
            OMTPrefixBlock returnedPrefixBlock = curieUtil.getPrefixBlock(curieElement, true);
            assertEquals(prefixBlock, returnedPrefixBlock);
        });
    }

    @Test
    void getPrefixBlockReturnsNull() {
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement activityWithoutPrefixBlock = exampleFiles.getActivityWithQueryWatcher();
            OMTVariable variable = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, activityWithoutPrefixBlock);
            OMTPrefixBlock returnedPrefixBlock = curieUtil.getPrefixBlock(variable, false);
            assertNull(returnedPrefixBlock);
        });
    }

    @Test
    void getPrefixBlockReturnsNewPrefixBlock() {
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement activityWithoutPrefixBlock = exampleFiles.getActivityWithQueryWatcher();
            OMTVariable variable = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, activityWithoutPrefixBlock);
            OMTPrefixBlock returnedPrefixBlock = curieUtil.getPrefixBlock(variable, true);
            assertNotNull(returnedPrefixBlock);
        });
    }

    @Test
    void annotateNamespacePrefixThrowsNotUsedAnnotation() {
        ApplicationManager.getApplication().runReadAction(() -> {
            loadUndeclaredNamespacePrefixes(exampleFiles.getActivityWithUndeclaredElements());
            curieUtil.annotateNamespacePrefix(def, annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.ERROR), eq("def: is never used"));
            verify(annotationBuilder, times(1)).create();
        });
    }

    @Test
    void annotateNamespacePrefixDoesNotThrowNotUsedAnnotation() {
        ApplicationManager.getApplication().runReadAction(() -> {
            loadUndeclaredNamespacePrefixes(exampleFiles.getActivityWithUndeclaredElements());
            curieUtil.annotateNamespacePrefix(abcDeclared, annotationHolder);
            verify(annotationBuilder, times(0)).create();
        });
    }

    @Test
    void annotateNamespacePrefixThrowsNotDeclaredAnnotation() {
        ApplicationManager.getApplication().runReadAction(() -> {
            loadUndeclaredNamespacePrefixes(exampleFiles.getActivityWithUndeclaredElements());
            curieUtil.annotateNamespacePrefix(ghi, annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.ERROR), eq("ghi: is not declared"));
            verify(annotationBuilder, times(1)).create();
        });
    }

    @Test
    void annotateNamespacePrefixDoesNotThrowNotDeclaredAnnotation() {
        ApplicationManager.getApplication().runReadAction(() -> {
            loadUndeclaredNamespacePrefixes(exampleFiles.getActivityWithUndeclaredElements());
            curieUtil.annotateNamespacePrefix(abcUsage, annotationHolder);
            verify(annotationBuilder, times(0)).create();
        });
    }

    private void loadUndeclaredNamespacePrefixes(PsiElement rootBlock) {
        List<OMTNamespacePrefix> prefixes = exampleFiles.getPsiElementsFromRootDocument(OMTNamespacePrefix.class, rootBlock);
        prefixes.forEach(prefix ->
                {
                    switch (Objects.requireNonNull(prefix.getName())) {
                        case "def:":
                            def = prefix;
                            break;
                        case "ghi:":
                            ghi = prefix;
                            break;
                        case "abc:":
                            if (abcDeclared == null) {
                                abcDeclared = prefix;
                            } else {
                                abcUsage = prefix;
                            }
                            break;
                    }
                }
        );
    }

}