package com.misset.opp.omt.intentions.imports;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.psi.OMTImportSource;
import org.jetbrains.annotations.NotNull;

public class UnwrapIntention {
    public static IntentionAction getUnwrapIntention(OMTImportSource importSource) {
        return new IntentionAction() {
            @NotNull
            @Override
            public @IntentionName String getText() {
                return "Unwrap";
            }

            @NotNull
            @Override
            public @IntentionFamilyName String getFamilyName() {
                return "Import source";
            }

            @Override
            public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
                return true;
            }

            @Override
            public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
                if (importSource.getName() == null) {
                    return;
                }
                importSource.setName(
                        importSource.getName().replace("'", "")
                );
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }
}
