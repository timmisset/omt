package com.misset.opp.omt.intentions.generic;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTImportBlock;
import com.misset.opp.omt.psi.OMTMemberListItem;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.OMTPrefixBlock;
import com.misset.opp.omt.psi.OMTSequenceItem;
import com.misset.opp.omt.psi.OMTVariable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RemoveIntention {
    public IntentionAction getRemoveIntention(PsiElement element) {
        return getRemoveIntention(element, "Remove");
    }

    public IntentionAction getRemoveIntention(PsiElement element, String text) {
        return new IntentionAction() {
            @NotNull
            @Override
            public String getText() {
                return text;
            }

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
            deletePrefix((OMTNamespacePrefix) element);
        }
        if (element instanceof OMTVariable) {
            deleteVariable((OMTVariable) element);
        }
        if (element instanceof OMTMemberListItem) {
            deleteImportMember((OMTMemberListItem) element);
        }
    }

    private void deletePrefix(OMTNamespacePrefix namespacePrefix) {
        OMTPrefixBlock prefixBlock = PsiTreeUtil.getParentOfType(namespacePrefix, OMTPrefixBlock.class);
        if (namespacePrefix.getParent() instanceof OMTPrefix) {
            namespacePrefix.getParent().delete();
        }
        if (prefixBlock != null) {
            CodeStyleManager.getInstance(namespacePrefix.getProject()).reformat(prefixBlock);
        }
    }

    private void deleteVariable(OMTVariable variable) {
        List<OMTSequenceItem> omtSequenceItems = PsiTreeUtil.collectParents(variable, OMTSequenceItem.class, false, item -> item instanceof OMTBlockEntry);
        if (omtSequenceItems.size() == 1) {
            omtSequenceItems.get(0).delete();
        }
    }

    private void deleteImportMember(OMTMemberListItem listItem) {
        OMTImportBlock importBlock = PsiTreeUtil.getParentOfType(listItem, OMTImportBlock.class);
        listItem.delete();
        if (importBlock != null) {
            CodeStyleManager.getInstance(listItem.getProject()).reformat(importBlock);
        }
    }
}
