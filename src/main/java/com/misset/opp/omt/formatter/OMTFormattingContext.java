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

import static com.misset.opp.omt.psi.OMTTypes.*;


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

    public Indent computeBlockIndent(@NotNull ASTNode node, OMTFormattingBlock omtFormattingBlock) {
        IElementType nodeType = PsiUtilCore.getElementType(node);

        Indent indent = Indent.getNoneIndent();
        if (!isRootElement(node)) {
            if (OMTTokenSets.ENTRIES.contains(nodeType) || OMTTokenSets.QUERY_INDENTATION_PARENT.contains(nodeType)) {
                indent = Indent.getNormalIndent(true);
            } else if (isQueryPart(node) || OMTTokenSets.SEQUENCES.contains(nodeType)) {
                indent = Indent.getNormalIndent(false);
            } else if (SCRIPT_LINE == node.getElementType() && !hasQueryPart(node)) {
                indent = Indent.getNormalIndent(false);
            } else if (node.getTreeParent() != null && INTERPOLATED_STRING == node.getTreeParent().getElementType()) {
                indent = Indent.getNormalIndent(false);
            }
        }

        System.out.println(node.getElementType().toString() + " -->  " + node.getText().substring(0, Math.min(node.getTextLength(), 10)) +
                " --> " + indent.toString());
        if (TokenSet.create(SCRIPT_LINE).contains(node.getElementType())) {
            System.out.println("here");
        }
        return indent;

    }

    private boolean isQueryPart(ASTNode node) {
        return TokenSet.create(
                QUERY_STEP, QUERY_ARRAY
        ).contains(PsiUtilCore.getElementType(node.getTreeParent()));
    }

    private boolean hasQueryPart(ASTNode node) {
        while (node != null) {
            if (QUERY_STEP == node.getElementType()) {
                return true;
            }
            node = node.getFirstChildNode();
        }
        return false;
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

    private ASTNode getFirstOfItsKindInParent(@NotNull ASTNode node) {
        return getFirstOfKindInParent(node.getTreeParent(), node.getElementType());
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

    public Alignment computeAlignment(@NotNull ASTNode node) {
        if (OMTTokenSets.ENTRIES.contains(node.getElementType())) {
            if (isFirstOfItsKindInParent(node)) {
                return registerAlignementAndReturn(node);
            } else {
                return nodeAlignment.get(getFirstOfItsKindInParent(node));
            }
        } else if (OMTTokenSets.CHOOSE.contains(node.getElementType())) {
            return alignChooseBlock(node);
        } else if (node.getTreeParent() != null && INTERPOLATED_STRING == node.getTreeParent().getElementType()) {
            return alignInterpolatedString(node);
        } else if (isJavaDocsPart(node)) {
            return alignJavaDocs(node);
        }

        return null;
    }

    private Alignment alignChooseBlock(ASTNode node) {
        if (CHOOSE_OPERATOR == node.getElementType()) {
            return registerAlignementAndReturn(node);
        } else if (END_PATH == node.getElementType()) {
            return nodeAlignment.get(getFirstOfKindInParent(node.getTreeParent(), CHOOSE_OPERATOR));
        } else if (OMTTokenSets.CHOOSE_INNER.contains(node.getElementType())) {
            if (isFirstOfItsKindInParent(node)) {
                return registerAlignementAndReturn(node);
            } else {
                return nodeAlignment.get(getFirstOfKindInParent(node, WHEN_PATH));
            }
        }
        return null;
    }

    private Alignment alignJavaDocs(ASTNode node) {
        if (JAVADOCS_START == node.getElementType()) {
            return registerAlignementAndReturn(node);
        } else if (JAVADOCS_END == node.getElementType()) {
            // END shares the same parent
            return nodeAlignment.get(getFirstOfKindInParent(node.getTreeParent(), JAVADOCS_START));
        } else {
            // Only other option is JAVADOCS_CONTENT, which is contained by JD_CONTENT which is part of the same parent
            return nodeAlignment.get(getFirstOfKindInParent(node.getTreeParent().getTreeParent(), JAVADOCS_START));
        }
    }

    private Alignment alignInterpolatedString(ASTNode node) {
        if (STRING == node.getElementType() ||
                INTERPOLATION_TEMPLATE == node.getElementType()) {
            // get first element:
            final ASTNode firstOfKindInParent = getFirstOfKindInParent(node.getTreeParent(), STRING, INTERPOLATION_TEMPLATE);
            if (firstOfKindInParent == node) {
                return registerAlignementAndReturn(node);
            } else {
                return nodeAlignment.get(firstOfKindInParent);
            }
        }
        return null;
    }

    private Alignment registerAlignementAndReturn(ASTNode node) {
        Alignment alignment = Alignment.createAlignment();
        nodeAlignment.put(node, alignment);
        return alignment;
    }
}

