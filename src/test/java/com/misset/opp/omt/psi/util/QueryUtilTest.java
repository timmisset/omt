package com.misset.opp.omt.psi.util;

import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTQueryReverseStep;
import com.misset.opp.omt.psi.OMTQueryStep;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.misset.opp.util.UtilManager.getQueryUtil;

public class QueryUtilTest extends OMTTestSuite {

    private QueryUtil queryUtil = getQueryUtil();

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setName("QueryUtilTest");
        super.setUp();

        setOntologyModel();
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void isPreviousStepATypeReturnTrueWhenPreviousStepIsCurieConstantOfClassType() {
        String content = withPrefixes("queries: |\n" +
                "   DEFINE QUERY query => /ont:ClassA / ^rdf:type");
        final PsiFile psiFile = myFixture.configureByText(getFileName(), content);
        ReadAction.run(
                () -> {
                    final OMTQueryReverseStep reverseStep = PsiTreeUtil.findChildOfType(psiFile, OMTQueryReverseStep.class);
                    assertTrue(queryUtil.isPreviousStepAType(reverseStep));
                }
        );
    }

    @Test
    void isPreviousStepATypeReturnTrueWhenPreviousStepIsFilterContainer() {
        String content = withPrefixes("queries: |\n" +
                "   DEFINE QUERY query => /ont:ClassA [ . ]");
        final PsiFile psiFile = myFixture.configureByText(getFileName(), content);
        ReadAction.run(
                () -> {
                    final OMTQueryStep dotStep = PsiTreeUtil.findChildrenOfType(psiFile, OMTQueryStep.class)
                            .stream().filter(queryStep -> queryStep.getText().equals(".")).findFirst().orElse(null);
                    assertTrue(queryUtil.isPreviousStepAType(dotStep));
                }
        );
    }

    @Test
    void isPreviousStepATypeReturnFalseWhenNoPreviousStep() {
        String content = withPrefixes("queries: |\n" +
                "   DEFINE QUERY query => /ont:ClassA");
        final PsiFile psiFile = myFixture.configureByText(getFileName(), content);
        ReadAction.run(
                () -> {
                    final OMTQueryStep queryStep = PsiTreeUtil.findChildOfType(psiFile, OMTQueryStep.class);
                    assertFalse(queryUtil.isPreviousStepAType(queryStep));
                }
        );
    }

}
