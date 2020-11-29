package com.misset.opp.omt.formatter;

import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.misset.opp.omt.psi.support.OMTTokenSets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OMTFormattingBlock extends AbstractBlock {
    private final OMTFormattingContext formattingContext;

    private final Indent indent;

    @NotNull
    private final TextRange textRange;

    @Nullable
    private final Indent myNewChildIndent;

    protected OMTFormattingBlock(@NotNull ASTNode node, @NotNull OMTFormattingContext omtFormattingContext) {
        super(node, null, omtFormattingContext.computeAlignment(node));
        this.formattingContext = omtFormattingContext;
        this.indent = formattingContext.computeIndent(node);
        this.textRange = excludeTrailingEOLs(node);
        this.myNewChildIndent = omtFormattingContext.newChildIndent(node);
    }

    private static TextRange excludeTrailingEOLs(@NotNull ASTNode node) {
        CharSequence text = node.getChars();
        int last = text.length() - 1;
        if (last == -1 || text.charAt(last) != '\n') {
            return node.getTextRange();
        }
        for (int i = last; i >= 0; i--) {
            if (text.charAt(i) != '\n') {
                int start = node.getTextRange().getStartOffset();
                return new TextRange(start, start + i + 1);
            }
        }
        // It seems this node is a file and this file consists of only empty lines
        return node.getTextRange();
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
        return indent;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @NotNull
    @Override
    public TextRange getTextRange() {
        return textRange;
    }

    @Nullable
    @Override
    protected Indent getChildIndent() {
        return myNewChildIndent;
    }
}
