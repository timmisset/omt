package com.misset.opp.omt;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.*;
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
    public static final TextAttributesKey MODEL_ITEM_TYPE =
            createTextAttributesKey("MODEL_ITEM_TYPE", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey VARIABLE_TYPE =
            createTextAttributesKey("CURIE", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey VARIABLE =
            createTextAttributesKey("VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey STRING =
            createTextAttributesKey("STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey NUMBER =
            createTextAttributesKey("NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey CONSTANT =
            createTextAttributesKey("CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey OPERATOR_OR_COMMAND =
            createTextAttributesKey("OPERATOR", DefaultLanguageHighlighterColors.IDENTIFIER);

    private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{BAD_CHARACTER};
    private static final TextAttributesKey[] COMMENTLINE_KEYS = new TextAttributesKey[]{COMMENTLINE};
    private static final TextAttributesKey[] COMMENTBLOCK_KEYS = new TextAttributesKey[]{COMMENTBLOCK};
    private static final TextAttributesKey[] STRING_KEYS = new TextAttributesKey[]{STRING};
    private static final TextAttributesKey[] NUMBER_KEYS = new TextAttributesKey[]{NUMBER};
    private static final TextAttributesKey[] CONSTANT_VALUE_KEYS = new TextAttributesKey[]{CONSTANT};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
    private static final TextAttributesKey[] MODEL_ITEM_TYPE_KEYS = new TextAttributesKey[]{MODEL_ITEM_TYPE};
    private static final TextAttributesKey[] VARIABLE_TYPE_KEYS = new TextAttributesKey[]{VARIABLE};
    private static final TextAttributesKey[] OPERATOR_OR_COMMAND_KEYS = new TextAttributesKey[]{OPERATOR_OR_COMMAND};

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
            case "OMTTokenType.JAVA_DOCS":
                return COMMENTLINE_KEYS;
            case "BAD_CHARACTER":
                return BAD_CHAR_KEYS;
            case "OMTTokenType.MODEL_ITEM_TYPE":
                return MODEL_ITEM_TYPE_KEYS;
            case "OMTTokenType.PARAMETER":
            case "OMTTokenType.VARIABLE_NAME":
                return VARIABLE_TYPE_KEYS;
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

            default: return EMPTY_KEYS;
        }
    }

}
