package com.misset.opp.omt;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class OMTSyntaxHighlighter extends SyntaxHighlighterBase {
    public static final TextAttributesKey SEPARATOR =
            createTextAttributesKey("SIMPLE_SEPARATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey COMMENTLINE =
            createTextAttributesKey("END_OF_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey COMMENTBLOCK =
            createTextAttributesKey("COMMENT_BLOCK", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey BAD_CHARACTER =
            createTextAttributesKey("SIMPLE_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);
    public static final TextAttributesKey ITEM_TYPE =
            createTextAttributesKey("ITEM_TYPE", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey PREFIX =
            createTextAttributesKey("PREFIX", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey VARIABLE =
            createTextAttributesKey("VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey GLOBAL_VARIABLE =
            createTextAttributesKey("GLOBAL_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
    public static final TextAttributesKey STRING =
            createTextAttributesKey("STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey NUMBER =
            createTextAttributesKey("NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey CONSTANT =
            createTextAttributesKey("CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey OPERATOR_OR_COMMAND =
            createTextAttributesKey("OPERATOR", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey OWLPROPERTY =
            createTextAttributesKey("OWLPROPERTY", DefaultLanguageHighlighterColors.IDENTIFIER);

    private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{BAD_CHARACTER};
    private static final TextAttributesKey[] COMMENTLINE_KEYS = new TextAttributesKey[]{COMMENTLINE};
    private static final TextAttributesKey[] COMMENTBLOCK_KEYS = new TextAttributesKey[]{COMMENTBLOCK};
    private static final TextAttributesKey[] STRING_KEYS = new TextAttributesKey[]{STRING};
    private static final TextAttributesKey[] NUMBER_KEYS = new TextAttributesKey[]{NUMBER};
    private static final TextAttributesKey[] CONSTANT_VALUE_KEYS = new TextAttributesKey[]{CONSTANT};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
    private static final TextAttributesKey[] TYPE_KEYS = new TextAttributesKey[]{ITEM_TYPE};
    private static final TextAttributesKey[] VARIABLE_TYPE_KEYS = new TextAttributesKey[]{VARIABLE};
    private static final TextAttributesKey[] GLOBAL_VARIABLE_KEYS = new TextAttributesKey[]{GLOBAL_VARIABLE};
    private static final TextAttributesKey[] OPERATOR_OR_COMMAND_KEYS = new TextAttributesKey[]{OPERATOR_OR_COMMAND};
    private static final TextAttributesKey[] OWLPROPERTY_KEYS = new TextAttributesKey[]{OWLPROPERTY};
    private static final TextAttributesKey[] PREFIX_KEYS = new TextAttributesKey[]{PREFIX};

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new OMTLexerAdapter();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        switch (tokenType.toString()) {
            case "OMTTokenType.END_OF_LINE_COMMENT":
                return COMMENTLINE_KEYS;
            case "OMTTokenType.JAVADOCS_START":
            case "OMTTokenType.JAVADOCS_END":
            case "OMTTokenType.JAVADOCS_CONTENT":
                return COMMENTBLOCK_KEYS;
            case "BAD_CHARACTER":
                return BAD_CHAR_KEYS;
            case "OMTTokenType.MODEL_ITEM_TYPE":
                return TYPE_KEYS;
            case "OMTTokenType.PARAMETER":
            case "OMTTokenType.VARIABLE_NAME":
            case "OMTTokenType.DEFINED_VARIABLE_name":
                return VARIABLE_TYPE_KEYS;
            case "OMTTokenType.GLOBAL_VARIABLE_NAME":
                return GLOBAL_VARIABLE_KEYS;
            case "OMTTokenType.OPERATOR":
            case "OMTTokenType.COMMAND":
            case "OMTTokenType.CONDITIONAL_OPERATOR":
            case "OMTTokenType.IF_OPERATOR":
            case "OMTTokenType.ELSE_OPERATOR":
            case "OMTTokenType.RETURN_OPERATOR":
                return OPERATOR_OR_COMMAND_KEYS;

            case "OMTTokenType.STRING":
                return STRING_KEYS;
            case "OMTTokenType.BOOLEAN":
            case "OMTTokenType.NULL":
                return CONSTANT_VALUE_KEYS;
            case "OMTTokenType.INTEGER":
            case "OMTTokenType.DECIMAL":
                return NUMBER_KEYS;

            case "OMTTokenType.NAMESPACE":
            case "OMTTokenType.NAMESPACE_MEMBER":
                return PREFIX_KEYS;

            case "OMTTokenType.OWLPROPERTY":
                return OWLPROPERTY_KEYS;

            default:
                return EMPTY_KEYS;
        }
    }

}
