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
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtilCore;
import com.misset.opp.omt.OMTLanguage;
import com.misset.opp.omt.psi.support.OMTTokenSets;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;

import static com.misset.opp.omt.psi.OMTTypes.*;


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
                .around(OMTTokenSets.ASSIGNMENT_OPERATORS)
                .spaceIf(common.SPACE_AROUND_ASSIGNMENT_OPERATORS)
        ;
    }

    public Indent computeBlockIndent(@NotNull ASTNode node) {
        IElementType nodeType = PsiUtilCore.getElementType(node);

        Indent indent = Indent.getNoneIndent();
        if (!isRootElement(node)) {
            if (OMTTokenSets.ENTRIES.contains(nodeType)) {
                indent = Indent.getNormalIndent(true);
            } else if (isFirstIndentableQueryStep(node) || OMTTokenSets.SEQUENCES.contains(nodeType)) {
                indent = Indent.getNormalIndent(false);
            } else if (OMTTokenSets.CHOOSE_INNER.contains(node.getElementType())) {
                indent = Indent.getNormalIndent(true);
            } else if (SCRIPT_LINE == node.getElementType()) {
                indent = Indent.getNormalIndent(false);
            } else if (node.getTreeParent() != null && INTERPOLATED_STRING == node.getTreeParent().getElementType()) {
                indent = Indent.getNormalIndent(false);
            } else if (SCALAR == node.getElementType()) {
                indent = Indent.getNormalIndent(true);
            }
        }

//        System.out.println(node.getElementType().toString() + " -->  " + node.getText().substring(0, Math.min(node.getTextLength(), 10)) +
//                " --> " + indent.toString() + " --> " + isFirstIndentableQueryStep(node));
        return indent;

    }

    /**
     * Returns true if this element is part of the query but it's parent is nots
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
        // >= to include blank lines
        return document.getLineNumber(node.getStartOffset()) >= comparedToLine + 1;
    }

    /**
     * Returns true if this node's parent is the query root and itself is the first part of the query
     * which is contained on a new line
     * Only this part should be indented and used as anchor for the query alignment
     *
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

    public Alignment computeAlignment(@NotNull ASTNode node) {
        Alignment alignment = null;
        if (TokenSet.orSet(OMTTokenSets.ENTRIES, OMTTokenSets.SEQUENCES)
                .contains(node.getElementType())) {
            // All entry types and sequences are anchored to the first instance
            alignment = registerAndReturnIfAnyOf(node, node.getElementType());
        } else if (OMTTokenSets.ALL_QUERY_TOKENS.contains(node.getElementType())) {
            // Arrays are aligned to first indentable element
            ASTNode previous = getTreePrev(node, OMTTokenSets.ALL_QUERY_TOKENS);
            if (previous != null &&
                    OMTTokenSets.ALL_QUERY_TOKENS.contains(previous.getElementType()) &&
                    nodeAlignment.containsKey(previous)) {
                alignment = getAlignmentAndRegisterSelf(node, previous); // use the previous sibling alignment
            } else if (node.getTreeParent() != null &&
                    OMTTokenSets.ALL_QUERY_TOKENS.contains(node.getTreeParent().getElementType()) &&
                    nodeAlignment.containsKey(node.getTreeParent())) {
                alignment = getAlignmentAndRegisterSelf(node, node.getTreeParent()); // use the parent alignment
            } else {
                // create alignment anchor for first
                if (isFirstIndentableQueryStep(node)) {
                    alignment = registerAlignmentAndReturn(node);
                } // this is the top level query alignment, self register and return
            }
        } else if (OMTTokenSets.CHOOSE.contains(node.getElementType())) {
            alignment = alignChooseBlock(node);
        } else if (node.getTreeParent() != null && INTERPOLATED_STRING == node.getTreeParent().getElementType()) {
            // An interpolated string as a block is aligned in the query, the parts of the string are further aligned
            // if applicable. This will align the first placeholder ${} or string used to be the alignment anchor
            alignment = alignInterpolatedString(node);
        } else if (isJavaDocsPart(node)) {
            // All Javadocs are aligned to the START /** anchor
            alignment = alignJavaDocs(node);
        }
        //        System.out.println(node.getText().substring(0, Math.min(10, node.getTextLength())) + " --> " + (alignment != null ? alignment.toString() : "null"));
        return alignment;
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
            return nodeAlignment.get(getFirstOfKindInParent(node.getTreeParent(), JAVADOCS_START));
        } else {
            // Only other option is JAVADOCS_CONTENT, which is contained by JD_CONTENT which is part of the same parent
            return nodeAlignment.get(getFirstOfKindInParent(node.getTreeParent().getTreeParent(), JAVADOCS_START));
        }
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

    private Alignment registerAlignmentAndReturn(ASTNode node) {
        Alignment alignment = Alignment.createAlignment();
        nodeAlignment.put(node, alignment);
        return alignment;
    }

    private Alignment getAlignmentAndRegisterSelf(ASTNode node, ASTNode registeredNode) {
        Alignment alignment = nodeAlignment.get(registeredNode);
        nodeAlignment.put(node, alignment);
        return alignment;
    }
}

