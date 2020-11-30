package com.misset.opp.omt.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Indent;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtilCore;
import com.misset.opp.omt.OMTLanguage;
import com.misset.opp.omt.psi.OMTTypes;
import com.misset.opp.omt.psi.support.OMTTokenSets;
import com.misset.opp.omt.psi.util.TokenFinderUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;

import static com.misset.opp.omt.psi.OMTIgnored.END_OF_LINE_COMMENT;
import static com.misset.opp.omt.psi.OMTTypes.*;

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

    private HashMap<ASTNode, Alignment> nodeAlignment = new HashMap<>();


    private final CodeStyleSettings settings;
    private final PsiFile file;
    private final SpacingBuilder spacingBuilder;
    private final Document document;

    public OMTFormattingContext(@NotNull CodeStyleSettings settings, @NotNull PsiFile file) {
        this.settings = settings;
        this.file = file;
        document = file.getVirtualFile() != null ?
                FileDocumentManager.getInstance().getDocument(file.getVirtualFile()) : null;

        CommonCodeStyleSettings common = settings.getCommonSettings(OMTLanguage.INSTANCE);
        spacingBuilder = new SpacingBuilder(settings, OMTLanguage.INSTANCE)
                .around(OMTTokenSets.ASSIGNMENT_OPERATORS).spaceIf(common.SPACE_AROUND_ASSIGNMENT_OPERATORS)
                .before(NAMESPACE_IRI).spaces(4)
                .after(IMPORT_BLOCK).blankLines(1)
                .after(PREFIX_BLOCK).blankLines(1)
        ;
    }

    public Indent computeIndent(@NotNull ASTNode node) {
        IElementType nodeType = PsiUtilCore.getElementType(node);

        Indent indent = Indent.getNoneIndent();
        if (OMTTokenSets.ENTRIES.contains(nodeType) && !isRootElement(node)) {
            indent = Indent.getNormalIndent(true);
        } else if (OMTTypes.MEMBER_LIST_ITEM == nodeType) {
            indent = Indent.getNormalIndent(true); // TODO: Make optional based on a setting
        } else if (SEQUENCE_ITEM == nodeType) {
            indent = Indent.getNormalIndent(true); // TODO: Make optional based on a setting
        } else if (OMTTokenSets.DEFINED_STATEMENTS.contains(nodeType)) { // Query and Command statements
            indent = Indent.getNormalIndent(false);
        } else if (isFirstIndentableQueryStep(node)) {
            indent = Indent.getNormalIndent(false);
        } else if (OMTTokenSets.CHOOSE_INNER.contains(node.getElementType())) {
            indent = Indent.getNormalIndent(true);
        } else if (SCRIPT_LINE == node.getElementType()) {
            indent = Indent.getNormalIndent(false);
        } else if (node.getTreeParent() != null && INTERPOLATED_STRING == node.getTreeParent().getElementType()) {
            indent = Indent.getNormalIndent(false);
        } else if (END_OF_LINE_COMMENT == node.getElementType()) {
            indent = indentEOLComment(node);
        }
//        System.out.println(node.getElementType().toString() + " -->  " + node.getText().substring(0, Math.min(node.getTextLength(), 10)) +
//                " --> " + indent.toString() + " --> " + isFirstIndentableQueryStep(node));
        return indent;

    }

    public Alignment computeAlignment(@NotNull ASTNode node) {
        Alignment alignment = null;
        final IElementType nodeType = node.getElementType();
        if (OMTTokenSets.SAME_LEVEL_ALIGNMENTS.contains(nodeType)) {
            alignment = registerAndReturnIfAnyOf(node, nodeType);
        } else if (OMTTokenSets.CONTAINERS.contains(nodeType)) {
            // align the container to their first child:
            final ASTNode[] children = node.getChildren(OMTTokenSets.SAME_LEVEL_ALIGNMENTS);
            if (children.length == 0) {
                return null;
            }
            alignment = computeAlignment(children[0]);
        } else if (OMTTokenSets.ALL_QUERY_TOKENS.contains(nodeType)) {
            alignment = computeQueryAlignment(node);
        } else if (OMTTokenSets.CHOOSE.contains(nodeType)) {
            alignment = alignChooseBlock(node);
        } else if (node.getTreeParent() != null && INTERPOLATED_STRING == node.getTreeParent().getElementType()) {
            // An interpolated string as a block is aligned in the query, the parts of the string are further aligned
            // if applicable. This will align the first placeholder ${} or string used to be the alignment anchor
            alignment = alignInterpolatedString(node);
        } else if (isJavaDocsPart(node)) {
            // All Javadocs are aligned to the START /** anchor
            alignment = alignJavaDocs(node);
        } else if (END_OF_LINE_COMMENT == nodeType) {
            alignment = alignEOLComment(node);
        } else if (node.getTreeParent() != null && BLOCK == node.getTreeParent().getElementType()) {
            alignment = nodeAlignment.get(node.getTreeParent());
        } else if (NAMESPACE_IRI == nodeType) {
            alignment = alignNamespaceIri(node);
        }
        //        System.out.println(node.getText().substring(0, Math.min(10, node.getTextLength())) + " --> " + (alignment != null ? alignment.toString() : "null"));
        return alignment;
    }

    /**
     * Returns true if this element is part of the query but it's parent is not
     *
     * @param node
     * @return
     */
    private boolean isTopLevelQuery(ASTNode node) {
        return OMTTokenSets.ALL_QUERY_TOKENS.contains(node.getElementType()) &&
                (node.getTreeParent() == null ||
                        (node.getTreeParent() != null && !OMTTokenSets.ALL_QUERY_TOKENS.contains(node.getTreeParent().getElementType())));
    }

    private boolean isOnNextLineNumber(ASTNode node, int comparedToLine) {
        if (node.getTreeParent() == null) {
            return false;
        }
        return document.getLineNumber(node.getStartOffset()) > comparedToLine;
    }

    /**
     * Check if it is the first indentable step in the query:
     * DEFINE QUERY myQuery => .. / .. /
     *     ..  <-- first indentable query step
     * @param node
     * @return
     */
    private boolean isFirstIndentableQueryStep(ASTNode node) {
        return node.getTreeParent() != null &&
                (isTopLevelQuery(node) || isPartOfRootStep(node)) &&
                node == Arrays.stream(node.getTreeParent().getChildren(
                        OMTTokenSets.ALL_QUERY_TOKENS_AND_FILTER))
                        .filter(childNode -> isOnNextLineNumber(childNode, rootLineLevel(node)))
                        .findFirst().orElse(null) &&
                !(node.getTreeParent() != null && isFirstIndentableQueryStep(node.getTreeParent()));
    }

    /**
     * Returns the line level of the query root
     * DEFINE QUERY myQuery => .. / .. / <-- root line
     * ..  / .. / ..
     * DEFINE QUERY myQuery => <-- root line
     * .. / .. / ..
     *
     * @param node
     * @return
     */
    private int rootLineLevel(ASTNode node) {
        while (!isTopLevelQuery(node)) {
            node = node.getTreeParent();
        }
        ASTNode container = node.getTreeParent(); // get query container
        while (OMTTokenSets.SKIPPABLE_CONTAINERS.contains(container.getElementType())) {
            container = container.getTreeParent();
        }

        return document.getLineNumber(container.getStartOffset());
    }

    private boolean isPartOfRootStep(ASTNode node) {
        while (node != null && !OMTTokenSets.ALL_QUERY_TOKENS.contains(node.getElementType())) {
            node = node.getTreeParent();
        }
        return node != null && isTopLevelQuery(node.getTreeParent());
    }

    private boolean isJavaDocsPart(ASTNode node) {
        return OMTTokenSets.JAVADOCS.contains(node.getElementType()) ||
                (node.getTreeParent() != null && OMTTokenSets.JAVADOCS.contains(node.getTreeParent().getElementType()));
    }

    private boolean isFirstOfItsKindInParent(@NotNull ASTNode node) {
        final ASTNode[] children = node.getTreeParent().getChildren(
                TokenSet.create(node.getElementType())
        );
        return children[0] == node;
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

    public SpacingBuilder getSpacingBuilder() {
        return spacingBuilder;
    }

    private ASTNode getTreePrev(ASTNode node, TokenSet tokenSet) {
        node = node.getTreePrev();
        while (node != null && !tokenSet.contains(node.getElementType())) {
            node = node.getTreePrev();
        }
        return node;
    }

    private Alignment computeQueryAlignment(ASTNode node) {
        // Arrays are aligned to first indentable element
        ASTNode previous = getTreePrev(node, OMTTokenSets.ALL_QUERY_TOKENS);
        if (previous != null &&
                OMTTokenSets.ALL_QUERY_TOKENS.contains(previous.getElementType()) &&
                nodeAlignment.containsKey(previous)) {
            return getAlignmentAndRegisterSelf(node, previous); // use the previous sibling alignment
        } else if (node.getTreeParent() != null &&
                OMTTokenSets.ALL_QUERY_TOKENS.contains(node.getTreeParent().getElementType()) &&
                nodeAlignment.containsKey(node.getTreeParent())) {
            return getAlignmentAndRegisterSelf(node, node.getTreeParent()); // use the parent alignment
        } else {
            // create alignment anchor for first
            if (isFirstIndentableQueryStep(node)) {
                return registerAlignmentAndReturn(node);
            } // this is the top level query alignment, self register and return
        }
        return null;
    }

    /**
     * Checks if the provided node is the first instance in the entry or anchor
     * If true, it creates a new alignment that it and all subsequent elements of the same
     * type(s) are anchored to
     *
     * @param node
     * @param types
     * @return
     */
    private Alignment registerAndReturnIfAnyOf(ASTNode node, IElementType... types) {
        if (nodeAlignment.containsKey(node)) {
            return nodeAlignment.get(node); }
        final ASTNode firstOfKindInParent = getFirstOfKindInParent(node.getTreeParent(), types);
        if (firstOfKindInParent == node) {
            return registerAlignmentAndReturn(node);
        } else {
            return nodeAlignment.get(firstOfKindInParent);
        }
    }

    /**
     * Choose/When/Otherwise blocks are indented separately and align in 2-levels
     * .. /
     * CHOOSE
     * WHEN =>
     * OTHERWISE =>
     * END
     *
     * @param node
     * @return
     */
    private Alignment alignChooseBlock(ASTNode node) {
        if (CHOOSE_OPERATOR == node.getElementType()) {
            return registerAlignmentAndReturn(node);
        } else if (END_PATH == node.getElementType()) {
            return nodeAlignment.get(getFirstOfKindInParent(node.getTreeParent(), CHOOSE_OPERATOR));
        } else if (OMTTokenSets.CHOOSE_INNER.contains(node.getElementType())) {
            if (isFirstOfItsKindInParent(node)) {
                return registerAlignmentAndReturn(node);
            } else {
                return nodeAlignment.get(getFirstOfKindInParent(node, WHEN_PATH));
            }
        }
        return null;
    }

    /**
     * All JavaDocs are aligned to the Start element
     * The
     *
     * @param node
     * @return
     */
    private Alignment alignJavaDocs(ASTNode node) {
        if (JAVADOCS_START == node.getElementType()) {
            return registerAlignmentAndReturn(node);
        } else if (JAVADOCS_END == node.getElementType()) {
            // END shares the same parent
            if (node.getTreeParent() == null) {
                return null;
            } // probably not completed yet
            return nodeAlignment.get(getFirstOfKindInParent(node.getTreeParent(), JAVADOCS_START));
        } else {
            // Only other option is JAVADOCS_CONTENT, which is contained by JD_CONTENT which is part of the same parent
            if (node.getTreeParent().getTreeParent() == null) {
                return null;
            }
            return nodeAlignment.get(getFirstOfKindInParent(node.getTreeParent().getTreeParent(), JAVADOCS_START));
        }
    }

    /**
     * @param namespaceIri
     * @return
     */
    private Alignment alignNamespaceIri(ASTNode namespaceIri) {
        if (namespaceIri.getElementType() != NAMESPACE_IRI) {
            return null;
        }
        // get the prefixes:
        ASTNode prefix = namespaceIri.getTreeParent();
        final ASTNode firstPrefix = getFirstOfKindInParent(prefix.getTreeParent(), PREFIX);
        final ASTNode[] children = firstPrefix.getChildren(TokenSet.create(NAMESPACE_IRI));
        final ASTNode firstNamespaceIri = children[0];
        return registerAlignmentAndReturn(firstNamespaceIri);

    }

    /**
     * Alignment of the Interpolated is based on the element types, STRING and INTERPOLATION_TEMPLATE ${}
     * It doens't check the content which is why something like an aligned Json structure with quotes
     * is only aligned if after the initial { } block, another new line is entered since that is considered
     * the first part element.
     *
     * @param node
     * @return
     */
    private Alignment alignInterpolatedString(ASTNode node) {
        if (STRING == node.getElementType() ||
                INTERPOLATION_TEMPLATE == node.getElementType()) {
            // get first element:
            final ASTNode firstOfKindInParent = getFirstOfKindInParent(node.getTreeParent(), STRING, INTERPOLATION_TEMPLATE);
            if (firstOfKindInParent == node) {
                return registerAlignmentAndReturn(node);
            } else {
                return nodeAlignment.get(firstOfKindInParent);
            }
        }
        return null;
    }


    private Alignment alignEOLComment(ASTNode node) {
        if (!TokenFinderUtil.SINGLETON.isStartOfLine(node)) {
            return null;
        } // only align applicable comments
        node = getEOLCommentSibling(node);
        return node != null ? computeAlignment(node) : null;
    }

    private Indent indentEOLComment(ASTNode node) {
        node = getEOLCommentSibling(node);
        return node != null ? computeIndent(node) : null;
    }

    // The end-of-line comments are assumed to describe the element following it (placed on top if them)
    // therefore, they can be anchored to the first alignable element that follows the comment(s)
    private ASTNode getEOLCommentSibling(ASTNode node) {
        // anchor to next sibling:
        node = node.getTreeNext();
        while (node != null &&
                TokenSet.orSet(OMTTokenSets.WHITESPACE, TokenSet.create(END_OF_LINE_COMMENT))
                        .contains(node.getElementType())) {
            node = node.getTreeNext();
        }
        if (node != null && BLOCK == node.getElementType()) {
            return getEOLCommentSibling(node.getFirstChildNode());
        }
        return node;
    }

    private Alignment registerAlignmentAndReturn(ASTNode node) {
        if(nodeAlignment.containsKey(node)) {
            return nodeAlignment.get(node);
        }
        Alignment alignment = Alignment.createAlignment();
        nodeAlignment.put(node, alignment);
        return alignment;
    }

    private Alignment getAlignmentAndRegisterSelf(ASTNode node, ASTNode registeredNode) {
        if(nodeAlignment.containsKey(node)) {
            return nodeAlignment.get(node);
        }
        Alignment alignment = nodeAlignment.get(registeredNode);
        nodeAlignment.put(node, alignment);
        return alignment;
    }

    public Indent newChildIndent(ASTNode node) {
        return node instanceof FileElement
                ? Indent.getSpaceIndent(0, true)
                : Indent.getNormalIndent(true);
    }
}

