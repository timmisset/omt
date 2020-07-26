// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.misset.opp.omt;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.misset.opp.omt.psi.OMTElementType;import com.misset.opp.omt.psi.OMTModelItem;import com.misset.opp.omt.psi.OMTTokenType;import com.misset.opp.omt.psi.OMTTypes;
import com.intellij.psi.TokenType;

%%

%class OMTLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType

%eof{  return;
%eof}

WHITE_SPACE=                    [\ \f]
ALPHA=                          [A-Za-z]
NEWLINE=                        [\r\n]
UNDERSCORE=                     [_]
DIGIT=                          [0-9]
STRING=                         (\"[^\"]*\")|(\'[^\']*\')
INTEGER=                        \-?([1-9][0-9]+|[0-9])
DECIMAL=                        {INTEGER}\.[0-9]+
BOOLEAN=                        "true"|"false"
NULL=                           "null"

LATIN_EXT_A=                    [\u0100-\u017F] // Zie: http://en.wikipedia.org/wiki/Latin_script_in_Unicode
SYMBOL=                         ({ALPHA}|{DIGIT}|{LATIN_EXT_A}|[_@\-])+
SCHEME=                         {ALPHA}({ALPHA}|{DIGIT}|[+.-])*
IRI=                            "<"{SCHEME}":"({SYMBOL}|[?&#/+*.-])+">"
SCHEMALESS_IRI=                 "<"({SYMBOL}|[?&#/+*.-])+">"
END_OF_LINE_COMMENT=            ("#" | "\/\/")[^\r\n]*
JAVADOCS=                       \/\*\*([^]*)\*\/ // all between /** and */
NAME=                           {ALPHA}({ALPHA}|{DIGIT}|{UNDERSCORE})*
CURIE=                          ({NAME})?":"{SYMBOL}
CURIE_PREFIX=                   ({ALPHA}({ALPHA}|{DIGIT})*)?":"
TYPED_VALUE=                    {STRING}"^^"({IRI}|{CURIE})

%{
/* globals to track current indentation */
int current_line_indent = 0;   /* indentation of the current line */
int indent_level = 0;          /* indentation level passed to the parser */
%}

%state INDENT
%state DECLARE_VAR
%state DEFINE

%%
<YYINITIAL> {NEWLINE}                                                { current_line_indent = 0; yybegin(INDENT); return OMTTypes.NEW_LINE; }
<YYINITIAL> {WHITE_SPACE}                                            { return TokenType.WHITE_SPACE; } // ignore whitespace
// INDENTATION
// Required for YAML like grouping of blocks based on indents
<INDENT>(\ {4})                                                      { current_line_indent++; }
<INDENT>"\n"                                                         { current_line_indent = 0; /*ignoring blank line */ }
<INDENT>.                                                            {
                                                                        yypushback(1);
                                                                        if (current_line_indent > indent_level) {
                                                                            indent_level++;
                                                                            return OMTTypes.INDENT;
                                                                        } else if (current_line_indent < indent_level) {
                                                                            indent_level--;
                                                                            return OMTTypes.DEDENT;
                                                                        } else {
                                                                            yybegin(YYINITIAL);
                                                                            if (indent_level > 0) { return TokenType.WHITE_SPACE; }
                                                                        }
                                                                     }
<INDENT><<EOF>>                                                      {
                                                                        // will resolve all remaining dedents
                                                                        if(indent_level > 0) {
                                                                            indent_level -= 1;
                                                                            return OMTTypes.DEDENT;
                                                                        }
                                                                        else {
                                                                            yybegin(YYINITIAL);
                                                                        }
                                                                      }

// DEFINE QUERY / COMMAND
<YYINITIAL>"DEFINE"                                                  { yybegin(DEFINE); return OMTTypes.DEFINE_START; }
<DEFINE>"QUERY"                                                      { return OMTTypes.DEFINE_QUERY; }
<DEFINE>"COMMAND"                                                    { return OMTTypes.DEFINE_COMMAND; }
<DEFINE>{SYMBOL}                                                     { return OMTTypes.NAME; }
<DEFINE>"("                                                          { return OMTTypes.PARENTHESES_OPEN; }
<DEFINE>"$"{NAME}                                                    { return OMTTypes.VARIABLE_NAME; }
<DEFINE>","                                                          { return OMTTypes.COMMA; }
<DEFINE>")"                                                          { return OMTTypes.PARENTHESES_CLOSE; }
<DEFINE>"=>"                                                         { yybegin(YYINITIAL); return OMTTypes.LAMBDA; }
<DEFINE>{NEWLINE}{NEWLINE}                                           { current_line_indent = 0; yybegin(INDENT); return OMTTypes.NEW_LINE; }
<DEFINE>{WHITE_SPACE}                                                { return TokenType.WHITE_SPACE; } // ignore whitespace

<YYINITIAL>{IRI}                                                     { return OMTTypes.IRI; }

// SPECIFIC BLOCKS
<YYINITIAL>"prefixes:"                                               { return OMTTypes.PREFIX_BLOCK_START; }
<YYINITIAL>"commands:"                                               { return OMTTypes.COMMAND_BLOCK_START; }
<YYINITIAL>"queries:"                                                { return OMTTypes.QUERY_BLOCK_START; }
<YYINITIAL>"import:"                                                 { return OMTTypes.IMPORT_START; }
<YYINITIAL>"model:"                                                  { return OMTTypes.MODEL_BLOCK_START; }
<YYINITIAL>"!"("Activity" | "Component" | "Procedure" | "StandAloneQuery") { return OMTTypes.MODEL_ITEM_TYPE; }

// COMMENT
<YYINITIAL>{JAVADOCS}                                                { return OMTTypes.JAVA_DOCS; }
<YYINITIAL>{END_OF_LINE_COMMENT}                                     { return OMTTypes.END_OF_LINE_COMMENT; }

<YYINITIAL>{NAME}":"                                                 { return OMTTypes.PROPERTY; }
<YYINITIAL>"@"{NAME}                                                 { return OMTTypes.COMMAND; }

// VALUES
<YYINITIAL>{STRING}                                                   { return OMTTypes.STRING; }
<YYINITIAL>{INTEGER}                                                  { return OMTTypes.INTEGER; }
<YYINITIAL>{DECIMAL}                                                  { return OMTTypes.DECIMAL; }
<YYINITIAL>{TYPED_VALUE}                                              { return OMTTypes.TYPED_VALUE; }
<YYINITIAL>{BOOLEAN}                                                  { return OMTTypes.BOOLEAN; }
<YYINITIAL>{NULL}                                                     { return OMTTypes.NULL; }

// ODT
// DECLARE VARIABLE BLOCK
<YYINITIAL>"VAR"                                                     { yybegin(DECLARE_VAR); return OMTTypes.DECLARE_VAR; }
<YYINITIAL>"$"{NAME}                                                 { yybegin(DECLARE_VAR); return OMTTypes.VARIABLE_NAME; }
<DECLARE_VAR>"$"{NAME}                                               { return OMTTypes.VARIABLE_NAME; }
<DECLARE_VAR>"("{CURIE}")"                                           { return OMTTypes.VARIABLE_TYPE; }
<DECLARE_VAR>{NEWLINE}                                               { current_line_indent = 0; yybegin(INDENT); return OMTTypes.NEW_LINE; }
<DECLARE_VAR> {WHITE_SPACE}                                          { return TokenType.WHITE_SPACE; }

<YYINITIAL>"PREFIX"                                                  { return OMTTypes.PREFIX_DEFINE_START; }
<YYINITIAL>"AND" | "OR" | "NOT" | "IN" | ">=" | "<=" | "=="          { return OMTTypes.CONDITIONAL_OPERATOR; }

// anything else can be defined as an operator
<YYINITIAL>{NAME}                                                    { return OMTTypes.OPERATOR; }

<YYINITIAL>"/"{CURIE}                                                { return OMTTypes.CURIE_CONSTANT; }
<YYINITIAL>{CURIE}                                                   { return OMTTypes.CURIE; }

// SINGLE CHARACTERS
// Some tokens are accessible from the DECLARE_VAR state after variables are processed
// When this happens the lexer state must be reset to YYINITIAL. In case of another variable it will set itself to DECLARE_VAR once more
<YYINITIAL>"-"                                                       { return OMTTypes.LISTITEM_BULLET; }
<YYINITIAL>"|"                                                       { return OMTTypes.PIPE; }
<YYINITIAL>":"                                                       { return OMTTypes.COLON; }
<YYINITIAL, DECLARE_VAR>"="                                          { yybegin(YYINITIAL); return OMTTypes.EQUALS; }
<YYINITIAL, DECLARE_VAR>","                                          { yybegin(YYINITIAL); return OMTTypes.COMMA; }
<YYINITIAL, DECLARE_VAR>";"                                          { yybegin(YYINITIAL); return OMTTypes.SEMICOLON; }
<YYINITIAL>"{"                                                       { return OMTTypes.CURLY_OPEN; }
<YYINITIAL>"}"                                                       { return OMTTypes.CURLY_CLOSED; }
<YYINITIAL, DECLARE_VAR>"/"                                          { yybegin(YYINITIAL); return OMTTypes.FORWARD_SLASH; }
<YYINITIAL>"^"                                                       { return OMTTypes.CARAT; }
<YYINITIAL>"["                                                       { return OMTTypes.BRACKET_OPEN; }
<YYINITIAL>"]"                                                       { return OMTTypes.BRACKET_CLOSED; }
<YYINITIAL>"+"                                                       { return OMTTypes.PLUS; }
<YYINITIAL>"("                                                       { return OMTTypes.PARENTHESES_OPEN; }
<YYINITIAL, DECLARE_VAR>")"                                          { return OMTTypes.PARENTHESES_CLOSE; }
<YYINITIAL>"."                                                       { return OMTTypes.DOT; }

[^]                                                                  { return TokenType.BAD_CHARACTER; }
