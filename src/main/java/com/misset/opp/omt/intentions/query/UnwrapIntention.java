package com.misset.opp.omt.intentions.query;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.misset.opp.omt.psi.OMTSubQuery;
import org.jetbrains.annotations.NotNull;

public class UnwrapIntention {
    public static IntentionAction getUnwrapIntention(OMTSubQuery subQuery) {
        return new IntentionAction() {
            @NotNull
            @Override
            public @IntentionName String getText() {
                return "Unwrap";
            }

            @NotNull
            @Override
            public @IntentionFamilyName String getFamilyName() {
                return "Query";
            }

            @Override
            public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
                return true;
            }

            @Override
            public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
                subQuery.replace(subQuery.getQuery());
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }
}
