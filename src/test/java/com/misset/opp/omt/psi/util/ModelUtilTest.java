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
import com.misset.opp.omt.psi.support.OMTCall;
import com.misset.opp.omt.util.ProjectUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ModelUtilTest extends LightJavaCodeInsightFixtureTestCase {

    @Mock
    AnnotationHolder annotationHolder;
    @Mock
    AnnotationBuilder annotationBuilder;
    @Spy
    ProjectUtil projectUtil;

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
        exampleFiles = new ExampleFiles(this, myFixture);
        MockitoAnnotations.initMocks(this);

        rootBlock = exampleFiles.getActivityWithImportsPrefixesParamsVariablesGraphsPayload();
        ApplicationManager.getApplication().runReadAction(() -> {
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
            assertEquals("variables", Objects.requireNonNull(((OMTGenericBlock) variables.get()).getPropertyLabel()).getPropertyLabelName());
        });
    }

    @Test
    void getModelRootItems() {
        JsonObject modelRootItem = new JsonObject();
        modelRootItem.addProperty("name", "modelRootItem");
        modelRootItem.addProperty("modelRoot", true);

        JsonObject notModelRootItem = new JsonObject();
        notModelRootItem.addProperty("name", "notModelRootItem");
        notModelRootItem.addProperty("modelRoot", false);

        JsonObject notModelRootItem2 = new JsonObject();
        notModelRootItem2.addProperty("name", "notModelRootItem2");

        JsonObject tree = new JsonObject();
        tree.add("modelRoot", modelRootItem);
        tree.add("notModelRootItem", notModelRootItem);
        tree.add("notModelRootItem2", notModelRootItem2);
        doReturn(tree).when(projectUtil).getParsedModel();
        assertContainsElements(modelUtil.getModelRootItems(), "modelRootItem");
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
    void annotateModelItemType_ThrowsUnknownModelType() {
        PsiElement modelWithWrongModelItemType = exampleFiles.getModelWithWrongModelItemType();
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTModelItemBlock modelItemBlock = exampleFiles.getPsiElementFromRootDocument(OMTModelItemBlock.class, modelWithWrongModelItemType);
            modelUtil.annotateModelItemType(modelItemBlock.getModelItemLabel().getModelItemTypeElement(), annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.ERROR), eq("Unknown model type: !WrongModelItemType"));
            verify(annotationBuilder, times(1)).create();
        });
    }

    @Test
    void annotateModelItemType() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTModelItemBlock modelItemBlock = exampleFiles.getPsiElementFromRootDocument(OMTModelItemBlock.class, rootBlock);
            modelUtil.annotateModelItemType(modelItemBlock.getModelItemLabel().getModelItemTypeElement(), annotationHolder);
            verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.ERROR), eq("Unknown model type: !WrongModelItemType"));
            verify(annotationBuilder, times(0)).create();
        });
    }

    @Test
    void annotateBlock_ThrowsMissingAttributes() {
        PsiElement modelWithWrongModelItemType = exampleFiles.getStandaloneQueryWithMissingAttribute();
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTModelItemBlock modelItemBlock = exampleFiles.getPsiElementFromRootDocument(OMTModelItemBlock.class, modelWithWrongModelItemType);
            modelUtil.annotateBlock(modelItemBlock.getBlock(), annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.ERROR), startsWith("myStandAloneQuery is missing attribute(s)"));
            verify(annotationBuilder, times(1)).create();
        });
    }

    @Test
    void annotateBlockEntry_ThrowsWrongAttribute() {
        PsiElement modelWithWrongNestedAttribute = exampleFiles.getActivityWithWrongNestedAttribute();
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTBlockEntry blockEntry = exampleFiles.getPsiElementFromRootDocument(OMTBlockEntry.class, modelWithWrongNestedAttribute, omtBlockEntry -> modelUtil.getEntryBlockLabel(omtBlockEntry).equals("queryX"));
            modelUtil.annotateBlockEntry(blockEntry, annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.ERROR), startsWith("queryX is not a known attribute for PayloadProperty"));
            verify(annotationBuilder, times(1)).create();
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
        PsiElement modelWithLoadOntology = exampleFiles.getLoadOntology();
        ApplicationManager.getApplication().runReadAction(() -> {
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
        PsiElement activityWithVariablesActions = exampleFiles.getActivityWithVariablesActions();
        ApplicationManager.getApplication().runReadAction(() -> {
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
        PsiElement activityWithVariablesActions = exampleFiles.getActivityWithVariablesActions();
        ApplicationManager.getApplication().runReadAction(() -> {
            List<OMTVariable> variables = exampleFiles.getPsiElementsFromRootDocument(OMTVariable.class, activityWithVariablesActions);
            // last variable is in the action
            OMTVariable payloadVariable = variables.get(variables.size() - 1);
            JsonObject json = modelUtil.getJsonAttributes(payloadVariable);
            assertEquals("ActionProperty", json.get("name").getAsString());
        });
    }

    @Test
    void getJsonAttributes_returnsTitelAttributes() {
        PsiElement activity = exampleFiles.getActivityWithMembers();
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCall titel = exampleFiles.getPsiElementFromRootDocument(OMTCall.class, activity, call -> call.getName().equals("MijnTweedeActiviteit"));
            // last variable is in the action
            JsonObject json = modelUtil.getJson(titel);
            assertEquals("interpolatedString", json.get("type").getAsString());
        });
    }

    @Test
    void isOntology() {
        PsiElement loadOntology = exampleFiles.getLoadOntology();
        ApplicationManager.getApplication().runReadAction(() -> {
            List<OMTModelItemBlock> modelBlocks = exampleFiles.getPsiElementsFromRootDocument(OMTModelItemBlock.class, loadOntology);
            assertEquals(2, modelBlocks.size());
            assertFalse(modelUtil.isOntology(modelBlocks.get(0)));
            assertTrue(modelUtil.isOntology(modelBlocks.get(1)));
        });
    }

}
