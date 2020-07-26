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
            new AttributesDescriptor("String", OMTSyntaxHighlighter.STRING),
            new AttributesDescriptor("Number", OMTSyntaxHighlighter.NUMBER),
            new AttributesDescriptor("Boolean, Null", OMTSyntaxHighlighter.CONSTANT),
            new AttributesDescriptor("Variable", OMTSyntaxHighlighter.VARIABLE),
            new AttributesDescriptor("Bad Value", OMTSyntaxHighlighter.BAD_CHARACTER),
            new AttributesDescriptor("Comment Block", OMTSyntaxHighlighter.COMMENTBLOCK),
            new AttributesDescriptor("Operator and Command names", OMTSyntaxHighlighter.OPERATOR_OR_COMMAND),


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
                "    pol: <http://www.politie.nl/>\n" +
                "    polx: <http://enter.your/iri/>\n" +
                "\n" +
                "queries:\n" +
                "    DEFINE QUERY MyQuery($queryVariable1, $queryVariable2) => $queryVariable1 / polx:dicate / pol:there;\n" +
                "\n" +
                "model:\n" +
                "    mijnActiviteit: !Activity\n" +
                "        queries:\n" +
                "            DEFINE QUERY anotherQuery() => $mijnVariable / MyQuery('something');\n" +
                "\n" +
                "        variables:\n" +
                "            - $eenTweedeVariabel = 'test'\n" +
                "            - $mijnVariable\n" +
                "\n" +
                "        params:\n" +
                "            - $somethingElse\n" +
                "            - $anotherOne\n" +
                "\n" +
                "        payload:\n" +
                "            somethingElse: $somethingElse\n" +
                "            anotherOne:\n" +
                "                variable: $anotherOne\n" +
                "                list: true\n" +
                "\n" +
                "        queryWatcher:\n" +
                "            query: Something()\n" +
                "            onChange: |\n" +
                "\n" +
                "        onStart: |\n" +
                "            VAR $x = $eenTweedeVariabel;\n" +
                "            VAR $s = 'something';\n" +
                "            VAR $y = 12345.10;\n" +
                "            VAR $z = $x / pol:bla / anotherQuery();\n" +
                "            $x = $y / PLUS($z) / MIN($somethingElse);\n" +
                "\n" +
                "    mijnProcedure: !Procedure\n" +
                "        variables:\n" +
                "            - $mijnVariable\n" +
                "            - $mijnTweedeVariable\n";
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
