package com.misset.opp.omt.psi.impl;


import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.*;

public class OMTPsiImplUtil {

    // Variable
    public static String getName(OMTVariable variable) {
        return variable.getText();
    }

    public static OMTVariable setName(OMTVariable variable, String newName) {
        OMTVariable replacement = OMTElementFactory.createVariable(variable.getProject(), newName);
        variable.replace(replacement);
        return replacement;
    }
    public static PsiElement getNameIdentifier(OMTVariable variable) {
        return variable;
    }
    public static boolean isDeclaredVariable(OMTVariable variable) {
        return variable.getDeclaredVariable() != null;
    }




    // Namespace prefixes
    public static String getName(OMTNamespacePrefix curieElement) { return curieElement.getText(); }
    public static OMTCurieElement setName(OMTNamespacePrefix curieElement, String newName) {
        OMTCurieElement replacement = OMTElementFactory.createCurieElement(curieElement.getProject(), newName);
        curieElement.replace(replacement);
        return replacement;
    }
    public static PsiElement getNameIdentifier(OMTNamespacePrefix namespacePrefix) { return namespacePrefix; }

    // OperatorCall
    public static String getName(OMTOperatorCall operatorCall) { return operatorCall.getFirstChild().getText(); }
    public static PsiElement setName(OMTOperatorCall operatorCall, String newName) {
        PsiElement replacement = OMTElementFactory.createOperator(operatorCall.getProject(), newName);
        operatorCall.replace(replacement);
        return replacement;
    }
    public static PsiElement getNameIdentifier(OMTOperatorCall operatorCall) { return operatorCall.getFirstChild(); }

    // DefineQueryStatement
    public static String getName(OMTDefineName defineName) { return defineName.getText(); }
    public static PsiElement setName(OMTDefineName defineName, String newName) {
        PsiElement replacement = OMTElementFactory.createOperator(defineName.getProject(), newName);
        defineName.replace(replacement);
        return replacement;
    }
    public static PsiElement getNameIdentifier(OMTDefineName defineName) { return defineName; }

    // Members
    public static String getName(OMTMember member) { return member.getText(); }
    public static PsiElement setName(OMTMember member, String newName) {
        PsiElement replacement = OMTElementFactory.createMember(member.getProject(), newName);
        member.replace(replacement);
        return replacement;
    }
    public static PsiElement getNameIdentifier(OMTMember member) { return member; }


    // Curies
    public static PsiElement getPrefix(OMTCurieElement curieElement) {
        return curieElement.getFirstChild();
    }
    public static boolean isDefinedByPrefix(OMTCurieElement curieElement, OMTPrefix prefix) {
        return curieElement.getText().startsWith(prefix.getNamespacePrefix().getText());
    }
}
