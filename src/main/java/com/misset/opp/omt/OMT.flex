// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.misset.opp.omt;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.misset.opp.omt.psi.OMTTypes;
import com.intellij.psi.TokenType;import com.sun.jna.platform.win32.WinNT;import jdk.nashorn.internal.parser.Token;

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
NEWLINE=                        (\r\n) | (\r) | (\n)
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
IMPORT_PATH=                    (\.{1,2}[^:]*:)

%{
/* globals to track current indentation */
int current_line_indent = 0;   /* indentation of the current line */
int indent_level = 0;          /* indentation level passed to the parser */

IElementType firstAfterIndentation() {
    yypushback(1);
    if (current_line_indent > indent_level) {
        indent_level++;
        return OMTTypes.INDENT;
    } else if (current_line_indent < indent_level) {
        indent_level--;
        return OMTTypes.DEDENT;
    } else {
        yybegin(YYINITIAL);
        return TokenType.WHITE_SPACE;
    }
}
IElementType finishIdentation() {
    if(inODTBlock) {
        inODTBlock = false;
        return OMTTypes.ODT_END;
    }
    if(indent_level > 0) {
        indent_level--;
        return OMTTypes.DEDENT;
    }
    return null;
}
void yypushback(int number, String id) {
    yypushback(number);
}
int previousState;
boolean startOfLine;
boolean inODTBlock = false;
boolean declaringVariable = false;
boolean inCurieStatement = false;
void setState(int state) {
    previousState = state != yystate() ? yystate() : previousState;
    inODTBlock = state == ODT;
    yybegin(state);
}
IElementType returnElement(IElementType element) {
    startOfLine = element == OMTTypes.NEW_LINE;
    return element;
}
IElementType returnVariable() {
    return declaringVariable ? OMTTypes.DECLARED_VARIABLE_NAME : OMTTypes.VARIABLE_NAME;
}
%}

%state YAML_SCALAR
%state YAML_SEQUENCE

%state INDENT
%state ODT
%state CURIE

%%
// when in initial and newline, start the indent counting
<YYINITIAL, YAML_SCALAR, YAML_SEQUENCE, INDENT> {NEWLINE}           { current_line_indent = 0; setState(INDENT); return returnElement(OMTTypes.NEW_LINE); }
<YYINITIAL, YAML_SCALAR, YAML_SEQUENCE> {WHITE_SPACE}               { return TokenType.WHITE_SPACE; } // capture all whitespace
// INDENTATION
// Required for YAML like grouping of blocks based on indents
<INDENT>(\ {4})                                                      { current_line_indent++; }
<INDENT>.                                                            { return firstAfterIndentation(); }

<<EOF>>                                                              { return finishIdentation(); }
// a block starts with a property name, defined as NAME:
// this block can contain a SCALAR BLOCK, a MAPPING BLOCK or a SEQUENCE BLOCK
<YYINITIAL> {
    // specific OMT Blocks, used by the grammar part
    "prefixes:"                                               { setState(YAML_SCALAR); return returnElement(OMTTypes.PREFIX_BLOCK_START); }
    "commands:"                                               { setState(YAML_SCALAR); return returnElement(OMTTypes.COMMAND_BLOCK_START); }
    "queries:"                                                { setState(YAML_SCALAR); return returnElement(OMTTypes.QUERY_BLOCK_START); }
    "import:"                                                 { setState(YAML_SCALAR); return returnElement(OMTTypes.IMPORT_START); }
    "model:"                                                  { setState(YAML_SCALAR); return returnElement(OMTTypes.MODEL_BLOCK_START); }
    "moduleName:"                                             { setState(YAML_SCALAR); return returnElement(OMTTypes.MODULE_NAME_START); }
    "export:"                                                 { setState(YAML_SCALAR); return returnElement(OMTTypes.EXPORT_START); }

    {IMPORT_PATH}                                             { setState(YAML_SCALAR); return returnElement(OMTTypes.IMPORT_PATH); }

    "params:" | "variables:"                                  { setState(YAML_SCALAR); declaringVariable = true; return returnElement(OMTTypes.PROPERTY); }
    // the initial state is only used for the key parts and can result in a switch to the scalar or sequence node
    // when a new line is reached in the state of initial (consecutive new lines), scalar or mapping, the state is always returned
    // to initial. This way, the initial state will detect the "-" indicator of the sequence items
    {NAME}":"                                                 { setState(YAML_SCALAR); declaringVariable = false; return returnElement(OMTTypes.PROPERTY); }
    "$"{NAME}                                                 { setState(ODT); return returnElement(OMTTypes.VARIABLE_NAME); }
    "-"                                                       { setState(YAML_SEQUENCE); return returnElement(OMTTypes.SEQUENCE_BULLET); }
}

