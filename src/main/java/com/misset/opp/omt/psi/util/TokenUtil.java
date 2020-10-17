package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;

public class TokenUtil {

    public static TokenUtil SINGLETON = new TokenUtil();

    public boolean isModelItemType(PsiElement element) {
        return isToken(element, "OMTTokenType.MODEL_ITEM_TYPE");
    }

    public boolean isProperty(PsiElement element) {
        return isToken(element, "OMTTokenType.PROPERTY");
    }

    public boolean isCommand(PsiElement element) {
        return isToken(element, "OMTTokenType.COMMAND");
    }

    public boolean isOperator(PsiElement element) {
        return isToken(element, "OMTTokenType.OPERATOR");
    }

    public boolean isSemiColon(PsiElement element) {
        return isToken(element, "OMTTokenType.SEMICOLON");
    }

    public boolean isWhiteSpace(PsiElement element) {
        return element instanceof PsiWhiteSpace;
    }

    private boolean isToken(PsiElement element, String debugName) {
        if (element == null) {
            return false;
        }
        return element instanceof LeafPsiElement &&
                ((LeafPsiElement) element).getElementType().toString().equals(debugName);
    }
}
