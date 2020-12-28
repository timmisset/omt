package com.misset.opp.omt.psi.util;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.ExampleFiles;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.util.ProjectUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class CurieUtilTest extends OMTTestSuite {

    @Mock
    AnnotationHolder annotationHolder;
    @Mock
    AnnotationBuilder annotationBuilder;
    @Mock
    OMTNamespacePrefix namespacePrefix;

    @Spy
    ProjectUtil projectUtil;

    @InjectMocks
    CurieUtil curieUtil;

    private ExampleFiles exampleFiles;
    PsiElement rootBlock;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setName("CurieUtilTest");
        super.setUp();

        exampleFiles = new ExampleFiles(this, myFixture);

        MockitoAnnotations.openMocks(this);
        setUtilMock(projectUtil);

        setOntologyModel();

        rootBlock = exampleFiles.getActivityWithImportsPrefixesParamsVariablesGraphsPayload();
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
    void getDefinedByPrefixReturnsEmptyWhenPrefix() {
        doReturn(mock(OMTPrefix.class)).when(namespacePrefix).getParent();
        assertTrue(curieUtil.getDefinedByPrefix(namespacePrefix).isEmpty());
    }

    @Test
    void getDefinedByPrefixReturnsPrefixFromRoot() {
        String content = "" +
                "prefixes:\n" +
                "   ont: <http://ontologie>\n" +
                "queries: |\n" +
                "   DEFINE QUERY query => /ont:ClassA;";
        myFixture.configureByText(getFileName(), content);
        ReadAction.run(() -> {
            final OMTCurieElement curieElement = myFixture.findElementByText("ont:ClassA", OMTCurieElement.class);
            final Optional<OMTPrefix> definedByPrefix = curieUtil.getDefinedByPrefix(curieElement.getNamespacePrefix());
            definedByPrefix.get().getNamespaceIri().getName().equals("http://ontologie");
        });
    }

    @Test
    void getDefinedByPrefixReturnsPrefixFromModel() {
        String content = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       prefixes:\n" +
                "           ont: <http://ontologie>\n" +
                "       queries: |\n" +
                "           DEFINE QUERY query => /ont:ClassA;";
        myFixture.configureByText(getFileName(), content);
        ReadAction.run(() -> {
            final OMTCurieElement curieElement = myFixture.findElementByText("ont:ClassA", OMTCurieElement.class);
            final Optional<OMTPrefix> definedByPrefix = curieUtil.getDefinedByPrefix(curieElement.getNamespacePrefix());
            definedByPrefix.get().getNamespaceIri().getName().equals("http://ontologie");
        });
    }

    @Test
    void addPrefixToExistingBlock() {
        String content = "prefixes:\n" +
                "    ont: <http://ontologie>\n" +
                "\n" +
                "model:\n" +
                "    Activiteit: !Activity\n";

        String expected = "prefixes:\n" +
                "    ont:    <http://ontologie>\n" +
                "    abc:    <http://ontologie.alfabet.nl/abc#>\n" +
                "\n" +
                "model:\n" +
                "    Activiteit: !Activity\n";

        final PsiFile psiFile = myFixture.configureByText(getFileName(), content);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            curieUtil.addPrefixToBlock(psiFile, "abc", "http://ontologie.alfabet.nl/abc#");
            assertEquals(expected, myFixture.getEditor().getDocument().getText());
        });
    }

    @Test
    void addPrefixToNewBlock() {
        String content =
                "model:\n" +
                        "    Activiteit: !Activity\n";

        String expected = "prefixes:\n" +
                "    abc:    <http://ontologie.alfabet.nl/abc#>\n" +
                "\n" +
                "model:\n" +
                "    Activiteit: !Activity\n";

        final PsiFile psiFile = myFixture.configureByText(getFileName(), content);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            curieUtil.addPrefixToBlock(psiFile, "abc", "http://ontologie.alfabet.nl/abc#");
            assertEquals(expected, myFixture.getEditor().getDocument().getText());
        });
    }
}
