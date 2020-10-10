package com.misset.opp.omt.psi.util;

import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.external.util.builtIn.BuiltInUtil;
import com.misset.opp.omt.psi.ExampleFiles;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.support.OMTExportMember;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.*;

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
    void loadBuiltInMembers_ShowsErrorWhenDocumentNotLoaded() {
        doReturn(Arrays.asList(virtualFile)).when(projectUtil).getVirtualFilesByName(
                any(Project.class), anyString()
        );
        ApplicationManager.getApplication().runReadAction(() -> projectUtil.loadBuiltInMembers(getProject()));
        verify(statusBar).setInfo("Error loading builtinCommands.ts");
        verify(statusBar).setInfo("Error loading builtinOperators.ts");
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

    @Test
    void getWindowManager() {
        assertNotNull(projectUtil.getWindowManager());
    }

    @Test
    void getFileDocumentManager() {
        assertNotNull(projectUtil.getFileDocumentManager());
    }

    @Test
    void getFilesByName() {
        ApplicationManager.getApplication().runReadAction(() -> {
            String[] allFilenames = FilenameIndex.getAllFilenames(getProject());
            String firstFile = allFilenames[0];
            PsiFile[] filesByName = projectUtil.getFilesByName(getProject(), firstFile);
            assertNotNull(filesByName);
            assertNotEmpty(Arrays.asList(filesByName));
        });
    }

    @Test
    void getDocument() {
        Document document = mock(Document.class);
        doReturn(document).when(fileDocumentManager).getDocument(eq(virtualFile));
        assertEquals(document, projectUtil.getDocument(virtualFile));
    }

    @Test
    void getKnownPrefixes() throws NoSuchFieldException, IllegalAccessException {
        String prefix = "abc:";
        Field prefixCollectionField = ProjectUtil.class.getDeclaredField("knownPrefixes");
        prefixCollectionField.setAccessible(true);
        HashMap<String, List<OMTPrefix>> prefixCollection = (HashMap<String, List<OMTPrefix>>) prefixCollectionField.get(projectUtil);
        List<OMTPrefix> prefixes = new ArrayList<>();
        prefixCollection.put("abc", prefixes);

        assertEquals(prefixes, projectUtil.getKnownPrefixes(prefix));
    }

    @Test
    void getExportingCommandsAsSuggestions() throws NoSuchFieldException, IllegalAccessException {
        OMTExportMember mockCommand = mock(OMTExportMember.class);
        OMTExportMember mockOperator = mock(OMTExportMember.class);

        doReturn("myCommand").when(mockCommand).asSuggestion();
        doReturn(true).when(mockCommand).isCommand();
        doReturn(false).when(mockCommand).isOperator();
        doReturn("myOperator").when(mockOperator).asSuggestion();
        doReturn(true).when(mockOperator).isOperator();
        doReturn(false).when(mockOperator).isCommand();

        Field exportMemberCollection = ProjectUtil.class.getDeclaredField("exportingMembers");
        exportMemberCollection.setAccessible(true);
        HashMap<String, List<OMTExportMember>> exportMembers = (HashMap<String, List<OMTExportMember>>) exportMemberCollection.get(projectUtil);

        exportMembers.put("commandOnly", new ArrayList<>(Collections.singletonList(mockCommand)));
        exportMembers.put("operatorOnly", new ArrayList<>(Collections.singletonList(mockOperator)));
        exportMembers.put("both", new ArrayList<>(Arrays.asList(mockCommand, mockOperator)));

        List<String> exportingCommandsAsSuggestions = projectUtil.getExportingCommandsAsSuggestions();
        List<String> exportingOperatorsAsSuggestions = projectUtil.getExportingOperatorsAsSuggestions();

        assertEquals(1, exportingCommandsAsSuggestions.size());
        assertEquals(1, exportingOperatorsAsSuggestions.size());
        assertContainsElements(exportingCommandsAsSuggestions, "myCommand");
        assertContainsElements(exportingOperatorsAsSuggestions, "myOperator");
    }
}
