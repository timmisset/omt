package com.misset.opp.omt.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

public class AbstractCodeInspection extends LocalInspectionTool {

    ProblemsHolder holder;

    public void setHolder(ProblemsHolder holder) {
        this.holder = holder;
    }

    /**
     * True if the file is contained in a mocha subfolder, used in the project for mocking imports during unit tests
     * These files should by default not be processed for inspection
     *
     * @return
     */
    protected boolean isTestFile(ProblemsHolder holder) {
        final VirtualFile virtualFile = holder.getFile().getVirtualFile();
        return virtualFile.getPath().endsWith("/mocha/" + virtualFile.getName()) ||
                virtualFile.getName().endsWith(".spec.omt");
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
