package com.misset.opp.omt.psi.intentions.generic;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.util.CurieUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RemoveIntention {
    public static final RemoveIntention SINGLETON = new RemoveIntention();

    private CurieUtil curieUtil = CurieUtil.SINGLETON;

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
                deleteElementContainer(element);
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }

    private void deleteElementContainer(PsiElement element) {
        if (element instanceof OMTNamespacePrefix) {
            if (element.getParent() instanceof OMTPrefix) {
                PsiFile containingFile = element.getContainingFile();
                element.getParent().delete();
                curieUtil.resetPrefixBlock(containingFile);
            }
        }

        if (element instanceof OMTVariable) {
            List<OMTSequenceItem> omtSequenceItems = PsiTreeUtil.collectParents(element, OMTSequenceItem.class, false, item -> item instanceof OMTBlockEntry);
            if (omtSequenceItems.size() == 1) {
                omtSequenceItems.get(0).delete();
            }
        }
    }
}
