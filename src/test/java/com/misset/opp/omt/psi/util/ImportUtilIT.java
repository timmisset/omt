package com.misset.opp.omt.psi.util;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;

public class ImportUtilIT extends LightJavaCodeInsightFixtureTestCase {

    public ImportUtilIT() {
        super();
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    @BeforeEach
    void init() throws Exception {
        // calls the setUp() on the TestCase
        setUp();
    }

    @Test
    public void getAllExportingMembers() {
        myFixture.configureByFiles("test.OMT");
        myFixture.complete(CompletionType.BASIC, 1);
        File resource = Helper.getResource("test.OMT");
        HashMap<String, Object> allExportingMembers =
                ImportUtil.getAllExportingMembers(
                        resource.getPath(),
                        myFixture.getProject()
                );

        System.out.println(allExportingMembers.size());
    }


}
