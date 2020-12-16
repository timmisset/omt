package com.misset.opp.omt.psi.annotations;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTDefineParam;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.util.VariableUtil;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DefineParameterAnnotatorTest extends OMTTestSuite {

    @Mock
    VariableUtil variableUtil;

    @Mock
    OMTDefineParam defineParam;

    @Mock
    OMTVariable variable;

    @Mock
    OMTFile file;

    @InjectMocks
    DefinedParameterAnnotator definedParameterAnnotator;

    @Mock
    AnnotationHolder annotationHolder;

    @Mock
    AnnotationBuilder annotationBuilder;

    List<OMTVariable> variables;

    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("VariableAnnotatorTest");
        super.setUp();

        MockitoAnnotations.openMocks(this);

        setUtilMock(variableUtil);
        variables = new ArrayList<>();
        variables.add(variable);
        doReturn(variables).when(defineParam).getVariableList();

        doReturn(annotationBuilder).when(annotationHolder).newAnnotation(any(HighlightSeverity.class), anyString());
        doReturn(annotationBuilder).when(annotationBuilder).tooltip(anyString());
        doReturn(annotationBuilder).when(annotationBuilder).withFix(any(IntentionAction.class));
        doReturn(annotationBuilder).when(annotationBuilder).range(eq(variable));

        doReturn("$variable").when(variable).getName();
        doReturn(file).when(variable).getContainingFile();

    }

    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void annotateDefineParameter_DoesNothingWhenVariableHasType() {
        List<Resource> typeResources = new ArrayList<>();
        typeResources.add(mock(Resource.class));
        doReturn(typeResources).when(variable).getType();

        definedParameterAnnotator.annotateDefineParameter(defineParam, annotationHolder);
        assertNotEmpty(variables);
        verify(annotationBuilder, times(0)).create();
    }

    @Test
    void annotateDefineParameter_ShowsPrefixClassAsDefaultWhenNoTypeSuggestions() {
        List<Resource> emptyResourceList = new ArrayList<>();
        doReturn(emptyResourceList).when(variable).getType();

        doReturn(emptyResourceList).when(variableUtil).getTypeSuggestions(eq(defineParam), eq(variable));

        definedParameterAnnotator.annotateDefineParameter(defineParam, annotationHolder);
        ArgumentCaptor<IntentionAction> intentionActionArgumentCaptor = ArgumentCaptor.forClass(IntentionAction.class);
        verify(annotationBuilder).withFix(intentionActionArgumentCaptor.capture());
        verify(annotationBuilder, times(1)).create();
        assertEquals("Add annotation as prefix:Class", intentionActionArgumentCaptor.getValue().getText());
    }

    @Test
    void annotateDefineParameter_ShowsResourceSuggestions() {
        List<Resource> emptyResourceList = new ArrayList<>();
        Resource suggestionA = mock(Resource.class);
        Resource suggestionB = mock(Resource.class);
        List<Resource> suggestedTypes = Arrays.asList(suggestionA, suggestionB);

        doReturn("SuggestionA").when(file).resourceToCurie(eq(suggestionA));
        doReturn("SuggestionB").when(file).resourceToCurie(eq(suggestionB));

        doReturn(emptyResourceList).when(variable).getType();

        doReturn(suggestedTypes).when(variableUtil).getTypeSuggestions(eq(defineParam), eq(variable));

        definedParameterAnnotator.annotateDefineParameter(defineParam, annotationHolder);
        ArgumentCaptor<IntentionAction> intentionActionArgumentCaptor = ArgumentCaptor.forClass(IntentionAction.class);
        verify(annotationBuilder, times(2)).withFix(intentionActionArgumentCaptor.capture());
        verify(annotationBuilder, times(1)).create();
        final List<String> suggestions = intentionActionArgumentCaptor.getAllValues().stream().map(IntentionAction::getText).collect(Collectors.toList());
        assertContainsElements(suggestions, "Add annotation as SuggestionA", "Add annotation as SuggestionB");
        assertDoesntContain(suggestions, "Add annotation as prefix:Class");
    }
}
