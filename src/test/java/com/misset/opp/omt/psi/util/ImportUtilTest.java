package com.misset.opp.omt.psi.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.psi.OMTImport;
import com.misset.opp.omt.psi.OMTImportSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;

import static com.misset.opp.omt.psi.util.Helper.getResource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

class ImportUtilTest {

    @Mock OMTImport omtImport;
    @Mock OMTImportSource omtImportSource;
    @Mock Project project;
    private static String projectBaseDir = "c:/myProject";

    @Mock PsiFile containingFile;
    @Mock VirtualFile virtualFile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        // set the mock behavior
        doReturn(omtImportSource).when(omtImport).getImportSource();
        doReturn(project).when(omtImport).getProject();
        doReturn(projectBaseDir).when(project).getBasePath();
        doReturn(containingFile).when(omtImport).getContainingFile();
        doReturn(virtualFile).when(containingFile).getVirtualFile();
    }

    @Test
    void resolvePathToSource_client() {
        String path = "@client/test/someOmtFile.OMT";
        doReturn(path).when(omtImportSource).getText();

        String expected = String.format("%s/frontend/src/test/someOmtFile.OMT", projectBaseDir);

        assertSamePath(expected, ImportUtil.resolvePathToSource(omtImport));
    }

    @Test
    void resolvePathToSource_relative() {
        File file = getResource("core/omt/src/model/actions/Action.ts");
        String expectedPath = file.getParentFile().getParentFile().getParentFile() + "/someFile.ttl";
        String relativePath = "../../../someFile.ttl";
        doReturn(relativePath).when(omtImportSource).getText();
        doReturn(file.getPath()).when(virtualFile).getPath();

        assertSamePath(expectedPath, ImportUtil.resolvePathToSource(omtImport));
    }

    @Test
    void resolvePathToSource_sameFolder() {
        File file = getResource("core/omt/src/model/actions/Action.ts");
        String expectedPath = file.getPath();
        String relativePath = "./Action.ts";
        doReturn(relativePath).when(omtImportSource).getText();
        doReturn(file.getPath()).when(virtualFile).getPath();

        assertSamePath(expectedPath, ImportUtil.resolvePathToSource(omtImport));
    }

    private void assertSamePath(String pathA, String pathB) {
        assertEquals(pathA.replace("\\", "/"), pathB.replace("\\", "/"));
    }
}
