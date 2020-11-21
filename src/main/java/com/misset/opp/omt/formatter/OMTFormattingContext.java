package com.misset.opp.omt.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Indent;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.lang.ASTNode;
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

import java.util.HashMap;


public class OMTFormattingContext {

    private final static Indent DIRECT_NORMAL_INDENT = Indent.getNormalIndent(true);
    private final static Indent SAME_AS_PARENT_INDENT = Indent.getSpaceIndent(0, true);
    private final static Indent SAME_AS_INDENTED_ANCESTOR_INDENT = Indent.getSpaceIndent(0);

    private HashMap<ASTNode, Alignment> nodeAlignment = new HashMap<>();

    private CodeStyleSettings settings;
    private PsiFile file;
    private SpacingBuilder spacingBuilder;

    public OMTFormattingContext(@NotNull CodeStyleSettings settings, @NotNull PsiFile file) {
        this.settings = settings;
        this.file = file;

        CommonCodeStyleSettings common = settings.getCommonSettings(OMTLanguage.INSTANCE);
        spacingBuilder = new SpacingBuilder(settings, OMTLanguage.INSTANCE)
                .around(OMTTokenSets.ASSIGNMENT_OPERATORS)
                .spaceIf(common.SPACE_AROUND_ASSIGNMENT_OPERATORS)
        ;
    }

    private static Indent computeKeyValuePairIndent(@NotNull ASTNode node) {
//        IElementType parentType = PsiUtilCore.getElementType(node.getTreeParent());
//        IElementType grandParentType = parentType == null ? null : PsiUtilCore.getElementType(node.getTreeParent().getTreeParent());

        return DIRECT_NORMAL_INDENT;
//        if (parentType == YAMLElementTypes.HASH) {
//            // {
//            //   key: value
//            // }
//            return Indent.getNormalIndent();
//        } else if (parentType == YAMLElementTypes.SEQUENCE_ITEM) {
//            // [
//            //   a: x,
//            //   b: y
//            // ]
//            return Indent.getNormalIndent();
//        } else {
//            // - - a: x
//            //     b: y
//            return DIRECT_NORMAL_INDENT;
//        }
    }

    public Indent computeBlockIndent(@NotNull ASTNode node, OMTFormattingBlock omtFormattingBlock) {
        IElementType nodeType = PsiUtilCore.getElementType(node);
        IElementType parentType = PsiUtilCore.getElementType(node.getTreeParent());
        IElementType grandParentType = parentType == null ? null : PsiUtilCore.getElementType(node.getTreeParent().getTreeParent());

//        if(isRootElement(node)) {
//            return Indent.getNoneIndent();
//        } else if (OMTTokenSets.BLOCKS.contains(nodeType)) {
//            if(OMTTokenSets.SPECIFIC_BLOCKS.contains(nodeType) && isRootElement(node.getTreeParent())) {
//                return Indent.getNoneIndent(); // specific blocks inside the root are still indented at root
//            } else {
//                return TokenSet.create(MODEL_ITEM_BLOCK).contains(nodeType) ? computeModelItemBlock(node) : DIRECT_NORMAL_INDENT;
//            }
//        } else if (OMTTokenSets.SEQUENCES.contains(nodeType)) {
//            return DIRECT_NORMAL_INDENT;
//        }
//        return DIRECT_NORMAL_INDENT;

        if (OMTTokenSets.ENTRIES.contains(nodeType)) {
            return Indent.getNormalIndent(true);
        } else {
            return DIRECT_NORMAL_INDENT;
        }

    }

    private Indent computeModelItemBlock(@NotNull ASTNode node) {
        return isFirstOfItsKindInParent(node) ? DIRECT_NORMAL_INDENT : Indent.getNormalIndent(true);
    }

    private boolean isFirstOfItsKindInParent(@NotNull ASTNode node) {
        final ASTNode[] children = node.getTreeParent().getChildren(
                TokenSet.create(node.getElementType())
        );
        return children[0] == node;
    }

    private ASTNode getFirstOfItsKindInParent(@NotNull ASTNode node) {
        final ASTNode[] children = node.getTreeParent().getChildren(
                TokenSet.create(node.getElementType())
        );
        return children[0];
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

    public Alignment computeAlignment(@NotNull ASTNode node) {
        if (OMTTokenSets.ENTRIES.contains(node.getElementType())) {
            if (isFirstOfItsKindInParent(node)) {
                Alignment alignment = Alignment.createAlignment();
                nodeAlignment.put(node, alignment);
                return alignment;
            } else {
                return nodeAlignment.get(getFirstOfItsKindInParent(node));
            }
        }
        return null;
    }
}
//        assert nodeType != YAMLElementTypes.SEQUENCE : "Sequence should be inlined!";
//        assert nodeType != YAMLElementTypes.MAPPING  : "Mapping should be inlined!";
//        assert nodeType != YAMLElementTypes.DOCUMENT : "Document should be inlined!";

//        if (YAMLElementTypes.DOCUMENT_BRACKETS.contains(nodeType)) {
//            return SAME_AS_PARENT_INDENT;
//        }
//        else if (YAMLElementTypes.BRACKETS.contains(nodeType)) {
//            return SAME_AS_INDENTED_ANCESTOR_INDENT;
//        }
//        else if (nodeType == YAMLTokenTypes.TEXT) {
//            return grandParentIsDocument ? SAME_AS_PARENT_INDENT : DIRECT_NORMAL_INDENT;
//        }
//        else if (nodeType == YAMLElementTypes.FILE) {
//            return SAME_AS_PARENT_INDENT;
//        }
//        else if (YAMLElementTypes.SCALAR_VALUES.contains(nodeType)) {
//            return DIRECT_NORMAL_INDENT;
//        }
//        else if (nodeType == YAMLElementTypes.SEQUENCE_ITEM) {
//            return computeSequenceItemIndent(node);
//        }
//        else if (nodeType == YAMLElementTypes.KEY_VALUE_PAIR) {
//            return computeKeyValuePairIndent(node);
//        }
//        else {
//            if (nodeType == YAMLTokenTypes.COMMENT) {
//                if (parentType == YAMLElementTypes.SEQUENCE) {
//                    return computeSequenceItemIndent(node);
//                }
//                if (parentType == YAMLElementTypes.MAPPING) {
//                    return computeKeyValuePairIndent(node);
//                }
//            }
//            return YAMLElementTypes.TOP_LEVEL.contains(parentType) ? SAME_AS_PARENT_INDENT : null;
//        }
