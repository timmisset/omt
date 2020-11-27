package com.misset.opp.omt.psi.impl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.psi.OMTDefineQueryStatement;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.support.ExportMemberType;
import com.misset.opp.omt.util.ProjectUtil;
import com.misset.opp.omt.util.RDFModelUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

class OMTExportMemberImplTest extends LightJavaCodeInsightFixtureTestCase {

    RDFModelUtil rdfModelUtil;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("OMTExportMemberImplTest");
        super.setUp();

        myFixture.copyFileToProject(new File("src/test/resources/examples/model.ttl").getAbsolutePath(), "test/resources/examples/root.ttl");
//        myFixture.copyFileToProject(new File("src/test/resources/builtinOperators.ts").getAbsolutePath(), "builtinOperators.ts");
//        myFixture.copyFileToProject(new File("src/test/resources/builtinCommands.ts").getAbsolutePath(), "builtinCommands.ts");
        ApplicationManager.getApplication().runReadAction(() -> {
            ProjectUtil.SINGLETON.loadOntologyModel(getProject());
        });

//        memberUtil = new MemberUtil();
        rdfModelUtil = ProjectUtil.SINGLETON.getRDFModelUtil();

    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void getReturnTypeQuery() {
        final PsiFile psiFile = myFixture.configureByText("test.omt", "" +
                "queries:|\n" +
                "   DEFINE QUERY myQuery() => 'test';\n");
        ApplicationManager.getApplication().runReadAction(() -> {
            final OMTDefineQueryStatement queryStatement = PsiTreeUtil.findChildOfType(psiFile, OMTDefineQueryStatement.class);
            final OMTExportMemberImpl omtExportMember = new OMTExportMemberImpl(queryStatement, ExportMemberType.Query);
            assertContainsElements(omtExportMember.getReturnType(), rdfModelUtil.getPrimitiveTypeAsResource("string"));
        });
    }

    @Test
    void getReturnTypeStandaloneQuery() {
        final PsiFile psiFile = myFixture.configureByText("test.omt", "" +
                "model:\n" +
                "    MijnStandaloneQuery: !StandaloneQuery\n" +
                "        query: |" +
                "           'test'\n");
        ApplicationManager.getApplication().runReadAction(() -> {
            final OMTModelItemBlock modelItemBlock = PsiTreeUtil.findChildOfType(psiFile, OMTModelItemBlock.class);
            final OMTExportMemberImpl omtExportMember = new OMTExportMemberImpl(modelItemBlock, ExportMemberType.StandaloneQuery);
            assertContainsElements(omtExportMember.getReturnType(), rdfModelUtil.getPrimitiveTypeAsResource("string"));
        });
    }
}
