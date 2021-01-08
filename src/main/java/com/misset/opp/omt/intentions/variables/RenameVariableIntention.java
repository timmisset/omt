package com.misset.opp.omt.intentions.variables;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.psi.OMTVariable;
import org.jetbrains.annotations.NotNull;

public class RenameVariableIntention {

    public IntentionAction getRenameVariableIntention(OMTVariable variable, String newName) {
        return new IntentionAction() {
            @Override
            @NotNull
            public String getText() {
                return String.format("Rename to %s", newName);
            }

            @Override
            @NotNull
            public String getFamilyName() {
                return "Variables";
            }

            @Override
            public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
                return true;
            }

            @Override
            public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
                variable.setName(newName);
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }

}
