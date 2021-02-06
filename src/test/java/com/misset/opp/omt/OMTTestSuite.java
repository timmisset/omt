package com.misset.opp.omt;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.util.Query;
import com.misset.opp.omt.psi.util.*;
import com.misset.opp.omt.util.BuiltInUtil;
import com.misset.opp.omt.util.ProjectUtil;
import com.misset.opp.omt.util.RDFModelUtil;
import com.misset.opp.omt.util.UtilManager;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.misset.opp.omt.util.UtilManager.*;
import static org.mockito.Mockito.mock;

public class OMTTestSuite extends LightJavaCodeInsightFixtureTestCase {

    private MockedStatic<ReferencesSearch> referencesSearchMockedStatic;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected static final String XSD_BOOLEAN = "http://www.w3.org/2001/XMLSchema#boolean";
    protected static final String XSD_STRING = "http://www.w3.org/2001/XMLSchema#string";
    protected static final String XSD_INTEGER = "http://www.w3.org/2001/XMLSchema#int";
    protected static final String XSD_DOUBLE = "http://www.w3.org/2001/XMLSchema#double";
    protected static final String RDF_TYPE = "http://www.w3.org/2001/XMLSchema#double";
    private MockedStatic<PsiTreeUtil> psiTreeUtil;

    /**
     * Returns the type resource, only works when setOntologyModel() has been called
     */
    protected Resource rdfType() {
        return rdfType(getProjectUtil().getOntologyModel());
    }

    protected Resource rdfType(Model model) {
        return model.createResource(RDF_TYPE);
    }

    protected Resource xsdBoolean(Model model) {
        return model.createResource(XSD_BOOLEAN);
    }

    // @Test
    // Run this test once locally to check if an environmental variable can be retrieved for puslishing the plugin
    // to the IntelliJ repository
    void systemToken() {
        final String token = System.getenv().getOrDefault("ORG_GRADLE_PROJECT_intellijPublishToken", "");
        if (token.isEmpty()) {
            fail("A token is required for publishing the plugin");
        }
    }

    protected Resource xsdString(Model model) {
        return model.createResource(XSD_STRING);
    }

    protected Resource xsdInteger(Model model) {
        return model.createResource(XSD_INTEGER);
    }

    /**
     * Returns the string type resource, only works when setOntologyModel() has been called
     */
    protected Resource xsdString() {
        return xsdString(getProjectUtil().getOntologyModel());
    }

    protected Resource createResource(String localName) {
        return getProjectUtil().getOntologyModel().createResource("http://ontologie#" + localName);
    }

    protected List<Resource> classesAsResourceList(String... classes) {
        return classesAsResourceList(getProjectUtil().getOntologyModel(), classes);
    }

    private MockedStatic<UtilManager> utilManager;

    protected List<Resource> classesAsResourceList(Model model, String... classes) {
        return Arrays.stream(classes).map(
                classId -> model.createResource("http://ontologie#" + classId)
        ).collect(Collectors.toList());
    }

    protected Resource xsdDouble(Model model) {
        return model.createResource(XSD_DOUBLE);
    }

    @Override
    protected void tearDown() throws Exception {
        if (utilManager != null && !utilManager.isClosed()) {
            utilManager.close();
        }
        if (psiTreeUtil != null && !psiTreeUtil.isClosed()) {
            psiTreeUtil.close();
        }
        if (referencesSearchMockedStatic != null && !referencesSearchMockedStatic.isClosed()) {
            referencesSearchMockedStatic.close();
        }
        if (myFixture != null) {
            super.tearDown();
        }
    }

    protected MockedStatic<PsiTreeUtil> getPsiTreeUtilMock() {
        if (psiTreeUtil == null || psiTreeUtil.isClosed()) {
            psiTreeUtil = Mockito.mockStatic(PsiTreeUtil.class);
        }
        return psiTreeUtil;
    }

    protected void setPsiTreeUtilMock(Consumer<MockedStatic<PsiTreeUtil>> consumer) {
        final MockedStatic<PsiTreeUtil> psiTreeUtilMock = getPsiTreeUtilMock();
        consumer.accept(psiTreeUtilMock);
    }

    protected void setPsiTreeUtilMockWhenThenReturn(MockedStatic.Verification verification, Object thenReturn) {
        final MockedStatic<PsiTreeUtil> psiTreeUtilMock = getPsiTreeUtilMock();
        psiTreeUtilMock.when(verification).thenReturn(thenReturn);
    }

