package com.misset.opp.omt.style;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class OMTColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Global variable type", OMTSyntaxHighlighter.GLOBAL_VARIABLE),
            new AttributesDescriptor("YAML tags", OMTSyntaxHighlighter.TAGS),
            new AttributesDescriptor("String", OMTSyntaxHighlighter.STRING),
            new AttributesDescriptor("Number", OMTSyntaxHighlighter.NUMBER),
            new AttributesDescriptor("Boolean, null", OMTSyntaxHighlighter.CONSTANT),
            new AttributesDescriptor("Variable", OMTSyntaxHighlighter.VARIABLE),
            new AttributesDescriptor("Readonly variable", OMTSyntaxHighlighter.READ_ONLY_VARIABLE),
            new AttributesDescriptor("Comment block", OMTSyntaxHighlighter.COMMENTBLOCK),
            new AttributesDescriptor("Comment line", OMTSyntaxHighlighter.COMMENTLINE),
            new AttributesDescriptor("Operator & command names", OMTSyntaxHighlighter.OPERATOR_OR_COMMAND),
            new AttributesDescriptor("Curies & IRIs", OMTSyntaxHighlighter.CURIE_IRI)
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return com.misset.opp.util.Icons.OMTFile;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new OMTSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "import:\n" +
                "    '@client/some/src/thing.omt':\n" +
                "    -   aCommand\n" +
                "    -   anOperator\n" +
                "    ../something/else.omt:\n" +
                "    -   anotherCommand\n" +
                "    -   anotherMethod\n" +
                "\n" +
                "prefixes:\n" +
                "    /**\n" +
                "    * Some info about abc\n" +
                "    */\n" +
                "    abc:    <http://ontologie.alfabet.nl/alfabet#>\n" +
                "    foaf:   <http://ontologie.foaf.nl/friendOfAfriend#> // and about foaf\n" +
                "\n" +
                "model:\n" +
                "    MijnActiviteit: !Activity\n //some comment about the activity\n" +
                "        title: Mijn Activiteit\n" +
                "\n" +
                "        params:\n" +
                "        -   $paramA (abc:Something)\n" +
                "        -   $paramB (foaf:SomethingElse)\n" +
                "\n" +
                "        queries: |\n" +
                "            DEFINE QUERY myFirstQuery() => true;\n" +
                "            /**\n" +
                "            * mySecondQuery is used to ...\n" +
                "            * @param $param1 (abc:Alpha)\n" +
                "            */\n" +
                "            DEFINE QUERY mySecondQuery($param1) => CONCAT('Hello ', $param1);\n" +
                "            DEFINE QUERY myThirdQuery() => $medewerkerGraph;\n" +
                "\n" +
                "        variables:\n" +
                "        -   $variableA\n" +
                "        -   $variableB\n" +
                "\n" +
                "        graphs:\n" +
                "            edit:\n" +
                "            -   $variableA / GRAPH\n" +
                "            live:\n" +
                "            -   $variableB / GRAPH\n" +
                "\n" +
                "        onStart: |\n" +
                "            $variableA = 10;\n" +
                "            $variableB = null;\n" +
                "            VAR $declaredVariable = @CONCAT($variableA, $variableB);\n" +
                "\n" +
                "        payload:\n" +
                "            payloadA: $variableA\n" +
                "            payloadB: $variableB\n" +
                "            payloadC:\n" +
                "                query:  someQuery\n" +
                "                list:   true\n";
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
