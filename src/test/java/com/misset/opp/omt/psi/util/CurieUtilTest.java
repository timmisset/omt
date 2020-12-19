package com.misset.opp.omt.psi.util;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.*;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CurieUtilTest extends OMTTestSuite {

    @Mock
    AnnotationHolder annotationHolder;
    @Mock
    AnnotationBuilder annotationBuilder;
    @Spy
    ProjectUtil projectUtil;

    @InjectMocks
    CurieUtil curieUtil;

    private ExampleFiles exampleFiles;
    PsiElement rootBlock;

    private OMTNamespacePrefix abcDeclared;
    private OMTNamespacePrefix abcUsage;
    private OMTNamespacePrefix def;
    private OMTNamespacePrefix ghi;

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
    void testGetDefinedByPrefixForOMTParameterType() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTParameterType parameterType = exampleFiles.getPsiElementFromRootDocument(OMTParameterType.class, rootBlock);
            Optional<OMTPrefix> definedByPrefix = curieUtil.getDefinedByPrefix(parameterType);

            assertTrue(definedByPrefix.isPresent());
            assertEquals(definedByPrefix.get().getNamespacePrefix().getName(), parameterType.getNamespacePrefix().getName());
        });
    }

    @Test
    void testGetDefinedByPrefixForOMTCurieElement() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCurieElement curieElement = exampleFiles.getPsiElementFromRootDocument(OMTCurieElement.class, rootBlock);
            Optional<OMTPrefix> definedByPrefix = curieUtil.getDefinedByPrefix(curieElement);

            assertTrue(definedByPrefix.isPresent());
            assertEquals(definedByPrefix.get().getNamespacePrefix().getName(), curieElement.getNamespacePrefix().getName());
        });
    }

    @Test
    void testGetDefinedByPrefixOMTCurieElementReturnsEmpty() {
        assertEquals(Optional.empty(), curieUtil.getDefinedByPrefix(null, mock(OMTCurieElement.class)));
    }

    @Test
    void testGetDefinedByPrefixOMTParameterTypeReturnsEmpty() {
        assertEquals(Optional.empty(), curieUtil.getDefinedByPrefix(null, mock(OMTParameterType.class)));
    }

    @Test
    void getPrefixBlockReturnsExisting() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCurieElement curieElement = exampleFiles.getPsiElementFromRootDocument(OMTCurieElement.class, rootBlock);
            OMTPrefixBlock prefixBlock = exampleFiles.getPsiElementFromRootDocument(OMTPrefixBlock.class, rootBlock);
            OMTPrefixBlock returnedPrefixBlock = curieUtil.getPrefixBlock(curieElement);
            assertEquals(prefixBlock, returnedPrefixBlock);
        });
    }

    @Test
    void getPrefixBlockReturnsExistingWhenNew() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCurieElement curieElement = exampleFiles.getPsiElementFromRootDocument(OMTCurieElement.class, rootBlock);
            OMTPrefixBlock prefixBlock = exampleFiles.getPsiElementFromRootDocument(OMTPrefixBlock.class, rootBlock);
            OMTPrefixBlock returnedPrefixBlock = curieUtil.getPrefixBlock(curieElement);
            assertEquals(prefixBlock, returnedPrefixBlock);
        });
    }

    @Test
    void getPrefixBlockReturnsNull() {
        PsiElement activityWithoutPrefixBlock = exampleFiles.getActivityWithQueryWatcher();
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTVariable variable = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, activityWithoutPrefixBlock);
            OMTPrefixBlock returnedPrefixBlock = curieUtil.getPrefixBlock(variable);
            assertNull(returnedPrefixBlock);
        });
    }

    @Test
    void addPrefixToBlockFromString() {
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            OMTPrefix prefix = exampleFiles.getPsiElementFromRootDocument(OMTPrefix.class, rootBlock);
            curieUtil.addPrefixToBlock(prefix, "def", "<http://ontologie.alfabet.nl/def#>");
            OMTPrefixBlock prefixBlock = exampleFiles.getPsiElementFromRootDocument(OMTPrefixBlock.class, rootBlock);
            String asText = prefixBlock.getText();
            assertSameContent("prefixes:\n" +
                    "    /**\n" +
                    "    * Some info about abc\n" +
                    "    */\n" +
                    "    abc:    <http://ontologie.alfabet.nl/alfabet#>\n" +
                    "    foaf:   <http://ontologie.foaf.nl/friendOfAfriend#> // and about foaf\n" +
                    "    def:    <http://ontologie.alfabet.nl/def#>\n" +
                    "\n", asText);
        });
    }

    @Test
    void addNewPrefixToBlockFromString() {
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            OMTPrefix prefix = exampleFiles.getPsiElementFromRootDocument(OMTPrefix.class, rootBlock);

            OMTFile file = (OMTFile) spy(prefix.getContainingFile());
            OMTPrefix prefixSpy = spy(prefix);
            doReturn(file).when(prefixSpy).getContainingFile();
            doReturn(Optional.empty()).when(file).getRootBlock("prefixes");

            curieUtil.addPrefixToBlock(prefixSpy, "def", "<http://ontologie.alfabet.nl/def#>");
            OMTPrefixBlock prefixBlock = exampleFiles.getPsiElementFromRootDocument(OMTPrefixBlock.class, rootBlock);
            String asText = prefixBlock.getText();
            assertSameContent("prefixes:\n" +
                    "    def:    <http://ontologie.alfabet.nl/def#>\n" +
                    "\n", asText);
        });
    }

    @Test
    void addPrefixToBlockFromPrefix() {
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            OMTPrefix prefix = exampleFiles.getPsiElementFromRootDocument(OMTPrefix.class, rootBlock);
            curieUtil.addPrefixToBlock(prefix, prefix);
            OMTPrefixBlock prefixBlock = exampleFiles.getPsiElementFromRootDocument(OMTPrefixBlock.class, rootBlock);
            String asText = prefixBlock.getText();
            assertSameContent("prefixes:\n" +
                    "    /**\n" +
                    "    * Some info about abc\n" +
                    "    */\n" +
                    "    abc:    <http://ontologie.alfabet.nl/alfabet#>\n" +
                    "    foaf:   <http://ontologie.foaf.nl/friendOfAfriend#> // and about foaf\n" +
                    "    abc:    <http://ontologie.alfabet.nl/alfabet#>\n" +
                    "\n", asText);
        });
    }

    @Test
    void annotateParameterTypeAsResource() {
        List<HighlightInfo> highlighting = getHighlighting("prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        params:\n" +
                "        -   $paramA (ont:Something)\n" +
                "        ");
        assertEquals(2, highlighting.size());
        assertEquals("http://ontologie#Something", highlighting.get(1).getDescription());
    }

    List<HighlightInfo> getHighlighting(String content) {
        myFixture.configureByText("test.omt", content);
        return myFixture.doHighlighting();
    }


    private void loadUndeclaredNamespacePrefixes(PsiElement rootBlock) {
        // TODO: refactor to usage of the predicate getPsiElementsFromRootDocument
        List<OMTNamespacePrefix> prefixes = exampleFiles.getPsiElementsFromRootDocument(OMTNamespacePrefix.class, rootBlock);
        prefixes.forEach(prefix ->
                {
                    switch (Objects.requireNonNull(prefix.getName())) {
                        case "def":
                            def = prefix;
                            break;
                        case "ghi":
                            ghi = prefix;
                            break;
                        case "abc":
                            if (abcDeclared == null) {
                                abcDeclared = prefix;
                            } else {
                                abcUsage = prefix;
                            }
                            break;
                    }
                }
        );
    }

    private void assertSameContent(String expected, String value) {
        assertEquals(expected.replaceAll("\\s+", ""), value.replaceAll("\\s+", ""));
    }
}
