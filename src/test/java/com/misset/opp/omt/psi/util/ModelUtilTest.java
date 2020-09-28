package com.misset.opp.omt.psi.util;

import com.google.gson.JsonObject;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ModelUtilTest extends LightJavaCodeInsightFixtureTestCase {

    @Mock
    AnnotationHolder annotationHolder;
    @Mock
    AnnotationBuilder annotationBuilder;

    OMTModelItemBlock activity;
    OMTImportBlock importBlock;
    OMTVariable variable;

    @InjectMocks
    ModelUtil modelUtil;

    PsiElement rootBlock;

    private ExampleFiles exampleFiles;

    @BeforeEach
    void setUpSuite() throws Exception {
        super.setName("ModelUtilTest");
        super.setUp();
        exampleFiles = new ExampleFiles(this);
        MockitoAnnotations.initMocks(this);

        ApplicationManager.getApplication().runReadAction(() -> {
            rootBlock = exampleFiles.getActivityWithImportsPrefixesParamsVariablesGraphsPayload();
            activity = exampleFiles.getPsiElementFromRootDocument(OMTModelItemBlock.class, rootBlock);
            importBlock = exampleFiles.getPsiElementFromRootDocument(OMTImportBlock.class, rootBlock);
            variable = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, rootBlock);
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
    void getModelItemBlock_ReturnsSelfIfOMTModelItemBlock() {
        ApplicationManager.getApplication().runReadAction(() -> {
            Optional<OMTModelItemBlock> optionalOMTModelItemBlock = modelUtil.getModelItemBlock(activity);
            assertTrue(optionalOMTModelItemBlock.isPresent());
            assertEquals(activity, optionalOMTModelItemBlock.get());
        });
    }

    @Test
    void getModelItemBlock_ReturnsEmpty() {
        ApplicationManager.getApplication().runReadAction(() -> {
            // import block is not contained by a modelitem block
            Optional<OMTModelItemBlock> optionalOMTModelItemBlock = modelUtil.getModelItemBlock(importBlock);
            assertFalse(optionalOMTModelItemBlock.isPresent());
        });
    }

    @Test
    void getModelItemBlock_ReturnsModelItemBlock() {
        ApplicationManager.getApplication().runReadAction(() -> {
            Optional<OMTModelItemBlock> optionalOMTModelItemBlock = modelUtil.getModelItemBlock(variable);
            assertTrue(optionalOMTModelItemBlock.isPresent());
            assertEquals(activity, optionalOMTModelItemBlock.get());
        });
    }

    @Test
    void getModelItemType_ReturnsNullWhenNoModelItemBlock() {
        ApplicationManager.getApplication().runReadAction(() -> {
            assertNull(modelUtil.getModelItemType(importBlock));
        });
    }

    @Test
    void getModelItemType_ReturnsTextWhenModelItemBlock() {
        ApplicationManager.getApplication().runReadAction(() -> {
            assertEquals("Activity", modelUtil.getModelItemType(variable));
        });
    }

    @Test
    void getModelItemBlockEntry_ReturnsEmptyWhenNoModelItemBlock() {
        ApplicationManager.getApplication().runReadAction(() -> {
            assertEquals(Optional.empty(), modelUtil.getModelItemBlockEntry(importBlock, "SUPERVARIABLES"));
        });
    }

    @Test
    void getModelItemBlockEntry_ReturnsEmptyWhenNoMatching() {
        ApplicationManager.getApplication().runReadAction(() -> {
            assertEquals(Optional.empty(), modelUtil.getModelItemBlockEntry(variable, "SUPERVARIABLES"));
        });
    }

    @Test
    void getModelItemBlockEntry_ReturnsMatching() {
        ApplicationManager.getApplication().runReadAction(() -> {
            Optional<OMTBlockEntry> variables = modelUtil.getModelItemBlockEntry(variable, "variables");
            assertTrue(variables.isPresent());
            assertEquals("variables", Objects.requireNonNull(variables.get().getPropertyLabel()).getPropertyLabelName());
        });
    }

    @Test
    void getConnectedEntries_Returns2Items() {
        ApplicationManager.getApplication().runReadAction(() -> {
            List<OMTBlockEntry> connectedEntries = modelUtil.getConnectedEntries(variable, Arrays.asList("variables", "params"));
            List<String> labels = connectedEntries.stream().map(omtBlockEntry ->
                    Objects.requireNonNull(omtBlockEntry.getPropertyLabel()).getPropertyLabelName())
                    .collect(Collectors.toList());
            assertEquals(2, labels.size());
            assertTrue(labels.contains("variables"));
            assertTrue(labels.contains("params"));
        });
    }


    @Test
    void getEntryBlockLabel_ReturnsEmptyStringWhenNull() {
        ApplicationManager.getApplication().runReadAction(() -> {
            // rootblock has no entryblock label and should return "" for label
            assertEquals("", modelUtil.getEntryBlockLabel(rootBlock));
        });
    }

    @Test
    void getEntryBlockLabel_ReturnsLabelWithoutColon() {
        ApplicationManager.getApplication().runReadAction(() -> {
            assertEquals("import", modelUtil.getEntryBlockLabel(importBlock));
        });
    }

    @Test
    void getModelItemEntryLabel() {
        ApplicationManager.getApplication().runReadAction(() -> {
            assertEquals("params", modelUtil.getModelItemEntryLabel(variable));
        });
    }

    @Test
    void annotateModelItem_StopsWhenBlockPartIsFound() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTModelItemBlock mockBlock = mock(OMTModelItemBlock.class);
            doReturn(null).when(mockBlock).getBlock();
            modelUtil.annotateModelItem(mockBlock, annotationHolder);
            verify(mockBlock, times(1)).getBlock();
        });
    }

    @Test
    void annotateModelItem_ThrowsUnknownModelType() {
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement modelWithWrongModelItemType = exampleFiles.getModelWithWrongModelItemType();
            OMTModelItemBlock modelItemBlock = exampleFiles.getPsiElementFromRootDocument(OMTModelItemBlock.class, modelWithWrongModelItemType);
            modelUtil.annotateModelItem(modelItemBlock, annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.ERROR), eq("Unknown model type: !WrongModelItemType"));
            verify(annotationBuilder, times(1)).create();
        });
    }

    @Test
    void annotateModelItem_ThrowsMissingAttributes() {
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement modelWithWrongModelItemType = exampleFiles.getStandaloneQueryWithMissingAttribute();
            OMTModelItemBlock modelItemBlock = exampleFiles.getPsiElementFromRootDocument(OMTModelItemBlock.class, modelWithWrongModelItemType);
            modelUtil.annotateModelItem(modelItemBlock, annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.ERROR), startsWith("myStandAloneQuery is missing attribute(s)"));
            verify(annotationBuilder, times(1)).create();
        });
    }

    @Test
    void annotateModelItem_ThrowsWrongAttributeErrorOnNestedAttribute() {
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement modelWithWrongNestedAttribute = exampleFiles.getActivityWithWrongNestedAttribute();
            OMTModelItemBlock modelItemBlock = exampleFiles.getPsiElementFromRootDocument(OMTModelItemBlock.class, modelWithWrongNestedAttribute);
            modelUtil.annotateModelItem(modelItemBlock, annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.ERROR), startsWith("queryX is not a known attribute for payloadC"));
            verify(annotationBuilder, times(1)).create();
        });
    }

    @Test
    void annotateModelItem() {
        ApplicationManager.getApplication().runReadAction(() -> {
            modelUtil.annotateModelItem(activity, annotationHolder);
        });
    }

    @Test
    void getLocalCommands_ContainsActivityCommands() {
        ApplicationManager.getApplication().runReadAction(() -> {
            List<String> localCommands = modelUtil.getLocalCommands(variable);
            assertTrue(localCommands.contains("COMMIT"));
            assertTrue(localCommands.contains("CANCEL"));
            assertTrue(localCommands.contains("DONE"));
            assertTrue(localCommands.contains("DRAFT"));
            assertTrue(localCommands.contains("ROLLBACK"));
        });
    }

    @Test
    void getLocalCommands_ContainsOntologyCommand() {
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement modelWithLoadOntology = exampleFiles.getLoadOntology();
            OMTCommandCall call = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, modelWithLoadOntology);

            List<String> localCommands = modelUtil.getLocalCommands(call);
            assertTrue(localCommands.contains("LOAD_ONTOLOGY"));

            assertTrue(localCommands.contains("COMMIT"));
            assertTrue(localCommands.contains("CANCEL"));
            assertTrue(localCommands.contains("DONE"));
            assertTrue(localCommands.contains("DRAFT"));
            assertTrue(localCommands.contains("ROLLBACK"));
        });
    }

    @Test
    void getJson_returnsJsonForSequenceItem() {
        ApplicationManager.getApplication().runReadAction(() -> {
            JsonObject json = modelUtil.getJson(variable);
            assertEquals("Param", json.get("name").getAsString());
        });
    }

    @Test
    void getJson_returnsJsonForMapItem() {
        ApplicationManager.getApplication().runReadAction(() -> {
            List<OMTVariable> variables = exampleFiles.getPsiElementsFromRootDocument(OMTVariable.class, rootBlock);
            // second to last variable is the the $variableA usage in the payload
            OMTVariable payloadVariable = variables.get(variables.size() - 2);
            JsonObject json = modelUtil.getJson(payloadVariable);
            assertEquals("PayloadProperty", json.get("name").getAsString());
        });
    }

    @Test
    void getJson_returnsJsonForMapOfItemScalarValue() {
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement activityWithVariablesActions = exampleFiles.getActivityWithVariablesActions();
            List<OMTVariable> variables = exampleFiles.getPsiElementsFromRootDocument(OMTVariable.class, activityWithVariablesActions);
            // last variable is in the action
            OMTVariable payloadVariable = variables.get(variables.size() - 1);
            JsonObject json = modelUtil.getJson(payloadVariable);
            assertEquals("scalar", json.get("node").getAsString());
        });
    }

    @Test
    void getJson_returnsNamedReferenceProperty() {
        ApplicationManager.getApplication().runReadAction(() -> {
            List<OMTOperatorCall> operatorCalls = exampleFiles.getPsiElementsFromRootDocument(OMTOperatorCall.class, rootBlock);
            // last variable is in the action
            OMTOperatorCall operatorCall = operatorCalls.get(operatorCalls.size() - 1);
            JsonObject json = modelUtil.getJson(operatorCall);
            assertTrue(json.get("namedReference").getAsBoolean());
        });
    }

    @Test
    void getJsonAttributes_returnsJsonAttributesForMapOfItemScalarValue() {
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement activityWithVariablesActions = exampleFiles.getActivityWithVariablesActions();
            List<OMTVariable> variables = exampleFiles.getPsiElementsFromRootDocument(OMTVariable.class, activityWithVariablesActions);
            // last variable is in the action
            OMTVariable payloadVariable = variables.get(variables.size() - 1);
            JsonObject json = modelUtil.getJsonAttributes(payloadVariable);
            assertEquals("ActionProperty", json.get("name").getAsString());
        });
    }

    @Test
    void isOntology() {
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement loadOntology = exampleFiles.getLoadOntology();
            List<OMTModelItemBlock> modelBlocks = exampleFiles.getPsiElementsFromRootDocument(OMTModelItemBlock.class, loadOntology);
            assertEquals(2, modelBlocks.size());
            assertFalse(modelUtil.isOntology(modelBlocks.get(0)));
            assertTrue(modelUtil.isOntology(modelBlocks.get(1)));
        });
    }
    

    
}