package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.misset.opp.omt.psi.OMTConstantValue;

import java.util.Arrays;

import static com.misset.opp.omt.psi.OMTTypes.BOOLEAN;
import static com.misset.opp.omt.psi.OMTTypes.DECIMAL;
import static com.misset.opp.omt.psi.OMTTypes.INTEGER;
import static com.misset.opp.omt.psi.OMTTypes.NOT_OPERATOR;
import static com.misset.opp.omt.psi.OMTTypes.NULL;
import static com.misset.opp.omt.psi.OMTTypes.STRING;

public class TokenUtil {

    private static final String TRUE = "true";

    public boolean isNotOperator(PsiElement element) {
        return isToken(element, NOT_OPERATOR);
    }

    public Object parseToTypedLiteral(OMTConstantValue constantValue) {
        if (isToken(constantValue.getFirstChild(), STRING)) {
            return constantValue.getText().length() > 1 ? constantValue.getText().substring(1, constantValue.getTextLength() - 1) : "";
        }
        if (isToken(constantValue.getFirstChild(), INTEGER)) {
            return Integer.parseInt(constantValue.getText());
        }
        if (isToken(constantValue.getFirstChild(), DECIMAL)) {
            return Double.parseDouble(constantValue.getText());
        }
        if (isToken(constantValue.getFirstChild(), BOOLEAN)) {
            return constantValue.textMatches(TRUE);
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
