package com.misset.opp.omt.formatter;

import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.lang.ASTNode;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.misset.opp.omt.psi.OMTSpecificBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OMTFormattingBlock extends AbstractBlock {
    private final OMTFormattingContext formattingContext;

    private final Indent indent;

    @Nullable
    private final Indent myNewChildIndent;

    protected OMTFormattingBlock(@NotNull ASTNode node, @NotNull OMTFormattingContext omtFormattingContext) {
        super(node, null, omtFormattingContext.computeAlignment(node));
        this.formattingContext = omtFormattingContext;
        this.indent = formattingContext.computeIndent(node);
        this.myNewChildIndent = omtFormattingContext.newChildIndent(node);
    }

    @Override
    protected List<Block> buildChildren() {
        return buildChildren(myNode);
    }

    private List<Block> buildChildren(@NotNull ASTNode node) {
        List<Block> blocks = new ArrayList<>();
        ASTNode child = node.getFirstChildNode();
        while (child != null) {
            if (!OMTTokenSets.WHITESPACE.contains(child.getElementType())) {
                blocks.add(new OMTFormattingBlock(child, formattingContext));
            }
            child = child.getTreeNext();
        }
        return blocks;
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        return formattingContext.computeSpacing(this, child1, child2);
    }

    @Override
    public Indent getIndent() {
        return indent;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public boolean isIncomplete() {
        if (OMTTokenSets.INCOMPLETE.contains(getNode().getElementType()) ||
                getNode().getPsi() instanceof OMTSpecificBlock) {
            return true;
        }
        return super.isIncomplete();
    }

    @Nullable
    @Override
    protected Indent getChildIndent() {
        return myNewChildIndent;
    }
}
