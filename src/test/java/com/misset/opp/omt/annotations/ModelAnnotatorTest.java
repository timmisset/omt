package com.misset.opp.omt.annotations;

import com.google.gson.JsonObject;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTGenericBlock;
import com.misset.opp.omt.psi.OMTModelItemTypeElement;
import com.misset.opp.omt.psi.util.ModelUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

class ModelAnnotatorTest extends OMTAnnotationTest {

    private final static String BLOCK_NAME = "BLOCK_NAME";
    private final static String BLOCK_ENTRY_NAME = "BLOCK_ENTRY_NAME";

    @Mock
    ModelUtil modelUtil;

    @Mock
    PsiElement targetLabel;

    JsonObject container;
    JsonObject attributes;

    @Mock
    OMTGenericBlock genericBlock;

    @Mock
    OMTBlock block;

    @Mock
    OMTBlockEntry blockEntry;

    @InjectMocks
    ModelAnnotator modelAnnotator;

    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("ModelAnnotatorTest");
        super.setUp();
        MockitoAnnotations.openMocks(this);
        setUtilMock(modelUtil);

        doReturn(targetLabel).when(modelUtil).getEntryBlockLabelElement(eq(genericBlock));
        doReturn(BLOCK_ENTRY_NAME).when(blockEntry).getName();
        doReturn(Collections.singletonList(blockEntry)).when(block).getBlockEntryList();