// YAML SEQUENCE:
// YAML: A sequence node is a series of zero or more nodes, can contain the same node multiple times or even itself
// OMT: We can expect variables, imports etc
<YAML_SEQUENCE> {
    // Variables
    "$"{NAME}                                                        { setState(ODT); return returnVariable();  }
    "/"                                                              { yypushback(1, "YAML_SCALAR"); setState(ODT); }
}

// YAML SCALAR
<YAML_SCALAR> {
    {IRI}                                                            { return returnElement(OMTTypes.IRI); }
    "!"{NAME}                                                        { return returnElement(OMTTypes.MODEL_ITEM_TYPE); }
    "$"{NAME}                                                        { setState(ODT); return returnVariable(); }
    "@"{NAME}                                                        { setState(ODT); return returnElement(OMTTypes.COMMAND); }
    {NAME}                                                           { setState(ODT); return returnElement(OMTTypes.OPERATOR); }
    // a YAML_SCALAR can start with any value, however once any of these prefixes is introduced it should switch to an ODT state
    // this will support things like:
    //
    // queryWatcher:
    //      query: $someVariable / pol:property
    // OR   query: /pol:className / ^rdf:type
    //
    // in these cases, a multiline entry is also automatically supported and it will stay collecting data until
    // next key: scalar or key: sequence block appears
    "/" | "|"                                                       { yypushback(1, "YAML_SCALAR"); setState(ODT); }
}

