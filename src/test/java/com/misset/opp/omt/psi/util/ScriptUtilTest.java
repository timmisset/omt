package com.misset.opp.omt.psi.util;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.psi.ExampleFiles;
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

class ScriptUtilTest extends LightJavaCodeInsightFixtureTestCase {

    @Mock
    AnnotationHolder annotationHolder;
    @Mock
    AnnotationBuilder annotationBuilder;

    @InjectMocks
    ScriptUtil scriptUtil;

    PsiElement rootBlock;

    private ExampleFiles exampleFiles;

    @BeforeEach
    void setUpSuite() throws Exception {
        super.setName("ModelUtilTest");
        super.setUp();

        exampleFiles = new ExampleFiles(this, myFixture);
        MockitoAnnotations.initMocks(this);
        rootBlock = exampleFiles.getProcedureWithScript();
        doReturn(annotationBuilder).when(annotationHolder).newAnnotation(any(), anyString());
        doReturn(annotationBuilder).when(annotationBuilder).range(any(PsiElement.class));
        doReturn(annotationBuilder).when(annotationBuilder).withFix(any(IntentionAction.class));
    }

    @AfterEach
    void tearDownSuite() throws Exception {
        super.tearDown();
    }

    @Test
    void getScript() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable variable = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock);
            Optional<OMTScript> script = scriptUtil.getScript(variable);
            assertTrue(script.isPresent());
        });
    }

    @Test
    void getAccessibleElements_ReturnsAccessibleElements() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable variableA = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock, omtVariable -> omtVariable.getName().equals("$variableA"));
            OMTVariable variableB = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock, omtVariable -> omtVariable.getName().equals("$variableB"));
            OMTVariable variableC = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock, omtVariable -> omtVariable.getName().equals("$variableC"));
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
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable variableC = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock, omtVariable -> omtVariable.getName().equals("$variableC"));
            OMTVariable variableD = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock, omtVariable -> omtVariable.getName().equals("$variableD"));
            assertNotNull(variableC);
            assertNotNull(variableD);
            List<OMTVariable> accessibleElements = scriptUtil.getAccessibleElements(variableC, OMTVariable.class);
            assertFalse(accessibleElements.contains(variableD));
        });
    }

    @Test
    void isBeforeReturnsTrue() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable variableADeclared =
                    exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock, omtVariable ->
                            omtVariable.getName().equals("$variableA") &&
                                    omtVariable.isDeclaredVariable()
                    );
            OMTVariable variableAUsed = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock, omtVariable ->
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
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable variableA =
                    exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock, omtVariable ->
                            omtVariable.getName().equals("$variableA")
                    );
            OMTVariable variableC = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock, omtVariable ->
                    omtVariable.getName().equals("$variableC")
            );
            assertNotNull(variableA);
            assertNotNull(variableC);
            assertTrue(scriptUtil.isBefore(variableA, variableC));
        });
    }


    @Test
    void isBeforeReturnsFalseForDifferentLevels() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable variableA =
                    exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock, omtVariable ->
                            omtVariable.getName().equals("$variableA")
                    );
            OMTVariable variableC = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock, omtVariable ->
                    omtVariable.getName().equals("$variableC")
            );
            assertNotNull(variableA);
            assertNotNull(variableC);
            assertFalse(scriptUtil.isBefore(variableC, variableA));
        });
    }

    @Test
    void isBeforeReturnsFalse() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable variableBDeclared =
                    exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock, omtVariable ->
                            omtVariable.getName().equals("$variableB") &&
                                    omtVariable.isDeclaredVariable()
                    );
            OMTVariable variableBUsed = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock, omtVariable ->
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
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTReturnStatement returnStatement = exampleFiles.getPsiElementFromRootDocument(OMTReturnStatement.class, rootBlock);
            scriptUtil.annotateFinalStatement(returnStatement, annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.ERROR), eq("Unreachable code"));
            verify(annotationBuilder, times(1)).create();
        });
    }
}
