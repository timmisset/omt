package com.misset.opp.omt.formatter;

import com.intellij.application.options.CodeStyle;
import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.FormattingContext;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtilCore;
import com.misset.opp.omt.OMTLanguage;
import com.misset.opp.omt.psi.OMTTypes;
import com.misset.opp.omt.settings.OMTCodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import static com.misset.opp.omt.psi.OMTIgnored.END_OF_LINE_COMMENT;
import static com.misset.opp.omt.psi.OMTTypes.BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.CHOOSE_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.COMMA;
import static com.misset.opp.omt.psi.OMTTypes.COMMAND_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.CURIE_CONSTANT_ELEMENT;
import static com.misset.opp.omt.psi.OMTTypes.CURIE_ELEMENT;
import static com.misset.opp.omt.psi.OMTTypes.CURLY_CLOSED;
import static com.misset.opp.omt.psi.OMTTypes.CURLY_OPEN;
import static com.misset.opp.omt.psi.OMTTypes.DEFINE_COMMAND_STATEMENT;
import static com.misset.opp.omt.psi.OMTTypes.DEFINE_NAME;
import static com.misset.opp.omt.psi.OMTTypes.DEFINE_QUERY_STATEMENT;
import static com.misset.opp.omt.psi.OMTTypes.ELSE_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.ELSE_OPERATOR;
import static com.misset.opp.omt.psi.OMTTypes.END_PATH;
import static com.misset.opp.omt.psi.OMTTypes.FORWARD_SLASH;
import static com.misset.opp.omt.psi.OMTTypes.IF_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.IMPORT;
import static com.misset.opp.omt.psi.OMTTypes.IMPORT_SOURCE;
import static com.misset.opp.omt.psi.OMTTypes.INTERPOLATED_STRING;
import static com.misset.opp.omt.psi.OMTTypes.JD_COMMENT;
import static com.misset.opp.omt.psi.OMTTypes.LAMBDA;
import static com.misset.opp.omt.psi.OMTTypes.MEMBER_LIST;
import static com.misset.opp.omt.psi.OMTTypes.MEMBER_LIST_ITEM;
import static com.misset.opp.omt.psi.OMTTypes.MODEL_ITEM_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.NAMESPACE_IRI;
import static com.misset.opp.omt.psi.OMTTypes.NAMESPACE_PREFIX;
import static com.misset.opp.omt.psi.OMTTypes.OTHERWISE_PATH;
import static com.misset.opp.omt.psi.OMTTypes.PARENTHESES_CLOSE;
import static com.misset.opp.omt.psi.OMTTypes.PARENTHESES_OPEN;
import static com.misset.opp.omt.psi.OMTTypes.PREFIX;
import static com.misset.opp.omt.psi.OMTTypes.PREFIX_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.QUERY_PATH;
import static com.misset.opp.omt.psi.OMTTypes.ROOT_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.SCALAR_VALUE;
import static com.misset.opp.omt.psi.OMTTypes.SCRIPT;
import static com.misset.opp.omt.psi.OMTTypes.SCRIPT_LINE;
import static com.misset.opp.omt.psi.OMTTypes.SEQUENCE;
import static com.misset.opp.omt.psi.OMTTypes.SEQUENCE_BULLET;
import static com.misset.opp.omt.psi.OMTTypes.SEQUENCE_ITEM;
import static com.misset.opp.omt.psi.OMTTypes.SIGNATURE;
import static com.misset.opp.omt.psi.OMTTypes.SIGNATURE_ARGUMENT;
import static com.misset.opp.omt.psi.OMTTypes.WHEN_PATH;
import static com.misset.opp.util.UtilManager.getModelUtil;

/**
 * The formatter has 2 main functions, indentation and alignment
 * The indentation is used to indent a block (relative to its parent).
 * The element that is indented should generate an alignment token that can be used by subsequent items as an anchor
 * Even when these items have indentations, the alignment will overrule it and make sure identical
 * alignment tokens are levelled.
 * <p>
 * There is some complexity in this code due to the levelling of query statements which are not
 * simply processed as YAML scalar values but have certain additional indentation and alignment
 * based on their query(step) types.
 */
public class OMTFormattingContext {
    private static final TokenSet INDENT_NOT_RELATIVE_TO_PARENT = TokenSet.create(SCRIPT, COMMAND_BLOCK, SIGNATURE);

    private static final Indent JD_COMMENT_INDENT = Indent.getSpaceIndent(1);

    // used for look-ahead alignment (mostly for comments) where they should anchor to adjacent elements
    private final HashMap<ASTNode, Alignment> nodeAlignment = new HashMap<>();
    private final SpacingBuilder spacingBuilder;
    private final CommonCodeStyleSettings common;
    private final OMTCodeStyleSettings customCodeStyleSettings;

