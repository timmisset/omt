package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;

public class ParsingUtil {

    public static <T> T castToOrNull(PsiElement element, Class<T> castToClass) {
        return castToClass.isAssignableFrom(element.getClass()) ? castToClass.cast(element) : null;
    }

}
