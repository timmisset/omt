package com.misset.opp.omt.settings;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import com.misset.opp.omt.OMTLanguage;

public class OMTCodeStyleSettings extends CustomCodeStyleSettings {
    public boolean INDENT_SEQUENCE_VALUE = false;
    public boolean INDENT_AFTER_SEQUENCE_VALUE = true;

    protected OMTCodeStyleSettings(CodeStyleSettings container) {
        super(OMTLanguage.INSTANCE.getID(), container);
    }
}