        container = new JsonObject();
        container.addProperty("name", BLOCK_NAME);
        attributes = new JsonObject();
        attributes.addProperty("property", "");
        container.add("attributes", attributes);
    }

    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void annotateModelNoAnnotationWhenInvalidType() {
        modelAnnotator.annotate(mock(PsiElement.class));
        verifyNoAnnotations();
    }

    @Test
    void annotateModelItemTypeThrowsUnknownTypeError() {
        final OMTModelItemTypeElement modelItemTypeElement = mock(OMTModelItemTypeElement.class);
        doReturn("InvalidType").when(modelUtil).getModelItemType(eq(modelItemTypeElement));
        doReturn(Arrays.asList("ValidType")).when(modelUtil).getModelRootItems();

        modelAnnotator.annotate(modelItemTypeElement);
        verifyError("Unknown model type: InvalidType");
    }

    @Test
    void annotateModelItemTypeThrowsNoUnknownTypeError() {
        final OMTModelItemTypeElement modelItemTypeElement = mock(OMTModelItemTypeElement.class);
        doReturn("ValidType").when(modelUtil).getModelItemType(eq(modelItemTypeElement));
        doReturn(Arrays.asList("ValidType")).when(modelUtil).getModelRootItems();

        modelAnnotator.annotate(modelItemTypeElement);
        verifyNoErrors();
    }

    @Test
    void annotateBlockEntryThrowsUnknownAttributeErrorWhenNoAttributes() {
        doReturn("Unknown").when(modelUtil).getEntryBlockLabel(eq(genericBlock));
        doReturn(container).when(modelUtil).getJsonAtElementLevel(eq(genericBlock));
        doReturn(false).when(modelUtil).isMapNode(eq(container));

        modelAnnotator.annotate(genericBlock);

        verifyError("Unknown is not a known attribute for " + BLOCK_NAME);
    }

    @Test
    void annotateBlockEntryThrowsNoUnknownAttributeErrorWhenMapNode() {
        doReturn("Unknown").when(modelUtil).getEntryBlockLabel(eq(genericBlock));
        doReturn(container).when(modelUtil).getJsonAtElementLevel(eq(genericBlock));
        doReturn(true).when(modelUtil).isMapNode(eq(container));

        modelAnnotator.annotate(genericBlock);

        verifyNoErrors();
    }

    @Test
    void annotateBlockEntryThrowsNoUnknownAttributeErrorWhenLabelIsPartOfContainer() {
        doReturn("property").when(modelUtil).getEntryBlockLabel(eq(genericBlock));
        doReturn(container).when(modelUtil).getJsonAtElementLevel(eq(genericBlock));

        modelAnnotator.annotate(genericBlock);

        verifyNoErrors();
    }

    @Test
    void annotateBlockEntryThrowsUnknownAttributeErrorWhenNoAttributesCanBeResolved() {
        doReturn("property").when(modelUtil).getEntryBlockLabel(eq(genericBlock));
        doReturn(container).when(modelUtil).getJsonAtElementLevel(eq(genericBlock));
        container.remove("name");
        container.remove("attributes");

        modelAnnotator.annotate(genericBlock);

        verifyError("property is not a known attribute for ");
    }

    @Test
    void annotateMissingEntriesThrowsNoErrorWhenShortcut() {
        doReturn(container).when(modelUtil).getJsonAttributes(eq(block));
        container.addProperty("shortcut", "");
        modelAnnotator.annotate(block);
        verifyNoErrors();
    }

    @Test
    void annotateMissingEntriesThrowsErrorWhenShortcutIsDestructed() {
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       variables:\n" +
                "           - readonly: true\n";
        myFixture.configureByText(getFileName(), content);
        assertHasError("variables is missing attribute(s): name");
    }

    @Test
    void annotateMissingEntriesThrowsErrorWhenShortcutIsDestructedWithWrongProperty() {
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       variables:\n" +
                "           - property: true\n";
        myFixture.configureByText(getFileName(), content);
        assertHasError("property is not a known attribute for Variable");
    }

    @Test
    void annotateMissingEntriesThrowsNoErrorWhenShortcutIsDestructed() {
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       rules:\n" +
                "           mijnRegel:\n" +
                "               query: ''\n" +
                "               strict: false\n";
        myFixture.configureByText(getFileName(), content);
        assertNoErrors();
    }

    @Test
    void annotateEntryTypeThrowsErrorWhenWrongType() {
        setOntologyModel();
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       rules:\n" +
                "           mijnRegel:\n" +
                "               strict: 'false'\n";
        myFixture.configureByText(getFileName(), content);
        assertHasError("Expected: boolean, got: string");
    }

    @Test
    void annotateEntryTypeThrowsNoErrorWhenStringOrInterpolatedString() {
        setOntologyModel();
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       title: titel";
        myFixture.configureByText(getFileName(), content);
        assertNoErrors();
    }

    @Test
    void annotateMissingEntriesThrowsErrorWhenMissingAttribute() {
        doReturn(container).when(modelUtil).getJsonAttributes(eq(block));
        JsonObject requiredAttribute = new JsonObject();
        requiredAttribute.addProperty("required", true);
        attributes.remove("property");
        attributes.add("requiredAttribute", requiredAttribute);
        doReturn(BLOCK_NAME).when(modelUtil).getEntryBlockLabel(block);

        modelAnnotator.annotate(block);
        verifyError(BLOCK_NAME + " is missing attribute(s): requiredAttribute");
    }

    @Test
    void annotateMissingEntriesThrowsErrorForMultipleMissingAttribute() {
        doReturn(container).when(modelUtil).getJsonAttributes(eq(block));
        JsonObject requiredAttribute = new JsonObject();
        requiredAttribute.addProperty("required", true);
        attributes.remove("property");
        attributes.add("requiredAttribute", requiredAttribute);
        attributes.add("anotherRequiredAttribute", requiredAttribute);
        doReturn(BLOCK_NAME).when(modelUtil).getEntryBlockLabel(block);

        modelAnnotator.annotate(block);
        verifyError(BLOCK_NAME + " is missing attribute(s): requiredAttribute, anotherRequiredAttribute");
    }

    @Test
    void annotateMissingEntriesThrowsNoErrorWhenAttributeHasNoRequiredAttribute() {
        doReturn(container).when(modelUtil).getJsonAttributes(eq(block));
        attributes.remove("property");
        attributes.add("nonRequiredAttribute", new JsonObject());

        modelAnnotator.annotate(block);
        verifyNoErrors();
    }

    @Test
    void annotateMissingEntriesThrowsNoErrorWhenAttributeRequiredIsFalse() {
        doReturn(container).when(modelUtil).getJsonAttributes(eq(block));
        JsonObject nonRequiredAttribute = new JsonObject();
        nonRequiredAttribute.addProperty("required", false);
        attributes.remove("property");
        attributes.add("nonRequiredAttribute", nonRequiredAttribute);

        modelAnnotator.annotate(block);
        verifyNoErrors();
    }

    @Test
    void annotateMissingEntriesThrowsNoErrorWhenRequiredAttributeIsPresent() {
        doReturn(container).when(modelUtil).getJsonAttributes(eq(block));
        JsonObject requiredAttribute = new JsonObject();
        requiredAttribute.addProperty("required", true);
        attributes.remove("property");
        attributes.add("requiredAttribute", requiredAttribute);
        doReturn("requiredAttribute").when(blockEntry).getName();

        modelAnnotator.annotate(block);
        verifyNoErrors();
    }
}
