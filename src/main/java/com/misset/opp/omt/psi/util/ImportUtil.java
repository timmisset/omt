package com.misset.opp.omt.psi.util;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTImport;
import com.misset.opp.omt.psi.OMTMember;
import com.misset.opp.omt.psi.support.OMTExportMember;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public class ImportUtil {

    public static final ImportUtil SINGLETON = new ImportUtil();
    private static String MODULE = "module";

    private PsiManager psiManager;

    private PsiManager getPsiManager(Project project) {
        if (psiManager == null) {
            psiManager = PsiManager.getInstance(project);
        }
        return psiManager;
    }

    public void annotateImport(OMTImport omtImport, AnnotationHolder holder) {
        VirtualFile importedFile = getImportedFile(omtImport);
        if (importedFile == null) {
            holder.newAnnotation(HighlightSeverity.ERROR,
                    String.format("%s could not be resolved to a file", omtImport.getImportSource().getText()))
                    .range(omtImport)
                    .create();
        }
    }

    public Optional<PsiElement> resolveImportMember(OMTMember member) {
        // resolve the import member to an import
        OMTImport omtImport = (OMTImport) member.getParent().getParent();
        if (omtImport == null) {
            return Optional.of(member);
        }
        VirtualFile importedFile = getImportedFile(omtImport);
        PsiFile psiFile = getPsiManager(omtImport.getProject()).findFile(importedFile);
        if (psiFile instanceof OMTFile) {
            OMTFile omtFile = (OMTFile) psiFile;
            Optional<OMTExportMember> exportedMember = omtFile.getExportedMember(member.getName());
            return exportedMember.map(OMTExportMember::getResolvingElement);
        }
        return Optional.of(member);
    }

    public VirtualFile getImportedFile(OMTImport omtImport) {
        VirtualFile currentFile = omtImport.getContainingFile().getVirtualFile();
        String importLocation = omtImport.getImportSource().getImportLocation().getText();
        importLocation = importLocation.replaceAll("[\'\":]", "");

        String relativePath = null;
        if (importLocation.startsWith("@")) {
            String basePath = omtImport.getProject().getBasePath();
            String mappedLocation = String.format("%s/%s", basePath, getMappedLocation(importLocation));
            relativePath = getRelativePath(mappedLocation, currentFile);
        } else if (importLocation.startsWith(".")) {
            relativePath = "../" + importLocation;
        } else if (importLocation.startsWith(MODULE)) {
            relativePath = getRelativePath(getModuleLocation(importLocation), currentFile);
        }
        return relativePath != null ? currentFile.findFileByRelativePath(relativePath) : null;
    }

    private String getRelativePath(String path, VirtualFile currentFile) {
        File current = new File(currentFile.getPath());
        File target = new File(path);
        if (!current.exists() || !target.exists()) {
            return null;
        }
        Path relativePath = current.toPath().relativize(target.toPath());
        return relativePath.toString().replace("\\", "/");
    }

    private String getMappedLocation(String location) {
        String mapping = location.substring(0, location.indexOf("/"));
        if ("@client".equals(mapping)) {
            return location.replace(mapping, "frontend/libs");
        }
        return null;
    }

    private String getModuleLocation(String module) {
        if (module.startsWith(MODULE)) {
            module = module.substring(MODULE.length());
        }
        return String.format("frontend/libs/%s/src/%s.module.omt", module, module);
    }
}
