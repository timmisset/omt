package com.misset.opp.omt.psi.util;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.misset.opp.omt.exceptions.UnknownMappingException;
import com.misset.opp.omt.psi.OMTImport;
import com.misset.opp.omt.psi.OMTMember;
import com.misset.opp.omt.psi.OMTMemberList;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class ImportUtil {

    public static void annotateImport(OMTImport omtImport, AnnotationHolder holder) {
        try {
            VirtualFile importedFile = getImportedFile(omtImport);
        } catch (URISyntaxException | UnknownMappingException | FileNotFoundException e) {
            holder.createErrorAnnotation(omtImport, e.getMessage());
        }
    }

    public static OMTImport getImport(OMTMember member) {
        OMTMemberList memberList = getImportMemberList(member);
        if (memberList == null) {
            return null;
        }
        return ParsingUtil.castToOrNull(memberList.getParent(), OMTImport.class);
    }

    public static OMTMemberList getImportMemberList(OMTMember member) {
        return ParsingUtil.castToOrNull(member.getParent(), OMTMemberList.class);
    }

    public static VirtualFile getImportedFile(OMTImport omtImport) throws URISyntaxException, UnknownMappingException, FileNotFoundException {
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

    private static String getRelativePath(String path, VirtualFile currentFile) throws URISyntaxException {
        File current = new File(currentFile.getPath());
        File target = new File(path);
        if (!current.exists() || !target.exists()) {
            return null;
        }
        Path relativePath = current.toPath().relativize(target.toPath());
        return relativePath.toString().replace("\\", "/");
    }

    public static String getMappedLocation(String location) {
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

    public static String getModuleLocation(String module) {
        if (module.startsWith("module:")) {
            module = module.substring("module:".length());
        }
        return String.format("frontend/libs/%s/src/%s.module.omt", module, module);
    }
}
