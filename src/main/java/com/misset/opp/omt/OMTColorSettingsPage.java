package com.misset.opp.omt;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.util.Map;

public class OMTColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Separator", OMTSyntaxHighlighter.SEPARATOR),
            new AttributesDescriptor("Prefix IRI", OMTSyntaxHighlighter.PREFIX_IRI),
            new AttributesDescriptor("Model Item Type", OMTSyntaxHighlighter.MODEL_ITEM_TYPE),
            new AttributesDescriptor("Variable", OMTSyntaxHighlighter.VARIABLE),
            new AttributesDescriptor("Bad Value", OMTSyntaxHighlighter.BAD_CHARACTER)
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return OMTIcons.FILE;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new OMTSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "prefixes:\n" +
                "    pol = <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "\n" +
                "model:\n" +
                "\n" +
                "    mijnActiviteit: !Activity\n" +
                "\n" +
                "        variables:\n" +
                "            - $mijnVariable";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "OMT";
    }
}
