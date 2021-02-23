package com.misset.opp.omt;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OMTFileType extends LanguageFileType {
    public static final OMTFileType INSTANCE = new OMTFileType();

    private OMTFileType() {
        super(OMTLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "OMT File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "OMT language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "omt";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return com.misset.opp.util.Icons.OMTFile;
    }
}

