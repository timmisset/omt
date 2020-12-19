package com.misset.opp.omt.psi.annotations;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.*;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ParameterAnnotatorTest extends OMTAnnotationTest {

    @Mock
    VariableUtil variableUtil;

    @Mock
    OMTDefineParam defineParam;

    @Mock
    OMTVariable variable;

    @Mock
    OMTFile file;

    @Mock
    Resource resource;

    @Mock
    OMTParameterWithType parameterWithType;

    @Mock
    OMTParameterType parameterType;

    @InjectMocks
    ParameterAnnotator parameterAnnotator;

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

        doReturn("$variable").when(variable).getName();
        doReturn(file).when(variable).getContainingFile();

    }

    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void annotateParameterNoAnnotationWhenInvalidType() {
        parameterAnnotator.annotate(mock(PsiElement.class));
        verifyNoAnnotations();
    }

    @Test
    void annotateParameterWithTypeThrowsError() {
        doReturn(null).when(parameterWithType).getParameterType();
        parameterAnnotator.annotate(parameterWithType);
        verify(getHolder()).newAnnotation(HighlightSeverity.ERROR, "No type specified");
    }

    @Test
    void annotateParameterWithTypeThrowsNoError() {
        doReturn(mock(OMTParameterType.class)).when(parameterWithType).getParameterType();
        parameterAnnotator.annotate(parameterWithType);
        verify(getHolder(), times(0)).newAnnotation(HighlightSeverity.ERROR, "No type specified");
    }

    @Test
    void annotateDefineParameter_DoesNothingWhenVariableHasType() {
        List<Resource> typeResources = new ArrayList<>();
        typeResources.add(mock(Resource.class));
        doReturn(typeResources).when(variable).getType();

        parameterAnnotator.annotate(defineParam);
        assertNotEmpty(variables);
        verify(getBuilder(), times(0)).create();
    }

    @Test
    void annotateDefineParameter_ShowsPrefixClassAsDefaultWhenNoTypeSuggestions() {
        List<Resource> emptyResourceList = new ArrayList<>();
        doReturn(emptyResourceList).when(variable).getType();

        doReturn(emptyResourceList).when(variableUtil).getTypeSuggestions(eq(defineParam), eq(variable));

        parameterAnnotator.annotate(defineParam);
        ArgumentCaptor<IntentionAction> intentionActionArgumentCaptor = ArgumentCaptor.forClass(IntentionAction.class);
        verify(getBuilder()).withFix(intentionActionArgumentCaptor.capture());
        verify(getBuilder(), times(1)).create();
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

        parameterAnnotator.annotate(defineParam);
        ArgumentCaptor<IntentionAction> intentionActionArgumentCaptor = ArgumentCaptor.forClass(IntentionAction.class);
        verify(getBuilder(), times(2)).withFix(intentionActionArgumentCaptor.capture());
        verify(getBuilder(), times(1)).create();
        final List<String> suggestions = intentionActionArgumentCaptor.getAllValues().stream().map(IntentionAction::getText).collect(Collectors.toList());
        assertContainsElements(suggestions, "Add annotation as SuggestionA", "Add annotation as SuggestionB");
        assertDoesntContain(suggestions, "Add annotation as prefix:Class");
    }

    @Test
    void annotateParameterTypeAnnotatesAsResource() {
        setOntologyModel();
        resource = xsdString();
        doReturn(resource).when(parameterType).getAsResource();

        parameterAnnotator.annotate(parameterType);

        verifyInfo(xsdString().toString());
    }
}
