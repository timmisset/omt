package com.misset.opp.omt.psi.util;

import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

class ProjectUtilTest extends LightJavaCodeInsightFixtureTestCase {

    ProjectUtil projectUtil;

    @BeforeEach
    void setUpSuite() throws Exception {
        super.setName("ProjectUtilTest");
        super.setUp();
        projectUtil = ProjectUtil.SINGLETON;
    }

    @AfterEach
    void tearDownSuite() throws Exception {
        super.tearDown();
    }

    @Test
    void loadBuiltInMembers() {
        ApplicationManager.getApplication().runReadAction(() -> projectUtil.loadBuiltInMembers(getProject()));
    }

    @Test
    void getParsedModel() {
        JsonObject parsedModel = projectUtil.getParsedModel();
        Set<String> keySet = parsedModel.keySet();

        assertTrue(keySet.contains("Action"));
        assertTrue(keySet.contains("DossierAction"));
        assertTrue(keySet.contains("GlobalActions"));
        assertTrue(keySet.contains("Activity"));
        assertTrue(keySet.contains("Binding"));
        assertTrue(keySet.contains("Component"));
        assertTrue(keySet.contains("GraphSelection"));
        assertTrue(keySet.contains("onChange"));
        assertTrue(keySet.contains("Ontology"));
        assertTrue(keySet.contains("OntologyClass"));
        assertTrue(keySet.contains("OntologyProperty"));
        assertTrue(keySet.contains("Param"));
        assertTrue(keySet.contains("Payload"));
        assertTrue(keySet.contains("Procedure"));
        assertTrue(keySet.contains("QueryWatcher"));
        assertTrue(keySet.contains("Service"));
        assertTrue(keySet.contains("StandaloneQuery"));
        assertTrue(keySet.contains("Variable"));
    }

}
