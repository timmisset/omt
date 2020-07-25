package com.misset.opp.omt;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.misset.opp.omt.psi.OMTTypes;
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
    public static final TextAttributesKey CONSTANT_VALUE =
            createTextAttributesKey("CONSTANT_VALUE", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey OPERATOR_OR_COMMAND =
            createTextAttributesKey("OPERATOR", DefaultLanguageHighlighterColors.IDENTIFIER);

    private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{BAD_CHARACTER};
    private static final TextAttributesKey[] COMMENTLINE_KEYS = new TextAttributesKey[]{COMMENTLINE};
    private static final TextAttributesKey[] COMMENTBLOCK_KEYS = new TextAttributesKey[]{COMMENTBLOCK};
    private static final TextAttributesKey[] CONSTANT_VALUE_KEYS = new TextAttributesKey[]{CONSTANT_VALUE};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
    private static final TextAttributesKey[] MODEL_ITEM_TYPES = new TextAttributesKey[]{MODEL_ITEM_TYPE};
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
        if (tokenType.equals(OMTTypes.END_OF_LINE_COMMENT)) {
            return COMMENTLINE_KEYS;
        } else if (tokenType.equals(OMTTypes.JAVA_DOCS)) {
            return COMMENTBLOCK_KEYS;
        } else if (tokenType.equals(TokenType.BAD_CHARACTER)) {
            return BAD_CHAR_KEYS;
        } else if (tokenType.equals(OMTTypes.MODEL_ITEM_TYPE)) {
            return MODEL_ITEM_TYPES;
        } else if (tokenType.equals(OMTTypes.VARIABLE_TYPE)) {
            return VARIABLE_TYPE_KEYS;
        } else if (tokenType.equals(OMTTypes.VARIABLE_NAME)) {
            return VARIABLE_TYPE_KEYS;
        } else if (tokenType.equals(OMTTypes.OPERATOR)) {
            return OPERATOR_OR_COMMAND_KEYS;
        } else if (tokenType.equals(OMTTypes.CONDITIONAL_OPERATOR)) {
            return OPERATOR_OR_COMMAND_KEYS;
        } else if (tokenType.equals(OMTTypes.COMMAND)) {
            return OPERATOR_OR_COMMAND_KEYS;
        } else if (tokenType.equals(OMTTypes.CONSTANT_VALUE)) {
            return CONSTANT_VALUE_KEYS;
        } else {
            return EMPTY_KEYS;
        }
    }

}
