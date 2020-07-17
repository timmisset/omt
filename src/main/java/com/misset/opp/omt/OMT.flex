// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.misset.opp.omt;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.misset.opp.omt.psi.OMTElementType;import com.misset.opp.omt.psi.OMTTokenType;import com.misset.opp.omt.psi.OMTTypes;
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
LATIN_EXT_A=                    [\u0100-\u017F] // Zie: http://en.wikipedia.org/wiki/Latin_script_in_Unicode
SYMBOL=                         ({ALPHA}|{DIGIT}|{LATIN_EXT_A}|[_@\-])+
SCHEME=                         {ALPHA}({ALPHA}|{DIGIT}|[+.-])*
IRI=                            "<"{SCHEME}":"({SYMBOL}|[?&#/+*.-])+">"
SCHEMALESS_IRI=                 "<"({SYMBOL}|[?&#/+*.-])+">"
END_OF_LINE_COMMENT=            ("#" | "\/\/")[^\r\n]*
JAVADOCS=                       \/\*\*([^]*)\*\/ // all between /** and */
NAME=                           {ALPHA}({ALPHA}|{DIGIT})*
CURIE=                          ({NAME})?":"{SYMBOL}

%{
/* globals to track current indentation */
int current_line_indent = 0;   /* indentation of the current line */
int indent_level = 0;          /* indentation level passed to the parser */
%}

%state INDENT
%sate DECLARE_VAR

%%
<YYINITIAL> {NEWLINE}                                                { current_line_indent = 0; yybegin(indent); return OMTTypes.NEW_LINE; }
<YYINITIAL> {WHITE_SPACE}                                            { return TokenType.WHITE_SPACE; }

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
            }
            yybegin(YYINITIAL);
         }

// COMMENT
<YYINITIAL>{JAVADOCS}                                                { return OMTTypes.JAVA_DOCS; }
<YYINITIAL>{END_OF_LINE_COMMENT}                                     { return OMTTypes.END_OF_LINE_COMMENT; }


<YYINITIAL>"$"{NAME}                                                 { return OMTTypes.VARIABLE_NAME; }
<YYINITIAL>{NAME}":"                                                 { return OMTTypes.PROPERTY; }
<YYINITIAL>"!"("Activity" | "Component" | "Procedure" | "StandAloneQuery") { return OMTTypes.MODEL_ITEM_TYPE; }

<YYINITIAL>"VAR"                                                     { yystart(DECLARE_VAR); return OMTTypes.DECLARE_VAR; }
<DECLARE_VAR>"$"{NAME}                                               { return OMTTypes.VARIABLE_NAME; }
<DECLARE_VAR>"("{CURIE}")"                                           { return OMTTypes.VARIABLE_TYPE; }
<DECLARE_VAR>"="                                                     { return OMTTypes.EQUALS; }
<DECLARE_VAR>"="                                                     { return OMTTypes.EQUALS; }

// SINGLE CHARACTERS
<YYINITIAL>"-"                                                       { return OMTTypes.LISTITEM_BULLET; }
<YYINITIAL>"|"                                                       { return OMTTypes.PIPE; }
<YYINITIAL>"="                                                       { return OMTTypes.EQUALS; }
[^]                                                                  { return TokenType.BAD_CHARACTER; }
