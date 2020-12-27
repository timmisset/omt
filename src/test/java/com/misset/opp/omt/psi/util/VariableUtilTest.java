package com.misset.opp.omt.psi.util;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.ExampleFiles;
import com.misset.opp.omt.psi.OMTGenericBlock;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.impl.OMTBuiltInMember;
import com.misset.opp.omt.psi.support.BuiltInType;
import com.misset.opp.omt.util.BuiltInUtil;
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
import static org.mockito.Mockito.doReturn;

class VariableUtilTest extends OMTTestSuite {

    @Mock
    BuiltInUtil builtInUtil;

    @InjectMocks
    VariableUtil variableUtil;

    @Mock
    AnnotationHolder annotationHolder;
    @Mock
    AnnotationBuilder annotationBuilder;
    PsiElement rootBlock;
    OMTGenericBlock onStartBlock;
    private ExampleFiles exampleFiles;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setName("VariableUtilTest");
        super.setUp();

        MockitoAnnotations.openMocks(this);

        setUtilMock(builtInUtil);

        exampleFiles = new ExampleFiles(this, myFixture);
        rootBlock = exampleFiles.getActivityWithVariables();

        ApplicationManager.getApplication().runReadAction(() -> {
            onStartBlock = exampleFiles.getPsiElementFromRootDocument(
                    OMTGenericBlock.class, rootBlock,
                    genericBlock ->
                            genericBlock.getPropertyLabel().getName().equals("onStart")
            );
        });
        doReturn(annotationBuilder).when(annotationHolder).newAnnotation(any(), anyString());
        doReturn(annotationBuilder).when(annotationBuilder).range(any(PsiElement.class));
        doReturn(annotationBuilder).when(annotationBuilder).tooltip(anyString());
        doReturn(annotationBuilder).when(annotationBuilder).withFix(any(IntentionAction.class));
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
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
        OMTBuiltInMember forEachCommand = new OMTBuiltInMember("FOREACH", new ArrayList<>(), BuiltInType.Command, Arrays.asList("$value"));
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
