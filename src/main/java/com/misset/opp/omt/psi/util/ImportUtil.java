package com.misset.opp.omt.psi.util;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.support.OMTExportMember;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private void addImportSource(String source, String leadingComment, String endOfLineComment, StringBuilder builder) {
        addLeadingComment(leadingComment, builder);
        builder
                .append(OMTElementFactory.getIndentSpace(1))
                .append(formatImportPath(source))
                .append(endOfLineComment.isEmpty() ? "" : (" " + endOfLineComment))
                .append("\n");
    }

    private void addImportMember(String member, String leadingComment, String endOfLineComment, StringBuilder builder) {
        addLeadingComment(leadingComment, builder);
        builder.append(OMTElementFactory.getIndentSpace(1))
                .append("-   ")
                .append(member.trim())
                .append(endOfLineComment.isEmpty() ? "" : (" " + endOfLineComment))
                .append("\n");
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

    private static void addLeadingComment(String leadingComment, StringBuilder builder) {
        if (!leadingComment.isEmpty()) {
            builder.append(OMTElementFactory.getIndentSpace(1))
                    .append(leadingComment)
                    .append("\n");
        }
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
                    String memberName = omtMemberListItem.getMember().getName().trim();
                    if (!exportingFile.getExportedMember(memberName).isPresent()) {
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
        PsiFile psiFile = getPsiManager(omtImport.getProject()).findFile(importedFile);
        if (psiFile instanceof OMTFile) {
            OMTFile omtFile = (OMTFile) psiFile;
            Optional<OMTExportMember> exportedMember = omtFile.getExportedMember(member.getName());
            return exportedMember.map(OMTExportMember::getResolvingElement);
        }
        return Optional.of(member);
    }

    public void resetImportBlock(PsiElement element) {
        resetImportBlock(element, null, null);
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
        Optional<OMTBlockEntry> rootBlock = targetFile.getRootBlock("import");
        AtomicBoolean importProcessed = new AtomicBoolean(importPath == null || importMember == null);

        StringBuilder importBlockBuilder = new StringBuilder();
        importBlockBuilder.append("import:\n");
        if (rootBlock.isPresent() && rootBlock.get().getSpecificBlock() != null && rootBlock.get().getSpecificBlock().getImportBlock() != null) {
            OMTImportBlock omtBlockEntry = rootBlock.get().getSpecificBlock().getImportBlock();
            omtBlockEntry.getImportList()
                    .forEach(omtImport -> {
                        if (omtImport.getImportSource().getName() != null) {
                            String leadingComment = omtImport.getLeading() != null ? omtImport.getLeading().getText() : "";
                            String trailingComment = omtImport.getTrailing() != null ? omtImport.getTrailing().getText() : "";
                            addImportSource(omtImport.getImportSource().getName(), leadingComment, trailingComment, importBlockBuilder);

                            // add existing imports
                            if (omtImport.getMemberList() != null) {
                                omtImport.getMemberList().getMemberListItemList().forEach(
                                        memberItem -> {
                                            if (memberItem.getMember().getName() != null) {
                                                String leadingMemberComment = memberItem.getLeading() != null ? memberItem.getLeading().getText() : "";
                                                String trailingMemberComment = memberItem.getMember().getEnd().getTrailing() != null ? memberItem.getMember().getEnd().getTrailing().getText() : "";
                                                addImportMember(memberItem.getMember().getName(), leadingMemberComment, trailingMemberComment, importBlockBuilder);
                                            }
                                        }
                                );
                            }

                            // add new import if identical import path:
                            if (!importProcessed.get() && sameImportLocation(omtImport, importPath)) {
                                addImportMember(importMember, "", "", importBlockBuilder);
                                importProcessed.set(true);
                            }
                        }
                    });
        }
        if (!importProcessed.get()) {
            addImportSource(importPath, "", "", importBlockBuilder);
            addImportMember(importMember, "", "", importBlockBuilder);
        }
        importBlockBuilder.append("\n");
        targetFile.setRootBlock((OMTImportBlock) OMTElementFactory.fromString(importBlockBuilder.toString(), OMTImportBlock.class, element.getProject()));
    }

    private boolean sameImportLocation(OMTImport omtImport, String path) {
        return cleanUpPath(omtImport.getImportSource().getImportLocation().getText()).equals(cleanUpPath(path));
    }

    private String cleanUpPath(String path) {
        path = path.trim();
        path = path.startsWith("\"") ? path.substring(1) : path;
        path = path.startsWith("'") ? path.substring(1) : path;
        path = path.endsWith(":") ? path.substring(0, path.length() - 1) : path;
        path = path.endsWith("\"") ? path.substring(0, path.length() - 1) : path;
        path = path.endsWith("'") ? path.substring(0, path.length() - 1) : path;
        return path;
    }

    private String formatImportPath(String path) {
        path = cleanUpPath(path);
        return String.format("'%s':", path);
    }
}
