package com.misset.opp.omt.psi.intentions.generic;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class RemoveIntention {
    public static final RemoveIntention SINGLETON = new RemoveIntention();

    public IntentionAction getRemoveIntention(PsiElement element) {
        return getRemoveIntention(element, "Remove");
    }

    public IntentionAction getRemoveIntention(PsiElement element, String text) {
        return new IntentionAction() {
            @Nls(capitalization = Nls.Capitalization.Sentence)
            @NotNull
            @Override
            public String getText() {
                return text;
            }

            @Nls(capitalization = Nls.Capitalization.Sentence)
            @NotNull
            @Override
            public String getFamilyName() {
                return "Remove";
            }

            @Override
            public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
                return true;
            }

            @Override
            public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
                element.delete();
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }
}
