// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.misset.opp.omt;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.misset.opp.omt.psi.OMTTypes;
import com.intellij.psi.TokenType;

%%

%class OMTLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType

%eof{  return;
%eof}

CRLF=                           \R
WHITE_SPACE=                    [\f]
ALPHA=                          [A-Za-z]
UNDERSCORE=                     [_]
DIGIT=                          [0-9]
STRING=                         (\"[^\"]*\")|(\'[^\']*\')
LATIN_EXT_A=                    [\u0100-\u017F] // Zie: http://en.wikipedia.org/wiki/Latin_script_in_Unicode
SYMBOL=                         ({ALPHA}|{DIGIT}|{LATIN_EXT_A}|[_@\-])+
SCHEME=                         {ALPHA}({ALPHA}|{DIGIT}|[+.-])*
IRI=                            "<"{SCHEME}":"({SYMBOL}|[?&#/+*.-])+">"
SCHEMALESS_IRI=                 "<"({SYMBOL}|[?&#/+*.-])+">"
END_OF_LINE_COMMENT=            ("#" | "\/\/")[^\r\n]*
CURIE=                          (NAME)?":"{SYMBOL}
NAME=                           {ALPHA}({ALPHA}|{DIGIT})*
BNODE=                          "<"{UNDERSCORE}":"({SYMBOL}|[?&#/+*.-])+">"
//IRI_CHARACTER=[A-z0-9\/\.]
//QUERY_CHARACTER=[A-z0-9\/\.\$\"\'\=\>]
//PREFIX_IDTAG=[\#\/]
//BOOLEAN_VALUES=(false|true)
//NUMBERS=[0-9\.]



//
//// Quotes in strings kunnen met een \ ge-escapet worden
//STRING=                         (\"[^\"]*\")|(\'[^\']*\')    // Strings binnen ' of "
//TYPED_VALUE=                    {STRING}"^^"({IRI}|{CURIE})
//INTEGER=                        \-?([1-9][0-9]+|[0-9])
//DECIMAL=                        {INTEGER}\.[0-9]+
//
//VARIABLE=                       "$"{SYMBOL}

%{
/* globals to track current indentation */
int current_line_indent = 0;   /* indentation of the current line */
int indent_level = 0;          /* indentation level passed to the parser */
%}

/* start state for parsing the indentation */
%x indent
/* normal start state for everything else */

%%
<YYINITIAL> "\n"                                                     { current_line_indent = 0; yybegin(indent); }
<YYINITIAL> "DEFINE QUERY"                                           { yybegin(YYINITIAL); return OMTTypes.QUERY_DEFINE; }
<YYINITIAL> "VAR"                                                    { yybegin(YYINITIAL); return OMTTypes.DECLARE_VAR; }

<YYINITIAL> {STRING}                                                 { yybegin(YYINITIAL); return OMTTypes.STRING; }
<YYINITIAL> "true|false"                                             { yybegin(YYINITIAL); return OMTTypes.BOOLEAN; }
<YYINITIAL> {DIGIT}+                                                 { yybegin(YYINITIAL); return OMTTypes.NUMBER; }

<YYINITIAL> "!"{NAME}                                                { yybegin(YYINITIAL); return OMTTypes.MODEL_ITEM_TYPE; }
<YYINITIAL> {NAME}":"                                                { yybegin(YYINITIAL); return OMTTypes.BLOCK_ID; }
<YYINITIAL> {IRI}                                                    { yybegin(YYINITIAL); return OMTTypes.PREFIX_IRI; }
<YYINITIAL> {SYMBOL}                                                 { yybegin(YYINITIAL); return OMTTypes.SYMBOL; }
//<YYINITIAL> {BNODE}                                                  { yybegin(YYINITIAL); return OMTTypes.BNODE; }
<YYINITIAL> {CURIE}                                                  { yybegin(YYINITIAL); return OMTTypes.CURIE; }
//<YYINITIAL> {SCHEMALESS_IRI}                                         { yybegin(YYINITIAL); return OMTTypes.SCHEMALESS_IRI; }

//Single tokens
<YYINITIAL> "="                                                      { yybegin(YYINITIAL); return OMTTypes.EQUALS; }
<YYINITIAL> ":"                                                      { yybegin(YYINITIAL); return OMTTypes.COLON; }
<YYINITIAL> "- "                                                     { yybegin(YYINITIAL); return OMTTypes.LISTITEM_BULLET; }
<YYINITIAL> "("                                                      { yybegin(YYINITIAL); return OMTTypes.PARENTHESIS_OPEN; }
<YYINITIAL> ")"                                                      { yybegin(YYINITIAL); return OMTTypes.PARENTHESIS_CLOSED; }
<YYINITIAL> ","                                                      { yybegin(YYINITIAL); return OMTTypes.COMMA; }
<YYINITIAL> "/"                                                      { yybegin(YYINITIAL); return OMTTypes.SLASH; }
<YYINITIAL> ";"                                                      { yybegin(YYINITIAL); return OMTTypes.SEMICOLON; }
//<YYINITIAL> "^"                                                      { yybegin(YYINITIAL); return OMTTypes.REVERSEPATH; }
<YYINITIAL> "$"                                                      { yybegin(YYINITIAL); return OMTTypes.DOLLAR; }
<YYINITIAL> "|"                                                      { yybegin(YYINITIAL); return OMTTypes.PIPE; }
<YYINITIAL> "=>"                                                     { yybegin(YYINITIAL); return OMTTypes.LAMBDA_ARROW; }

<YYINITIAL> {CRLF}({CRLF}|{WHITE_SPACE})+                            { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
<YYINITIAL> {END_OF_LINE_COMMENT}                                    { yybegin(YYINITIAL); return OMTTypes.END_OF_LINE_COMMENT; }

<indent>" "                                                          { current_line_indent++; System.out.println("indent space, line index = " + current_line_indent); }
<indent>"\t"                                                         { current_line_indent = (current_line_indent + 8) & ~7; System.out.println("indent tab, line index = " + current_line_indent); }
<indent>"\n"                                                         { current_line_indent = 0; System.out.println("new line = " + current_line_indent); /*ignoring blank line */ }
<indent>.                                                            {
          if (current_line_indent > indent_level) {
               indent_level++;
               System.out.println("Found indent");
               return OMTTypes.INDENT;
            } else if (current_line_indent < indent_level) {
               indent_level--;
               System.out.println("Found dedent");
               return OMTTypes.DEDENT;
            } else {
               yybegin(YYINITIAL);
            }
         }
({CRLF}|{WHITE_SPACE})+                                              { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }

[^]                                                                  { return TokenType.BAD_CHARACTER; }
