package com.misset.opp.omt.psi.intentions.members;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.IncorrectOperationException;
import com.misset.opp.omt.psi.OMTElementFactory;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTImport;
import com.misset.opp.omt.psi.support.OMTCall;
import com.misset.opp.omt.psi.support.OMTExportMember;
import com.misset.opp.omt.psi.util.MemberUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MemberIntention {

    private static MemberUtil memberUtil = MemberUtil.SINGLETON;

    public static List<IntentionAction> getImportMemberIntentions(OMTCall call, Predicate<OMTExportMember> isCorrectType) {
        OMTFile containingFile = (OMTFile) call.getContainingFile();
        return containingFile.getImportedFiles().entrySet().stream()
                .map(omtImportVirtualFileEntry -> {
                    OMTFile importedFile = (OMTFile) PsiManager.getInstance(call.getProject())
                            .findFile(omtImportVirtualFileEntry.getValue());
                    HashMap<String, OMTExportMember> exportedMembers = importedFile.getExportedMembers();
                    OMTExportMember omtExportMember = exportedMembers.get(memberUtil.getCallName(call));
                    if (omtExportMember != null && isCorrectType.test(omtExportMember)) {
                        return getImportIntentionFromExistingImportedFile(omtExportMember, omtImportVirtualFileEntry.getValue(), omtImportVirtualFileEntry.getKey());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static IntentionAction getImportIntentionFromExistingImportedFile(OMTExportMember member, VirtualFile file, OMTImport omtImport) {
        return new IntentionAction() {
            @Override
            public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getText() {
                return String.format("Add import %s to existing imports of %s", member.getName(), file.getName());
            }

            @Override
            public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getFamilyName() {
                return "Import";
            }

            @Override
            public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
                return true;
            }

            @Override
            public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
                omtImport.replace(OMTElementFactory.addMemberToImport(project, omtImport, member.getName()));
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }
}
