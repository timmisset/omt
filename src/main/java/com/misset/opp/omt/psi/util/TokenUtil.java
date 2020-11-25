package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.named.NamedMemberType;

import java.util.Arrays;

import static com.misset.opp.omt.psi.OMTTypes.*;

public class TokenUtil {

    private static final String TRUE = "true";
    public static TokenUtil SINGLETON = new TokenUtil();

    public boolean isModelItemType(PsiElement element) {
        return isToken(element, MODEL_ITEM_TYPE);
    }

    public boolean isProperty(PsiElement element) {
        return isToken(element, PROPERTY);
    }

    public boolean isCommand(PsiElement element) {
        return isToken(element, COMMAND);
    }

    public boolean isOperator(PsiElement element) {
        return isToken(element, OPERATOR);
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
        return isToken(element, SEQUENCE_BULLET);
    }

    public boolean isNotOperator(PsiElement element) {
        return isToken(element, NOT_OPERATOR);
    }

    public boolean isNamespaceMember(PsiElement element) {
        return isToken(element, NAMESPACE, NAMESPACE_MEMBER);
    }

    public boolean isParameterType(PsiElement element) {
        return element instanceof OMTParameterWithType
                || element instanceof OMTParameterType
                || (element instanceof OMTNamespacePrefix && isParameterType(element.getParent()));
    }
    public Object parseToTypedLiteral(OMTConstantValue constantValue) {
        if (isToken(constantValue.getFirstChild(), STRING)) {
            return constantValue.getText().substring(1, constantValue.getTextLength() - 1);
        }
        if (isToken(constantValue.getFirstChild(), INTEGER)) {
            return Integer.parseInt(constantValue.getText());
        }
        if (isToken(constantValue.getFirstChild(), DECIMAL)) {
            return Double.parseDouble(constantValue.getText());
        }
        if (isToken(constantValue.getFirstChild(), BOOLEAN)) {
            return constantValue.getText().equals(TRUE);
        }
        if (isToken(constantValue.getFirstChild(), NULL)) {
            return null;
        }
        return constantValue.getText();
    }

    private boolean isToken(PsiElement element, IElementType... types) {
        if (element == null || element.getNode() == null) {
            return false;
        }
        return Arrays.stream(types).anyMatch(type -> element.getNode().getElementType() == type);
    }


}
