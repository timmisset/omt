package com.misset.opp.omt.psi.intentions.members;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.support.OMTCall;
import com.misset.opp.omt.psi.support.OMTExportMember;
import com.misset.opp.omt.psi.util.ImportUtil;
import com.misset.opp.omt.psi.util.MemberUtil;
import com.misset.opp.omt.psi.util.ProjectUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MemberIntention {

    private MemberUtil memberUtil = MemberUtil.SINGLETON;
    private ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    public static MemberIntention SINGLETON = new MemberIntention();

    public List<IntentionAction> getImportMemberIntentions(OMTCall call) {
        String _callname = call.getFirstChild().getText();
        final String callName = _callname.startsWith("@") ? _callname.substring(1) : _callname;
        List<OMTExportMember> exportMembers = projectUtil.getExportMember(callName);

        List<IntentionAction> intentionActions = new ArrayList<>();
        exportMembers.forEach(exportMember -> {
            OMTFile exportingFile = (OMTFile) exportMember.getResolvingElement().getContainingFile();
            String exportPath = exportingFile.getVirtualFile().getPath();

            OMTFile callFile = (OMTFile) call.getContainingFile();
            String callPath = callFile.getVirtualFile().getPath();

            Path relativize = new File(callPath).getParentFile().toPath().relativize(new File(exportPath).toPath());
            exportPath = exportPath.replace("\\", "/");
            String clientPath = "@client" + exportPath.substring(exportPath.indexOf("/frontend/libs") + "/frontend/libs".length());

            String relativePath = relativize.toString().replace("\\", "/");

            intentionActions.add(getImportIntention(clientPath, callName, call));
            intentionActions.add(getImportIntention(relativePath, callName, call));
        });
        return intentionActions;
    }

    private IntentionAction getImportIntention(String path, String name, PsiElement target) {
        ImportUtil importUtil = ImportUtil.SINGLETON;
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
            public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
                String importPath = String.format("'%s':", path);
                importUtil.addImportMemberToBlock(target, importPath, name);
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }
}
