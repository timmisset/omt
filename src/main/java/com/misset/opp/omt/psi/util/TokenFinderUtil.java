package com.misset.opp.omt.psi.util;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTImport;
import com.misset.opp.omt.psi.OMTIndentToken;
import com.misset.opp.omt.psi.support.OMTTokenSets;

import java.util.Optional;

/**
 * Additional util functions to find tokens based on the position of a provider element
 */
public class TokenFinderUtil {

    public static final TokenFinderUtil SINGLETON = new TokenFinderUtil();

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
     *
     * @param element
     * @param document
     * @return
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

}
