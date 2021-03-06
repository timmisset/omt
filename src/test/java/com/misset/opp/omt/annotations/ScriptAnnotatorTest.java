package com.misset.opp.omt.annotations;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTCommandBlock;
import com.misset.opp.omt.psi.OMTIfBlock;
import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.OMTReturnStatement;
import com.misset.opp.omt.psi.OMTScriptContent;
import com.misset.opp.omt.psi.OMTScriptLine;
import com.misset.opp.omt.psi.util.ModelUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static com.misset.opp.omt.psi.OMTTypes.SEMICOLON;
import static com.misset.opp.util.UtilManager.getRDFModelUtil;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ScriptAnnotatorTest extends OMTAnnotationTest {

    @Mock
    OMTScriptContent scriptContent;

    @Mock
    OMTScriptLine scriptLine;

    @Mock
    PsiElement element;

    @Mock
    ModelUtil modelUtil;

    @Mock
    ASTNode astNode;

    @Mock
    OMTQuery query;

    @Mock
    OMTIfBlock ifBlock;

    @InjectMocks
    ScriptAnnotator scriptAnnotator;

    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("ScriptAnnotatorTest");
        super.setUp();
        MockitoAnnotations.openMocks(this);
        setUtilMock(modelUtil);
        doReturn(true).when(modelUtil).isScalarEntry(any());
        doReturn(astNode).when(element).getNode();
        doReturn(query).when(ifBlock).getQuery();

    }

    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void annotateScriptNoAnnotationWhenInvalidType() {
        scriptAnnotator.annotate(mock(PsiElement.class));
        verifyNoAnnotations();
    }

    @Test
    void annotateSemicolonForScriptContentThrowsErrorWhenNoNextPsiElement() {
        setPsiTreeUtilMockWhenThenReturn(() -> PsiTreeUtil.nextVisibleLeaf(eq(scriptContent)), null);
        setPsiTreeUtilMockWhenThenReturn(() -> PsiTreeUtil.getParentOfType(eq(scriptContent), eq(OMTCommandBlock.class)), mock(OMTCommandBlock.class));
        scriptAnnotator.annotate(scriptContent);
        verify(getHolder()).newAnnotation(eq(HighlightSeverity.ERROR), eq("; expected"));
    }

    @Test
    void annotateSemicolonForScriptContentThrowsErrorWhenNotEndingWithSemicolon() {
        setPsiTreeUtilMockWhenThenReturn(() -> PsiTreeUtil.nextVisibleLeaf(eq(scriptContent)), element);
        setPsiTreeUtilMockWhenThenReturn(() -> PsiTreeUtil.getParentOfType(eq(scriptContent), eq(OMTCommandBlock.class)), mock(OMTCommandBlock.class));
        doReturn(null).when(astNode).getElementType();

        scriptAnnotator.annotate(scriptContent);
        verify(getHolder()).newAnnotation(eq(HighlightSeverity.ERROR), eq("; expected"));
    }

    @Test
    void annotateSemicolonForScriptContentThrowsNoErrorWhenEndingWithSemicolon() {
        setPsiTreeUtilMockWhenThenReturn(() -> PsiTreeUtil.nextVisibleLeaf(eq(scriptContent)), element);

        doReturn(SEMICOLON).when(astNode).getElementType();

        scriptAnnotator.annotate(scriptContent);
        verifyNoErrors();
    }

    @Test
    void annotateSemicolonForScriptContentThrowsNoErrorWhenNotEndingWithSemicolonButNotScalarEntry() {
        setPsiTreeUtilMockWhenThenReturn(() -> PsiTreeUtil.nextVisibleLeaf(eq(scriptContent)), element);

        doReturn(null).when(astNode).getElementType();
        doReturn(false).when(modelUtil).isScalarEntry(eq(scriptContent));

        scriptAnnotator.annotate(scriptContent);
        verifyNoErrors();
    }

    @Test
    void annotateSemicolonForScriptContentThrowsErrorWhenEndingWithSemicolonButQueryEntry() {
        setPsiTreeUtilMockWhenThenReturn(() -> PsiTreeUtil.nextVisibleLeaf(eq(scriptContent)), element);

        doReturn(SEMICOLON).when(astNode).getElementType();
        doReturn(false).when(modelUtil).isScalarEntry(eq(scriptContent));
        doReturn(true).when(modelUtil).isQueryEntry(eq(scriptContent));

        scriptAnnotator.annotate(scriptContent);
        verify(getHolder()).newAnnotation(eq(HighlightSeverity.ERROR), eq("Query entry should not end with semicolon"));
    }

    @Test
    void annotateSemicolonForScriptContentThrowsNoErrorWhenEndingWithSemicolonButNotQueryEntry() {
        setPsiTreeUtilMockWhenThenReturn(() -> PsiTreeUtil.nextVisibleLeaf(eq(scriptContent)), element);

        doReturn(SEMICOLON).when(astNode).getElementType();
        doReturn(false).when(modelUtil).isScalarEntry(eq(scriptContent));
        doReturn(false).when(modelUtil).isQueryEntry(eq(scriptContent));

        scriptAnnotator.annotate(scriptContent);
        verifyNoErrors();
    }

    @Test
    void annotateBooleanThrowsNoErrorWhenNoType() {
        setOntologyModel();
        doReturn(Collections.emptyList()).when(query).resolveToResource();

        scriptAnnotator.annotate(ifBlock);
        verifyNoErrors();
    }

    @Test
    void annotateBooleanThrowsErrorWhenWrongType() {
        setOntologyModel();
        doReturn(Collections.singletonList(getRDFModelUtil().getStringType())).when(query).resolveToResource();

        scriptAnnotator.annotate(ifBlock);
        verify(getHolder(), times(1)).newAnnotation(eq(HighlightSeverity.ERROR), eq(
                "Expected boolean, got string"
        ));
    }

    @Test
    void annotateScriptLineThrowsErrorWhenUnreachable() {
        final OMTScriptLine previousLine = mock(OMTScriptLine.class);
        setPsiTreeUtilMock(
                psiTreeUtilMockedStatic -> {
                    psiTreeUtilMockedStatic.when(
                            () -> PsiTreeUtil.getPrevSiblingOfType(eq(scriptLine), eq(OMTScriptLine.class))
                    ).thenReturn(previousLine);
                    psiTreeUtilMockedStatic.when(
                            () -> PsiTreeUtil.findChildOfType(eq(previousLine), eq(OMTReturnStatement.class), eq(true), eq(OMTCommandBlock.class))
                    ).thenReturn(mock(PsiElement.class));
                }
        );
        scriptAnnotator.annotate(scriptLine);
        verifyError("Unreachable code");
    }

    @Test
    void annotateScriptLineThrowsErrorWhenUnreachableByPreviousToAdjecentSibling() {
        final OMTScriptLine firstPreviousLine = mock(OMTScriptLine.class);
        final OMTScriptLine secondPreviousLine = mock(OMTScriptLine.class);
        setPsiTreeUtilMock(
                psiTreeUtilMockedStatic -> {
                    psiTreeUtilMockedStatic.when(
                            () -> PsiTreeUtil.getPrevSiblingOfType(eq(scriptLine), eq(OMTScriptLine.class))
                    ).thenReturn(firstPreviousLine);
                    psiTreeUtilMockedStatic.when(
                            () -> PsiTreeUtil.getPrevSiblingOfType(eq(firstPreviousLine), eq(OMTScriptLine.class))
                    ).thenReturn(secondPreviousLine);
                    psiTreeUtilMockedStatic.when(
                            () -> PsiTreeUtil.findChildOfType(eq(secondPreviousLine), eq(OMTReturnStatement.class), eq(true), eq(OMTCommandBlock.class))
                    ).thenReturn(mock(PsiElement.class));
                }
        );
        scriptAnnotator.annotate(scriptLine);
        verifyError("Unreachable code");
    }

    @Test
    void annotateScriptLineThrowsNoErrorWhenNotUnreachable() {
        final OMTScriptLine previousLine = mock(OMTScriptLine.class);
        setPsiTreeUtilMock(
                psiTreeUtilMockedStatic -> {
                    psiTreeUtilMockedStatic.when(
                            () -> PsiTreeUtil.getPrevSiblingOfType(eq(scriptLine), eq(OMTScriptLine.class))
                    ).thenReturn(previousLine);
                    psiTreeUtilMockedStatic.when(
                            () -> PsiTreeUtil.getPrevSiblingOfType(eq(previousLine), eq(OMTScriptLine.class))
                    ).thenReturn(null);
                    psiTreeUtilMockedStatic.when(
                            () -> PsiTreeUtil.findChildOfType(eq(previousLine), eq(OMTReturnStatement.class), eq(true), eq(OMTCommandBlock.class))
                    ).thenReturn(null);
                }
        );
        scriptAnnotator.annotate(scriptLine);
        verifyNoErrors();
    }

    @Test
    void annotateScriptLineThrowsNoErrorWhenNoPreviousSibling() {
        setPsiTreeUtilMock(
                psiTreeUtilMockedStatic -> psiTreeUtilMockedStatic.when(
                        () -> PsiTreeUtil.getPrevSiblingOfType(eq(scriptLine), eq(OMTScriptLine.class))
                ).thenReturn(null)
        );
        scriptAnnotator.annotate(scriptLine);
        verifyNoErrors();
    }

    @Test
    void annotateScriptLineIT() {
        String content = "commands: |\n" +
                "   DEFINE COMMAND command => {\n" +
                "       VAR $x = 'test';\n" +
                "       RETURN $x;\n" +
                "       $x = 'test2';\n" +
                "   }";
        myFixture.configureByText(getFileName(), content);
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEquals(1, highlighting.size());
        assertEquals("Unreachable code", highlighting.get(0).getDescription());
    }

    @Test
    void annotateScriptLineITThrowsNoError() {
        setOntologyModel();
        String content = "commands: |\n" +
                "   DEFINE COMMAND command => {\n" +
                "       VAR $x = 'test';\n" +
                "       IF 1 == 1 { RETURN $x; }\n" +
                "       $x = 'test2';\n" +
                "   }";
        myFixture.configureByText(getFileName(), content);
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEquals(0, highlighting.size());
    }
}
