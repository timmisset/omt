package com.misset.opp.omt.psi.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.misset.opp.omt.psi.*;
import org.intellij.sdk.language.parser.OMTParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ImportUtil {

    /**
     * Returns all exported members from the source, cast to either an Operator or a Command
     * A runnable (Activity, Procedure, Component etc) is considered a command
     * @param omtImport
     * @return
     */
    public static HashMap<String, Object> getAllExportingMembers(OMTImport omtImport) {
        String pathToFile = resolvePathToSource(omtImport);
        if(pathToFile == null) { return null; }
        return getAllExportingMembers(pathToFile, omtImport.getProject());
    }

    public static HashMap<String, Object> getAllExportingMembers(String pathToFile, Project project) {
        HashMap<String, Object> exportedMembers = new HashMap<>();

        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(pathToFile));
        if(virtualFile != null && virtualFile.exists()) {
            ApplicationManager.getApplication().runReadAction(() -> {
                PsiManager psiManager = PsiManager.getInstance(project);
                PsiFile file = psiManager.findFile(virtualFile);

                @NotNull PsiElement[] children = file.getChildren();
                for(PsiElement child : children) {
                    if(child instanceof OMTSpecificBlock) {
                        OMTSpecificBlock specificBlock = (OMTSpecificBlock) child;
                        if(specificBlock.getQueriesBlock() != null) { setExportingQueries(exportedMembers, specificBlock.getQueriesBlock()); }
                    }
                }
            });
        }

        exportedMembers.forEach((s, o) -> System.out.println("processed " + s));
        return exportedMembers;
    }

    private static void setExportingQueries(HashMap<String, Object> exportedMembers, OMTQueriesBlock specificBlock) {
        // these are items in queries and modelitem !StandAloneQuery
        specificBlock.getDefineQueryStatementList().forEach(
                defineQueryStatement -> {
                    OMTOperator omtOperator = new OMTOperator(defineQueryStatement);
                    exportedMembers.put(omtOperator.getName(), omtOperator);
                }
        );
    }

    public static Optional<OMTSpecificBlock> getSpecificBlock(OMTFile file, SpecificBlock blockType) {

        for(PsiElement element : file.getChildren()) {
            if(element instanceof OMTSpecificBlock) {
                OMTSpecificBlock specificBlock = (OMTSpecificBlock)element;
                if(blockType == SpecificBlock.Commands && specificBlock.getCommandsBlock() != null) {
                    return Optional.of(specificBlock);
                }
                if(blockType == SpecificBlock.Queries && specificBlock.getQueriesBlock() != null) {
                    return Optional.of(specificBlock);
                }
                if(blockType == SpecificBlock.Prefixes && specificBlock.getPrefixBlock() != null) {
                    return Optional.of(specificBlock);
                }
                if(blockType == SpecificBlock.Model && specificBlock.getModelBlock() != null) {
                    return Optional.of(specificBlock);
                }
                if(blockType == SpecificBlock.Import && specificBlock.getImportBlock() != null) {
                    return Optional.of(specificBlock);
                }
            }
        }
        return Optional.empty();
    }

    public static String resolvePathToSource(OMTImport omtImport) {
        String path = omtImport.getImportSource().getText().replace("'", "").replace("\"", "");
        if(path.endsWith(":")) { path = path.substring(0, path.length() - 1); }
        if(path.startsWith("@client")) {
            return omtImport.getProject().getBasePath() + path.replace("@client", "/frontend/src");
        }
        else if(path.startsWith("module:")) {
            // we need to locatie the module file... todo
        }
        else {
            // TODO: There is probably a way to use relativize to do this in one line
            File file = new File(omtImport.getContainingFile().getVirtualFile().getPath());
            if(path.startsWith("../")) {
                while(path.startsWith("../")) {
                    file = file.getParentFile();
                    path = path.substring(3);
                }
                return file.getPath() + "/" + path;
            } else if(path.startsWith("./")) {
                path = path.substring(2);
                return file.getParent() + "/" + path;
            }

        }
        return null;
    }
}
