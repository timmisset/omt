package com.misset.opp.omt.psi.util;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.misset.opp.omt.external.util.builtIn.BuiltInType;
import com.misset.opp.omt.external.util.builtIn.BuiltInUtil;
import com.sun.tools.javac.util.List;

import java.util.Collection;

public class ProjectUtil {

    /**
     * Tries to load all built-in commands and operators that can be retrieved from the BuiltInUtil
     */
    public static void loadBuiltInMembers(Project project) {
        BuiltInUtil.reset();
        WindowManager.getInstance().getStatusBar(project).setInfo("Loading BuiltIn Members of OMT");
        Collection<VirtualFile> builtInCommandsCollection = FilenameIndex.getVirtualFilesByName(project, "builtinCommands.ts", GlobalSearchScope.allScope(project));
        if (builtInCommandsCollection.size() == 1) {
            WindowManager.getInstance().getStatusBar(project).setInfo("Discover builtinCommands.ts file, loading data");
            VirtualFile virtualFile = List.from(builtInCommandsCollection).get(0);
            Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
            if (document != null) {
                BuiltInUtil.reloadBuiltInFromDocument(document, BuiltInType.Command);
            }
        }

        Collection<VirtualFile> builtInOperatorsCollection = FilenameIndex.getVirtualFilesByName(project, "builtinOperators.ts", GlobalSearchScope.allScope(project));
        if (builtInOperatorsCollection.size() == 1) {
            WindowManager.getInstance().getStatusBar(project).setInfo("Discover builtinOperators.ts file, loading data");
            VirtualFile virtualFile = List.from(builtInOperatorsCollection).get(0);
            Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
            if (document != null) {
                BuiltInUtil.reloadBuiltInFromDocument(document, BuiltInType.Operator);
            }
        }

    }


}
