package com.misset.opp.omt.psi.util;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.external.util.builtIn.BuiltInMember;
import com.misset.opp.omt.external.util.builtIn.BuiltInType;
import com.misset.opp.omt.external.util.builtIn.BuiltInUtil;
import com.misset.opp.omt.psi.ExampleFiles;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTVariable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VariableUtilTest extends LightJavaCodeInsightFixtureTestCase {

    @Mock
    BuiltInUtil builtInUtil;

    @Mock
    AnnotationUtil annotationUtil;

    @InjectMocks
    VariableUtil variableUtil;
    @Mock
    AnnotationHolder annotationHolder;
    @Mock
    AnnotationBuilder annotationBuilder;
    PsiElement rootBlock;
    OMTBlockEntry onStartBlock;
    private ExampleFiles exampleFiles;

    @BeforeEach
    void setUpSuite() throws Exception {
        super.setName("MemberUtilTest");
        super.setUp();

        MockitoAnnotations.initMocks(this);
        exampleFiles = new ExampleFiles(this);

        ApplicationManager.getApplication().runReadAction(() -> {
            rootBlock = exampleFiles.getActivityWithVariables();
            onStartBlock = exampleFiles.getPsiElementFromRootDocument(
                    OMTBlockEntry.class, rootBlock,
                    omtBlockEntry ->
                            omtBlockEntry.getPropertyLabel() != null &&
                                    omtBlockEntry.getPropertyLabel().getPropertyLabelName().equals("onStart")
            );
        });
        doReturn(annotationBuilder).when(annotationHolder).newAnnotation(any(), anyString());
        doReturn(annotationBuilder).when(annotationBuilder).range(any(PsiElement.class));
        doReturn(annotationBuilder).when(annotationBuilder).tooltip(anyString());
        doReturn(annotationBuilder).when(annotationBuilder).withFix(any(IntentionAction.class));
    }

    @AfterEach
    void tearDownSuite() throws Exception {
        super.tearDown();
    }

    @Test
    void getFirstAppearance() {
        ApplicationManager.getApplication().runReadAction(() -> {
            List<OMTVariable> variableList = exampleFiles.getPsiElementsFromRootDocument(OMTVariable.class, onStartBlock,
                    omtVariable -> omtVariable.getName().equals("$variableA"));
            assertEquals(2, variableList.size());
            OMTVariable firstInstance = variableList.get(0);
            OMTVariable secondInstance = variableList.get(1);
            assertEquals(firstInstance, variableUtil.getFirstAppearance(firstInstance, onStartBlock).get());
            assertEquals(firstInstance, variableUtil.getFirstAppearance(secondInstance, onStartBlock).get());
        });
    }

    @Test
    void getDeclaredVariables() {
        ApplicationManager.getApplication().runReadAction(() -> {
            List<OMTVariable> variableList = exampleFiles.getPsiElementsFromRootDocument(OMTVariable.class, onStartBlock,
                    omtVariable -> omtVariable.getName().equals("$myDeclaredVariable"));
            assertEquals(2, variableList.size());
            OMTVariable firstInstance = variableList.get(0);
            OMTVariable secondInstance = variableList.get(1);
            assertContainsElements(variableUtil.getDeclaredVariables(secondInstance), firstInstance);
        });
    }

    @Test
    void getDeclaredByVariable() {
        ApplicationManager.getApplication().runReadAction(() -> {
            List<OMTVariable> variableList = exampleFiles.getPsiElementsFromRootDocument(OMTVariable.class, onStartBlock,
                    omtVariable -> omtVariable.getName().equals("$myDeclaredVariable"));
            assertEquals(2, variableList.size());
            OMTVariable declared = variableList.get(0);
            OMTVariable usage = variableList.get(1);
            assertEquals(declared, variableUtil.getDeclaredByVariable(usage).get());
        });
    }

    @Test
    void annotateVariable_GlobalVariable() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable globalVariable = exampleFiles.getPsiElementFromRootDocument(
                    OMTVariable.class, rootBlock, omtVariable -> omtVariable.getName().equals("$username")
            );
            variableUtil.annotateVariable(globalVariable, annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.INFORMATION), eq("$username is a global variable which is always available"));
            verify(annotationBuilder, times(1)).create();
            verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.ERROR), anyString());
        });
    }

    @Test
    void annotateVariable_IgnoredVariable() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable globalVariable = exampleFiles.getPsiElementFromRootDocument(
                    OMTVariable.class, rootBlock, omtVariable -> omtVariable.getName().equals("$_")
            );
            variableUtil.annotateVariable(globalVariable, annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.INFORMATION), eq("$_ is used to indicate the variable ignored"));
            verify(annotationBuilder, times(1)).create();
            verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.ERROR), anyString());
        });
    }

    @Test
    void annotateVariable_DeclaredVariable() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable declaredVariable = exampleFiles.getPsiElementFromRootDocument(
                    OMTVariable.class, rootBlock, omtVariable -> omtVariable.getName().equals("$myDeclaredVariable")
            );
            variableUtil.annotateVariable(declaredVariable, annotationHolder);
            verify(annotationUtil, times(1)).annotateUsageGetBuilder(eq(declaredVariable), eq(OMTVariable.class), eq(annotationHolder));
        });
    }

    @Test
    void annotateVariable_LocalVariable() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable declaredVariable = exampleFiles.getPsiElementFromRootDocument(
                    OMTVariable.class, rootBlock, omtVariable -> omtVariable.getName().equals("$newValue")
            );
            variableUtil.annotateVariable(declaredVariable, annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.INFORMATION), eq("$newValue is locally available in onChange"));
            verify(annotationBuilder, times(1)).create();
            verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.ERROR), anyString());
        });
    }

    @Test
    void annotateVariable_ThrowsNotDeclared() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable declaredVariable = exampleFiles.getPsiElementFromRootDocument(
                    OMTVariable.class, rootBlock, omtVariable -> omtVariable.getName().equals("$value")
            );
            variableUtil.annotateVariable(declaredVariable, annotationHolder);
            verify(annotationHolder, times(1)).newAnnotation(eq(HighlightSeverity.ERROR), eq("$value is not declared"));
        });
    }

    @Test
    void getLocalVariables() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable localVariable = exampleFiles.getPsiElementFromRootDocument(
                    OMTVariable.class, rootBlock, omtVariable -> omtVariable.getName().equals("$newValue")
            );
            assertTrue(variableUtil.getLocalVariables(localVariable).containsKey("$newValue"));
        });
    }

    @Test
    void getLocalVariablesForBuiltInCommands() {
        BuiltInMember forEachCommand = new BuiltInMember("FOREACH", new ArrayList<>(), BuiltInType.Command, Arrays.asList("$value"));
        doReturn(forEachCommand).when(builtInUtil).getBuiltInMember(eq("FOREACH"), eq(BuiltInType.Command));
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable localVariable = exampleFiles.getPsiElementFromRootDocument(
                    OMTVariable.class, rootBlock, omtVariable -> omtVariable.getName().equals("$value")
            );
            assertTrue(variableUtil.getLocalVariables(localVariable).containsKey("$value"));
        });
    }

    @Test
    void isDeclaredVariable_DeclaredInScript() {
        ApplicationManager.getApplication().runReadAction(() -> {
            List<OMTVariable> variableList = exampleFiles.getPsiElementsFromRootDocument(OMTVariable.class, onStartBlock,
                    omtVariable -> omtVariable.getName().equals("$myDeclaredVariable"));
            assertEquals(2, variableList.size());
            OMTVariable declared = variableList.get(0);
            OMTVariable usage = variableList.get(1);
            assertTrue(variableUtil.isDeclaredVariable(declared));
            assertFalse(variableUtil.isDeclaredVariable(usage));
        });

    }

    @Test
    void isDeclaredVariable_DeclaredParamWithType() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable variable = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock,
                    omtVariable -> omtVariable.getName().equals("$paramWithType"));
            assertTrue(variableUtil.isDeclaredVariable(variable));
        });
    }

    @Test
    void isDeclaredVariable_DeclaredBindingsVariable() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable variable = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock,
                    omtVariable -> omtVariable.getName().equals("$bindingsVariable"));
            assertTrue(variableUtil.isDeclaredVariable(variable));
        });
    }

    @Test
    void isDeclaredVariable_DeclaredBaseVariable() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable variable = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock,
                    omtVariable -> omtVariable.getName().equals("$baseVariable"));
            assertTrue(variableUtil.isDeclaredVariable(variable));
        });
    }

    @Test
    void isDeclaredVariable_DeclaredVariablesVariable() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable variable = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock,
                    omtVariable -> omtVariable.getName().equals("$declaredVariablesVariable"));
            assertTrue(variableUtil.isDeclaredVariable(variable));
        });
    }
}
