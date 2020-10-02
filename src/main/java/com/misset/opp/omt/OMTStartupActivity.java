package com.misset.opp.omt;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.util.ProjectUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Run the initial analysis on the project files after the Project has started
 */
public class OMTStartupActivity implements StartupActivity {

    private final ProjectUtil projectUtil = ProjectUtil.SINGLETON;

    @Override
    public void runActivity(@NotNull Project project) {
        DumbService.getInstance(project).runWhenSmart(() -> {
            projectUtil.loadBuiltInMembers(project);
            projectUtil.getParsedModel();

            PsiManager psiManager = PsiManager.getInstance(project);
            List<String> processedPaths = new ArrayList<>();
            FilenameIndex.getAllFilesByExt(project, "omt")
                    .forEach(virtualFile -> {
                                if (!processedPaths.contains(virtualFile.getPath())) {
                                    projectUtil.analyzeFile((OMTFile) psiManager.findFile(virtualFile));
                                    processedPaths.add(virtualFile.getPath());
                                }
                            }
                    );
            projectUtil.getStatusBar(project).setInfo("Analyzed " + processedPaths.size() + " file(s)");
            projectUtil.getKnownPrefixes("test");
        });


    }
}
