package com.misset.opp.omt.psi.impl;

import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTDefineQueryStatement;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.support.ExportMemberType;
import com.misset.opp.omt.util.RDFModelUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.misset.opp.util.UtilManager.getRDFModelUtil;

class OMTExportMemberImplTest extends OMTTestSuite {

    RDFModelUtil rdfModelUtil;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("OMTExportMemberImplTest");
        super.setUp();
        setOntologyModel();
        rdfModelUtil = getRDFModelUtil();

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
        ReadAction.run(() -> {
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
        ReadAction.run(() -> {
            final OMTModelItemBlock modelItemBlock = PsiTreeUtil.findChildOfType(psiFile, OMTModelItemBlock.class);
            final OMTExportMemberImpl omtExportMember = new OMTExportMemberImpl(modelItemBlock, ExportMemberType.StandaloneQuery);
            assertContainsElements(omtExportMember.getReturnType(), rdfModelUtil.getPrimitiveTypeAsResource("string"));
        });
    }
}
