package com.misset.opp.omt.psi.util;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.misset.opp.omt.exceptions.UnknownMappingException;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTImport;
import com.misset.opp.omt.psi.OMTMember;
import com.misset.opp.omt.psi.OMTMemberList;
import com.misset.opp.omt.psi.support.OMTExportMember;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Optional;

public class ImportUtil {

    public static final ImportUtil SINGLETON = new ImportUtil();
    private static final ParsingUtil parsingUtil = new ParsingUtil();

    public void annotateImport(OMTImport omtImport, AnnotationHolder holder) {
        try {
            VirtualFile importedFile = getImportedFile(omtImport);
        } catch (URISyntaxException | UnknownMappingException | FileNotFoundException e) {
            holder.createErrorAnnotation(omtImport, e.getMessage());
        }
    }

    public Optional<PsiElement> resolveImportMember(OMTMember member) {
        // resolve the import member to an import
        OMTImport omtImport = getImport(member);
        if (omtImport == null) {
            return Optional.of(member);
        }
        try {
            VirtualFile importedFile = getImportedFile(omtImport);
            PsiFile psiFile = PsiManager.getInstance(omtImport.getProject()).findFile(importedFile);
            if (psiFile instanceof OMTFile) {
                OMTFile omtFile = (OMTFile) psiFile;
                Optional<OMTExportMember> exportedMember = omtFile.getExportedMember(member.getName());
                return exportedMember.map(OMTExportMember::getResolvingElement);
            }
            throw new UnknownMappingException(omtImport.getImportSource().getImportLocation().getText());
        } catch (URISyntaxException | UnknownMappingException | FileNotFoundException e) {
            return Optional.of(member);
        }
    }

    public OMTImport getImport(OMTMember member) {
        OMTMemberList memberList = getImportMemberList(member);
        if (memberList == null) {
            return null;
        }
        return parsingUtil.castToOrNull(memberList.getParent(), OMTImport.class);
    }

    public OMTMemberList getImportMemberList(OMTMember member) {
        return parsingUtil.castToOrNull(member.getParent(), OMTMemberList.class);
    }

    /**
     * Returns corresponding virtual file or null if an error occurs or it could not be found
     *
     * @param omtImport
     * @return
     */
    public VirtualFile getImportedFileWithoutExceptions(OMTImport omtImport) {
        try {
            return getImportedFile(omtImport);
        } catch (URISyntaxException | UnknownMappingException | FileNotFoundException e) {
            return null;
        }
    }

    public VirtualFile getImportedFile(OMTImport omtImport) throws URISyntaxException, UnknownMappingException, FileNotFoundException {
        VirtualFile currentFile = omtImport.getContainingFile().getVirtualFile();
        String importLocation = omtImport.getImportSource().getImportLocation().getText();
        importLocation = importLocation.replaceAll("[\'\":]", "");

        String relativePath;
        if (importLocation.startsWith("@")) {
            relativePath = getRelativePath(
                    omtImport.getProject().getBasePath() + "/" + getMappedLocation(importLocation)
                    , currentFile);
        } else if (importLocation.startsWith(".")) {
            relativePath = "../" + importLocation;
        } else if (importLocation.startsWith("module:")) {
            relativePath = getRelativePath(getModuleLocation(importLocation), currentFile);
        } else {
            throw new UnknownMappingException(importLocation);
        }
        VirtualFile fileByRelativePath = currentFile.findFileByRelativePath(relativePath);
        if (fileByRelativePath == null) {
            throw new FileNotFoundException(String.format("%s was resolved to %s, but could not be found", omtImport.getImportSource().getImportLocation().getText(), relativePath));
        }
        return fileByRelativePath;
    }

    private String getRelativePath(String path, VirtualFile currentFile) throws URISyntaxException {
        File current = new File(currentFile.getPath());
        File target = new File(path);
        if (!current.exists() || !target.exists()) {
            return null;
        }
        Path relativePath = current.toPath().relativize(target.toPath());
        return relativePath.toString().replace("\\", "/");
    }

    public String getMappedLocation(String location) {
        String mapping = location.substring(0, location.indexOf("/"));
        String resolvedMapping;
        switch (mapping) {
            case "@client":
                resolvedMapping = "frontend/libs";
                break;
            default:
                throw new Error(String.format("Unknown mapping %s", mapping));
        }
        return location.replace(mapping, resolvedMapping);
    }

    public String getModuleLocation(String module) {
        if (module.startsWith("module:")) {
            module = module.substring("module:".length());
        }
        return String.format("frontend/libs/%s/src/%s.module.omt", module, module);
    }
}
