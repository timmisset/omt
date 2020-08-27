package com.misset.opp.omt.psi.intentions.model;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTElementFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class ModelIntention {

    public static IntentionAction addBlockEntryIntention(OMTBlock block, String propertyLabel, String text) {
        return addBlockEntryIntention(block, propertyLabel, null, text);
    }

    public static IntentionAction addBlockEntryIntention(OMTBlock block, String propertyLabel, String propertyValue, String text) {
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
                return "Add";
            }

            @Override
            public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
                return true;
            }

            @Override
            public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
                OMTBlock omtBlock = propertyValue == null ?
                        OMTElementFactory.addEntryToBlock(project, block, propertyLabel, editor.getDocument()) :
                        OMTElementFactory.addEntryToBlock(project, block, propertyLabel, propertyValue, editor.getDocument());
                block.replace(omtBlock);
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }

    public static IntentionAction getRemoveBlockEntryIntention(OMTBlockEntry blockEntry, String text) {
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
                blockEntry.delete();
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }
}
