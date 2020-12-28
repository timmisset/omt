package com.misset.opp.omt.psi.impl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTCommandsBlock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OMTCommandsBlockImplTest extends OMTTestSuite {

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

        String content = "commands: |\n" +
                "   DEFINE COMMAND command1 => {}\n" +
                "   DEFINE COMMAND command2 => {}";
        final PsiFile psiFile = myFixture.configureByText(getFileName(), content);
        final OMTCommandsBlock commandsBlock = ApplicationManager.getApplication()
                .runReadAction((Computable<OMTCommandsBlock>) () -> PsiTreeUtil.findChildOfType(psiFile, OMTCommandsBlock.class));

        assertEquals(2, commandsBlock.getDefineCommandStatementList().size());
        assertEquals(commandsBlock.getStatements(), commandsBlock.getDefineCommandStatementList());
    }
}
