package com.misset.opp.omt.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Supports storing the application settings in a persistent way.
 * The {@link State} and {@link Storage} annotations define the name of the data and the file name where
 * these persistent application settings are stored.
 */
@State(
        name = "com.misset.opp.omt.settings.OMTSettingsState",
        storages = {@Storage("omtodt.xml")}
)
public class OMTSettingsState implements PersistentStateComponent<OMTSettingsState> {

    public String builtInCommandsPath = "";
    public String builtInOperatorsPath = "";
    public String builtInHttpCommandsPath = "";
    public String builtInParseJsonPath = "";

    public static OMTSettingsState getInstance() {
        return ServiceManager.getService(OMTSettingsState.class);
    }

    @Nullable
    @Override
    public OMTSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull OMTSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Override
    public void noStateLoaded() {

    }

    @Override
    public void initializeComponent() {

    }
}