    public OMTFormattingContext(FormattingContext formattingContext) {
        final PsiFile file = formattingContext.getContainingFile();
        final VirtualFile virtualFile = file.getVirtualFile();
        final CodeStyleSettings settings = formattingContext.getCodeStyleSettings();

        common = settings.getCommonSettings(OMTLanguage.INSTANCE);
        customCodeStyleSettings = settings.getCustomSettings(OMTCodeStyleSettings.class);
        spacingBuilder = getDefaultSpacingBuilder(settings);
    }

    private SpacingBuilder getDefaultSpacingBuilder(@NotNull CodeStyleSettings settings) {
        // the KEEP_BLANK_LINES_IN_DECLARATIONS is used to set a minimum number of blank lines
        // this would negate the blank lines provided into the spacingbuilder when < KEEP_BLANK_LINES_IN_DECLARATIONS;
        // For now, don't make the blank lines a setting but implement simple blank line settings
        settings.getCommonSettings(OMTLanguage.INSTANCE).KEEP_BLANK_LINES_IN_DECLARATIONS = 0;
        // When the SpacingBuilder returns a Spacing with the blankLines set it will be interpreted as:
        // minLineFeed == blankLines + 1
        // minBlankLines == blankLines

        return new SpacingBuilder(settings, OMTLanguage.INSTANCE)
                // assignment operators (=, ==, +=, -=)
                .around(OMTTokenSets.ASSIGNMENT_OPERATORS)
                .spaceIf(common.SPACE_AROUND_ASSIGNMENT_OPERATORS)
                // after sequence bullet
                .afterInside(SEQUENCE_BULLET, TokenSet.create(SEQUENCE_ITEM, MEMBER_LIST_ITEM))
                .spaces(customCodeStyleSettings.INDENT_AFTER_SEQUENCE_VALUE ? getIndentSize() - 1 : 1)
                // inside curieConstantElement
                .betweenInside(FORWARD_SLASH, CURIE_ELEMENT, CURIE_CONSTANT_ELEMENT).spaces(0)
                // between rootblocks
                .betweenInside(OMTTokenSets.ROOTBLOCK_ENTRIES, OMTTokenSets.ROOTBLOCK_ENTRIES, ROOT_BLOCK).blankLines(1)
                .betweenInside(OMTTokenSets.ROOTBLOCK_ENTRIES, TokenSet.create(END_OF_LINE_COMMENT), ROOT_BLOCK).blankLines(1)
                .between(MODEL_ITEM_BLOCK, MODEL_ITEM_BLOCK).blankLines(1)
                // the content of the import block can be added by intentions, make sure there are not empty lines:
                .around(IMPORT).blankLines(0)
                .around(IMPORT_SOURCE).blankLines(0)
                .aroundInside(MEMBER_LIST_ITEM, MEMBER_LIST).blankLines(0)
                .aroundInside(PREFIX, PREFIX_BLOCK).blankLines(0)
                .around(DEFINE_COMMAND_STATEMENT).blankLines(0)
                .around(DEFINE_QUERY_STATEMENT).blankLines(0)
                // sequence items
                .around(SEQUENCE_ITEM).blankLines(0)
                .after(SEQUENCE).blankLines(1)
                .before(SEQUENCE).blankLines(0)
                // spacing in a call or defined structure
                .between(COMMA, SIGNATURE_ARGUMENT).spaces(1)
                .between(SIGNATURE_ARGUMENT, COMMA).spaces(0)
                .between(PARENTHESES_OPEN, SIGNATURE_ARGUMENT).spaces(0)
                .between(SIGNATURE_ARGUMENT, PARENTHESES_CLOSE).spaces(0)
                .between(SIGNATURE, LAMBDA).spaces(1)
                .between(DEFINE_NAME, LAMBDA).spaces(1)

                // within scripts
                // this makes sure there are no blank lines but there is always a line-feed
                .between(CURLY_OPEN, SCRIPT).blankLines(0)
                .between(SCRIPT, CURLY_CLOSED).blankLines(0)

                // within queries
                // configure the where block to position the CHOOSE, WHEN, OTHERWISE and END all at their own lines
                .before(TokenSet.create(CHOOSE_BLOCK, WHEN_PATH, OTHERWISE_PATH, END_PATH)).blankLines(0)
                .aroundInside(FORWARD_SLASH, QUERY_PATH).spaces(1)
                ;
    }

