package com.misset.opp.omt.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.NotNull;

public class AbstractCodeInspection extends LocalInspectionTool {

    ProblemsHolder holder;

    public void setHolder(ProblemsHolder holder) {
        this.holder = holder;
    }

    protected LocalQuickFix getRenameQuickFix(PsiNameIdentifierOwner element, String newName) {
        return new LocalQuickFix() {
            @Override
            public @IntentionFamilyName @NotNull String getFamilyName() {
                return "Rename";
            }

            @Override
            public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
                element.setName(newName);
            }
        };
    }

    protected LocalQuickFix getRemoveQuickFix(PsiElement element) {
        return new LocalQuickFix() {
            @Override
            public @IntentionFamilyName @NotNull String getFamilyName() {
                return "Remove";
            }

            @Override
            public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
                element.delete();
            }
        };
    }

    /**
     * True if the file is contained in a mocha subfolder, used in the project for mocking imports during unit tests
     * These files should by default not be processed for inspection
     *
     * @return
     */
    protected boolean isMochaFile(ProblemsHolder holder) {
        final VirtualFile virtualFile = holder.getFile().getVirtualFile();
        return virtualFile.getPath().endsWith("/mocha/" + virtualFile.getName());
    }

    protected void validateReference(PsiElement element, String errorMessage) {
        if (element.getReference() != null &&
                (element.getReference().resolve() == null ||
                        element.getReference().resolve() == element)) {
            setError(element, errorMessage);
        }
    }

    protected void setError(PsiElement element, String message) {
        holder.registerProblem(element, message, ProblemHighlightType.ERROR);
    }

}
