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
    public static final TextAttributesKey KEY =
            createTextAttributesKey("SIMPLE_KEY", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey VALUE =
            createTextAttributesKey("SIMPLE_VALUE", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey COMMENT =
            createTextAttributesKey("SIMPLE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BAD_CHARACTER =
            createTextAttributesKey("SIMPLE_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);
    public static final TextAttributesKey MODEL_ITEM_TYPE =
            createTextAttributesKey("MODEL_ITEM_TYPE", DefaultLanguageHighlighterColors.MARKUP_ENTITY);
    public static final TextAttributesKey PREFIX_IRI =
            createTextAttributesKey("PREFIX_IRI", DefaultLanguageHighlighterColors.METADATA);
    public static final TextAttributesKey VARIABLE =
            createTextAttributesKey("VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);

    private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{BAD_CHARACTER};
    private static final TextAttributesKey[] SEPARATOR_KEYS = new TextAttributesKey[]{SEPARATOR};
    private static final TextAttributesKey[] KEY_KEYS = new TextAttributesKey[]{KEY};
    private static final TextAttributesKey[] VALUE_KEYS = new TextAttributesKey[]{VALUE};
    private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
    private static final TextAttributesKey[] MODEL_ITEM_TYPES = new TextAttributesKey[]{MODEL_ITEM_TYPE};
    private static final TextAttributesKey[] PREFIX_IRI_TYPES = new TextAttributesKey[]{PREFIX_IRI};
    private static final TextAttributesKey[] VARIABLE_TYPES = new TextAttributesKey[]{VARIABLE};

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new OMTLexerAdapter();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (isSeperator(tokenType)) {
            return SEPARATOR_KEYS;
        } else if (tokenType.equals(OMTTypes.KEY)) {
            return KEY_KEYS;
        } else if (tokenType.equals(OMTTypes.VALUE)) {
            return VALUE_KEYS;
        } else if (tokenType.equals(OMTTypes.COMMENT)) {
            return COMMENT_KEYS;
        } else if (tokenType.equals(TokenType.BAD_CHARACTER)) {
            return BAD_CHAR_KEYS;
        } else if (tokenType.equals(OMTTypes.MODEL_ITEM_TYPE)) {
            return MODEL_ITEM_TYPES;
        } else if (tokenType.equals(OMTTypes.PREFIX_IRI)) {
            return PREFIX_IRI_TYPES;
        } else if (tokenType.equals(OMTTypes.VARIABLE)) {
            return VARIABLE_TYPES;
        } else {
            return EMPTY_KEYS;
        }
    }

    private boolean isSeperator(IElementType tokenType) {
        return tokenType == OMTTypes.COLON || tokenType == OMTTypes.EQUALS;
    }
}
