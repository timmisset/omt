package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;

public class ParsingUtil {

    public static final ParsingUtil SINGLETON = new ParsingUtil();

    public <T> T castToOrNull(PsiElement element, Class<T> castToClass) {
        return castToClass.isAssignableFrom(element.getClass()) ? castToClass.cast(element) : null;
    }

}
