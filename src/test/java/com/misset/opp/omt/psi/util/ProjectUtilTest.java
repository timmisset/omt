package com.misset.opp.omt.psi.util;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectUtilTest {

    @Test
    void loadModelAttributes() throws IOException {


        ProjectUtil.SINGLETON.loadModelAttributes();
        JsonObject parsedModel = ProjectUtil.SINGLETON.getParsedModel();
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
        assertTrue(keySet.contains("StandAloneQuery"));
        assertTrue(keySet.contains("Variable"));
    }
}
