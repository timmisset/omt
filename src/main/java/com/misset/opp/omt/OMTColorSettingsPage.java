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
            new AttributesDescriptor("Variable Type", OMTSyntaxHighlighter.VARIABLE_TYPE),
            new AttributesDescriptor("Model Item Type", OMTSyntaxHighlighter.MODEL_ITEM_TYPE),
            new AttributesDescriptor("Constant Value", OMTSyntaxHighlighter.CONSTANT_VALUE),
            new AttributesDescriptor("Variable", OMTSyntaxHighlighter.VARIABLE),
            new AttributesDescriptor("Bad Value", OMTSyntaxHighlighter.BAD_CHARACTER),
            new AttributesDescriptor("Comment Block", OMTSyntaxHighlighter.COMMENTBLOCK)
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
                "    sldf: <http://www.data.nl>\n" +
                "\n" +
                "queries:\n" +
                "    DEFINE QUERY myFirstQuery() => 'Hello world!' / ^rdf:type;\n" +
                "\n" +
                "model:\n" +
                "    asdfasd: !Activity\n" +
                "        Anotherblock:\n" +
                "\n" +
                "        /**\n" +
                "        * Een JavaDoc blok om wat te vertellen over PropertyMetValue\n" +
                "        */\n" +
                "        PropertyMetValue: 'Test' // Kan ook via een EndOfLineComment\n" +
                "\n" +
                "    NewBlock:\n" +
                "        NewBlockAB:\n" +
                "            - $myVariable\n" +
                "            - $sometakaslm\n" +
                "            - $asdfas\n" +
                "        asdfasdf: |\n" +
                "            $myVariable = \"Something\";\n" +
                "\n" +
                "        NewBlockCD:\n" +
                "\n" +
                "    ThirdBlock:\n" +
                "\n" +
                "something:\n" +
                "\n" +
                "    Block:\n";
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
