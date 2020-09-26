package com.misset.opp.omt.psi.util;

import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.external.util.builtIn.BuiltInUtil;
import com.misset.opp.omt.psi.ExampleFiles;
import com.misset.opp.omt.psi.OMTFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProjectUtilTest extends LightJavaCodeInsightFixtureTestCase {

    @Mock
    VirtualFile virtualFile;

    @Mock
    BuiltInUtil builtInUtil;

    @Mock
    Document document;

    @Mock
    FileDocumentManager fileDocumentManager;

    @InjectMocks
    ProjectUtil projectUtil;


    ExampleFiles exampleFiles = new ExampleFiles(this);

    @Mock
    StatusBar statusBar;

    PsiElement rootBlock;


    @BeforeEach
    void setUpSuite() throws Exception {
        super.setName("ProjectUtilTest");
        super.setUp();
        MockitoAnnotations.initMocks(this);

        projectUtil = spy(projectUtil);
        doReturn(statusBar).when(projectUtil).getStatusBar(eq(getProject()));

        ApplicationManager.getApplication().runReadAction(() ->
                {
                    rootBlock = exampleFiles.getActivityWithImportsPrefixesParamsVariablesGraphsPayload();
                }
        );
    }

    @AfterEach
    void tearDownSuite() throws Exception {
        super.tearDown();
    }

    @Test
    void loadBuiltInMembers_ShowsSuccess() {
        doReturn(Arrays.asList(virtualFile)).when(projectUtil).getVirtualFilesByName(
                any(Project.class), anyString()
        );
        doReturn(document).when(fileDocumentManager).getDocument(eq(virtualFile));
        ApplicationManager.getApplication().runReadAction(() -> projectUtil.loadBuiltInMembers(getProject()));
        verify(statusBar).setInfo("Finished loading builtinCommands.ts");
        verify(statusBar).setInfo("Finished loading builtinOperators.ts");
    }

    @Test
    void loadBuiltInMembers_ShowsErrorWhenNotDocumentNotLoaded() {
        doReturn(Arrays.asList(virtualFile)).when(projectUtil).getVirtualFilesByName(
                any(Project.class), anyString()
        );
        ApplicationManager.getApplication().runReadAction(() -> projectUtil.loadBuiltInMembers(getProject()));
        verify(statusBar).setInfo("Error loading builtinCommands.ts");
        verify(statusBar).setInfo("Error loading builtinOperators.ts");
    }

    @Test
    void loadBuiltInMembers_ShowsErrorWhenNotFound() {
        doReturn(document).when(fileDocumentManager).getDocument(eq(virtualFile));
        ApplicationManager.getApplication().runReadAction(() -> projectUtil.loadBuiltInMembers(getProject()));
        verify(statusBar).setInfo("Number of virtual files found for builtinCommands.ts in project != 1, number found is = 0");
        verify(statusBar).setInfo("Number of virtual files found for builtinOperators.ts in project != 1, number found is = 0");
    }

    @Test
    void loadBuiltInMembers_ShowsErrorWhenTooManyFound() {
        doReturn(Arrays.asList(virtualFile, virtualFile)).when(projectUtil).getVirtualFilesByName(
                any(Project.class), anyString()
        );
        doReturn(document).when(fileDocumentManager).getDocument(eq(virtualFile));
        ApplicationManager.getApplication().runReadAction(() -> projectUtil.loadBuiltInMembers(getProject()));
        verify(statusBar).setInfo("Number of virtual files found for builtinCommands.ts in project != 1, number found is = 2");
        verify(statusBar).setInfo("Number of virtual files found for builtinOperators.ts in project != 1, number found is = 2");
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

    @Test
    void analyzeFile() {
        ApplicationManager.getApplication().runReadAction(() -> {
            projectUtil.analyzeFile((OMTFile) rootBlock);
            assertEquals(1, projectUtil.getExportMember("MijnActiviteit").size());
            assertEquals(1, projectUtil.getKnownPrefixes("abc").size());
            assertEquals(1, projectUtil.getKnownPrefixes("foaf").size());
        });
    }
}
