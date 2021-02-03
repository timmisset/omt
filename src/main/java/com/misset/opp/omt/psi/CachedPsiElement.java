package com.misset.opp.omt.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public abstract class CachedPsiElement extends ASTWrapperPsiElement {

    private Project project;
    private PsiFile file;

    public CachedPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Project getProject() {
        if (project == null) project = super.getProject();
        return project;
    }

    @Override
    public PsiFile getContainingFile() {
        if (file == null) file = super.getContainingFile();
        return file;
    }
}
