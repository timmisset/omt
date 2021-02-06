package com.misset.opp.omt.psi.util;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTReturnStatement;
import com.misset.opp.omt.psi.OMTScript;
import com.misset.opp.omt.psi.OMTVariable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ScriptUtilTest extends OMTTestSuite {

    @Mock
    AnnotationHolder annotationHolder;
    @Mock
    AnnotationBuilder annotationBuilder;

    @InjectMocks
    ScriptUtil scriptUtil;


    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setName("ModelUtilTest");
        super.setUp();

        MockitoAnnotations.openMocks(this);
        setExampleFileProcedureWithScript();
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
    void getScript() {
        ReadAction.run(() -> {
            OMTVariable variable = getElement(OMTVariable.class);
            Optional<OMTScript> script = scriptUtil.getScript(variable);
            assertTrue(script.isPresent());
        });
    }

    @Test
    void getAccessibleElements_ReturnsAccessibleElements() {
        ReadAction.run(() -> {
            OMTVariable variableA = getElement(OMTVariable.class, omtVariable -> omtVariable.getName().equals("$variableA"));
            OMTVariable variableB = getElement(OMTVariable.class, omtVariable -> omtVariable.getName().equals("$variableB"));
            OMTVariable variableC = getElement(OMTVariable.class, omtVariable -> omtVariable.getName().equals("$variableC"));
            List<OMTVariable> accessibleElements = scriptUtil.getAccessibleElements(variableC, OMTVariable.class);
            assertNotNull(variableA);
            assertNotNull(variableB);
            assertNotNull(variableC);
            assertTrue(accessibleElements.contains(variableA));
            assertTrue(accessibleElements.contains(variableB));
        });
    }


    @Test
    void getAccessibleElements_DoesNotReturnInAccessibleElements() {
        ReadAction.run(() -> {
            OMTVariable variableC = getElement(OMTVariable.class, omtVariable -> omtVariable.getName().equals("$variableC"));
            OMTVariable variableD = getElement(OMTVariable.class, omtVariable -> omtVariable.getName().equals("$variableD"));
            assertNotNull(variableC);
            assertNotNull(variableD);
            List<OMTVariable> accessibleElements = scriptUtil.getAccessibleElements(variableC, OMTVariable.class);
            assertFalse(accessibleElements.contains(variableD));
        });
    }

    @Test
    void isBeforeReturnsTrue() {
        ReadAction.run(() -> {
            OMTVariable variableADeclared =
                    getElement(OMTVariable.class, omtVariable ->
                            omtVariable.getName().equals("$variableA") &&
                                    omtVariable.isDeclaredVariable()
                    );
            OMTVariable variableAUsed = getElement(OMTVariable.class, omtVariable ->
                    omtVariable.getName().equals("$variableA") &&
                            !omtVariable.isDeclaredVariable()
            );
            assertNotNull(variableADeclared);
            assertNotNull(variableAUsed);
            assertTrue(scriptUtil.isBefore(variableADeclared, variableAUsed));
        });
    }

    @Test
    void isBeforeReturnsTrueForDifferentLevels() {
        ReadAction.run(() -> {
            OMTVariable variableA =
                    getElement(OMTVariable.class, omtVariable ->
                            omtVariable.getName().equals("$variableA")
                    );
            OMTVariable variableC = getElement(OMTVariable.class, omtVariable ->
                    omtVariable.getName().equals("$variableC")
            );
            assertNotNull(variableA);
            assertNotNull(variableC);
            assertTrue(scriptUtil.isBefore(variableA, variableC));
        });
    }


    @Test
    void isBeforeReturnsFalseForDifferentLevels() {
        ReadAction.run(() -> {
            OMTVariable variableA =
                    getElement(OMTVariable.class, omtVariable ->
                            omtVariable.getName().equals("$variableA")
                    );
            OMTVariable variableC = getElement(OMTVariable.class, omtVariable ->
                    omtVariable.getName().equals("$variableC")
            );
            assertNotNull(variableA);
            assertNotNull(variableC);
            assertFalse(scriptUtil.isBefore(variableC, variableA));
        });
    }

    @Test
    void isBeforeReturnsFalse() {
        ReadAction.run(() -> {
            OMTVariable variableBDeclared =
                    getElement(OMTVariable.class, omtVariable ->
                            omtVariable.getName().equals("$variableB") &&
                                    omtVariable.isDeclaredVariable()
                    );
            OMTVariable variableBUsed = getElement(OMTVariable.class, omtVariable ->
                    omtVariable.getName().equals("$variableB") &&
                            !omtVariable.isDeclaredVariable()
            );
            assertNotNull(variableBDeclared);
            assertNotNull(variableBUsed);
            assertFalse(scriptUtil.isBefore(variableBDeclared, variableBUsed));
        });
    }

    @Test
    void annotateFinalStatement() {
        ReadAction.run(() -> {
            OMTReturnStatement returnStatement = getElement(OMTReturnStatement.class);
            scriptUtil.annotateFinalStatement(returnStatement, annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.ERROR), eq("Unreachable code"));
            verify(annotationBuilder, times(1)).create();
        });
    }
}
