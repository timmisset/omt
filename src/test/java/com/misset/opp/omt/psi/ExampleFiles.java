package com.misset.opp.omt.psi;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExampleFiles {

    private final LightJavaCodeInsightFixtureTestCase testCase;
    private final JavaCodeInsightTestFixture fixture;

    public ExampleFiles(LightJavaCodeInsightFixtureTestCase testCase, JavaCodeInsightTestFixture myFixture) {
        this.testCase = testCase;
        this.fixture = myFixture;
    }

    public PsiElement getActivityWithImportsPrefixesParamsVariablesGraphsPayload() {
        return process("examples/activity_with_imports_prefixes_params_variables_graphs_payload.omt");
    }
    public PsiElement getModelWithWrongModelItemType() {
        return process("examples/model_with_wrong_model_item_type.omt");
    }
    public PsiElement getStandaloneQueryWithMissingAttribute() {
        return process("examples/standaloneQuery_with_missing_attribute.omt");
    }
    public PsiElement getActivityWithQueryWatcher() {
        return process("examples/activity_with_query_watcher.omt");
    }
    public PsiElement getActivityWithVariablesActions() {
        return process("examples/activity_with_variables_actions.omt");
    }

    public PsiElement getLoadOntology() {
        return process("examples/load_ontology.omt");
    }

    public PsiElement getActivityWithMembers() {
        return process("examples/activity_with_members.omt");
    }

    public PsiElement getActivityWithWrongNestedAttribute() {
        return process("examples/activity_with_wrong_nested_attribute.omt");
    }

    public PsiElement getActivityWithUndeclaredElements() {
        return process("examples/activity_with_undeclared_elements.omt");
    }

    public PsiElement getProcedureWithScript() {
        return process("examples/procedure_with_script.omt");
    }

    public PsiElement getActivityWithImports() {
        return process("examples/activity_with_imports.omt");
    }

    public PsiElement getActivityWithVariables() {
        return process("examples/activity_with_variables.omt");
    }

    private PsiElement process(String resourcePath) {

        File file = new File(getClass().getClassLoader().getResource(resourcePath).getFile());
        String data = null;
        try {
            data = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fromContent(file.getName(), data);
    }

    public PsiElement fromContent(String content) {
        return fromContent("test.omt", content);
    }

    public PsiElement fromContent(String fileName, String content) {
        return fixture.configureByText(fileName, content);
    }

    public <T> T getPsiElementFromRootDocument(Class<? extends PsiElement> elementClass, PsiElement rootBlock) {
        return getPsiElementFromRootDocument(elementClass, rootBlock, item -> true);
    }

    public <T> T getPsiElementFromRootDocument(Class<? extends PsiElement> elementClass, PsiElement rootBlock, Predicate<T> condition) {
        return getPsiElementsFromRootDocument(elementClass, rootBlock, condition).get(0);
    }

    public <T> List<T> getPsiElementsFromRootDocument(Class<? extends PsiElement> elementClass, PsiElement rootBlock) {
        return getPsiElementsFromRootDocument(elementClass, rootBlock, item -> true);
    }

    public <T> List<T> getPsiElementsFromRootDocument(Class<? extends PsiElement> elementClass, PsiElement rootBlock, Predicate<T> condition) {
        final List<T> arrayList = new ArrayList<>();
        ApplicationManager.getApplication().runReadAction(
                () -> PsiTreeUtil.findChildrenOfType(rootBlock, elementClass).stream()
                        .map(item -> (T) item)
                        .filter(condition::test)
                        .forEach(arrayList::add)
        );

        assertTrue(arrayList.size() > 0);
        return arrayList;
    }
}
