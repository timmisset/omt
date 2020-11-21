package com.misset.opp.omt.formatter;

import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.misset.opp.omt.psi.support.OMTTokenSets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OMTFormattingBlock extends AbstractBlock {
    private final OMTFormattingContext formattingContext;

    private final Indent indent;

    protected OMTFormattingBlock(@NotNull ASTNode node, @NotNull OMTFormattingContext omtFormattingContext) {
        super(node, null, omtFormattingContext.computeAlignment(node));
        this.formattingContext = omtFormattingContext;

        this.indent = formattingContext.computeBlockIndent(node, this);
    }

    @Override
    protected List<Block> buildChildren() {
        return buildChildren(formattingContext.getSpacingBuilder(), myNode);
    }

    private List<Block> buildChildren(@NotNull SpacingBuilder spacingBuilder, @NotNull ASTNode node) {
        List<Block> blocks = new ArrayList<>();
        ASTNode child = node.getFirstChildNode();
        while (child != null) {
            if (!OMTTokenSets.WHITESPACE.contains(child.getElementType())) {
//                if(OMTTokenSets.CONTAINERS.contains(child.getElementType())) {
//                    blocks.addAll(buildChildren(spacingBuilder, child));
//                } else if (OMTTokenSets.BLOCKS.contains(child.getElementType())) {
//                    blocks.add(new OMTFormattingBlock(child, formattingContext));
//                }
                blocks.add(new OMTFormattingBlock(child, formattingContext));
            }
            child = child.getTreeNext();
        }
        return blocks;
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        return formattingContext.getSpacingBuilder().getSpacing(this, child1, child2);
    }

    @Override
    public Indent getIndent() {
        return null;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}
