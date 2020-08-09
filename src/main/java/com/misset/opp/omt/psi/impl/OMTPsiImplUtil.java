package com.misset.opp.omt.psi.impl;


import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTDeclaredVariable;
import com.misset.opp.omt.psi.OMTElementFactory;
import com.misset.opp.omt.psi.OMTVariable;
import org.jetbrains.annotations.Nullable;

public class OMTPsiImplUtil {

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
}
