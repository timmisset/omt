package com.misset.opp.omt;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.util.ProjectUtil;

import java.io.File;

public class OMTTestSuite extends LightJavaCodeInsightFixtureTestCase {

    protected void setOntologyModel() {
        myFixture.copyFileToProject(new File("src/test/resources/examples/model.ttl").getAbsolutePath(), "test/resources/examples/root.ttl");
        ApplicationManager.getApplication().runReadAction(() -> ProjectUtil.SINGLETON.loadOntologyModel(getProject()));
    }

    protected void setBuiltinOperators() {
        myFixture.copyFileToProject(new File("src/test/resources/builtinOperators.ts").getAbsolutePath(), "builtinOperators.ts");
    }
}
