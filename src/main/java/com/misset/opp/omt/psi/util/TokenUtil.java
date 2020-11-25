package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.misset.opp.omt.psi.OMTConstantValue;
import com.misset.opp.omt.psi.OMTMember;
import com.misset.opp.omt.psi.OMTParameterType;
import com.misset.opp.omt.psi.OMTParameterWithType;
import com.misset.opp.omt.psi.named.NamedMemberType;

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

    public boolean isMemberImport(PsiElement element) {
        return isOperator(element) &&
                element.getParent() instanceof OMTMember &&
                ((OMTMember) element.getParent()).getType() == NamedMemberType.ImportingMember;
    }

    public boolean isWhiteSpace(PsiElement element) {
        return element instanceof PsiWhiteSpace;
    }

    public boolean isSequenceBullet(PsiElement element) {
        return isToken(element, "OMTTokenType.SEQUENCE_BULLET");
    }

    public boolean isNotOperator(PsiElement element) {
        return isToken(element, "OMTTokenType.NOT_OPERATOR");
    }

    public boolean isNamespaceMember(PsiElement element) {
        return isToken(element, "OMTTokenType.NAMESPACE_MEMBER");
    }

    public boolean isParameterType(PsiElement element) {
        return element instanceof OMTParameterWithType
                || element instanceof OMTParameterType;
    }
    public Object parseToTypedLiteral(OMTConstantValue constantValue) {
        if (isToken(constantValue.getFirstChild(), "OMTTokenType.STRING")) {
            return constantValue.getText().substring(1, constantValue.getTextLength() - 1);
        }
        if (isToken(constantValue.getFirstChild(), "OMTTokenType.INTEGER")) {
            return Integer.parseInt(constantValue.getText());
        }
        if (isToken(constantValue.getFirstChild(), "OMTTokenType.DECIMAL")) {
            return Double.parseDouble(constantValue.getText());
        }
        if (isToken(constantValue.getFirstChild(), "OMTTokenType.BOOLEAN")) {
            return constantValue.getText().equals("true");
        }
        if (isToken(constantValue.getFirstChild(), "OMTTokenType.NULL")) {
            return null;
        }
        return constantValue.getText();
    }

    private boolean isToken(PsiElement element, String debugName) {
        if (element == null) {
            return false;
        }
        return element instanceof LeafPsiElement &&
                ((LeafPsiElement) element).getElementType().toString().equals(debugName);
    }


}
