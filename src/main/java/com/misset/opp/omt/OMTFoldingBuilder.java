package com.misset.opp.omt;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.misset.opp.omt.psi.OMTTypes.MODEL_ITEM_BLOCK;

public class OMTFoldingBuilder extends FoldingBuilderEx implements DumbAware {
    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();

        // all block entries, generic entries, model items etc
        collectDescriptors(descriptors, root, OMTBlockEntry.class);

        // script parts:
        collectDescriptors(descriptors, root, OMTLogicalBlock.class);
        collectDescriptors(descriptors, root, OMTIfBlock.class);
        collectDescriptors(descriptors, root, OMTElseBlock.class);
        collectDescriptors(descriptors, root, OMTCommandBlock.class);

        // queries:
        collectDescriptors(descriptors, root, OMTDefinedStatement.class);

        return descriptors.toArray(FoldingDescriptor[]::new);
    }

    private void collectDescriptors(List<FoldingDescriptor> descriptors,
                                    PsiElement root,
                                    Class<? extends PsiElement> clazz) {
        PsiTreeUtil.findChildrenOfType(root, clazz).forEach(
                psiElement -> descriptors.add(new FoldingDescriptor(
                        psiElement.getNode(),
                        new TextRange(psiElement.getTextRange().getStartOffset(),
                                psiElement.getTextRange().getStartOffset() + psiElement.getText().trim().length()),
                        FoldingGroup.newGroup(descriptors.size() + "_foldingGroup")
                ))
        );
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }

    @Override
    public @Nullable String getPlaceholderText(@NotNull ASTNode node) {
        if (node.getElementType() == MODEL_ITEM_BLOCK) {
            return ((OMTModelItemBlock) node.getPsi()).getModelItemLabel().getText();
        } else if (node.getPsi() instanceof OMTBlockEntry) {
            return ((OMTBlockEntry) node.getPsi()).getLabel().getText();
        } else if (node.getPsi() instanceof OMTDefinedStatement) {
            return ((OMTDefinedStatement) node.getPsi()).getDefineName().getName();
        }
        return null;
    }
}
