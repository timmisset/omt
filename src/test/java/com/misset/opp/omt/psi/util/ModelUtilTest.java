package com.misset.opp.omt.psi.util;

import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.wrappers.PsiTreeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ModelUtilTest {

    final String MODELITEMTYPE = "!Activity";

    @Mock
    PsiTreeUtil psiTreeUtil;

    @Mock
    PsiElement element;
    @Mock
    OMTModelItemBlock modelItemBlock;
    @Mock
    OMTBlock block;
    @Mock
    OMTModelItemLabel modelItemLabel;
    @Mock
    OMTModelItemTypeElement modelItemTypeElement;
    @Mock
    OMTBlockEntry omtBlockEntry_variables;
    @Mock
    OMTPropertyLabel omtPropertyLabel_variables;
    @Mock
    OMTBlockEntry omtBlockEntry_params;
    @Mock
    OMTPropertyLabel omtPropertyLabel_params;
    @Mock
    AnnotationHolder annotationHolder;
    @Mock
    AnnotationBuilder annotationBuilder;

    List<OMTBlockEntry> blockEntryList = new ArrayList<>();

    @InjectMocks
    ModelUtil modelUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        // model item block with label
        doReturn(modelItemLabel).when(modelItemBlock).getModelItemLabel();
        doReturn(modelItemBlock).when(modelItemLabel).getParent();
        doReturn(block).when(modelItemBlock).getBlock();
        doReturn(modelItemBlock).when(block).getParent();

        // model item label with type element
        doReturn(modelItemTypeElement).when(modelItemLabel).getModelItemTypeElement();
        doReturn(modelItemLabel).when(modelItemTypeElement).getParent();

        doReturn(MODELITEMTYPE).when(modelItemTypeElement).getText();
        doReturn(modelItemBlock).when(psiTreeUtil).getTopmostParentOfType(eq(modelItemTypeElement), eq(OMTModelItemBlock.class));
        doReturn(modelItemBlock).when(psiTreeUtil).getTopmostParentOfType(eq(modelItemLabel), eq(OMTModelItemBlock.class));
        doReturn(modelItemBlock).when(psiTreeUtil).getTopmostParentOfType(eq(element), eq(OMTModelItemBlock.class));

        // model item block entries (the root elements, params and variables)
        doReturn(omtPropertyLabel_variables).when(omtBlockEntry_variables).getPropertyLabel();
        doReturn(omtPropertyLabel_params).when(omtBlockEntry_params).getPropertyLabel();
        doReturn("variables:").when(omtPropertyLabel_variables).getText();
        doReturn("params:").when(omtPropertyLabel_params).getText();
        blockEntryList.add(omtBlockEntry_variables);
        blockEntryList.add(omtBlockEntry_params);
        doReturn(blockEntryList).when(block).getBlockEntryList();

    }

    @Test
    void getModelItemBlock_ReturnsSelfIfOMTModelItemBlock() {
        Optional<OMTModelItemBlock> optionalOMTModelItemBlock = modelUtil.getModelItemBlock(modelItemBlock);
        assertTrue(optionalOMTModelItemBlock.isPresent());
        assertEquals(modelItemBlock, optionalOMTModelItemBlock.get());
    }

    @Test
    void getModelItemBlock_ReturnsEmpty() {
        doReturn(null).when(psiTreeUtil).getTopmostParentOfType(any(PsiElement.class), eq(OMTModelItemBlock.class));
        Optional<OMTModelItemBlock> optionalOMTModelItemBlock = modelUtil.getModelItemBlock(element);
        assertFalse(optionalOMTModelItemBlock.isPresent());
    }

    @Test
    void getModelItemBlock_ReturnsItem() {
        Optional<OMTModelItemBlock> optionalOMTModelItemBlock = modelUtil.getModelItemBlock(element);
        assertTrue(optionalOMTModelItemBlock.isPresent());
        assertEquals(modelItemBlock, optionalOMTModelItemBlock.get());
    }

    @Test
    void getModelItemType_ReturnsNullWhenNoModelItemBlock() {
        doReturn(null).when(psiTreeUtil).getTopmostParentOfType(any(PsiElement.class), eq(OMTModelItemBlock.class));
        assertNull(modelUtil.getModelItemType(element));
    }

    @Test
    void getModelItemType_ReturnsTextWhenModelItemBlock() {
        assertEquals("Activity", modelUtil.getModelItemType(modelItemLabel));
    }

    @Test
    void getModelItemBlockEntry_ReturnsEmptyWhenNoModelItemBlock() {
        doReturn(null).when(psiTreeUtil).getTopmostParentOfType(any(PsiElement.class), eq(OMTModelItemBlock.class));
        assertEquals(Optional.empty(), modelUtil.getModelItemBlockEntry(element, "variables"));
    }

    @Test
    void getModelItemBlockEntry_ReturnsEmptyWhenNoModelItemBlockOMTBlock() {
        doReturn(null).when(modelItemBlock).getBlock();
        assertEquals(Optional.empty(), modelUtil.getModelItemBlockEntry(element, "variables"));
    }

    @Test
    void getModelItemBlockEntry_ReturnsEmptyWhenNoPropertyLabelMatching() {
        doReturn(null).when(omtBlockEntry_variables).getPropertyLabel();
        doReturn(null).when(omtBlockEntry_params).getPropertyLabel();

        assertEquals(Optional.empty(), modelUtil.getModelItemBlockEntry(element, "variables"));
    }

    @Test
    void getModelItemBlockEntry_ReturnsMatching() {
        Optional<OMTBlockEntry> variables = modelUtil.getModelItemBlockEntry(element, "variables");
        assertTrue(variables.isPresent());
        assertEquals(omtBlockEntry_variables, variables.get());
    }

    @Test
    void getConnectedEntries_Returns2Items() {
        OMTBlockEntry parentBlockEntry = mock(OMTBlockEntry.class);
        PsiElement blockEntryParentParent = mock(PsiElement.class);

        doReturn(blockEntryParentParent).when(parentBlockEntry).getParent();
        doReturn(Collections.singletonList(parentBlockEntry))
                .when(psiTreeUtil)
                .collectParents(eq(element), eq(OMTBlockEntry.class), eq(false), any());

        doReturn(blockEntryList).when(psiTreeUtil).getChildrenOfTypeAsList(eq(blockEntryParentParent), eq(OMTBlockEntry.class));

        List<OMTBlockEntry> connectedEntries = modelUtil.getConnectedEntries(element, Arrays.asList("variables", "params"));
        assertTrue(connectedEntries.contains(omtBlockEntry_params));
        assertTrue(connectedEntries.contains(omtBlockEntry_variables));
        assertEquals(2, connectedEntries.size());
    }

    @Test
    void getConnectedEntries_Returns1Item() {
        OMTBlockEntry parentBlockEntry = mock(OMTBlockEntry.class);
        PsiElement blockEntryParentParent = mock(PsiElement.class);

        doReturn(blockEntryParentParent).when(parentBlockEntry).getParent();
        doReturn(Collections.singletonList(parentBlockEntry))
                .when(psiTreeUtil)
                .collectParents(eq(element), eq(OMTBlockEntry.class), eq(false), any());

        doReturn(blockEntryList).when(psiTreeUtil).getChildrenOfTypeAsList(eq(blockEntryParentParent), eq(OMTBlockEntry.class));

        List<OMTBlockEntry> connectedEntries = modelUtil.getConnectedEntries(element, Arrays.asList("variables"));
        assertFalse(connectedEntries.contains(omtBlockEntry_params));
        assertTrue(connectedEntries.contains(omtBlockEntry_variables));
        assertEquals(1, connectedEntries.size());
    }

    @Test
    void getEntryBlockLabel_ReturnsEmptyStringWhenNull() {
        doReturn(null).when(psiTreeUtil).findFirstParent(eq(element), any());
        assertEquals("", modelUtil.getEntryBlockLabel(element));
    }

    @Test
    void getEntryBlockLabel_ReturnsLabelWithoutColon() {
        doReturn(omtBlockEntry_params).when(psiTreeUtil).findFirstParent(eq(element), any());
        assertEquals("params", modelUtil.getEntryBlockLabel(element));
    }

    @Test
    void getModelItemEntryLabel() {
        // pretend that params is nested in variables
        doReturn(Arrays.asList(omtBlockEntry_params, omtBlockEntry_variables)).when(psiTreeUtil)
                .collectParents(eq(element), eq(OMTBlockEntry.class), eq(false), any());
        assertEquals("variables", modelUtil.getModelItemEntryLabel(element));
    }

    @Test
    void annotateModelItem_StopsWhenBlockPartIsFound() {
        doReturn(null).when(modelItemBlock).getBlock();
        modelUtil.annotateModelItem(modelItemTypeElement, annotationHolder);
        verify(modelItemBlock, times(1)).getBlock();
    }

    @Test
    void annotateModelItem_ErrorWhenModelItemTypeIsUnknown() {
        doReturn("!NotKnown").when(modelItemTypeElement).getText();
        doReturn(annotationBuilder).when(annotationHolder).newAnnotation(eq(HighlightSeverity.ERROR), anyString());
        doReturn(annotationBuilder).when(annotationBuilder).range(eq(modelItemLabel));
        doNothing().when(annotationBuilder).create();

        modelUtil.annotateModelItem(modelItemTypeElement, annotationHolder);
        verify(annotationHolder).newAnnotation(eq(HighlightSeverity.ERROR), eq("Unknown model type: !NotKnown"));
        verify(annotationBuilder, times(1)).create();
    }

    @Test
    void annotateModelItem_AnnotateActivity() {
        doReturn(annotationBuilder).when(annotationHolder).newAnnotation(eq(HighlightSeverity.ERROR), anyString());
        doReturn(annotationBuilder).when(annotationBuilder).range(eq(modelItemLabel));
        doNothing().when(annotationBuilder).create();

        modelUtil.annotateModelItem(modelItemTypeElement, annotationHolder);
        verify(annotationHolder).newAnnotation(eq(HighlightSeverity.ERROR), eq("Unknown model type: !NotKnown"));
        verify(annotationBuilder, times(1)).create();
    }

    @Test
    void getLocalCommands() {
    }

    @Test
    void getJson() {
    }

    @Test
    void isOntology() {
    }
}
