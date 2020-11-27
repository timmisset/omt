package com.misset.opp.omt.psi.util;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.OMTVariableAssignment;
import com.misset.opp.omt.util.ProjectUtil;
import com.misset.opp.omt.util.RDFModelUtil;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.misset.opp.omt.psi.util.VariableUtil.NO_TYPE_SPECIFIED;

public class VariableUtilTest_Fixture extends LightJavaCodeInsightFixtureTestCase {

    RDFModelUtil rdfModelUtil;
    VariableUtil variableUtil;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("OMTExportMemberImplTest");
        super.setUp();

        myFixture.copyFileToProject(new File("src/test/resources/examples/model.ttl").getAbsolutePath(), "test/resources/examples/root.ttl");
        ApplicationManager.getApplication().runReadAction(() -> {
            ProjectUtil.SINGLETON.loadOntologyModel(getProject());
        });
        rdfModelUtil = ProjectUtil.SINGLETON.getRDFModelUtil();
        variableUtil = VariableUtil.SINGLETON;
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void getTypeOMTParameterWithType() {
        assertContainsElements(getVariableType("model:\n" +
                "   activiteit: !Activity\n" +
                "       params:\n" +
                "           - $param1 (string)\n"), rdfModelUtil.getPrimitiveTypeAsResource("string"));
    }

    @Test
    void getTypeOMTDefineParam() {
        assertContainsElements(getVariableType("queries:|\n" +
                        "   /**\n" +
                        "   * @param $param1 (string)\n" +
                        "   */\n" +
                        "   DEFINE QUERY myQuery($param1) => $param1;\n"),
                rdfModelUtil.getPrimitiveTypeAsResource("string"));
    }

    @Test
    void getTypeVariableAssignment() {
        assertContainsElements(getVariableType("model:\n" +
                "   activiteit: !Activity\n" +
                "       onStart: |\n" +
                "           VAR $variable = 'test';\n"), rdfModelUtil.getPrimitiveTypeAsResource("string"));
    }

    @Test
    void getTypeVariableReAssignment() {
        assertContainsElements(getVariableType("model:\n" +
                "   activiteit: !Activity\n" +
                "       onStart: |\n" +
                "           VAR $variable = null;\n" +
                "           $variable = 'test';\n" +
                "           @LOG($variable);\n"), rdfModelUtil.getPrimitiveTypeAsResource("string"));
    }

    @Test
    void getDeclaredVariableFromNameAttribute() {
        String content = "model:\n" +
                "   activiteit: !Activity\n" +
                "       variables: \n" +
                "           -   name:   $naam\n" +
                "";
        final PsiFile psiFile = myFixture.configureByText("test.omt", content);
        ApplicationManager.getApplication().runReadAction(() -> {
            final OMTVariable variable = PsiTreeUtil.findChildOfType(psiFile, OMTVariable.class);
            assertEquals("$naam", variable.getName());
            assertTrue(variableUtil.isDeclaredVariable(variable));
        });
    }

    @Test
    void isDeclaredVariable() {
        String content = "model:\n" +
                "   activiteit: !Activity\n" +
                "       onStart: |\n" +
                "           VAR $variable = null;\n" +
                "           $variable = 'test';\n" +
                "           @LOG($variable);\n";
        final PsiFile psiFile = myFixture.configureByText("test.omt", content);
        ApplicationManager.getApplication().runReadAction(() -> {
            final List<OMTVariable> variables = new ArrayList<>(PsiTreeUtil.findChildrenOfType(psiFile, OMTVariable.class));
            List<Boolean> expected = Arrays.asList(true, false, false);
            for (int i = 0; i < variables.size(); i++) {
                assertEquals((boolean) expected.get(i), variables.get(i).isDeclaredVariable());
            }
        });
    }

    @Test
    void getAssignments() {
        String content = "model:\n" +
                "   activiteit: !Activity\n" +
                "       onStart: |\n" +
                "           VAR $variable = null;\n" +
                "           $variable = 'test';\n" +
                "           @LOG($variable);\n" +
                "           $variable = 'test2';\n" +
                "           @LOG($variable);\n";
        final PsiFile psiFile = myFixture.configureByText("test.omt", content);
        ApplicationManager.getApplication().runReadAction(() -> {
            final List<OMTVariable> variables = new ArrayList<>(PsiTreeUtil.findChildrenOfType(psiFile, OMTVariable.class));
            final OMTVariable variable = variables.get(2); //  the variable usage in the first LOG
            final List<OMTVariableAssignment> assignments = variable.getAssignments();

            assertEquals(2, assignments.size());
        });
    }

    @Test
    void getValue() {
        String content = "model:\n" +
                "   activiteit: !Activity\n" +
                "       onStart: |\n" +
                "           VAR $variable = null;\n" +
                "           $variable = 'test';\n" +
                "           @LOG($variable);\n" +
                "           $variable = 'test2';\n" +
                "           @LOG($variable);\n";
        final PsiFile psiFile = myFixture.configureByText("test.omt", content);
        ApplicationManager.getApplication().runReadAction(() -> {
            final List<OMTVariable> variables = new ArrayList<>(PsiTreeUtil.findChildrenOfType(psiFile, OMTVariable.class));
            assertEquals("'test'", variables.get(2).getValue().getQuery().getText()); // the first usage
            assertEquals("'test2'", variables.get(4).getValue().getQuery().getText()); // the second usage
        });
    }

    @Test
    void annotateEmptyParameterWithTypeError() {
        String content = "model:\n" +
                "   activiteit: !Activity\n" +
                "       params: \n" +
                "           - $param ()\n";
        myFixture.configureByText("test.omt", content);
        final List<HighlightInfo> infoList = myFixture.doHighlighting();
        assertTrue(infoList.stream().anyMatch(
                highlightInfo -> highlightInfo.getDescription().equals(NO_TYPE_SPECIFIED)
        ));
    }

    List<Resource> getVariableType(String content) {
        final PsiFile psiFile = myFixture.configureByText("test.omt", content);
        return ApplicationManager.getApplication().runReadAction((Computable<List<Resource>>) () -> {
            final List<OMTVariable> variables = new ArrayList<>(PsiTreeUtil.findChildrenOfType(psiFile, OMTVariable.class));
            final OMTVariable variable = variables.get(variables.size() - 1);
            return variable.getType();
        });
    }

}
