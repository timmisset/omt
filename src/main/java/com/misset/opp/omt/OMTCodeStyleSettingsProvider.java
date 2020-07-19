package com.misset.opp.omt;

import com.intellij.application.options.*;
import com.intellij.psi.codeStyle.*;
import org.jetbrains.annotations.*;

public class OMTCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
    @Override
    public CustomCodeStyleSettings createCustomSettings(CodeStyleSettings settings) {
        return new OMTCodeStyleSettings(settings);
    }

    @Nullable
    @Override
    public String getConfigurableDisplayName() {
        return "OMT";
    }


    @NotNull
    public CodeStyleConfigurable createConfigurable(@NotNull CodeStyleSettings settings, @NotNull CodeStyleSettings modelSettings) {
        return new CodeStyleAbstractConfigurable(settings, modelSettings, this.getConfigurableDisplayName()) {
            @Override
            protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings) {
                return new OMTCodeStyleMainPanel(getCurrentSettings(), settings);
            }
        };
    }

    private static class OMTCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {
        public OMTCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
            super(OMTLanguage.INSTANCE, currentSettings, settings);
        }
    }
}
