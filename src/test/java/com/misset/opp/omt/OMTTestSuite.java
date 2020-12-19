package com.misset.opp.omt;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.util.Query;
import com.misset.opp.omt.psi.util.*;
import com.misset.opp.omt.util.BuiltInUtil;
import com.misset.opp.omt.util.ProjectUtil;
import com.misset.opp.omt.util.RDFModelUtil;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.util.UtilManager.*;
import static org.mockito.Mockito.mock;

public class OMTTestSuite extends LightJavaCodeInsightFixtureTestCase {

    private MockedStatic<ReferencesSearch> referencesSearchMockedStatic;

    protected static final String XSD_BOOLEAN = "http://www.w3.org/2001/XMLSchema#boolean";
    protected static final String XSD_STRING = "http://www.w3.org/2001/XMLSchema#string";
    protected static final String XSD_INTEGER = "http://www.w3.org/2001/XMLSchema#int";
    protected static final String XSD_DOUBLE = "http://www.w3.org/2001/XMLSchema#double";
    protected static final String RDF_TYPE = "http://www.w3.org/2001/XMLSchema#double";
    private MockedStatic<PsiTreeUtil> psiTreeUtil;

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
        ApplicationManager.getApplication().runReadAction(() -> getProjectUtil().loadOntologyModel(getProject()));
    }

    protected void setBuiltinOperators() {
        myFixture.copyFileToProject(new File("src/test/resources/builtinOperators.ts").getAbsolutePath(), "builtinOperators.ts");
    }

    protected void setBuiltinCommands() {
        myFixture.copyFileToProject(new File("src/test/resources/builtinCommands.ts").getAbsolutePath(), "builtinCommands.ts");
    }

    protected void setBuiltin() {
        setBuiltinCommands();
        setBuiltinOperators();
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
            TokenFinderUtil tokenFinderUtil = getTokenFinderUtil();
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
            utilManager.when(UtilManager::getTokenFinderUtil).thenReturn(tokenFinderUtil);
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
        assertNotNull("Fixture is not created, run setup", myFixture);
        final PsiFile psiFile = myFixture.configureByText("test.omt", content);

        final Collection<PsiElement> allPsiElement =
                PsiTreeUtil.findChildrenOfType(psiFile, clazz);
        return allPsiElement.stream()
                .map(element -> (T) element)
                .filter(element -> condition.test(element))
                .findFirst().orElse(null);
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
}
