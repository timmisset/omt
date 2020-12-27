package com.misset.opp.omt.intentions.members;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.named.OMTCall;
import com.misset.opp.omt.psi.support.OMTExportMember;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.getImportUtil;
import static com.misset.opp.omt.psi.util.UtilManager.getProjectUtil;

public class MemberIntention {

    public static List<IntentionAction> getImportMemberIntentions(OMTCall call) {
        List<OMTExportMember> exportMembers = getProjectUtil().getExportMember(call.getName());

        List<IntentionAction> intentionActions = new ArrayList<>();
        exportMembers.forEach(exportMember -> {
            OMTFile exportingFile = (OMTFile) exportMember.getResolvingElement().getContainingFile();
            if (exportingFile != null && exportingFile.getVirtualFile() != null) {
                String exportPath = exportingFile.getVirtualFile().getPath();

                OMTFile callFile = (OMTFile) call.getContainingFile();
                String callPath = callFile.getVirtualFile().getPath();

                Path relativize = new File(callPath).getParentFile().toPath().relativize(new File(exportPath).toPath());
                exportPath = exportPath.replace("\\", "/");
                String clientPath = "@client" + exportPath.substring(exportPath.indexOf("/frontend/libs") + "/frontend/libs".length());

                String relativePath = relativize.toString().replace("\\", "/");
                if (!relativePath.startsWith(".")) {
                    relativePath = "./" + relativePath;
                }

                intentionActions.add(getImportIntention(clientPath, call.getName(), call));
                intentionActions.add(getImportIntention(relativePath, call.getName(), call));
            }
        });
        return intentionActions;
    }

    private static IntentionAction getImportIntention(String path, String name, PsiElement target) {
        return new IntentionAction() {
            @NotNull
            @Override
            public @IntentionName String getText() {
                return String.format("Import %s from %s", name, path);
            }

            @NotNull
            @Override
            public @IntentionFamilyName String getFamilyName() {
                return "Import";
            }

            @Override
            public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
                return true;
            }

            @Override
            public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
                String importPath = String.format("'%s':", path);
                getImportUtil().addImportMemberToBlock(target, importPath, name);
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }
}
