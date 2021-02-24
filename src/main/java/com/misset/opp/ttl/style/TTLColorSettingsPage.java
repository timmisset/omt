package com.misset.opp.ttl.style;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.util.TextRange;
import com.misset.opp.util.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class TTLColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("String", TTLSyntaxHighlighter.STRING),
            new AttributesDescriptor("Comment", TTLSyntaxHighlighter.COMMENT),
            new AttributesDescriptor("Subjects", TTLSyntaxHighlighter.SUBJECTS),
            new AttributesDescriptor("Predicates", TTLSyntaxHighlighter.PREDICATES),
            new AttributesDescriptor("Objects", TTLSyntaxHighlighter.OBJECTS),
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return Icons.TTLFile;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new TTLSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
                "@prefix dc: <http://purl.org/dc/elements/1.1/> .\n" +
                "@prefix ex: <http://example.org/stuff/1.0/> .\n" +
                "\n" +
                "<http://www.w3.org/TR/rdf-syntax-grammar>\n" +
                "  dc:title \"RDF/XML Syntax Specification (Revised)\" ;\n" +
                "  ex:editor [\n" +
                "    ex:fullname \"Dave Beckett\";\n" +
                "    ex:homePage <http://purl.org/net/dajobe/>\n" +
                "  ] .";
    }

    @Override
    public @Nullable PreviewCustomizer getPreviewEditorCustomizer() {
        return new PreviewCustomizer() {
            private TextRange getRangeForText(Editor editor, String text) {
                int startIndex = editor.getDocument().getText().indexOf(text);
                int endIndex = startIndex + text.length();
                return TextRange.create(startIndex, endIndex);
            }

            @Override
            public @Nullable TextRange addCustomizations(@NotNull Editor editor, @Nullable String selectedKeyName) {
                if (selectedKeyName == null) {
                    return null;
                }
                if (selectedKeyName.equals("SUBJECTS")) {
                    return getRangeForText(editor, "<http://www.w3.org/TR/rdf-syntax-grammar>");
                } else if (selectedKeyName.equals("PREDICATES")) {
                    return getRangeForText(editor, "dc:title");
                } else if (selectedKeyName.equals("OBJECTS")) {
                    return getRangeForText(editor, "<http://purl.org/net/dajobe/>");
                }
                return null;
            }

            @Override
            public void removeCustomizations(@NotNull Editor editor) {

            }

            @Override
            public @Nullable String getCustomizationAt(@NotNull Editor editor, @NotNull Point location) {
                return null;
            }
        };
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
        return "Turtle";
    }
}
