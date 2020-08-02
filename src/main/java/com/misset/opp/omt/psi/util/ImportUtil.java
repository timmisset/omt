package com.misset.opp.omt.psi.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.misset.opp.omt.psi.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ImportUtil {

    public static OMTFile getOMTFile(OMTImport omtImport) {
        String pathToFile = resolvePathToSource(omtImport);
        return getOMTFile(pathToFile, omtImport.getProject());
    }
    public static OMTFile getOMTFile(String path, Project project) {
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(path));
        if(virtualFile != null && virtualFile.exists()) {
            AtomicReference<OMTFile> file = new AtomicReference<>();
            ApplicationManager.getApplication().runReadAction(() -> {
                PsiManager psiManager = PsiManager.getInstance(project);
                file.set((OMTFile) psiManager.findFile(virtualFile));
            });
            return file.get();
        }
        return null;
    }

    /**
     * Returns all exported members from the source, cast to either an Operator or a Command
     * A runnable (Activity, Procedure, Component etc) is considered a command
     * @param omtImport
     * @return
     */
    public static HashMap<String, OMTBuiltIn> getAllExportedMembers(OMTImport omtImport) {
        HashMap<String, OMTBuiltIn> exportedMembers = new HashMap<>();
        String pathToFile = resolvePathToSource(omtImport);
        if(pathToFile == null) { return exportedMembers; }
        return getAllExportedMembers(pathToFile, omtImport.getProject());
    }
    public static HashMap<String, OMTBuiltIn> getAllExportedMembers(String pathToFile, Project project) {
        HashMap<String, OMTBuiltIn> exportedMembers = new HashMap<>();
        if(!pathToFile.endsWith(".omt")) { return exportedMembers; }
        OMTFile omtFile = getOMTFile(pathToFile, project);
        if(omtFile != null) {
            exportedMembers.putAll(getAllExportedMembers(omtFile));
        }
        return exportedMembers;
    }
    public static HashMap<String, OMTBuiltIn> getAllExportedMembers(OMTFile file) {
        return getAllExportedMembers(file, true);
    }
    public static HashMap<String, OMTBuiltIn> getAllExportedMembers(OMTFile file, boolean includingImported) {
        if(ProjectUtil.hasRegisteredExport(file)) { return ProjectUtil.getExports(file); }

        HashMap<String, OMTBuiltIn> exportedMembers = new HashMap<>();
        if(isModuleFile(file) && includingImported) {
            // modules can only export imported members, if includingImported is false there are no members to export
            exportedMembers.putAll(getModuleExports(file));
        }
        else {
            if(includingImported) {
                if(ProjectUtil.hasRegisteredImport(file)) {
                    exportedMembers.putAll(ProjectUtil.getImports(file));
                } else {
                    HashMap<String, OMTBuiltIn> importedMembers = getAllImportedMembers(file);
                    exportedMembers.putAll(importedMembers);
                    ProjectUtil.registerImports(file, importedMembers);
                }
            }
            List<OMTOperator> exportedOperators = getExportedOperators(file);
            List<OMTCommand> exportedCommands = getExportedCommands(file);
            exportedOperators.forEach(omtOperator -> exportedMembers.put(omtOperator.getName(), omtOperator));
            exportedCommands.forEach(omtCommand -> exportedMembers.put(omtCommand.getName(), omtCommand));
        }
        return exportedMembers;
    }

    private static HashMap<String, OMTBuiltIn> getModuleExports(OMTFile file) {
        // module files need to explicitly export members, this way they can be used by components in other modules
        HashMap<String, OMTBuiltIn> exportedMembers = new HashMap<>();

        Optional<OMTExportBlock> exportBlock = getSpecificBlock(file, SpecificBlock.Export);
        HashMap<String, OMTBuiltIn> importedMembers = getAllImportedMembers(file);
        if(importedMembers.isEmpty()) { return exportedMembers; }

        exportBlock.ifPresent(omtExportBlock -> {
            if(omtExportBlock.getExportList() != null) {
                List<OMTListItemExport> listItemExportList = omtExportBlock.getExportList().getListItemExportList();
                listItemExportList.forEach(omtListItemExport -> {
                    String memberName = omtListItemExport.getText();
                    if(importedMembers.containsKey(memberName)) {
                        exportedMembers.put(memberName, importedMembers.get(memberName));
                    }
                });
            }
        });
        return exportedMembers;
    }
    private static List<OMTOperator> getExportedOperators(OMTFile file) {
        List<OMTOperator> operators = new ArrayList<>();
        Optional<OMTQueriesBlock> queriesBlock = getSpecificBlock(file, SpecificBlock.Queries);

        // add all query blocks
        queriesBlock.ifPresent(omtQueriesBlock ->
                operators.addAll(omtQueriesBlock
                        .getDefineQueryStatementList().stream()
                        .map(OMTOperator::new)
                        .collect(Collectors.toList())
                ));

        // and StandAlone Query Items:
        Optional<OMTModelBlock> modelBlock = getSpecificBlock(file, SpecificBlock.Model);
        modelBlock.ifPresent(omtModelBlock ->
                ModelUtil.getModelItemBlocksOfType(omtModelBlock, "StandaloneQuery")
                        .forEach(modelItemBlock -> operators.add(QueryUtil.standAloneQueryToOperator(modelItemBlock))));
        return operators;
    }
    private static List<OMTCommand> getExportedCommands(OMTFile file) {
        List<OMTCommand> commands = new ArrayList<>();
        Optional<OMTCommandsBlock> commandsBlock = getSpecificBlock(file, SpecificBlock.Commands);

        // add all command blocks
        commandsBlock.ifPresent(omtCommandsBlock ->
                commands.addAll(omtCommandsBlock
                        .getDefineCommandStatementList().stream()
                        .map(OMTCommand::new)
                        .collect(Collectors.toList())
                ));

        // also the modelItems
        Optional<OMTModelBlock> modelBlock = getSpecificBlock(file, SpecificBlock.Model);
        modelBlock.ifPresent(omtModelBlock ->
                ModelUtil.getModelItemBlocksOfTypes(omtModelBlock, new String[] { "Activity", "Procedure" })
                    .forEach(modelItemBlock -> commands.add(CommandUtil.modelItemToCommand(modelItemBlock)))
        );


        return commands;
    }
    /**
     * Returns all imported members of this OMT file as Commands or Operators
     * @param file
     * @return
     */
    public static HashMap<String, OMTBuiltIn> getAllImportedMembers(OMTFile file) {
        if(ProjectUtil.hasRegisteredImport(file)) { return ProjectUtil.getImports(file); }
        HashMap<String, OMTBuiltIn> allImportedMembers = new HashMap<>();
        Optional<OMTImportBlock> importBlock = getSpecificBlock(file, SpecificBlock.Import);
        if(!importBlock.isPresent()) { return allImportedMembers; }

        importBlock.get().getImportList().forEach(omtImport -> {
            // check if there already is a registry entry for this file to extract the exported methods from
            // otherwise, build it
            OMTFile sourceFile = getOMTFile(omtImport);
            HashMap<String, OMTBuiltIn> exportedMembers = new HashMap<>();
            if(ProjectUtil.hasRegisteredExport(sourceFile)) {
                exportedMembers.putAll(ProjectUtil.getExports(sourceFile));
            } else {
                exportedMembers = getAllExportedMembers(omtImport);
                ProjectUtil.registerExports(sourceFile, exportedMembers);
            }

            // filter the available exports against the ones stated by the imports
            if(omtImport.getImportList() != null) {
                for(OMTListItemImport omtListItemImport : omtImport.getImportList().getListItemImportList()) {
                    String memberName = omtListItemImport.getListItemImportMember().getText();
                    if(exportedMembers.containsKey(memberName)) {
                        allImportedMembers.put(memberName, exportedMembers.get(memberName));
                    }
                }
            }

        });
        return allImportedMembers;
    }

    public static <T> Optional<T> getSpecificBlock(OMTFile file, SpecificBlock blockType) {

        for(PsiElement element : file.getChildren()) {
            if(element instanceof OMTSpecificBlock) {
                OMTSpecificBlock specificBlock = (OMTSpecificBlock)element;
                if(blockType == SpecificBlock.Commands && specificBlock.getCommandsBlock() != null) {
                    return Optional.of((T)specificBlock.getCommandsBlock());
                }
                if(blockType == SpecificBlock.Queries && specificBlock.getQueriesBlock() != null) {
                    return Optional.of((T)specificBlock.getQueriesBlock());
                }
                if(blockType == SpecificBlock.Prefixes && specificBlock.getPrefixBlock() != null) {
                    return Optional.of((T)specificBlock.getPrefixBlock());
                }
                if(blockType == SpecificBlock.Model && specificBlock.getModelBlock() != null) {
                    return Optional.of((T)specificBlock.getModelBlock());
                }
                if(blockType == SpecificBlock.Import && specificBlock.getImportBlock() != null) {
                    return Optional.of((T)specificBlock.getImportBlock());
                }
                if(blockType == SpecificBlock.Export && specificBlock.getExportBlock() != null) {
                    return Optional.of((T)specificBlock.getExportBlock());
                }
                if(blockType == SpecificBlock.Module && specificBlock.getModuleBlock() != null) {
                    return Optional.of((T)specificBlock.getModuleBlock());
                }
            }
        }
        return Optional.empty();
    }

    public static String resolvePathToSource(OMTImport omtImport) {
        String path = omtImport.getImportSource().getText().replace("'", "").replace("\"", "");
        if(path.endsWith(":")) { path = path.substring(0, path.length() - 1); }
        if(path.startsWith("@client")) {
            return omtImport.getProject().getBasePath() + path.replace("@client", "/frontend/libs");
        }
        else if(path.startsWith("module:")) {
            // we need to locate the module file... todo
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

    public static void setAllExportedMembers(OMTFile file) {

    }

    private static boolean isModuleFile(OMTFile file) {
        return getSpecificBlock(file, SpecificBlock.Module).isPresent();
    }
    private static String getModuleName(OMTFile file) {
        Optional<OMTModuleBlock> optionalOMTModuleBlock = getSpecificBlock(file, SpecificBlock.Module);
        return optionalOMTModuleBlock.map(omtModuleBlock -> omtModuleBlock.getModuleName().getText()).orElse(null);
    }
}
