package com.misset.opp.omt.style;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.misset.opp.omt.OMTLexerAdapter;
import com.misset.opp.omt.psi.OMTIgnored;
import com.misset.opp.omt.psi.OMTTypes;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class OMTSyntaxHighlighter extends SyntaxHighlighterBase {
    public static final TextAttributesKey COMMENTLINE =
            createTextAttributesKey("END_OF_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey COMMENTBLOCK =
            createTextAttributesKey("COMMENT_BLOCK", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey BAD_CHARACTER =
            createTextAttributesKey("SIMPLE_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);
    public static final TextAttributesKey ITEM_TYPE =
            createTextAttributesKey("ITEM_TYPE", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey CURIE_IRI =
            createTextAttributesKey("CURIE_IRI", DefaultLanguageHighlighterColors.CLASS_NAME);
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
    private static final TextAttributesKey[] CURIE_IRI_KEYS = new TextAttributesKey[]{CURIE_IRI};

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new OMTLexerAdapter("Highlight Lexer");
    }

    private static final TokenSet COMMENTLINE_TOKENS = TokenSet.create(OMTIgnored.END_OF_LINE_COMMENT);
    private static final TokenSet COMMENTBLOCK_TOKENS = TokenSet.create(OMTTypes.JAVADOCS_START, OMTTypes.JAVADOCS_CONTENT, OMTTypes.JAVADOCS_END);
    private static final TokenSet BAD_CHAR_TOKENS = TokenSet.create(TokenType.BAD_CHARACTER);
    private static final TokenSet TYPE_TOKENS = TokenSet.create(OMTTypes.MODEL_ITEM_TYPE);
    private static final TokenSet VARIABLE_TOKENS = TokenSet.create(OMTTypes.VARIABLE_NAME, OMTTypes.IGNORE_VARIABLE_NAME);
    private static final TokenSet GLOBAL_VARIABLE_TOKENS = TokenSet.create(OMTTypes.GLOBAL_VARIABLE_NAME);
    private static final TokenSet OPERATOR_OR_COMMAND_TOKENS = TokenSet.create(OMTTypes.OPERATOR,
            OMTTypes.COMMAND,
            OMTTypes.CONDITIONAL_OPERATOR,
            OMTTypes.IF_OPERATOR,
            OMTTypes.ELSE_OPERATOR,
            OMTTypes.CHOOSE_OPERATOR,
            OMTTypes.WHEN_OPERATOR,
            OMTTypes.OTHERWISE_OPERATOR,
            OMTTypes.END_OPERATOR,
            OMTTypes.RETURN_OPERATOR);
    private static final TokenSet STRING_TOKENS = TokenSet.create(OMTTypes.STRING);
    private static final TokenSet CONSTANT_VALUE_TOKENS = TokenSet.create(OMTTypes.BOOLEAN, OMTTypes.NULL);
    private static final TokenSet NUMBER_TOKENS = TokenSet.create(OMTTypes.INTEGER, OMTTypes.DECIMAL);
    private static final TokenSet CURIE_IRI_TOKENS = TokenSet.create(OMTTypes.NAMESPACE, OMTTypes.NAMESPACE_MEMBER, OMTTypes.IRI, OMTTypes.OWLPROPERTY);

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (COMMENTLINE_TOKENS.contains(tokenType)) {
            return COMMENTLINE_KEYS;
        }
        if (COMMENTBLOCK_TOKENS.contains(tokenType)) {
            return COMMENTBLOCK_KEYS;
        }
        if (BAD_CHAR_TOKENS.contains(tokenType)) {
            return BAD_CHAR_KEYS;
        }
        if (TYPE_TOKENS.contains(tokenType)) {
            return TYPE_KEYS;
        }
        if (VARIABLE_TOKENS.contains(tokenType)) {
            return VARIABLE_TYPE_KEYS;
        }
        if (GLOBAL_VARIABLE_TOKENS.contains(tokenType)) {
            return GLOBAL_VARIABLE_KEYS;
        }
        if (OPERATOR_OR_COMMAND_TOKENS.contains(tokenType)) {
            return OPERATOR_OR_COMMAND_KEYS;
        }
        if (STRING_TOKENS.contains(tokenType)) {
            return STRING_KEYS;
        }
        if (CONSTANT_VALUE_TOKENS.contains(tokenType)) {
            return CONSTANT_VALUE_KEYS;
        }
        if (NUMBER_TOKENS.contains(tokenType)) {
            return NUMBER_KEYS;
        }
        if (CURIE_IRI_TOKENS.contains(tokenType)) {
            return CURIE_IRI_KEYS;
        }
        return EMPTY_KEYS;
    }

}