// ODT BLOCK
<ODT> {
    // although identation and whitespace is ignored in the ODT blocks, it's used to pushback the right amount of tokens
    // to the stream when the end of the ODT block is recognized
    {NEWLINE}                                                       {
          startOfLine = true;
          current_line_indent = 0; return returnElement(OMTTypes.NEW_LINE); }
    (\ {4})                                                         {
          current_line_indent++; }
    {WHITE_SPACE}                                                   { return TokenType.WHITE_SPACE; }

    // exit code block
    // when a code block starts, the recorded indent level is of the key that preceeded the scalar or pipe
    // therefor, whenever something is present which has an indentation level lower or equal to this key
    // it can be safely assumed that the code block is finished. Or needs to adjust indentation
    {NAME}":" {
              if(startOfLine && current_line_indent <= indent_level) {
                    // exit code block
                    yypushback(yylength(), "ODT_END");
                    setState(INDENT);
                    declaringVariable = false;
                    return returnElement(OMTTypes.ODT_END);
              } else {
                    return returnElement(OMTTypes.NAMESPACE_PREFIX);
              }
          }

    "- " {
            setState(INDENT);
            yypushback(2);
            return returnElement(OMTTypes.ODT_END);
      }
    // statements that identify specific elements within the ODT language
    "DEFINE"                                                        { declaringVariable = true; return returnElement(OMTTypes.DEFINE_START); }
    "QUERY"                                                         { return returnElement(OMTTypes.DEFINE_QUERY); }
    "COMMAND"                                                       { return returnElement(OMTTypes.DEFINE_COMMAND); }
    "VAR"                                                           { declaringVariable = true; return returnElement(OMTTypes.DECLARE_VAR); }
    "PREFIX"                                                        { return returnElement(OMTTypes.PREFIX_DEFINE_START); }
    ";"                                                             { declaringVariable = false; return returnElement(OMTTypes.SEMICOLON); }
    "$"{NAME}                                                       { return returnVariable(); }
    "@"{NAME}                                                       { return returnElement(OMTTypes.COMMAND); }
    "!"{NAME}                                                       { return returnElement(OMTTypes.FLAG); }

    "/"{CURIE}                                                      {
          yypushback(yylength() - 1);
          return returnElement(OMTTypes.CURIE_CONSTANT_ELEMENT_PREFIX);
      }
    {CURIE}                                                         {
          setState(CURIE);
          yypushback(yylength() - yytext().toString().indexOf(":"));
          return returnElement(OMTTypes.NAMESPACE); }

    // the lambda is used for assigning the actual query/command block to it's constructor
    // and to assign a path to case condition
    "=>"                                                            { declaringVariable = false; return returnElement(OMTTypes.LAMBDA); }

    // ODT operators
    // certain operators are used for assertions and should be recognized. They can be used within querysteps (grammar part)
    // when making filters or other boolean assertions
    "AND" | "OR" | "NOT" | "IN" | ">=" | "<=" | "==" | ">" | "<"    { return returnElement(OMTTypes.CONDITIONAL_OPERATOR); }
    "IF"                                                            { return returnElement(OMTTypes.IF_OPERATOR); }
    "ELSE"                                                          { return returnElement(OMTTypes.ELSE_OPERATOR); }
    "RETURN"                                                        { return returnElement(OMTTypes.RETURN_OPERATOR); }
    {NAME}                                                          { return returnElement(OMTTypes.OPERATOR); }

    {IRI}                                                            { return returnElement(OMTTypes.IRI); }
    // start code block:
    "|"                                                             { return returnElement(OMTTypes.PIPE); }
}
<CURIE> {
    ":"                                                             { return OMTTypes.COLON; }
    {NAME}                                                          {
                                                                          setState(ODT);
                                                                          return OMTTypes.NAMESPACE_MEMBER;
      }
}
// Common tokens
<YYINITIAL, YAML_SCALAR, YAML_SEQUENCE, ODT> {
    {JAVADOCS}                                                      { return returnElement(OMTTypes.JAVA_DOCS); }
    {END_OF_LINE_COMMENT}                                           { return returnElement(OMTTypes.END_OF_LINE_COMMENT); }
    {STRING}                                                        { return returnElement(OMTTypes.STRING); }
    {INTEGER}                                                       { return returnElement(OMTTypes.INTEGER); }
    {DECIMAL}                                                       { return returnElement(OMTTypes.DECIMAL); }
    {TYPED_VALUE}                                                   { return returnElement(OMTTypes.TYPED_VALUE); }
    {BOOLEAN}                                                       { return returnElement(OMTTypes.BOOLEAN); }
    {NULL}                                                          { return returnElement(OMTTypes.NULL); }

    // all single characters that are resolved to special characters:
    // todo: some should be made more specifically available based on their lexer state
    ":"                                                             { return returnElement(OMTTypes.COLON); }
    "="                                                             { return returnElement(OMTTypes.EQUALS); }
    ","                                                             { return returnElement(OMTTypes.COMMA); }
    ";"                                                             { return returnElement(OMTTypes.SEMICOLON); }
    "{"                                                             { return returnElement(OMTTypes.CURLY_OPEN); }
    "}"                                                             { return returnElement(OMTTypes.CURLY_CLOSED); }
    "/"                                                             { return returnElement(OMTTypes.FORWARD_SLASH); }
    "^"                                                             { return returnElement(OMTTypes.CARAT); }
    "[]"                                                            { return returnElement(OMTTypes.EMPTY_ARRAY); }
    "["                                                             { return returnElement(OMTTypes.BRACKET_OPEN); }
    "]"                                                             { return returnElement(OMTTypes.BRACKET_CLOSED); }
    "+"                                                             { return returnElement(OMTTypes.PLUS); }
    "("                                                             { return returnElement(OMTTypes.PARENTHESES_OPEN); }
    ")"                                                             { return returnElement(OMTTypes.PARENTHESES_CLOSE); }
    "\."                                                            { return returnElement(OMTTypes.DOT); }
    "+="                                                            { return returnElement(OMTTypes.ADD); }
    "-="                                                            { return returnElement(OMTTypes.REMOVE); }
}
<YYINITIAL, YAML_SEQUENCE>{NAME}                                     { return returnElement(OMTTypes.NAME); }
[^]                                                                  { return returnElement(TokenType.BAD_CHARACTER); }
