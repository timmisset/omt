package com.misset.opp.omt;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.misset.opp.omt.psi.util.ProjectUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Run the initial analysis on the project files after the Project has started
 */
public class OMTStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        ProjectUtil.loadBuiltInMembers(project);
        System.out.println("activity started");
//        PsiManager psiManager = PsiManager.getInstance(project);
//        List<String> processedPaths = new ArrayList<>();
//        FilenameIndex.getAllFilesByExt(project, "omt")
//                .forEach(virtualFile -> {
//                    if(!processedPaths.contains(virtualFile.getPath())) {
//                        ProjectUtil.analyzeFile((OMTFile)psiManager.findFile(virtualFile));
//                        processedPaths.add(virtualFile.getPath());
//                    }
//                }
//        );

    }
}
