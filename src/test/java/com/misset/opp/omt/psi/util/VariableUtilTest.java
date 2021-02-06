package com.misset.opp.omt.psi.util;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.OMTTestSuite;
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
    OMTGenericBlock onStartBlock;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setName("VariableUtilTest");
        super.setUp();

        MockitoAnnotations.openMocks(this);

        setUtilMock(builtInUtil);

        setExampleFileActivityWithVariables();

        ReadAction.run(() -> {
            onStartBlock = getElement(
                    OMTGenericBlock.class,
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
        ReadAction.run(() -> {
            List<OMTVariable> variableList = getElements(onStartBlock, OMTVariable.class,
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
        ReadAction.run(() -> {
            List<OMTVariable> variableList = getElements(OMTVariable.class,
                    omtVariable -> omtVariable.getName().equals("$myDeclaredVariable"));
            assertEquals(2, variableList.size());
            OMTVariable firstInstance = variableList.get(0);
            OMTVariable secondInstance = variableList.get(1);
            assertContainsElements(variableUtil.getDeclaredVariables(secondInstance), firstInstance);
        });
    }

    @Test
    void getDeclaredByVariable() {
        ReadAction.run(() -> {
            List<OMTVariable> variableList = getElements(OMTVariable.class,
                    omtVariable -> omtVariable.getName().equals("$myDeclaredVariable"));
            assertEquals(2, variableList.size());
            OMTVariable declared = variableList.get(0);
            OMTVariable usage = variableList.get(1);
            assertEquals(declared, variableUtil.getDeclaredByVariable(usage).get());
        });
    }

    @Test
    void getLocalVariables() {
        ReadAction.run(() -> {
            OMTVariable localVariable = getElement(
                    OMTVariable.class, omtVariable -> omtVariable.getName().equals("$newValue")
            );
            assertTrue(variableUtil.getLocalVariables(localVariable).containsKey("$newValue"));
        });
    }

    @Test
    void getLocalVariablesForBuiltInCommands() {
        ReadAction.run(() -> {
            OMTBuiltInMember forEachCommand = new OMTBuiltInMember("FOREACH", new ArrayList<>(), BuiltInType.Command, Arrays.asList("$value"));
            doReturn(forEachCommand).when(builtInUtil).getBuiltInMember(eq("FOREACH"), eq(BuiltInType.Command));
            OMTVariable localVariable = getElement(
                    OMTVariable.class, omtVariable -> omtVariable.getName().equals("$value")
            );
            assertTrue(variableUtil.getLocalVariables(localVariable).containsKey("$value"));
        });
    }

    @Test
    void isDeclaredVariable_DeclaredInScript() {
        ReadAction.run(() -> {
            List<OMTVariable> variableList = getElements(OMTVariable.class,
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
        ReadAction.run(() -> {
            OMTVariable variable = getElement(OMTVariable.class,
                    omtVariable -> omtVariable.getName().equals("$paramWithType"));
            assertTrue(variableUtil.isDeclaredVariable(variable));
        });
    }

    @Test
    void isDeclaredVariable_DeclaredBindingsVariable() {
        ReadAction.run(() -> {
            OMTVariable variable = getElement(OMTVariable.class,
                    omtVariable -> omtVariable.getName().equals("$bindingsVariable"));
            assertTrue(variableUtil.isDeclaredVariable(variable));
        });
    }

    @Test
    void isDeclaredVariable_DeclaredBaseVariable() {
        ReadAction.run(() -> {
            OMTVariable variable = getElement(OMTVariable.class,
                    omtVariable -> omtVariable.getName().equals("$baseVariable"));
            assertTrue(variableUtil.isDeclaredVariable(variable));
        });
    }

    @Test
    void isDeclaredVariable_DeclaredVariablesVariable() {
        ReadAction.run(() -> {
            OMTVariable variable = getElement(OMTVariable.class,
                    omtVariable -> omtVariable.getName().equals("$declaredVariablesVariable"));
            assertTrue(variableUtil.isDeclaredVariable(variable));
        });
    }

}
