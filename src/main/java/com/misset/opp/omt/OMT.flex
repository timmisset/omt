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
%column

%eof{  return;
%eof}

CRLF=\R
WHITE_SPACE=[\ \n\t\f]
FIRST_VALUE_CHARACTER=[^ \$\!\n\f\\] | "\\"{CRLF} | "\\".
VALUE_CHARACTER=[A-z0-9] | "\\"{CRLF} | "\\".
END_OF_LINE_COMMENT=("#")[^\r\n]*
FIRST_KEY_CHARACTER=[A-z]
KEY_CHARACTER=[A-z0-9] | "\\ "
IRI_CHARACTER=[A-z0-9\/\.]
QUERY_CHARACTER=[A-z0-9\/\.\$\"\'\=\>]
PREFIX_IDTAG=[\#\/]
BOOLEAN_VALUES=(false|true)
NUMBERS=[0-9\.]
STRING=(\"[^\"]*\")|(\'[^\']*\')
//ALPHA=                          [A-Za-z]
//UNDERSCORE=                     [_]
//DIGIT=                          [0-9]
//LATIN_EXT_A=                    [\u0100-\u017F] // Zie: http://en.wikipedia.org/wiki/Latin_script_in_Unicode
//SYMBOL=                         ({ALPHA}|{DIGIT}|{LATIN_EXT_A}|[_@\-])+
//SCHEME=                         {ALPHA}({ALPHA}|{DIGIT}|[+.-])*
//IRI=                            "<"{SCHEME}":"({SYMBOL}|[?&#/+*.-])+">"
//SCHEMALESS_IRI=                 "<"({SYMBOL}|[?&#/+*.-])+">"
//BNODE=                          "<"{UNDERSCORE}":"(?:{SYMBOL}|[?&#/+*.-])+">"
//CURIE_PREFIX=                   ({ALPHA}({ALPHA}|{DIGIT})*)?":"
//CURIE=                          {CURIE_PREFIX}{SYMBOL}
//
//// Quotes in strings kunnen met een \ ge-escapet worden
//STRING=                         (\"[^\"]*\")|(\'[^\']*\')    // Strings binnen ' of "
//TYPED_VALUE=                    {STRING}"^^"({IRI}|{CURIE})
//INTEGER=                        \-?([1-9][0-9]+|[0-9])
//DECIMAL=                        {INTEGER}\.[0-9]+
//
//VARIABLE=                       "$"{SYMBOL}


%%

<YYINITIAL> {END_OF_LINE_COMMENT}                                    { yybegin(YYINITIAL); return OMTTypes.COMMENT; }

<YYINITIAL> "="                                                      { yybegin(YYINITIAL); return OMTTypes.EQUALS; }
<YYINITIAL> ":"                                                      { yybegin(YYINITIAL); return OMTTypes.COLON; }
<YYINITIAL> "-"                                                      { yybegin(YYINITIAL); return OMTTypes.LISTITEM_BULLET; }
<YYINITIAL> "("                                                      { yybegin(YYINITIAL); return OMTTypes.PARENTHESIS_OPEN; }
<YYINITIAL> ")"                                                      { yybegin(YYINITIAL); return OMTTypes.PARENTHESIS_CLOSED; }
<YYINITIAL> ","                                                      { yybegin(YYINITIAL); return OMTTypes.COMMA; }
<YYINITIAL> "/"                                                      { yybegin(YYINITIAL); return OMTTypes.SLASH; }
<YYINITIAL> ";"                                                      { yybegin(YYINITIAL); return OMTTypes.SEMICOLON; }
//<YYINITIAL> "^"                                                      { yybegin(YYINITIAL); return OMTTypes.REVERSEPATH; }
<YYINITIAL> "$"                                                      { yybegin(YYINITIAL); return OMTTypes.DOLLAR; }
<WAITING_VALUE> "$"                                                  { yybegin(WAITING_VALUE); return OMTTypes.DOLLAR; }
<YYINITIAL> "|"                                                      { yybegin(YYINITIAL); return OMTTypes.PIPE; }

<YYINITIAL> "DEFINE QUERY"                                           { yybegin(WAITING_VALUE); return OMTTypes.QUERY_DEFINE; }
<YYINITIAL> "VAR"                                                    { yybegin(WAITING_VALUE); return OMTTypes.DECLARE_VAR; }
<WAITING_VALUE> "=>"                                                 { yybegin(WAITING_VALUE); return OMTTypes.QUERY_SEPARATOR; }
<YYINITIAL> "=>"                                                     { yybegin(YYINITIAL); return OMTTypes.QUERY_SEPARATOR; }

<WAITING_VALUE> {CRLF}({CRLF}|{WHITE_SPACE})+                        { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }

<WAITING_VALUE> {WHITE_SPACE}+                                       { yybegin(WAITING_VALUE); return TokenType.WHITE_SPACE; }

<WAITING_VALUE> {FIRST_VALUE_CHARACTER}{VALUE_CHARACTER}*            { yybegin(YYINITIAL); return OMTTypes.VALUE; }
<YYINITIAL> {FIRST_KEY_CHARACTER}{KEY_CHARACTER}*                    { yybegin(YYINITIAL); return OMTTypes.KEY; }
<YYINITIAL> {STRING}                                                 { yybegin(YYINITIAL); return OMTTypes.STRING; }
<YYINITIAL> {BOOLEAN_VALUES}                                         { yybegin(YYINITIAL); return OMTTypes.BOOLEAN; }
<YYINITIAL> {NUMBERS}+                                               { yybegin(YYINITIAL); return OMTTypes.NUMBER; }
<YYINITIAL> "!"{VALUE_CHARACTER}+                                    { yybegin(YYINITIAL); return OMTTypes.MODEL_ITEM_TYPE; }

<YYINITIAL> "<http://"{IRI_CHARACTER}*{PREFIX_IDTAG}">"              { yybegin(YYINITIAL); return OMTTypes.PREFIX_IRI; }

({CRLF}|{WHITE_SPACE})+                                              { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }

[^]                                                                  { return TokenType.BAD_CHARACTER; }
