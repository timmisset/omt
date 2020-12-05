package com.misset.opp.omt.util;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.psi.support.BuiltInType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;


public class BuiltInUtilTest extends LightJavaCodeInsightFixtureTestCase {

    @Mock
    ProjectUtil projectUtil;

    Project project;

    @Mock
    Document document;

    @Mock
    Document helpDocument;

    @Mock
    PsiFile psiFile;

    @Mock
    VirtualFile virtualFile;

    @InjectMocks
    BuiltInUtil builtInUtil;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setName("BuiltInUtilTest");
        super.setUp();
        project = getProject();
        MockitoAnnotations.initMocks(this);
        WindowManager.getInstance();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void reloadBuiltInFromDocument() throws IOException {
        runReloadFor("builtinCommands.ts", BuiltInType.Command);
        runReloadFor("builtinOperators.ts", BuiltInType.Operator);
        runReloadFor("http-commands.ts", BuiltInType.HttpCommands);
        runReloadFor("json-parse-command.ts", BuiltInType.ParseJsonCommand);

        // check loaded content and accessibility
        assertTrue(builtInUtil.isLoaded());
        // check exclusive command
        assertNotNull(builtInUtil.getBuiltInMember("ADD_TO", BuiltInType.Command));
        assertNull(builtInUtil.getBuiltInMember("ADD_TO", BuiltInType.Operator));

        // exclusive operator
        assertNull(builtInUtil.getBuiltInMember("AND", BuiltInType.Command));
        assertNotNull(builtInUtil.getBuiltInMember("AND", BuiltInType.Operator));

        // available as operator and command
        assertNotNull(builtInUtil.getBuiltInMember("LOG", BuiltInType.Command));
        assertNotNull(builtInUtil.getBuiltInMember("LOG", BuiltInType.Operator));

        // http commands
        assertNotNull(builtInUtil.getBuiltInMember("HTTP_GET", BuiltInType.Command));
        assertNotNull(builtInUtil.getBuiltInMember("HTTP_POST", BuiltInType.Command));

        // json
        assertNotNull(builtInUtil.getBuiltInMember("JSON_PARSE", BuiltInType.Command));

        // check help content
        assertEquals("<p>helpText</p>\n", builtInUtil.getBuiltInMember("LOG", BuiltInType.Command).htmlDescription());

        // check suggestions
        assertContainsElements(builtInUtil.getBuiltInCommandsAsSuggestions(),
                "@CLEAR_GRAPH($param0)", "@HTTP_GET($param0, $param1, $param2)", "@JSON_PARSE($param0, $param1, $param2)");
        assertContainsElements(builtInUtil.getBuiltInOperatorsAsSuggestions(), "EXISTS", "SOME($param0)");
    }

    private void runReloadFor(String fileName, BuiltInType type) throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
        assert url != null;
        File file = new File(url.getPath());

        String content = new String(Files.readAllBytes(file.toPath()));

        doReturn(content).when(document).getText();
        doReturn(new PsiFile[]{psiFile}).when(projectUtil).getFilesByName(eq(project), anyString());
        doReturn(virtualFile).when(psiFile).getVirtualFile();
        doReturn("helpText").when(helpDocument).getText();
        doReturn(helpDocument).when(projectUtil).getDocument(eq(virtualFile));

        // ACT
        builtInUtil.reloadBuiltInFromDocument(document, type, project, projectUtil);
    }
}