    protected void setOntologyModel() {
        myFixture.copyFileToProject(new File("src/test/resources/examples/model.ttl").getAbsolutePath(), "test/resources/examples/root.ttl");
        ApplicationManager.getApplication().invokeAndWait(() -> getProjectUtil().loadOntologyModel(getProject(), false));
    }

    protected void setBuiltinOperators() {
        myFixture.copyFileToProject(new File("src/test/resources/builtinOperators.ts").getAbsolutePath(), "builtinOperators.ts");
    }

    protected void setBuiltinCommands() {
        myFixture.copyFileToProject(new File("src/test/resources/builtinCommands.ts").getAbsolutePath(), "builtinCommands.ts");
    }

    protected void setReasons() {
        myFixture.copyFileToProject(new File("src/test/resources/reasons.json").getAbsolutePath(), "reasons/reasons.json");
        ReadAction.run(() -> getProjectUtil().loadReasons(myFixture.getProject()));
    }

    protected void setBuiltin() {
        setBuiltinCommands();
        setBuiltinOperators();
        ReadAction.run(() -> getProjectUtil().loadBuiltInMembers(myFixture.getProject()));
    }

    protected void setBuiltinAndModel() {
        setBuiltin();
        setOntologyModel();
    }

    private void startUtilMock() {
        if (utilManager == null || utilManager.isClosed()) {
            // get the original utils first to be returned as valid utils when they are not mocked
            // since the entire UtilManager is statically mocked we need to manually return the non-mocked utils

            ProjectUtil projectUtil = getProjectUtil();
            QueryUtil queryUtil = getQueryUtil();
            MemberUtil memberUtil = getMemberUtil();
            ImportUtil importUtil = getImportUtil();
            ScriptUtil scriptUtil = getScriptUtil();
            ModelUtil modelUtil = getModelUtil();
            BuiltInUtil builtInUtil = getBuiltinUtil();
            TokenUtil tokenUtil = getTokenUtil();
            VariableUtil variableUtil = getVariableUtil();
            CurieUtil curieUtil = getCurieUtil();
            RDFModelUtil rdfModelUtil = getRDFModelUtil();

            utilManager = Mockito.mockStatic(UtilManager.class);
            utilManager.when(UtilManager::getProjectUtil).thenReturn(projectUtil);
            utilManager.when(UtilManager::getQueryUtil).thenReturn(queryUtil);
            utilManager.when(UtilManager::getMemberUtil).thenReturn(memberUtil);
            utilManager.when(UtilManager::getImportUtil).thenReturn(importUtil);
            utilManager.when(UtilManager::getScriptUtil).thenReturn(scriptUtil);
            utilManager.when(UtilManager::getModelUtil).thenReturn(modelUtil);
            utilManager.when(UtilManager::getBuiltinUtil).thenReturn(builtInUtil);
            utilManager.when(UtilManager::getTokenUtil).thenReturn(tokenUtil);
            utilManager.when(UtilManager::getVariableUtil).thenReturn(variableUtil);
            utilManager.when(UtilManager::getCurieUtil).thenReturn(curieUtil);
            utilManager.when(UtilManager::getRDFModelUtil).thenReturn(rdfModelUtil);
        }
    }

    protected void setUtilMock(ProjectUtil projectUtil) {
        validateMock(projectUtil);
        utilManager.when(UtilManager::getProjectUtil).thenReturn(projectUtil);
    }

    protected void setUtilMock(VariableUtil variableUtil) {
        validateMock(variableUtil);
        utilManager.when(UtilManager::getVariableUtil).thenReturn(variableUtil);
    }

    protected void setUtilMock(ImportUtil importUtil) {
        validateMock(importUtil);
        utilManager.when(UtilManager::getImportUtil).thenReturn(importUtil);
    }

    protected void setUtilMock(MemberUtil memberUtil) {
        validateMock(memberUtil);
        utilManager.when(UtilManager::getMemberUtil).thenReturn(memberUtil);
    }

    protected void setUtilMock(CurieUtil curieUtil) {
        validateMock(curieUtil);
        utilManager.when(UtilManager::getCurieUtil).thenReturn(curieUtil);
    }

