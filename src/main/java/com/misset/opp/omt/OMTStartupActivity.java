package com.misset.opp.omt;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.util.ProjectUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.misset.opp.omt.psi.util.UtilManager.getProjectUtil;

/**
 * Run the initial analysis on the project files after the Project has started
 */
public class OMTStartupActivity implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        DumbService.getInstance(project).runWhenSmart(() -> {

            // the builtin members are loaded directly from the project content
            // file changes result in reloading the loadBuiltInMembers
            getProjectUtil().loadBuiltInMembers(project);
            setFileListeners(project);

            // parse the OMT Model, this is currently a static resource in the project
            getProjectUtil().getParsedModel();

            // load the ontology model
            if (isInProductionMode(project)) {
                getProjectUtil().loadOntologyModel(project, true);
            }

            // finally, analyze all OMT files for exporting members and prefixess
            analyzeAllOMTFiles(project);

        });
    }

    private void analyzeAllOMTFiles(@NotNull Project project) {
        PsiManager psiManager = PsiManager.getInstance(project);
        List<String> processedPaths = new ArrayList<>();
        FilenameIndex.getAllFilesByExt(project, "omt")
                .forEach(virtualFile -> {
                            if (!processedPaths.contains(virtualFile.getPath())) {
                                getProjectUtil().analyzeFile((OMTFile) psiManager.findFile(virtualFile));
                                processedPaths.add(virtualFile.getPath());
                            }
                        }
                );
        getProjectUtil().setStatusbarMessage(project, "Analyzed " + processedPaths.size() + " file(s)");
    }

    private boolean isInProductionMode(Project project) {
        return !project.getName().startsWith("light_temp");
    }

    private void setFileListeners(@NotNull Project project) {
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                events
                        .stream()
                        .map(VFileEvent::getFile)
                        .filter(Objects::nonNull)
                        .filter(VirtualFile::isValid)
                        .forEach(virtualFile -> {
                            switch (virtualFile.getName()) {
                                case ProjectUtil.BUILTIN_COMMANDS:
                                case ProjectUtil.BUILTIN_HTTP_COMMANDS:
                                case ProjectUtil.BUILTIN_JSON_PARSE_COMMAND:
                                case ProjectUtil.BUILTIN_OPERATORS:
                                    getProjectUtil().loadBuiltInMembers(project);
                                    return;
                                default:
                                    byExtension(virtualFile);
                            }
                        });
            }

            private void byExtension(VirtualFile virtualFile) {
                if (virtualFile == null || virtualFile.getExtension() == null) {
                    return;
                }
                String extension = virtualFile.getExtension();
                if ("ttl".equals(extension)) {
                    if (isInProductionMode(project)) {
                        getProjectUtil().loadOntologyModel(project, true);
                    }
                } else if ("omt".equals(extension)) {
                    final OMTFile file = (OMTFile) PsiManager.getInstance(project).findFile(virtualFile);
                    if (file == null) {
                        return;
                    }
                    getProjectUtil().resetExportedMembers(file);
                }
            }
        });
    }
}
