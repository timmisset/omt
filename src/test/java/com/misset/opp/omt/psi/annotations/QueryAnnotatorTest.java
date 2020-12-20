package com.misset.opp.omt.psi.annotations;

import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.util.QueryUtil;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.getRDFModelUtil;
import static org.mockito.Mockito.*;

class QueryAnnotatorTest extends OMTAnnotationTest {

    private static final String RESOURCES_AS_STRING = "RESOURCES";
    @Mock
    OMTQueryStep step;

    @Mock
    OMTQueryReverseStep reverseStep;

    @Mock
    OMTSubQuery subQuery;

    @Mock
    OMTQuery query;

    @Mock
    QueryUtil queryUtil;

    @Mock
    OMTResolvableValue resolvableValue;

    @Mock
    OMTFile file;

    @Mock
    OMTCurieElement curieElement;
    @InjectMocks
    QueryAnnotator queryAnnotator;
    private List<Resource> queryStepResources;

    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("ScriptAnnotatorTest");
        super.setUp();
        setOntologyModel();
        MockitoAnnotations.openMocks(this);

        queryStepResources = new ArrayList<>();
        queryStepResources.add(getRDFModelUtil().getStringType());
        setUtilMock(queryUtil);

        doReturn(queryStepResources).when(step).resolveToResource();
        doReturn(queryStepResources).when(reverseStep).resolveToResource();
        doReturn(queryStepResources).when(subQuery).resolveToResource();
        doReturn(file).when(curieElement).getContainingFile();
        doReturn(file).when(step).getContainingFile();
        doReturn(file).when(reverseStep).getContainingFile();
        doReturn(file).when(subQuery).getContainingFile();
        doReturn(RESOURCES_AS_STRING).when(file).resourcesAsTypes(anyList());
    }

    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void annotateQueryNoAnnotationWhenInvalidType() {
        queryAnnotator.annotate(mock(PsiElement.class));
        verifyNoAnnotations();
    }

    @Test
    void annotateQueryStepThrowsUnknownForwardPath() {
        queryStepResources.clear();
        doReturn(classesAsResourceList("ClassA")).when(queryUtil).getPreviousStep(eq(step));
        doReturn(curieElement).when(step).getCurieElement();
        final Resource somePredicate = createResource("somePredicate");
        doReturn(createResource("somePredicate")).when(curieElement).getAsResource();

        queryAnnotator.annotate(step);

        verify(getHolder()).newAnnotation(HighlightSeverity.ERROR,
                String.format("%s is not a known %s-path predicate for type(s): %s", somePredicate, "FORWARD", RESOURCES_AS_STRING));
    }

    @Test
    void annotateQueryStepThrowsNoErrorWhenKnownPredicate() {
        queryStepResources.clear();
        doReturn(classesAsResourceList("ClassA")).when(queryUtil).getPreviousStep(eq(step));
        doReturn(curieElement).when(step).getCurieElement();
        final Resource somePredicate = createResource("somePredicate");
        doReturn(createResource("booleanProperty")).when(curieElement).getAsResource();

        queryAnnotator.annotate(step);

        verifyNoErrors();
    }

    @Test
    void annotateQueryStepThrowsNoErrorWhenNoPreviousStepCanBeResolved() {
        queryStepResources.clear();
        doReturn(Collections.emptyList()).when(queryUtil).getPreviousStep(eq(step));
        queryAnnotator.annotate(step);
        verifyNoErrors();
    }

    @Test
    void annotateQueryStepThrowsNoErrorWhenNoCurieElementCanBeResolved() {
        queryStepResources.clear();
        doReturn(classesAsResourceList("ClassA")).when(queryUtil).getPreviousStep(eq(step));
        doReturn(null).when(step).getCurieElement();
        queryAnnotator.annotate(step);
        verifyNoErrors();
    }

    @Test
    void annotateQueryStepThrowsUnknownReversePath() {
        queryStepResources.clear();
        doReturn(classesAsResourceList("ClassA")).when(queryUtil).getPreviousStep(eq(reverseStep));
        doReturn(curieElement).when(reverseStep).getCurieElement();
        final Resource somePredicate = createResource("somePredicate");
        doReturn(createResource("somePredicate")).when(curieElement).getAsResource();

        queryAnnotator.annotate(reverseStep);

        verify(getHolder()).newAnnotation(HighlightSeverity.ERROR,
                String.format("%s is not a known %s-path predicate for type(s): %s", somePredicate, "REVERSE", RESOURCES_AS_STRING));
    }

    @Test
    void annotateMultiFilters() {
        List<OMTQueryFilter> queryFilters = Arrays.asList(
                mock(OMTQueryFilter.class), mock(OMTQueryFilter.class)
        );
        doReturn(queryFilters).when(step).getQueryFilterList();

        queryAnnotator.annotate(step);

        verify(getHolder()).newAnnotation(eq(HighlightSeverity.WARNING), eq(
                "Multiple filter steps on the same step"
        ));
    }

    @Test
    void annotateSubqueryThrowsWarningWhenNotWrappable() {
        doReturn(false).when(queryUtil).isWrappableStep(subQuery);
        queryAnnotator.annotate(subQuery);
        verify(getHolder()).newAnnotation(eq(HighlightSeverity.WARNING), eq(
                "Unnecessary wrapping of statement"
        ));
    }

    @Test
    void annotateSubqueryThrowsNoWarningWhenWrappable() {
        doReturn(true).when(queryUtil).isWrappableStep(subQuery);
        queryAnnotator.annotate(subQuery);
        verifyNoWarnings();
    }

    @Test
    void annotateSubqueryThrowsWarningWhenNotDecoratable() {
        doReturn(mock(OMTStepDecorator.class)).when(step).getStepDecorator();
        doReturn(new OMTQueryStep[]{subQuery}).when(step).getChildren();
        doReturn(query).when(subQuery).getQuery();
        doReturn(false).when(queryUtil).isDecoratable(query);
        queryAnnotator.annotate(step);
        verify(getHolder()).newAnnotation(eq(HighlightSeverity.ERROR), eq(
                "Invalid decorator"
        ));
    }

    @Test
    void annotateSubqueryThrowsNoWarningWhenDecoratable() {
        doReturn(mock(OMTStepDecorator.class)).when(step).getStepDecorator();
        doReturn(new OMTQueryStep[]{subQuery}).when(step).getChildren();
        doReturn(query).when(subQuery).getQuery();
        doReturn(true).when(queryUtil).isDecoratable(query);
        queryAnnotator.annotate(step);
        verifyNoErrors();
    }

    @Test
    void annotateSubqueryThrowsNoWarningWhenNotASubquery() {
        doReturn(mock(OMTStepDecorator.class)).when(step).getStepDecorator();
        doReturn(new OMTQueryStep[]{mock(OMTQueryStep.class)}).when(step).getChildren();
        queryAnnotator.annotate(step);
        verifyNoErrors();
    }

    @Test
    void annotateOMTDefineQueryStatementMissingSemicolon() {
        String statement = "DEFINE QUERY query => 'hello world'";
        final OMTDefineQueryStatement queryStatement = mock(OMTDefineQueryStatement.class);
        doReturn(statement).when(queryStatement).getText();
        queryAnnotator.annotate(queryStatement);
        verify(getHolder()).newAnnotation(eq(HighlightSeverity.ERROR), eq(
                "; expected"
        ));
    }

    @Test
    void annotateOMTDefineQueryStatementMissingSemicolonNoError() {
        String statement = "DEFINE QUERY query => 'hello world';";
        final OMTDefineQueryStatement queryStatement = mock(OMTDefineQueryStatement.class);
        doReturn(statement).when(queryStatement).getText();
        queryAnnotator.annotate(queryStatement);
        verifyNoErrors();
    }

    @Test
    void annotateAddToCollectionThrowsErrorWhenNotCompatible() {
        doReturn(classesAsResourceList("ClassA")).when(query).resolveToResource();
        doReturn(classesAsResourceList("ClassC")).when(resolvableValue).resolveToResource();
        final OMTAddToCollection addToCollection = mock(OMTAddToCollection.class);
        doReturn(query).when(addToCollection).getQuery();
        doReturn(resolvableValue).when(addToCollection).getResolvableValue();

        queryAnnotator.annotate(addToCollection);
        verify(getHolder()).newAnnotation(eq(HighlightSeverity.ERROR), eq(
                "Incompatible types LEFT-HAND: ClassA, RIGHT-HAND ClassC"
        ));
    }

    @Test
    void annotateAddToCollectionThrowsNoErrorWhenCompatible() {
        doReturn(classesAsResourceList("ClassA")).when(query).resolveToResource();
        doReturn(classesAsResourceList("ClassB")).when(resolvableValue).resolveToResource();
        final OMTAddToCollection addToCollection = mock(OMTAddToCollection.class);
        doReturn(query).when(addToCollection).getQuery();
        doReturn(resolvableValue).when(addToCollection).getResolvableValue();

        queryAnnotator.annotate(addToCollection);
        verifyNoErrors();
    }

    @Test
    void annotateRemoveFromCollectionThrowsErrorWhenNotCompatible() {
        doReturn(classesAsResourceList("ClassA")).when(query).resolveToResource();
        doReturn(classesAsResourceList("ClassC")).when(resolvableValue).resolveToResource();
        final OMTRemoveFromCollection removeFromCollection = mock(OMTRemoveFromCollection.class);
        doReturn(query).when(removeFromCollection).getQuery();
        doReturn(resolvableValue).when(removeFromCollection).getResolvableValue();

        queryAnnotator.annotate(removeFromCollection);
        verify(getHolder()).newAnnotation(eq(HighlightSeverity.ERROR), eq(
                "Incompatible types LEFT-HAND: ClassA, RIGHT-HAND ClassC"
        ));
    }

    @Test
    void annotateRemoveFromCollectionThrowsNoErrorWhenCompatible() {
        doReturn(classesAsResourceList("ClassA")).when(query).resolveToResource();
        doReturn(classesAsResourceList("ClassB")).when(resolvableValue).resolveToResource();
        final OMTRemoveFromCollection removeFromCollection = mock(OMTRemoveFromCollection.class);
        doReturn(query).when(removeFromCollection).getQuery();
        doReturn(resolvableValue).when(removeFromCollection).getResolvableValue();

        queryAnnotator.annotate(removeFromCollection);
        verifyNoErrors();
    }

    @Test
    void annotateAssignmentStatementThrowsErrorWhenNotCompatible() {
        doReturn(classesAsResourceList("ClassA")).when(query).resolveToResource();
        doReturn(classesAsResourceList("ClassC")).when(resolvableValue).resolveToResource();
        final OMTAssignmentStatement assignmentStatement = mock(OMTAssignmentStatement.class);
        doReturn(query).when(assignmentStatement).getQuery();
        doReturn(resolvableValue).when(assignmentStatement).getResolvableValue();

        queryAnnotator.annotate(assignmentStatement);
        verify(getHolder()).newAnnotation(eq(HighlightSeverity.ERROR), eq(
                "Incompatible types LEFT-HAND: ClassA, RIGHT-HAND ClassC"
        ));
    }

    @Test
    void annotateAssignmentStatementThrowsNoErrorWhenCompatible() {
        doReturn(classesAsResourceList("ClassA")).when(query).resolveToResource();
        doReturn(classesAsResourceList("ClassB")).when(resolvableValue).resolveToResource();
        final OMTAssignmentStatement assignmentStatement = mock(OMTAssignmentStatement.class);
        doReturn(query).when(assignmentStatement).getQuery();
        doReturn(resolvableValue).when(assignmentStatement).getResolvableValue();

        queryAnnotator.annotate(assignmentStatement);
        verifyNoErrors();
    }

    @Test
    void annotateEquationStatementThrowsErrorWhenNotCompatible() {
        final OMTQuery query2 = mock(OMTQuery.class);
        doReturn(classesAsResourceList("ClassA")).when(query).resolveToResource();
        doReturn(classesAsResourceList("ClassC")).when(query2).resolveToResource();
        final OMTEquationStatement equationStatement = mock(OMTEquationStatement.class);
        doReturn(Arrays.asList(query, query2)).when(equationStatement).getQueryList();

        queryAnnotator.annotate(equationStatement);
        verify(getHolder()).newAnnotation(eq(HighlightSeverity.ERROR), eq(
                "Incompatible types LEFT-HAND: ClassA, RIGHT-HAND ClassC"
        ));
    }

    @Test
    void annotateEquationStatementThrowsNoErrorWhenCompatible() {
        final OMTQuery query2 = mock(OMTQuery.class);
        doReturn(classesAsResourceList("ClassA")).when(query).resolveToResource();
        doReturn(classesAsResourceList("ClassB")).when(query2).resolveToResource();
        final OMTEquationStatement equationStatement = mock(OMTEquationStatement.class);
        doReturn(Arrays.asList(query, query2)).when(equationStatement).getQueryList();

        queryAnnotator.annotate(equationStatement);
        verifyNoErrors();
    }

    @Test
    void annotateBooleanStatementThrowsErrorWhenAnyNotBoolean() {
        final OMTQuery query2 = mock(OMTQuery.class);
        doReturn(Collections.singletonList(getRDFModelUtil().getStringType())).when(query).resolveToResource();
        doReturn(Collections.singletonList(getRDFModelUtil().getBooleanType())).when(query2).resolveToResource();
        final OMTBooleanStatement booleanStatement = mock(OMTBooleanStatement.class);
        doReturn(Arrays.asList(query, query2)).when(booleanStatement).getQueryList();

        queryAnnotator.annotate(booleanStatement);
        verify(getHolder(), times(1)).newAnnotation(eq(HighlightSeverity.ERROR), eq(
                "Expected boolean, got string"
        ));
    }

    @Test
    void annotateBooleanStatementThrowsMultipleErrorsWhenNotBoolean() {
        final OMTQuery query2 = mock(OMTQuery.class);
        doReturn(Collections.singletonList(getRDFModelUtil().getStringType())).when(query).resolveToResource();
        doReturn(Collections.singletonList(getRDFModelUtil().getStringType())).when(query2).resolveToResource();
        final OMTBooleanStatement booleanStatement = mock(OMTBooleanStatement.class);
        doReturn(Arrays.asList(query, query2)).when(booleanStatement).getQueryList();

        queryAnnotator.annotate(booleanStatement);
        verify(getHolder(), times(2)).newAnnotation(eq(HighlightSeverity.ERROR), eq(
                "Expected boolean, got string"
        ));
    }

    @Test
    void annotateBooleanStatementThrowsNoErrorsWhenAllBoolean() {
        final OMTQuery query2 = mock(OMTQuery.class);
        doReturn(Collections.singletonList(getRDFModelUtil().getBooleanType())).when(query).resolveToResource();
        doReturn(Collections.singletonList(getRDFModelUtil().getBooleanType())).when(query2).resolveToResource();
        final OMTBooleanStatement booleanStatement = mock(OMTBooleanStatement.class);
        doReturn(Arrays.asList(query, query2)).when(booleanStatement).getQueryList();

        queryAnnotator.annotate(booleanStatement);
        verifyNoErrors();
    }

}