    protected void setUtilMock(RDFModelUtil rdfModelUtil) {
        validateMock(rdfModelUtil);
        utilManager.when(UtilManager::getRDFModelUtil).thenReturn(rdfModelUtil);
    }

    protected void setUtilMock(BuiltInUtil builtInUtil) {
        validateMock(builtInUtil);
        utilManager.when(UtilManager::getBuiltinUtil).thenReturn(builtInUtil);
    }

    protected void setUtilMock(ModelUtil modelUtil) {
        validateMock(modelUtil);
        utilManager.when(UtilManager::getModelUtil).then((invocationOnMock) -> modelUtil);
    }

    protected void setUtilMock(ScriptUtil scriptUtil) {
        validateMock(scriptUtil);
        utilManager.when(UtilManager::getScriptUtil).thenReturn(scriptUtil);
    }

    protected void setUtilMock(QueryUtil queryUtil) {
        validateMock(queryUtil);
        utilManager.when(UtilManager::getQueryUtil).thenReturn(queryUtil);
    }

    protected void setUtilMock(TokenUtil tokenUtil) {
        validateMock(tokenUtil);
        utilManager.when(UtilManager::getTokenUtil).thenReturn(tokenUtil);
    }

    protected void setSearchReferenceMock(PsiElement element, Consumer<Query> queryConsumer) {
        referencesSearchMockedStatic = Mockito.mockStatic(ReferencesSearch.class);
        final Query queryMock = mock(Query.class);
        queryConsumer.accept(queryMock);
        referencesSearchMockedStatic.when(() -> ReferencesSearch.search(Mockito.eq(element))).thenReturn(queryMock);
    }

    private void validateMock(Object mockInstance) {
        assertNotNull("Mock has not been created yet", mockInstance);
        startUtilMock();
    }

    protected <T> T fromContent(String content, Class<? extends PsiElement> clazz) {
        return fromContent(content, clazz, item -> true);
    }

    protected <T> T fromContent(String content, Class<? extends PsiElement> clazz, Predicate<T> condition) {
        assertFixtureExists();
        final PsiFile psiFile = myFixture.configureByText("test.omt", content);

        final Collection<PsiElement> allPsiElement =
                PsiTreeUtil.findChildrenOfType(psiFile, clazz);
        return allPsiElement.stream()
                .map(element -> (T) element)
                .filter(element -> condition.test(element))
                .findFirst().orElse(null);
    }

    protected PsiFile addFile(String name, String content) {
        return myFixture.addFileToProject("./" + name, content);
    }

    private void assertFixtureExists() {
        assertNotNull("Fixture is not created, run setup", myFixture);
    }

    protected String getFileName() {
        return String.format("test-%s.omt", LocalDateTime.now().getNano());
    }

    protected String withPrefixes(String content) {
        return String.format("prefixes:\n" +
                "   ont:     <http://ontologie#>\n" +
                "   rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "\n" +
                "%s", content);
    }

    /**
     * Places the input statement into a template with common prefixes and inside a queries block
     *
     * @param queryStatement only the queryStatement without the define and semicolon
     * @return
     */
    protected String queryWithPrefixes(String queryStatement) {
        return withPrefixes(String.format("queries: |\n" +
                "   DEFINE QUERY query => %s;\n" +
                "\n", queryStatement));

    }

    protected void withProgress(Runnable runnable) {
        ProgressManager.getInstance().runProcessWithProgressSynchronously(
                runnable, "Test", false, getProject()
        );
    }

    protected void assertNoErrors() {
        assertNoHighlighting(HighlightSeverity.ERROR);
    }

    protected void assertNoWarnings() {
        assertNoHighlighting(HighlightSeverity.WARNING);
    }

    private void assertNoHighlighting(HighlightSeverity severity) {
        final List<HighlightInfo> highlighting = getHighlighting(severity);
        if (!highlighting.isEmpty()) {
            fail(String.format("All highlighting%n%s", allHighlightingAsMessage())); // cannot use assertEmpty since it will call object.toString() which is not accessible unless in read-mode
        }
    }

    protected void assertHasError(String message) {
        assertHasHighlightingMessage(HighlightSeverity.ERROR, message);
    }

    protected void assertHasWarning(String message) {
        assertHasHighlightingMessage(HighlightSeverity.WARNING, message);
    }

    protected void assertHasWeakWarning(String message) {
        assertHasHighlightingMessage(HighlightSeverity.WEAK_WARNING, message);
    }

