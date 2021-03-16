package com.misset.opp.omt.psi.util;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTCommandCall;
import com.misset.opp.omt.psi.OMTImportBlock;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.OMTOperatorCall;
import com.misset.opp.omt.psi.OMTVariable;
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

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;

class ModelUtilTest extends OMTTestSuite {

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

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setName("ModelUtilTest");
        super.setUp();
        MockitoAnnotations.openMocks(this);

        setUtilMock(projectUtil);

        setExampleFileActivityWithImportsPrefixesParamsVariablesGraphsPayload();
        activity = getElement(OMTModelItemBlock.class);
        importBlock = getElement(OMTImportBlock.class);
        variable = getElement(OMTVariable.class);

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
    void getModelItemBlock_ReturnsSelfIfOMTModelItemBlock() {
        ReadAction.run(() -> {
            Optional<OMTModelItemBlock> optionalOMTModelItemBlock = modelUtil.getModelItemBlock(activity);
            assertTrue(optionalOMTModelItemBlock.isPresent());
            assertEquals(activity, optionalOMTModelItemBlock.get());
        });
    }

    @Test
    void getModelItemBlock_ReturnsEmpty() {
        ReadAction.run(() -> {
            // import block is not contained by a modelitem block
            Optional<OMTModelItemBlock> optionalOMTModelItemBlock = modelUtil.getModelItemBlock(importBlock);
            assertFalse(optionalOMTModelItemBlock.isPresent());
        });
    }

    @Test
    void getModelItemBlock_ReturnsModelItemBlock() {
        ReadAction.run(() -> {
            Optional<OMTModelItemBlock> optionalOMTModelItemBlock = modelUtil.getModelItemBlock(variable);
            assertTrue(optionalOMTModelItemBlock.isPresent());
            assertEquals(activity, optionalOMTModelItemBlock.get());
        });
    }

    @Test
    void getModelItemType_ReturnsNullWhenNoModelItemBlock() {
        ReadAction.run(() -> assertNull(modelUtil.getModelItemType(importBlock)));
    }

    @Test
    void getModelItemType_ReturnsTextWhenModelItemBlock() {
        ReadAction.run(() -> assertEquals("Activity", modelUtil.getModelItemType(variable)));
    }

    @Test
    void getModelItemBlockEntry_ReturnsEmptyWhenNoModelItemBlock() {
        ReadAction.run(() -> assertEquals(Optional.empty(), modelUtil.getModelItemBlockEntry(importBlock, "SUPERVARIABLES")));
    }

    @Test
    void getModelItemBlockEntry_ReturnsEmptyWhenNoMatching() {
        ReadAction.run(() -> assertEquals(Optional.empty(), modelUtil.getModelItemBlockEntry(variable, "SUPERVARIABLES")));
    }

    @Test
    void getModelItemBlockEntry_ReturnsMatching() {
        ReadAction.run(() -> {
            Optional<OMTBlockEntry> variables = modelUtil.getModelItemBlockEntry(variable, "variables");
            assertTrue(variables.isPresent());
            assertEquals("variables", Objects.requireNonNull((variables.get()).getName()));
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
        ReadAction.run(() -> {
            // rootblock has no entryblock label and should return "" for label
            assertEquals("", modelUtil.getEntryBlockLabel(getFile()));
        });
    }

    @Test
    void getEntryBlockLabel_ReturnsLabelWithoutColon() {
        ReadAction.run(() -> {
            assertEquals("import", modelUtil.getEntryBlockLabel(importBlock));
        });
    }

    @Test
    void getModelItemEntryLabel() {
        ReadAction.run(() -> {
            assertEquals("params", modelUtil.getModelItemEntryLabel(variable));
        });
    }

    @Test
    void getLocalCommands_ContainsActivityCommands() {
        ReadAction.run(() -> {
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
        setExampleFileLoadOntology();
        ReadAction.run(() -> {
            OMTCommandCall call = getElement(OMTCommandCall.class);

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
        ReadAction.run(() -> {
            JsonObject json = modelUtil.getJson(variable);
            assertEquals("Param", json.get("name").getAsString());
        });
    }

    @Test
    void getJson_returnsJsonForMapItem() {
        ReadAction.run(() -> {
            List<OMTVariable> variables = getElements(OMTVariable.class);
            // second to last variable is the the $variableA usage in the payload
            OMTVariable payloadVariable = variables.get(variables.size() - 2);
            JsonObject json = modelUtil.getJson(payloadVariable);
            assertEquals("PayloadProperty", json.get("name").getAsString());
        });
    }

    @Test
    void getJson_returnsJsonForMapOfItemScalarValue() {
        setExampleFileActivityWithVariablesActions();
        ReadAction.run(() -> {
            List<OMTVariable> variables = getElements(OMTVariable.class);
            // last variable is in the action
            OMTVariable payloadVariable = variables.get(variables.size() - 1);
            JsonObject json = modelUtil.getJson(payloadVariable);
            assertEquals("scalar", json.get("node").getAsString());
        });
    }

    @Test
    void getJson_returnsNamedReferenceProperty() {
        ReadAction.run(() -> {
            List<OMTOperatorCall> operatorCalls = getElements(OMTOperatorCall.class);
            // last variable is in the action
            OMTOperatorCall operatorCall = operatorCalls.get(operatorCalls.size() - 1);
            JsonObject json = modelUtil.getJson(operatorCall);
            assertTrue(json.get("namedReference").getAsBoolean());
        });
    }

    @Test
    void getJsonAttributes_returnsJsonAttributesForMapOfItemScalarValue() {
        setExampleFileActivityWithVariablesActions();
        ReadAction.run(() -> {
            List<OMTVariable> variables = getElements(OMTVariable.class);
            // last variable is in the action
            OMTVariable payloadVariable = variables.get(variables.size() - 1);
            JsonObject json = modelUtil.getJsonAttributes(payloadVariable);
            assertEquals("ActionProperty", json.get("name").getAsString());
        });
    }

    @Test
    void isOntology() {
        setExampleFileLoadOntology();
        ReadAction.run(() -> {
            List<OMTModelItemBlock> modelBlocks = getElements(OMTModelItemBlock.class);
            assertEquals(2, modelBlocks.size());
            assertFalse(modelUtil.isOntology(modelBlocks.get(0)));
            assertTrue(modelUtil.isOntology(modelBlocks.get(1)));
        });
    }

    @Test
    void isDuplicationAllowedThrowsNoErrorWhenDuplicationIsAllowed() {
        setOntologyModel();
        String content = "" +
                "moduleName: ModuleNaam\n" +
                "prefixes:\n" +
                "   abc: <http://abc/>\n" +
                "\n" +
                "declare:\n" +
                "   SomeModule:\n" +
                "       SomeActivity:\n" +
                "           type: Activity\n" +
                "           params:\n" +
                "           -   abc:string\n" +
                "           -   abc:string\n";
        myFixture.configureByText(getFileName(), content);
        assertNoErrors();
    }

    @Test
    void isDuplicationAllowedThrowsErrorWhenDuplicationIsNotAllowed() {
        setOntologyModel();
        String content = "" +
                "moduleName: ModuleNaam\n" +
                "prefixes:\n" +
                "   abc: <http://abc/>\n" +
                "\n" +
                "declare:\n" +
                "   SomeModule:\n" +
                "       SomeActivity:\n" +
                "           type: Activity\n" +
                "           type: Activity\n" +
                "           params:\n" +
                "           -   abc:string\n" +
                "           -   abc:string\n";
        myFixture.configureByText(getFileName(), content);
        assertHasError("Duplication");
    }
}
