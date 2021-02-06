package com.misset.opp.omt.psi.util;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.support.OMTExportMember;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ImportUtilTest extends OMTTestSuite {

    @Mock
    AnnotationHolder annotationHolder;
    @Mock
    AnnotationBuilder annotationBuilder;

    @Mock
    VirtualFile virtualFile;
    @Mock
    PsiFile psiFile;

    @Mock
    Project project;

    @Mock
    PsiManager psiManager;

    @Mock
    OMTFile omtFile;

    @Mock
    OMTExportMember exportMember;

    @Mock
    PsiElement psiElement;

    @InjectMocks
    ImportUtil importUtil;

    OMTImport omtImport;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setName("ImportUtilTest");
        super.setUp();

        MockitoAnnotations.openMocks(this);

        setExampleFileActivityWithImports();
        ReadAction.run(() -> {
            omtImport = getElement(OMTImport.class);
            omtImport = spy(omtImport);

            doReturn(psiFile).when(omtImport).getContainingFile();
            doReturn(virtualFile).when(psiFile).getVirtualFile();
            doReturn("myUrl").when(virtualFile).getUrl();
            doReturn(project).when(omtImport).getProject();
        });
        doReturn(annotationBuilder).when(annotationHolder).newAnnotation(any(), anyString());
        doReturn(annotationBuilder).when(annotationBuilder).range(any(PsiElement.class));
        doReturn(annotationBuilder).when(annotationBuilder).withFix(any(IntentionAction.class));

    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void resolveImportMember_returnsEmpty() {
        ReadAction.run(() -> {
            importUtil = spy(importUtil);
            doReturn(virtualFile).when(importUtil).getImportedFile(any(OMTImport.class));
            OMTMember member = getElement(OMTMember.class);
            assertEquals(Optional.empty(), importUtil.resolveImportMember(member));
        });
    }

    @Test
    void resolveImportMember_returnsResolved() {
        ReadAction.run(() -> {
            importUtil = spy(importUtil);
            doReturn(virtualFile).when(importUtil).getImportedFile(any(OMTImport.class));
            doReturn(omtFile).when(psiManager).findFile(eq(virtualFile));
            doReturn(Optional.of(exportMember)).when(omtFile).getExportedMember(anyString());
            doReturn(psiElement).when(exportMember).getResolvingElement();

            OMTMember member = getElement(OMTMember.class);

            assertEquals(Optional.of(psiElement), importUtil.resolveImportMember(member));
        });
    }

    @Test
    void getImportedFile_GetsRelativeFileFromClient() {
        ReadAction.run(() -> {
            doReturn(new File("src/test/resources/examples").getAbsolutePath()).when(project).getBasePath();
            doReturn(new File("src/test/resources/examples/activity_with_imports.omt").getAbsolutePath()).when(virtualFile).getPath();
            importUtil.getImportedFile(omtImport);

            verify(virtualFile).findFileByRelativePath("../frontend/libs/procedure_with_exporting_members.omt");
        });
    }

    @Test
    void getImportedFile_GetsRelativeFileFromRelative() {
        ReadAction.run(() -> {
            OMTImportSource importSource = mock(OMTImportSource.class);
            OMTImportLocation importLocation = mock(OMTImportLocation.class);
            doReturn(importSource).when(omtImport).getImportSource();
            doReturn(importLocation).when(importSource).getImportLocation();

            doReturn("./procedure_with_script.omt").when(importLocation).getText();
            doReturn(new File("src/test/resources/examples").getAbsolutePath()).when(project).getBasePath();
            doReturn(new File("src/test/resources/examples/activity_with_imports.omt").getAbsolutePath()).when(virtualFile).getPath();
            importUtil.getImportedFile(omtImport);

            verify(virtualFile).findFileByRelativePath(".././procedure_with_script.omt");
        });
    }

    @Test
    void getImportedFile_GetsFromModuleReturnsNull() {
        ReadAction.run(() -> {
            OMTImportSource importSource = mock(OMTImportSource.class);
            OMTImportLocation importLocation = mock(OMTImportLocation.class);
            doReturn(importSource).when(omtImport).getImportSource();
            doReturn(importLocation).when(importSource).getImportLocation();

            doReturn(virtualFile).when(virtualFile).findFileByRelativePath(anyString());
            doReturn("module:MyModule").when(importLocation).getText();
            doReturn(new File("src/test/resources/examples").getAbsolutePath()).when(project).getBasePath();
            doReturn(new File("src/test/resources/examples/activity_with_imports.omt").getAbsolutePath()).when(virtualFile).getPath();
            VirtualFile importedFile = importUtil.getImportedFile(omtImport);

            assertNull(importedFile);
        });
    }

    @Test
    void getImportedFile_UnknownMappingReturnsNull() {
        ReadAction.run(() -> {
            OMTImportSource importSource = mock(OMTImportSource.class);
            OMTImportLocation importLocation = mock(OMTImportLocation.class);
            doReturn(importSource).when(omtImport).getImportSource();
            doReturn(importLocation).when(importSource).getImportLocation();

            doReturn(virtualFile).when(virtualFile).findFileByRelativePath(anyString());
            doReturn("@unknownMapping/test").when(importLocation).getText();
            doReturn(new File("src/test/resources/examples").getAbsolutePath()).when(project).getBasePath();
            doReturn(new File("src/test/resources/examples/activity_with_imports.omt").getAbsolutePath()).when(virtualFile).getPath();
            VirtualFile importedFile = importUtil.getImportedFile(omtImport);

            assertNull(importedFile);
        });
    }

    private void assertSameContent(String expected, String value) {
        assertEquals(expected.replaceAll("\\s+", ""), value.replaceAll("\\s+", ""));
    }

    @Test
    void addImportMemberToBlock_ToExistingImport() {
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            importUtil.addImportMemberToBlock(getFile(), "'@client/procedure_with_exporting_members.omt':", "AnotherMember");
            OMTImportBlock importBlock = getElement(OMTImportBlock.class);
            String text = importBlock.getText();
            assertSameContent("import:\n" +
                    "    /**\n" +
                    "    * Some info about the import\n" +
                    "    */\n" +
                    "    '@client/procedure_with_exporting_members.omt': #or behind the path\n" +
                    "        /**\n" +
                    "        * specific information about the member\n" +
                    "        */\n" +
                    "        -   MijnProcedure\n" +
                    "        -   AnotherMember   #and something about the member\n" +
                    "\n", text);
        });
    }

    @Test
    void addImportMemberToBlock_ToNewImport() {
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            importUtil.addImportMemberToBlock(getFile(), "'@client/someModule/activity.omt':", "AnotherMember");
            OMTImportBlock importBlock = getElement(OMTImportBlock.class);
            String text = importBlock.getText();
            assertSameContent("import:\n" +
                    "    /**\n" +
                    "    * Some info about the import\n" +
                    "    */\n" +
                    "    '@client/procedure_with_exporting_members.omt': #or behind the path\n" +
                    "    /**\n" +
                    "    * specific information about the member\n" +
                    "    */\n" +
                    "    -   MijnProcedure #and something about the member\n" +
                    "    '@client/someModule/activity.omt':\n" +
                    "    -   AnotherMember\n" +
                    "\n", text);
        });
    }

    @Test
    void addImportMemberToBlock_NewImport() {
        setExampleFileProcedureWithScript();
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            importUtil.addImportMemberToBlock(getFile(), "'@client/someModule/activity.omt':", "AnotherMember");
            OMTImportBlock importBlock = getElement(OMTImportBlock.class);
            String text = importBlock.getText();
            assertEquals("import:\n" +
                    "    '@client/someModule/activity.omt':\n" +
                    "    -   AnotherMember\n" +
                    "\n", text);
        });
    }
}
