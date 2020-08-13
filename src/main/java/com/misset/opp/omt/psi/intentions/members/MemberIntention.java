package com.misset.opp.omt.psi.intentions.members;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.IncorrectOperationException;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.support.OMTExportMember;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.util.MemberUtil.getCallName;

public class MemberIntention {
    public static List<IntentionAction> getImportIntentions(OMTCommandCall commandCall) {
        return getImportMemberIntentions(commandCall, OMTExportMember::isCommand);
    }

    public static List<IntentionAction> getImportIntentions(OMTOperatorCall operatorCall) {
        return getImportMemberIntentions(operatorCall, OMTExportMember::isOperator);
    }

    private static List<IntentionAction> getImportMemberIntentions(PsiElement call, Predicate<OMTExportMember> isCorrectType) {
        OMTFile containingFile = (OMTFile) call.getContainingFile();
        return containingFile.getImportedFiles().entrySet().stream()
                .map(omtImportVirtualFileEntry -> {
                    OMTFile importedFile = (OMTFile) PsiManager.getInstance(call.getProject())
                            .findFile(omtImportVirtualFileEntry.getValue());
                    HashMap<String, OMTExportMember> exportedMembers = importedFile.getExportedMembers();
                    OMTExportMember omtExportMember = exportedMembers.get(getCallName(call));
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
                return "import";
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
