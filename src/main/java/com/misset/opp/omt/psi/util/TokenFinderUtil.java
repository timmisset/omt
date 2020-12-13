package com.misset.opp.omt.psi.util;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.misset.opp.omt.formatter.OMTTokenSets;
import com.misset.opp.omt.psi.OMTImport;
import com.misset.opp.omt.psi.OMTIndentToken;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Additional util functions to find tokens based on the position of a provider element
 */
public class TokenFinderUtil {

    public Optional<OMTImport> findImport(PsiElement sibling) {
        while (sibling != null && !(sibling instanceof OMTImport)) {
            sibling = sibling.getPrevSibling();
        }
        if (sibling != null) {
            return Optional.of((OMTImport) sibling);
        }
        return Optional.empty();
    }

    /**
     * Returns the line position of the token
     */
    public int getLineOffset(PsiElement element, Document document) {
        if (element.getFirstChild() instanceof OMTIndentToken) {
            // use the first element after indentation
            element = element.getFirstChild().getNextSibling();
        }
        return getLineOffset(element.getTextOffset(), document);
    }

    public int getLineOffset(int offset, Document document) {
        final int lineNumber = document.getLineNumber(offset);
        return offset - document.getLineStartOffset(lineNumber);
    }

    public boolean isStartOfLine(ASTNode node) {
        if (node.getTreePrev() == null) {
            return true;
        }
        while (node != null && OMTTokenSets.WHITESPACE.contains(node.getElementType())) {
            if (node.getTreePrev() != null && node.getTreePrev().getText().equals("\n")) {
                return true;
            }
            node = node.getTreePrev();
        }
        return false;
    }

    public boolean isStartOfLine(PsiElement element) {
        return isStartOfLine(element.getNode());
    }

    public List<ASTNode> getNonWhiteSpaceChildren(@NotNull ASTNode node) {
        List<ASTNode> children = new ArrayList<>();
        ASTNode child = node.getFirstChildNode();
        while (child != null) {
            if (!TokenSet.WHITE_SPACE.contains(child.getElementType())) {
                children.add(child);
            }
            child = child.getTreeNext();
        }
        return children;
    }

    /**
     * Returns siblings with identical types, skips the initial element but returns 2, 3 etc
     */
    public List<ASTNode> getDuplicateSiblings(@NotNull ASTNode parent, IElementType type) {
        final List<ASTNode> nonWhiteSpaceChildren = getNonWhiteSpaceChildren(parent);
        final List<ASTNode> identicalSiblings = new ArrayList<>();
        for (int i = 1; i < nonWhiteSpaceChildren.size(); i++) {
            if (nonWhiteSpaceChildren.get(i).getElementType() == type && nonWhiteSpaceChildren.get(i - 1).getElementType() == type) {
                identicalSiblings.add(nonWhiteSpaceChildren.get(i));
            }
        }
        return identicalSiblings;
    }
}
