//package com.misset.opp.omt.domain.util;
//
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.vfs.VirtualFile;
//import com.intellij.psi.PsiManager;
//import com.intellij.psi.search.FileTypeIndex;
//import com.intellij.psi.search.GlobalSearchScope;
//import com.misset.opp.omt.OMTFileType;
//import com.misset.opp.omt.domain.OMTBuiltIn;
//import com.misset.opp.omt.psi.OMTFile;
//
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class ProjectUtil {
//
//    /**
//     * Registry of all member imports and exports throughout the project
//     * Will be set once during analyzeProject, when the file contents are altered, the annotate OMTFile triggers a refresh
//     * for a specific file
//     */
//    private static HashMap<String, HashMap<String, OMTBuiltIn>> exportedMembers = new HashMap<>();
//    private static HashMap<String, HashMap<String, OMTBuiltIn>> importedMembers = new HashMap<>();
//
//    private static boolean isProjectAnalyzed = false;
//    public static void analyzeProject(Project project) { analyzeProject(project, false); }
//    public static void analyzeProject(Project project, boolean forceRefesh) {
//        if(isProjectAnalyzed && !forceRefesh) { return; }
//
//        clearRegistry();
//        getAllOMTFiles(project).forEach(omtFile -> {
//            exportedMembers.put(omtFile.getVirtualFile().getPath(), ImportUtil.getAllExportedMembers(omtFile, false));
//        });
//        isProjectAnalyzed = true;
//    }
//
//    public static List<OMTFile> getAllOMTFiles(Project project) {
//        Collection<VirtualFile> virtualFiles =
//                FileTypeIndex.getFiles(OMTFileType.INSTANCE, GlobalSearchScope.allScope(project));
//
//        // in a read action, read all the files
//        PsiManager psiManager = PsiManager.getInstance(project);
//
//        return virtualFiles.stream().map(virtualFile -> (OMTFile)psiManager.findFile(virtualFile)).collect(Collectors.toList());
//    }
//    private static void clearRegistry() {
//        exportedMembers.clear();
//        importedMembers.clear();
//    }
//
//    public static void registerImports(OMTFile file, HashMap<String, OMTBuiltIn> builtInHashMap) {
//        importedMembers.put(file.getVirtualFile().getPath(), builtInHashMap);
//    }
//    public static HashMap<String, OMTBuiltIn> getImports(OMTFile file) {
//        return importedMembers.getOrDefault(file.getVirtualFile().getPath(), new HashMap<>());
//    }
//    public static boolean hasRegisteredImport(OMTFile file) {
//        return file != null && file.getVirtualFile() != null && importedMembers.containsKey(file.getVirtualFile().getPath());
//    }
//
//    public static void registerExports(OMTFile file, HashMap<String, OMTBuiltIn> builtInHashMap) {
//
//        exportedMembers.put(file.getVirtualFile().getPath(), builtInHashMap);
//    }
//    public static HashMap<String, OMTBuiltIn> getExports(OMTFile file) {
//        return exportedMembers.getOrDefault(file.getVirtualFile().getPath(), new HashMap<>());
//    }
//    public static boolean hasRegisteredExport(OMTFile file) {
//        return file != null && file.getVirtualFile() != null && exportedMembers.containsKey(file.getVirtualFile().getPath());
//    }
//    public static void removeRegistration(OMTFile file) {
//        if(exportedMembers.containsKey(file.getVirtualFile().getPath())) {
//            exportedMembers.remove(file.getVirtualFile().getPath());
//        }
//        if(importedMembers.containsKey(file.getVirtualFile().getPath())) {
//            importedMembers.remove(file.getVirtualFile().getPath());
//        }
//    }
//
//    public static void resetImportedExportedMembers(OMTFile omtFile) {
//
//        removeRegistration(omtFile);
//        String path2File = omtFile.getVirtualFile().getPath();
//        exportedMembers.put(path2File, ImportUtil.getAllExportedMembers(omtFile, true));
//        importedMembers.put(path2File, ImportUtil.getAllImportedMembers(omtFile));
//
//    }
//}
