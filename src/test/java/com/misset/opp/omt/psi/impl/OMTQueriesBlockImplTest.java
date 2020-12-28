package com.misset.opp.omt.psi.impl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTQueriesBlock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OMTQueriesBlockImplTest extends OMTTestSuite {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void getStatements() {

        String content = "queries: |\n" +
                "   DEFINE QUERY query1 => ''\n" +
                "   DEFINE QUERY query2 => ''";
        final PsiFile psiFile = myFixture.configureByText(getFileName(), content);
        final OMTQueriesBlock queriesBlock = ApplicationManager.getApplication()
                .runReadAction((Computable<OMTQueriesBlock>) () -> PsiTreeUtil.findChildOfType(psiFile, OMTQueriesBlock.class));

        assertEquals(2, queriesBlock.getDefineQueryStatementList().size());
        assertEquals(queriesBlock.getStatements(), queriesBlock.getDefineQueryStatementList());

    }
}