    protected void assertHasInformation(String message) {
        assertHasHighlightingMessage(HighlightSeverity.INFORMATION, message);
    }

    private void assertHasHighlightingMessage(HighlightSeverity severity, String message) {
        assertFixtureExists();
        final List<HighlightInfo> highlighting = getHighlighting(severity);
        assertTrue(
                allHighlightingAsMessage(),
                highlighting.stream().anyMatch(
                        highlightInfo -> highlightInfo.getDescription().equals(message)
                ));
    }

    private List<HighlightInfo> getHighlighting(HighlightSeverity severity) {
        return myFixture.doHighlighting().stream().filter(
                highlightInfo -> highlightInfo.getSeverity() == severity
        ).collect(Collectors.toList());
    }

    private String allHighlightingAsMessage() {
        return myFixture.doHighlighting().stream()
                .map(highlightInfo -> String.format(
                        "%s: %s", highlightInfo.getSeverity().myName, highlightInfo.getDescription()
                )).collect(Collectors.joining("\n"));
    }

    protected void getElementAtCaret(String content, Consumer<PsiElement> elementConsumer, Class<? extends PsiElement> elementAtCaretClass, boolean consumeInReader) {
        getElementAtCaret(getFileName(), content, elementConsumer, elementAtCaretClass, consumeInReader);
    }

    protected void getElementAtCaret(String filename, String content, Consumer<PsiElement> elementConsumer, Class<? extends PsiElement> elementAtCaretClass, boolean consumeInReader) {
        myFixture.configureByText(filename, content);
        final PsiElement elementAtCaret = ReadAction.compute(() -> {
            PsiElement element = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
            if (element instanceof PsiWhiteSpace) {
                fail("Whitespace element, move the caret to the start of the PsiElement");
            }
            element = PsiTreeUtil.getParentOfType(element, elementAtCaretClass, false); // not strict will return itself if match
            assertNotNull("No element found at caret of " + elementAtCaretClass.getName(), element);

            if (consumeInReader) {
                elementConsumer.accept(element);
            }
            return element;
        });
        if (!consumeInReader) {
            elementConsumer.accept(elementAtCaret);
        }
    }

    protected <T extends PsiElement> T getElement(Class<? extends T> className) {
        return getElement(className, t -> true);
    }

    protected <T extends PsiElement> List<T> getElements(Class<? extends T> className) {
        return getElements(className, t -> true);
    }

    protected <T extends PsiElement> T getElement(Class<? extends T> className, Predicate<T> condition) {
        return className.cast(ReadAction.compute(() ->
                        PsiTreeUtil.findChildrenOfType(myFixture.getFile(), className)
                                .stream().filter(condition)
                                .findFirst()
                                .orElse(null)
                )
        );
    }

    protected <T extends PsiElement> List<T> getElements(Class<? extends T> className, Predicate<T> condition) {
        return getElements(getFile(), className, condition);
    }

    protected <T extends PsiElement> List<T> getElements(PsiElement container, Class<? extends T> className, Predicate<T> condition) {
        return ReadAction.compute(() ->
                PsiTreeUtil.findChildrenOfType(container, className)
                        .stream().filter(condition)
                        .map(className::cast)
                        .collect(Collectors.toList())
        );
    }

    protected void setExampleFileActivityWithImportsPrefixesParamsVariablesGraphsPayload() {
        process("examples/activity_with_imports_prefixes_params_variables_graphs_payload.omt");
    }

    protected void setExampleFileActivityWithVariablesActions() {
        process("examples/activity_with_variables_actions.omt");
    }

    protected void setExampleFileLoadOntology() {
        process("examples/load_ontology.omt");
    }

    protected void setExampleFileActivityWithMembers() {
        process("examples/activity_with_members.omt");
    }

    protected void setExampleFileProcedureWithScript() {
        process("examples/procedure_with_script.omt");
    }

    protected void setExampleFileActivityWithImports() {
        process("examples/activity_with_imports.omt");
    }

    protected void setExampleFileActivityWithVariables() {
        process("examples/activity_with_variables.omt");
    }

    private void process(String resourcePath) {

        File file = new File(getClass().getClassLoader().getResource(resourcePath).getFile());
        try {
            String data = new String(Files.readAllBytes(file.toPath()));
            myFixture.configureByText(getFileName(), data);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
