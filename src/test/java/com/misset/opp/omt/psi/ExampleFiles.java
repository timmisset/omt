package com.misset.opp.omt.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.OMTLanguage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExampleFiles {

    private final LightJavaCodeInsightFixtureTestCase testCase;

    public ExampleFiles(LightJavaCodeInsightFixtureTestCase testCase) {
        this.testCase = testCase;

    }

    public PsiElement getActivityWithImportsPrefixesParamsVariablesGraphsPayload() {
        return process("examples/activity_with_imports_prefixes_params_variables_graphs_payload.omt");
    }
    public PsiElement getModelWithWrongModelItemType() {
        return process("examples/model_with_wrong_model_item_type.omt");
    }
    public PsiElement getStandaloneQueryWithMissingAttribute() {
        return process("examples/StandaloneQuery_with_missing_attribute.omt");
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

    private PsiElement process(String resourcePath) {
        File file = new File(getClass().getClassLoader().getResource(resourcePath).getFile());
        String data = null;
        try {
            data = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return testCase.createLightFile(file.getName(), OMTLanguage.INSTANCE, data);
    }

    public <T> T getPsiElementFromRootDocument(Class<? extends PsiElement> elementClass, PsiElement rootBlock) {
        Collection<? extends PsiElement> childrenOfType = PsiTreeUtil.findChildrenOfType(rootBlock, elementClass);
        assertTrue(childrenOfType.size() > 0);

        Optional<? extends PsiElement> first = childrenOfType.stream().findFirst();
        return (T) first.orElse(null);
    }

    public <T> List<T> getPsiElementsFromRootDocument(Class<? extends PsiElement> elementClass, PsiElement rootBlock) {
        Collection<? extends PsiElement> childrenOfType = PsiTreeUtil.findChildrenOfType(rootBlock, elementClass);
        assertTrue(childrenOfType.size() > 0);

        return new ArrayList(childrenOfType);
    }
}
