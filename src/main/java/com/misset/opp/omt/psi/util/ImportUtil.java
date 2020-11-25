package com.misset.opp.omt.psi.util;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.support.OMTExportMember;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ImportUtil {

    public static final ImportUtil SINGLETON = new ImportUtil();
    private static final String MODULE = "module";

    private PsiManager psiManager;

    private PsiManager getPsiManager(Project project) {
        if (psiManager == null) {
            psiManager = PsiManager.getInstance(project);
        }
        return psiManager;
    }

    public VirtualFile getImportedFile(OMTImport omtImport) {
        return getImportedFile(omtImport, omtImport.getContainingFile().getVirtualFile());
    }

    public VirtualFile getImportedFile(OMTImport omtImport, VirtualFile importingFile) {
        if (importingFile == null) {
            return null;
        }
        String importLocation = omtImport.getImportSource().getImportLocation().getText();
        importLocation = importLocation.replaceAll("[\'\":]", "");

        // Fix to allow testing the completion which is triggered in the fixture
        // The fixture will copy files in a temp memory index only so the files do not exist
        // and thus cannot be resolved. Also there is no method to stub this behavior since
        // most of the process is performed using static classes
        // ugly solition: defer the virtualfile via the FilenameIndex:
        if (importingFile.getUrl().startsWith("temp:///")) {
            String[] split = importLocation.split("/");
            final PsiFile[] filesByName = FilenameIndex.getFilesByName(omtImport.getProject(), split[split.length - 1], GlobalSearchScope.allScope(omtImport.getProject()));
            return filesByName.length > 0 ? filesByName[0].getVirtualFile() : null;
        }

        String relativePath = null;
        if (importLocation.startsWith("@")) {
            String basePath = omtImport.getProject().getBasePath();
            importLocation = String.format("%s/%s", basePath, getMappedLocation(importLocation));
            relativePath = getRelativePath(importLocation, importingFile);
        } else if (importLocation.startsWith(".")) {
            relativePath = "../" + importLocation;
        } else if (importLocation.startsWith(MODULE)) {
            relativePath = getRelativePath(
                    String.format("%s/%s",
                            omtImport.getProject().getBasePath(),
                            getModuleLocation(importLocation.trim().toLowerCase())), importingFile);
        }
        return relativePath != null ? importingFile.findFileByRelativePath(relativePath) : null;
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

    public void annotateImport(OMTImport omtImport, AnnotationHolder holder) {
        VirtualFile importedFile = getImportedFile(omtImport);
        if (importedFile == null) {
            holder.newAnnotation(HighlightSeverity.ERROR,
                    String.format("%s could not be resolved to a file", omtImport.getImportSource().getText()))
                    .range(omtImport)
                    .create();
        } else {
            OMTFile exportingFile = (OMTFile) getPsiManager(omtImport.getProject()).findFile(importedFile);
            if (omtImport.getMemberList() != null) {
                omtImport.getMemberList().getMemberListItemList().forEach(omtMemberListItem -> {
                    String memberName = omtMemberListItem.getName().trim();
                    if (exportingFile != null &&
                            omtMemberListItem.getMember() != null &&
                            !exportingFile.getExportedMember(memberName).isPresent()) {
                        holder.newAnnotation(HighlightSeverity.ERROR,
                                String.format("%s is not an exported member of %s", memberName, omtImport.getImportSource().getText()))
                                .range(omtMemberListItem.getMember())
                                .create();
                    }
                });
            }
        }
    }

    public Optional<PsiElement> resolveImportMember(OMTMember member) {
        // resolve the import member to an import
        OMTImport omtImport = (OMTImport) member.getParent().getParent().getParent();
        if (omtImport == null) {
            return Optional.of(member);
        }
        VirtualFile importedFile = getImportedFile(omtImport);
        if (importedFile != null) {
            PsiFile psiFile = getPsiManager(omtImport.getProject()).findFile(importedFile);
            if (psiFile instanceof OMTFile) {
                OMTFile omtFile = (OMTFile) psiFile;
                Optional<OMTExportMember> exportedMember = omtFile.getExportedMember(member.getName());
                return exportedMember.map(OMTExportMember::getResolvingElement);
            }
        }
        return Optional.of(member);
    }

    public void addImportMemberToBlock(PsiElement element, String importPath, String importMember) {
        resetImportBlock(element, importPath, importMember);
    }

    /**
     * Returns a @client and relative import options for the exporting member in the context of the importing file
     * if the file already contains one of the options, that will be the only one showing
     *
     * @param exportMember
     * @param importingFile
     * @return
     */
    public List<String> getImportPaths(OMTExportMember exportMember, OMTFile importingFile) {
        OMTFile exportingFile = (OMTFile) exportMember.getResolvingElement().getContainingFile();
        String exportPath = exportingFile.getVirtualFile().getPath();

        String importFilePath = importingFile.getVirtualFile().getPath();

        Path relativize = new File(importFilePath).getParentFile().toPath().relativize(new File(exportPath).toPath());
        exportPath = exportPath.replace("\\", "/");
        String clientPath = "@client" + exportPath.substring(exportPath.indexOf("/frontend/libs") + "/frontend/libs".length());
        if (importingFile.hasImport(clientPath)) {
            return Collections.singletonList(clientPath);
        }

        String relativePath = relativize.toString().replace("\\", "/");
        if (!relativePath.startsWith(".")) {
            relativePath = "./" + relativePath;
        }
        if (importingFile.hasImport(relativePath)) {
            return Collections.singletonList(relativePath);
        }

        return Arrays.asList(clientPath, relativePath);
    }

    private void resetImportBlock(PsiElement element, String importPath, String importMember) {
        OMTFile targetFile = (OMTFile) element.getContainingFile();
        Project project = element.getProject();
        String template = String.format("import:\n" +
                "    %s\n" +
                "        -   %s\n\n", importPath, importMember);
        Optional<OMTImportBlock> optionalImportBlock = targetFile.getSpecificBlock("import", OMTImportBlock.class);
        if (optionalImportBlock.isPresent()) {
            final OMTImportBlock importBlock = optionalImportBlock.get();
            final Optional<OMTImport> existingImport = importBlock.getImportList().stream().filter(omtImport -> sameImportLocation(omtImport, importPath)).findFirst();
            if (existingImport.isPresent()) {
                // add member to existing import
                final OMTMemberList memberList = existingImport.get().getMemberList();
                if (memberList == null) {
                    return;
                } // invalid import statement (empty import)
                final List<OMTMemberListItem> memberListItemList = memberList.getMemberListItemList();
                final PsiElement importMemberItem = OMTElementFactory.fromString(template, OMTMemberListItem.class, project);
                final PsiElement addedMember = memberList.addAfter(importMemberItem, memberListItemList.get(memberListItemList.size() - 1));
                memberList.addBefore(OMTElementFactory.createNewLine(project), addedMember);
            } else {
                // add new import and member to existing import block
                final OMTImport newImport = (OMTImport) OMTElementFactory.fromString(template, OMTImport.class, project);
                final OMTImport lastImport = importBlock.getImportList().get(importBlock.getImportList().size() - 1);
                importBlock.addAfter(newImport, lastImport);
            }
            CodeStyleManager.getInstance(project).reformat(importBlock);
        } else {
            // add new import block:
            OMTImportBlock importBlock = (OMTImportBlock) OMTElementFactory.fromString(template, OMTImportBlock.class, project);
            targetFile.setRootBlock(importBlock);
            CodeStyleManager.getInstance(project).reformat(importBlock);
        }
    }

    private boolean sameImportLocation(OMTImport omtImport, String path) {
        return cleanUpPath(omtImport.getImportSource().getImportLocation().getText()).equals(cleanUpPath(path));
    }

    private String cleanUpPath(String path) {
        if (path == null) {
            return "";
        }
        path = path.trim();
        path = path.startsWith("\"") ? path.substring(1) : path;
        path = path.startsWith("'") ? path.substring(1) : path;
        path = path.endsWith(":") ? path.substring(0, path.length() - 1) : path;
        path = path.endsWith("\"") ? path.substring(0, path.length() - 1) : path;
        path = path.endsWith("'") ? path.substring(0, path.length() - 1) : path;
        return path;
    }

}
