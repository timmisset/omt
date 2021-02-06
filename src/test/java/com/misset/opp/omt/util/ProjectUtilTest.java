package com.misset.opp.omt.util;

import com.google.gson.JsonObject;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.misset.opp.omt.OMTTestSuite;
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

class ProjectUtilTest extends OMTTestSuite {

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

    @Mock
    StatusBar statusBar;



    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setName("ProjectUtilTest");
        super.setUp();
        MockitoAnnotations.openMocks(this);

        setUtilMock(builtInUtil);
        projectUtil = spy(projectUtil);
        setUtilMock(projectUtil);

        doReturn(statusBar).when(projectUtil).getStatusBar(eq(getProject()));
        setExampleFileActivityWithImportsPrefixesParamsVariablesGraphsPayload();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void loadBuiltInMembers_ShowsSuccess() {
        doReturn(Arrays.asList(virtualFile)).when(projectUtil).getVirtualFilesByName(
                any(Project.class), anyString()
        );
        doReturn(document).when(fileDocumentManager).getDocument(eq(virtualFile));
        ReadAction.run(() -> projectUtil.loadBuiltInMembers(getProject()));
        verify(statusBar).setInfo("OMT PLUGIN: Finished loading builtinCommands.ts");
        verify(statusBar).setInfo("OMT PLUGIN: Finished loading builtinOperators.ts");
    }

    @Test
    void loadBuiltInMembers_ShowsErrorWhenDocumentNotLoaded() {
        doReturn(Arrays.asList(virtualFile)).when(projectUtil).getVirtualFilesByName(
                any(Project.class), anyString()
        );
        ReadAction.run(() -> projectUtil.loadBuiltInMembers(getProject()));
        verify(statusBar).setInfo("OMT PLUGIN: Error loading builtinCommands.ts");
        verify(statusBar).setInfo("OMT PLUGIN: Error loading builtinOperators.ts");
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
        ReadAction.run(() -> {
            projectUtil.analyzeFile((OMTFile) getFile());
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
        ReadAction.run(() -> {
            String[] allFilenames = FilenameIndex.getAllFilenames(getProject());
            String firstFile = allFilenames[0];
            PsiFile[] filesByName = projectUtil.getFilesByName(getProject(), firstFile);
            assertNotNull(filesByName);
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
    void getReasons() {
        setReasons();
        final HashMap<String, String> reasons = projectUtil.getReasons();
        assertEquals("Een omschrijving", reasons.get("Naam"));
    }

    @Test
    void getExportingCommandsAsSuggestions() throws NoSuchFieldException, IllegalAccessException {
        OMTExportMember mockCommand = mock(OMTExportMember.class);
        OMTExportMember mockOperator = mock(OMTExportMember.class);

        doReturn("myCommand").when(mockCommand).getAsSuggestion();
        doReturn(true).when(mockCommand).isCommand();
        doReturn(false).when(mockCommand).isOperator();
        doReturn("myOperator").when(mockOperator).getAsSuggestion();
        doReturn(true).when(mockOperator).isOperator();
        doReturn(false).when(mockOperator).isCommand();

        Field exportMemberCollection = ProjectUtil.class.getDeclaredField("exportingMembers");
        exportMemberCollection.setAccessible(true);
        HashMap<String, List<OMTExportMember>> exportMembers = (HashMap<String, List<OMTExportMember>>) exportMemberCollection.get(projectUtil);

        exportMembers.put("commandOnly", new ArrayList<>(Collections.singletonList(mockCommand)));
        exportMembers.put("operatorOnly", new ArrayList<>(Collections.singletonList(mockOperator)));
        exportMembers.put("both", new ArrayList<>(Arrays.asList(mockCommand, mockOperator)));

        List<String> exportingCommandsAsSuggestions = projectUtil.getExportedMembersAsSuggestions(true);
        List<String> exportingOperatorsAsSuggestions = projectUtil.getExportedMembersAsSuggestions(false);

        assertEquals(1, exportingCommandsAsSuggestions.size());
        assertEquals(1, exportingOperatorsAsSuggestions.size());
        assertContainsElements(exportingCommandsAsSuggestions, "myCommand");
        assertContainsElements(exportingOperatorsAsSuggestions, "myOperator");
    }
}
