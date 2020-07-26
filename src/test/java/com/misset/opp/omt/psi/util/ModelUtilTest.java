package com.misset.opp.omt.psi.util;

import com.intellij.openapi.project.Project;
import com.misset.opp.omt.psi.OMTModelItem;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import static com.misset.opp.omt.psi.util.Helper.getResource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

class ModelUtilTest {

    @Mock Project project;

    @Test
    void getModelFiles() {
        File resource = getResource("Component.ts");
        List<File> allModelFiles = ModelUtil.getAllModelFiles(resource.getParent());

        assertEquals(19, allModelFiles.size()); // current number of model items
    }

    @Test
    void getModelItems() {
        MockitoAnnotations.initMocks(this);
        File resource = getResource("Component.ts");

        doReturn(resource.getParent()).when(project).getBasePath();
        HashMap<String, OMTModelItem> modelItems = ModelUtil.getModelItems(project);

        assertTrue(modelItems.containsKey("Activity"));
        assertTrue(modelItems.containsKey("Component"));
        assertTrue(modelItems.containsKey("Procedure"));
        // ... and many more

    }

    @Test
    void parseModelItem() throws IOException {

        URL url = Thread.currentThread().getContextClassLoader().getResource("Component.ts");
        assert url != null;
        File file = new File(url.getPath());

        String content = new String(Files.readAllBytes(file.toPath()));

        OMTModelItem omtModelItem = ModelUtil.parseModelItem(content);

        assertEquals("Component", omtModelItem.getType());
        assertEquals(12, omtModelItem.numberOfAttributes()); // currently 12 in the demo file
        assertTrue(omtModelItem.hasAttribute("title")); // check diversity
        assertTrue(omtModelItem.hasAttribute("autonomous"));
        assertTrue(omtModelItem.hasAttribute("graphs"));
        assertTrue(omtModelItem.hasAttribute("watchers"));

        assertEquals("AttributeType.InterpolatedString", omtModelItem.getAttributeType("title"));
        assertEquals("AttributeType.Boolean", omtModelItem.getAttributeType("autonomous"));
        assertEquals("AttributeType.Structure", omtModelItem.getAttributeType("graphs"));
        assertEquals("queryWatchersDef", omtModelItem.getAttributeType("watchers"));
    }
}