    public Spacing computeSpacing(@NotNull Block parent, @Nullable Block child1, @NotNull Block child2) {
        // spacing default from the SpacingBuilder
        final Spacing spacing = spacingBuilder.getSpacing(parent, child1, child2);
        if (spacing != null) {
            return spacing;
        }
        // custom spacing
        // whenever the default spacing builder is not sufficient
        if (isNodeType(parent, PREFIX) && isNodeType(child1, NAMESPACE_PREFIX) && isNodeType(child2, NAMESPACE_IRI)) {
            // spacing between the prefix and iri depends on the size of the largest prefix:
            final int maxLength = getMaxPrefixLength(((OMTFormattingBlock) parent).getNode());
            final int spaces = maxLength + getIndentSize() - child1.getTextRange().getLength();
            return Spacing.createSpacing(spaces, spaces, 0, false, 0);
        } else if (isNodeType(child1, SCRIPT_LINE) && isNodeType(child2, SCRIPT_LINE)) {
            // do not set the minimum number of blank lines but only the amount to keep:
            return Spacing.createSpacing(0, 0, 0, true, 1);
        } else if (isNodeType(child1, ELSE_OPERATOR) || isNodeType(child2, ELSE_BLOCK) ||
                (isNodeType(child1, IF_BLOCK) && isNodeType(child2, COMMAND_BLOCK))) {
            // actively remove the linefeed around ELSE operators:
            return Spacing.createSpacing(1, 1, 0, false, 0);
        }
        return null;
    }

    private int getIndentSize() {
        return common.getIndentOptions() != null ? common.getIndentOptions().INDENT_SIZE : 4;
    }

    private int getMaxPrefixLength(ASTNode prefix) {
        final ASTNode prefixBlock = prefix.getTreeParent();
        final Optional<Integer> max = Arrays.stream(prefixBlock.getChildren(TokenSet.create(PREFIX)))
                .map(ASTNode::getFirstChildNode)
                .map(ASTNode::getTextLength)
                .max(Integer::compareTo);
        return max.orElse(0);
    }

    private boolean isNodeType(Block block, IElementType elementType) {
        if (block == null) {
            return false;
        }
        return ((OMTFormattingBlock) block).getNode().getElementType() == elementType;
    }


    public Indent computeIndent(@NotNull ASTNode node) {
        /*
         * Indentation is computed by analyzing the element at the start of a new line,
         * All containers at this position will be checked. This means that when a new line starts
         * with a QueryPath both the QueryStep and QueryPath will be checked. When both return an indent
         * they will be accumulated
         * To prevent this, use the startsInParent check to make sure only the toplevel element at the new
         * line is indented.
         *
         * There seems to be no way around this, not indenting steps and only paths results in significant issues also.
         */
        IElementType nodeType = PsiUtilCore.getElementType(node);

        Indent indent = Indent.getNoneIndent();
        if (OMTTokenSets.ENTRIES.contains(nodeType) && !isRootElement(node)) {
            indent = Indent.getNormalIndent(true);
        } else if (customCodeStyleSettings.INDENT_SEQUENCE_VALUE && OMTTypes.MEMBER_LIST_ITEM == nodeType) {
            indent = Indent.getNormalIndent(true);
        } else if (customCodeStyleSettings.INDENT_SEQUENCE_VALUE && SEQUENCE_ITEM == nodeType) {
            indent = Indent.getNormalIndent(true);
        } else if (OMTTokenSets.DEFINED_STATEMENTS.contains(nodeType)) {
            // Query and Command statements are not nested in a scalar block but are directly nested
            // the first instance will be indented and the alignment method will make sure all subsequence
            // items will be aligned to the initial statement
            indent = Indent.getNormalIndent(true);
        } else if (OMTTokenSets.COMMENTS.contains(nodeType)) {
            indent = indentComment(node);
        } else if (OMTTokenSets.INDENTED_QUERY_STEPS.contains(nodeType)) {
            indent = !startsInParent(node) ? Indent.getNormalIndent(false) : indent;
        } else if (OMTTokenSets.CHOOSE_INNER.contains(nodeType)) {
            indent = Indent.getNormalIndent(true);
        } else if (nodeType == SCALAR_VALUE) {
            indent = Indent.getNormalIndent(false);
        } else if (OMTTokenSets.INDENTED_SCRIPT_PARTS.contains(nodeType)) {
            indent = !startsInParent(node) ? Indent.getNormalIndent(false) : indent;
        } else if (OMTTokenSets.INDENTED_JAVA_DOCS_PARTS.contains(nodeType)) {
            // indentation on new lines in a JavaDocs comment should be 1 to align with the start token /**
            indent = JD_COMMENT_INDENT;
        }
        return indent;
    }

