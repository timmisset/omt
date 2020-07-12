package com.misset.opp.omt;

import com.intellij.lang.Language;

public class OMTLanguage extends Language {
    public static final OMTLanguage INSTANCE = new OMTLanguage();
    private OMTLanguage() {
        super("OMT");
    }
}
