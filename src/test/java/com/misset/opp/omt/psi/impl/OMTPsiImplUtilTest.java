package com.misset.opp.omt.psi.impl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.psi.ExampleFiles;
import com.misset.opp.omt.psi.OMTQueryPath;
import com.misset.opp.omt.psi.util.ProjectUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OMTPsiImplUtilTest extends LightJavaCodeInsightFixtureTestCase {

    private final ExampleFiles exampleFiles = new ExampleFiles(this);

    @BeforeEach
    void setUpSuite() throws Exception {
        super.setName("OMTPsiImplUtilTest");
        super.setUp();

        ApplicationManager.getApplication().runReadAction(() -> ProjectUtil.SINGLETON.loadOntologyModel(getProject()));
    }

    @AfterEach
    void tearDownSuite() throws Exception {
        super.tearDown();
    }

    @Test
    void queryPathResolveToResource() {
        String content = "prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        onStart: |\n" +
                "           'test' / ont:Something\n" +
                "        ";
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTQueryPath queryPath = exampleFiles.getPsiElementFromRootDocument(OMTQueryPath.class, rootBlock);
            OMTPsiImplUtil.resolveToResource(queryPath);
        });

    }
}
