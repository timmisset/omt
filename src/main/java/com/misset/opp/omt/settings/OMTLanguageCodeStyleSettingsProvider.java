package com.misset.opp.omt.settings;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import com.misset.opp.omt.OMTLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OMTLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
    @Override
    public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
        if (settingsType == SettingsType.INDENT_SETTINGS) {
            consumer.showStandardOptions("INDENT_SIZE");
            consumer.showCustomOption(OMTCodeStyleSettings.class, "INDENT_SEQUENCE_BULLET", "Indent sequence bullet", "Sequence");
            consumer.showCustomOption(OMTCodeStyleSettings.class, "INDENT_AFTER_SEQUENCE_BULLET", "Indent after sequence bullet", "Sequence");
        }
    }

    @Nullable
    @Override
    public String getCodeSample(@NotNull SettingsType settingsType) {
        return "import:\n" +
                "    '@client/folder/file.omt':\n" +
                "    -   myImportedMethod\n" +
                "\n" +
                "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        title: MijnActiviteit\n" +
                "        \n" +
                "        params:\n" +
                "        -   $param\n" +
                "        -   $anotherParam\n" +
                "";
    }

    @NotNull
    @Override
    public Language getLanguage() {
        return OMTLanguage.INSTANCE;
    }

    @Nullable
    @Override
    public IndentOptionsEditor getIndentOptionsEditor() {
        return new OMTIndentOptionsEditor(this);
    }

    private static class OMTIndentOptionsEditor extends SmartIndentOptionsEditor {
        private JCheckBox indentSequenceBullet;
        private JCheckBox indentAfterSequenceBullet;

        OMTIndentOptionsEditor(@Nullable LanguageCodeStyleSettingsProvider provider) {
            super(provider);
        }

        @Override
        protected void addComponents() {
            super.addComponents();

            indentSequenceBullet = new JCheckBox("Indent sequence bullet");
            add(indentSequenceBullet);

            indentAfterSequenceBullet = new JCheckBox("Indent after sequence bullet");
            add(indentAfterSequenceBullet);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            indentSequenceBullet.setEnabled(enabled);
            indentAfterSequenceBullet.setEnabled(enabled);
        }

        @Override
        public boolean isModified(@NotNull CodeStyleSettings settings, @NotNull CommonCodeStyleSettings.IndentOptions options) {
            boolean isModified = super.isModified(settings, options);
            OMTCodeStyleSettings codeStyleSettings = settings.getCustomSettings(OMTCodeStyleSettings.class);

            isModified |= isFieldModified(indentSequenceBullet, codeStyleSettings.INDENT_SEQUENCE_VALUE);
            isModified |= isFieldModified(indentAfterSequenceBullet, codeStyleSettings.INDENT_AFTER_SEQUENCE_VALUE);

            return isModified;
        }

        @Override
        public void apply(@NotNull CodeStyleSettings settings, @NotNull CommonCodeStyleSettings.IndentOptions options) {
            super.apply(settings, options);

            OMTCodeStyleSettings codeStyleSettings = settings.getCustomSettings(OMTCodeStyleSettings.class);
            codeStyleSettings.INDENT_SEQUENCE_VALUE = indentSequenceBullet.isSelected();
            codeStyleSettings.INDENT_AFTER_SEQUENCE_VALUE = indentAfterSequenceBullet.isSelected();
        }

        @Override
        public void reset(@NotNull CodeStyleSettings settings, @NotNull CommonCodeStyleSettings.IndentOptions options) {
            super.reset(settings, options);

            OMTCodeStyleSettings codeStyleSettings = settings.getCustomSettings(OMTCodeStyleSettings.class);
            indentSequenceBullet.setSelected(codeStyleSettings.INDENT_SEQUENCE_VALUE);
            indentAfterSequenceBullet.setSelected(codeStyleSettings.INDENT_AFTER_SEQUENCE_VALUE);
        }
    }
}