    private boolean startsInParent(ASTNode node) {
        return node.getTreeParent() != null && node.getTreeParent().getText().startsWith(node.getText());
    }

    public Alignment computeAlignment(@NotNull ASTNode node) {
        Alignment alignment = null;
        final IElementType nodeType = node.getElementType();
        if (OMTTokenSets.SAME_LEVEL_ALIGNMENTS.contains(nodeType)) {
            alignment = registerAndReturnIfAnyOf(node, OMTTokenSets.SAME_LEVEL_ALIGNMENTS.getTypes());
        } else if (OMTTokenSets.COMMENTS.contains(nodeType)) {
            alignment = alignComment(node);
        } else if (OMTTokenSets.INTERPOLATED_STRING_COMPONENTS.contains(nodeType) &&
                node.getTreeParent().getElementType() == INTERPOLATED_STRING) {
            alignment = registerAndReturnIfAnyOf(node, OMTTokenSets.INTERPOLATED_STRING_COMPONENTS.getTypes());
        }
        return alignment;
    }

    private ASTNode getFirstOfKindInParent(@NotNull ASTNode parent, IElementType... type) {
        final ASTNode[] children = parent.getChildren(
                TokenSet.create(type)
        );
        return children.length > 0 ? children[0] : null;
    }

    private boolean isRootElement(@NotNull ASTNode node) {
        IElementType nodeType = PsiUtilCore.getElementType(node);
        IElementType parentType = PsiUtilCore.getElementType(node.getTreeParent());
        IElementType grandParentType = parentType == null ? null : PsiUtilCore.getElementType(node.getTreeParent().getTreeParent());

        // the file starts with FILE | BLOCK | BLOCKENTRY
        return nodeType instanceof IFileElementType ||
                parentType instanceof IFileElementType ||
                grandParentType instanceof IFileElementType;
    }

    /**
     * Checks if the provided node is the first instance in the entry or anchor
     * If true, it creates a new alignment that it and all subsequent elements of the same
     * type(s) are anchored to
     */
    private Alignment registerAndReturnIfAnyOf(ASTNode node, IElementType... types) {
        if (nodeAlignment.containsKey(node)) {
            return nodeAlignment.get(node);
        }
        final ASTNode firstOfKindInParent = getFirstOfKindInParent(node.getTreeParent(), types);
        if (firstOfKindInParent == node) {
            return registerAlignmentAndReturn(node);
        } else {
            return nodeAlignment.get(firstOfKindInParent);
        }
    }

    private Alignment alignComment(ASTNode node) {
        node = getCommentAnchor(node);
        return node != null ? computeAlignment(node) : null;
    }

    private Indent indentComment(ASTNode node) {
        node = getCommentAnchor(node);
        return node != null ? computeIndent(node) : null;
    }

    // The comments are assumed to describe the adjacent element (placed on top if them)
    // therefore, they can be anchored to the first alignable element that follows the comment(s)
    private ASTNode getCommentAnchor(ASTNode node) {
        // anchor to next sibling:
        node = node.getTreeNext();
        while (node != null &&
                TokenSet.orSet(OMTTokenSets.WHITESPACE, OMTTokenSets.COMMENTS)
                        .contains(node.getElementType())) {
            node = node.getTreeNext();
        }
        if (node != null && BLOCK == node.getElementType()) {
            return getCommentAnchor(node.getFirstChildNode());
        }
        return node;
    }

    private Alignment registerAlignmentAndReturn(ASTNode node) {
        if (nodeAlignment.containsKey(node)) {
            return nodeAlignment.get(node);
        }
        Alignment alignment = Alignment.createAlignment();
        nodeAlignment.put(node, alignment);
        return alignment;
    }

    public Indent newChildIndent(ASTNode node) {
        if (isFileNode(node)) {
            return Indent.getNoneIndent();
        } else if (node.getElementType() == JD_COMMENT) {
            // new children in JD_COMMENT should be indented with a 1 size-space indent:
            return JD_COMMENT_INDENT;
        } else if (getModelUtil().isSequenceNode(node) || getModelUtil().isImportNode(node)) {
            final OMTCodeStyleSettings codeStyleSettings = CodeStyle.getCustomSettings(node.getPsi().getContainingFile(), OMTCodeStyleSettings.class);
            if (!codeStyleSettings.INDENT_SEQUENCE_VALUE) {
                return Indent.getNoneIndent();
            } else {
                return Indent.getNormalIndent(true);
            }
        } else {
            return Indent.getNormalIndent(
                    !INDENT_NOT_RELATIVE_TO_PARENT.contains(node.getElementType())
            );
        }
    }

    private boolean isFileNode(ASTNode node) {
        return node.getTreeParent() == null;
    }
}

