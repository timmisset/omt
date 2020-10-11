package com.misset.opp.omt.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.NlsContexts;
import com.misset.opp.omt.psi.util.ProjectUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OMTConfigurable implements Configurable {
    private static ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    private OMTSettingsComponent settingsComponent;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "OMT Plugin Settings";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "Set specific fields for the OMT Plugin";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsComponent = new OMTSettingsComponent();
        return settingsComponent.getPanel();
    }


    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Override
    public boolean isModified() {
        OMTSettingsState settingsState = OMTSettingsState.getInstance();
        boolean modified = !settingsComponent.getBuiltinCommandsLocation().equals(settingsState.builtInCommandsPath);
        modified |= !settingsComponent.getBuiltinOperatorsLocation().equals(settingsState.builtInOperatorsPath);
        modified |= !settingsComponent.getBuiltinHttpCommandsLocation().equals(settingsState.builtInHttpCommandsPath);
        modified |= !settingsComponent.getBuiltinParseJsonLocation().equals(settingsState.builtInParseJsonPath);
        modified |= !settingsComponent.getOntologyModelRootPath().equals(settingsState.ontologyModelRootPath);
        return modified;
    }

    @Override
    public void apply() {
        OMTSettingsState settingsState = OMTSettingsState.getInstance();
        settingsState.builtInCommandsPath = settingsComponent.getBuiltinCommandsLocation();
        settingsState.builtInOperatorsPath = settingsComponent.getBuiltinOperatorsLocation();
        settingsState.builtInHttpCommandsPath = settingsComponent.getBuiltinHttpCommandsLocation();
        settingsState.builtInParseJsonPath = settingsComponent.getBuiltinParseJsonLocation();
        settingsState.ontologyModelRootPath = settingsComponent.getOntologyModelRootPath();

        // will try to reload the builtin members from the new file locations
        projectUtil.loadBuiltInMembers(ProjectManager.getInstance().getOpenProjects()[0]);
        projectUtil.loadOntologyModel(ProjectManager.getInstance().getOpenProjects()[0]);
    }

    @Override
    public void reset() {
        OMTSettingsState settingsState = OMTSettingsState.getInstance();
        settingsComponent.setBuiltinCommandsLocation(settingsState.builtInCommandsPath);
        settingsComponent.setBuiltinOperatorsLocation(settingsState.builtInOperatorsPath);
        settingsComponent.setBuiltinHttpCommandsLocation(settingsState.builtInHttpCommandsPath);
        settingsComponent.setBuiltinParseJsonLocation(settingsState.builtInParseJsonPath);
        settingsComponent.setOntologyModelRootPath(settingsState.ontologyModelRootPath);
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}
