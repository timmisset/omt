package com.misset.opp.omt.annotations;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.intentions.prefix.RegisterPrefixIntention;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.OMTNamespaceIri;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.util.ProjectUtil;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static util.UtilManager.getProjectUtil;

class CurieAnnotatorTest extends OMTAnnotationTest {

    @Mock
    OMTNamespacePrefix namespacePrefix;

    @Mock
    PsiReference reference;

    @Mock
    OMTPrefix knownPrefix;

    @Mock
    OMTNamespaceIri namespaceIri;

    @Mock
    ProjectUtil projectUtil;

    Model model;

    @InjectMocks
    CurieAnnotator curieAnnotator;

    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("CurieAnnotatorTest");
        super.setUp();

        MockitoAnnotations.openMocks(this);
        setOntologyModel();
        model = getProjectUtil().getOntologyModel();
        setUtilMock(projectUtil);

        doReturn(reference).when(namespacePrefix).getReference();
        doReturn("prefix").when(namespacePrefix).getName();
        doReturn(namespaceIri).when(knownPrefix).getNamespaceIri();
        doReturn("http://iri").when(namespaceIri).getName();
        List<OMTPrefix> knownPrefixes = new ArrayList<>();
        knownPrefixes.add(knownPrefix);
        doReturn(knownPrefixes).when(projectUtil).getKnownPrefixes(anyString());
    }

    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void annotateCurieNoAnnotationWhenInvalidType() {
        curieAnnotator.annotate(mock(PsiElement.class));
        verifyNoAnnotations();
    }

    @Test
    void annotateParameterTypeAnnotatesAsResource() {

        OMTCurieElement curieElement = mock(OMTCurieElement.class);
        Resource resource = xsdString(model);
        doReturn(resource).when(curieElement).getAsResource();

        curieAnnotator.annotate(curieElement);

        verifyInfo(xsdString(model).toString());
    }

    @Test
    void annotatePrefixThrowsNoWarningWhenUsed() {
        doReturn(mock(OMTPrefix.class)).when(namespacePrefix).getParent();
        setSearchReferenceMock(namespacePrefix, query -> doReturn(true).when(query).anyMatch(any()));
        curieAnnotator.annotate(namespacePrefix);
        verifyNoWarnings();
    }

    @Test
    void annotatePrefixThrowsWarningWhenNotUsed() {
        doReturn("prefix:").when(namespacePrefix).getText();
        doReturn(mock(OMTPrefix.class)).when(namespacePrefix).getParent();
        setSearchReferenceMock(namespacePrefix, query -> doReturn(false).when(query).anyMatch(any()));
        curieAnnotator.annotate(namespacePrefix);
        verifyWarning("prefix: is never used");
    }

    @Test
    void annotateCurieElementInQueryStepThrowsNoErrorWhenHasResolvableReference() {
        doReturn(mock(PsiElement.class)).when(reference).resolve();
        curieAnnotator.annotate(namespacePrefix);
        verifyNoErrors();
    }

    @Test
    void annotateCurieElementInQueryStepThrowErrorWhenHasNoResolvableReference() {
        final IntentionAction intentionAction = mock(IntentionAction.class);
        try (MockedStatic<RegisterPrefixIntention> intentionMockedStatic = mockStatic(RegisterPrefixIntention.class)) {
            intentionMockedStatic.when(() -> RegisterPrefixIntention.getRegisterPrefixIntention(eq(namespacePrefix), anyString()))
                    .thenReturn(intentionAction);
            curieAnnotator.annotate(namespacePrefix);
        }

        verifyError("Unknown prefix");
        verify(getBuilder()).withFix(eq(intentionAction));
    }
}
