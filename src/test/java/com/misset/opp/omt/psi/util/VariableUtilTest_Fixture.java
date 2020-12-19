package com.misset.opp.omt.psi.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.OMTVariableAssignment;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.getRDFModelUtil;

class VariableUtilTest_Fixture extends OMTTestSuite {

    VariableUtil variableUtil;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("OMTExportMemberImplTest");
        super.setUp();
        setOntologyModel();
        variableUtil = new VariableUtil();

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
                "           - $param1 (string)\n"), getRDFModelUtil().getPrimitiveTypeAsResource("string"));
    }

    @Test
    void getTypeOMTDefineParam() {
        assertContainsElements(getVariableType("queries:|\n" +
                        "   /**\n" +
                        "   * @param $param1 (string)\n" +
                        "   */\n" +
                        "   DEFINE QUERY myQuery($param1) => $param1;\n"),
                getRDFModelUtil().getPrimitiveTypeAsResource("string"));
    }

    @Test
    void getTypeVariableAssignment() {
        assertContainsElements(getVariableType("model:\n" +
                "   activiteit: !Activity\n" +
                "       onStart: |\n" +
                "           VAR $variable = 'test';\n"), getRDFModelUtil().getPrimitiveTypeAsResource("string"));
    }

    @Test
    void getTypeVariableReAssignment() {
        assertContainsElements(getVariableType("model:\n" +
                "   activiteit: !Activity\n" +
                "       onStart: |\n" +
                "           VAR $variable = null;\n" +
                "           $variable = 'test';\n" +
                "           @LOG($variable);\n"), getRDFModelUtil().getPrimitiveTypeAsResource("string"));
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

    List<Resource> getVariableType(String content) {
        final PsiFile psiFile = myFixture.configureByText("test.omt", content);
        return ApplicationManager.getApplication().runReadAction((Computable<List<Resource>>) () -> {
            final List<OMTVariable> variables = new ArrayList<>(PsiTreeUtil.findChildrenOfType(psiFile, OMTVariable.class));
            final OMTVariable variable = variables.get(variables.size() - 1);
            return variable.getType();
        });
    }

}
