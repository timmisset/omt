package com.misset.opp.omt.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import org.jdesktop.swingx.JXTitledSeparator;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class OMTSettingsComponent {

    private final JPanel myMainPanel;
    private final TextFieldWithBrowseButton builtInCommandsLocation = getFileLocationSetting("builtinCommands.ts");
    private final TextFieldWithBrowseButton builtInOperatorsLocation = getFileLocationSetting("builtinOperators.ts");
    private final TextFieldWithBrowseButton builtInHttpCommandsLocation = getFileLocationSetting("http-commands.ts");
    private final TextFieldWithBrowseButton builtInParseJsonCommandLocation = getFileLocationSetting("json-parse-command.ts");
    private final TextFieldWithBrowseButton ontologyModelRootPath = getFileLocationSetting("root.ttl");
    private final JBCheckBox includeMochaFolderImportSuggestions = new JBCheckBox("Suggest imports from mocha subfolders");

    public OMTSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addComponent(new JXTitledSeparator("Operator & Command locations"))
                .addLabeledComponent(new JBLabel("Commands:"), builtInCommandsLocation, 1, false)
                .addLabeledComponent(new JBLabel("Operators:"), builtInOperatorsLocation, 1, false)
                .addLabeledComponent(new JBLabel("HttpCommands:"), builtInHttpCommandsLocation, 1, false)
                .addLabeledComponent(new JBLabel("ParseJson:"), builtInParseJsonCommandLocation, 1, false)
                .addComponent(new JXTitledSeparator("Ontology root"))
                .addLabeledComponent(new JBLabel("Ontology model root:"), ontologyModelRootPath, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .addComponent(new JXTitledSeparator("Imports"))
                .addComponent(includeMochaFolderImportSuggestions)
                .getPanel();
    }

    private TextFieldWithBrowseButton getFileLocationSetting(String name) {
        TextFieldWithBrowseButton textFieldWithBrowseButton = new TextFieldWithBrowseButton();
        textFieldWithBrowseButton.setEditable(true);
        textFieldWithBrowseButton.addBrowseFolderListener(
                new TextBrowseFolderListener(getFileDescriptorByFilename(name)));
        return textFieldWithBrowseButton;
    }

    private FileChooserDescriptor getFileDescriptorByFilename(final String name) {
        return new FileChooserDescriptor(true, false, false, false, false, false).withFileFilter(
                file -> Comparing.equal(file.getName(), name, SystemInfo.isFileSystemCaseSensitive));
    }


    public JPanel getPanel() {
        return myMainPanel;
    }

    @NotNull
    public String getBuiltinOperatorsLocation() {
        return builtInOperatorsLocation.getText();
    }

    public void setBuiltinOperatorsLocation(@NotNull String newText) {
        builtInOperatorsLocation.setText(newText);
    }

    @NotNull
    public String getBuiltinCommandsLocation() {
        return builtInCommandsLocation.getText();
    }

    public void setBuiltinCommandsLocation(@NotNull String newText) {
        builtInCommandsLocation.setText(newText);
    }

    @NotNull
    public String getBuiltinHttpCommandsLocation() {
        return builtInHttpCommandsLocation.getText();
    }

    public void setBuiltinHttpCommandsLocation(@NotNull String newText) {
        builtInHttpCommandsLocation.setText(newText);
    }

    @NotNull
    public String getBuiltinParseJsonLocation() {
        return builtInParseJsonCommandLocation.getText();
    }

    public void setBuiltinParseJsonLocation(@NotNull String newText) {
        builtInParseJsonCommandLocation.setText(newText);
    }

    @NotNull
    public String getOntologyModelRootPath() {
        return ontologyModelRootPath.getText();
    }

    public void setOntologyModelRootPath(@NotNull String newText) {
        ontologyModelRootPath.setText(newText);
    }

    @NotNull
    public boolean getIncludeMochaFolderImportSuggestions() {
        return includeMochaFolderImportSuggestions.isSelected();
    }

    public void setIncludeMochaFolderImportSuggestions(boolean selected) {
        includeMochaFolderImportSuggestions.setSelected(selected);
    }
}
