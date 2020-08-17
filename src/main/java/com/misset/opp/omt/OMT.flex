// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.misset.opp.omt;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.misset.opp.omt.psi.OMTTokenType;import com.misset.opp.omt.psi.OMTTypes;
import com.intellij.psi.TokenType;import jdk.nashorn.internal.parser.Token;
import java.util.List;
import java.util.ArrayList;import java.util.stream.Collectors;

%%

%class OMTLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%column

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
GLOBAL_VARIABLE=                "$username" | "$medewerkerGraph" | "$offline" | "$mockValue[0-9]*"

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
List<Integer> blockIndentation = new ArrayList<>();
List<Integer> blockStates = new ArrayList<>();
int current_line_indent = 0;   /* indentation of the current line */
int indent_level = 0;          /* indentation level passed to the parser */
int yycolumn;
int stateBeforeIndent = -1;
IElementType lastDedentToken;
IElementType lastIndentToken;
IElementType returnDedent() {
    if(lastDedentToken == null || lastDedentToken == OMTTypes.DEDENT2) {
        lastDedentToken = OMTTypes.DEDENT;
    } else {
        lastDedentToken = OMTTypes.DEDENT2;
    }
    return returnElement(lastDedentToken);
}
IElementType returnIndent() {
    if(lastIndentToken == null || lastIndentToken == OMTTypes.INDENT2) {
        lastIndentToken = OMTTypes.INDENT;
    } else {
        lastIndentToken = OMTTypes.INDENT2;
    }
    return returnElement(lastIndentToken);
}
int getCurrentIndentLevel() { return blockIndentation.isEmpty() ? 0 : blockIndentation.get(blockIndentation.size() - 1); }
int getStateAtIndent() { return blockStates.isEmpty() ? YYINITIAL : blockStates.get(blockStates.size() - 1); }
void log(String message) {
//    System.out.println(message);
}
void logStates(String prefixMessage) {
    log(prefixMessage + ": " + blockIndentation + blockStates.stream().map(integer -> getStateName(integer)).collect(Collectors.toList()));
}
void removeLastIndentInfo() { removeLastIndentInfo(true); }
void removeLastIndentInfo(boolean log) {
    if(!blockStates.isEmpty()) { blockStates.remove(blockStates.size() - 1); }
    if(!blockIndentation.isEmpty()) { blockIndentation.remove(blockIndentation.size() - 1); }
    if(log) { logStates("states after removing: "); }
}
void addIndentInfo(int indentLevel, int state) { addIndentInfo(indentLevel, state, true); }
void addIndentInfo(int indentLevel, int state, boolean log) {
    if(log) { log("Adding indent info on level " + indentLevel + " = " + getStateName(state)); }
    blockIndentation.add(indentLevel);
    blockStates.add(state);
    if(log) { logStates("states after adding: "); }
}
void resetLastIndentInfo(int indentLevel, int state) { resetLastIndentInfo(indentLevel, state, true); }
void resetLastIndentInfo(int indentLevel, int state, boolean log) {
    log("Resetting current indent level state to: " + getStateName(state));
    removeLastIndentInfo(false);
    addIndentInfo(indentLevel, state, false);
    logStates("states after resetting: ");
}
boolean nestedODT() {
    return getStateAtIndent() == stateBeforeIndent && getStateAtIndent() == ODT;
}
IElementType firstAfterIndentation() {
    log("First after indent for " + yytext() + ", current = " + current_line_indent + ", previous: " + getCurrentIndentLevel());
    yypushback(yylength());
    if (current_line_indent > getCurrentIndentLevel() && !nestedODT()) {
        // register the block idnent and it's state
        addIndentInfo(current_line_indent, stateBeforeIndent);
        return returnIndent();
    } else if (current_line_indent < getCurrentIndentLevel()) {
        indent_level--;
        // step by step, remove the indentation levels
        removeLastIndentInfo();
        return returnDedent();
    } else {
        // revert to the state at the indentation level
        setState(getStateAtIndent());
        return TokenType.WHITE_SPACE;
    }
}
IElementType finishIdentation() {
    if(!blockIndentation.isEmpty()) {
        removeLastIndentInfo();
        return blockIndentation.isEmpty() ? null : returnDedent();
    }
    return null;
}
void yypushback(int number, String id) {
    yypushback(number);
}
boolean startOfLine;
String getStateName(int state) {
    switch(state) {
        case 0: return "YYINITIAL";
        case 2: return "YAML_SCALAR";
        case 4: return "YAML_SEQUENCE";
        case 6: return "YAML_DICTIONARY";
        case 8: return "INDENT";
        case 10: return "ODT";
        case 12: return "CURIE";
        case 14: return "BACKTICK";
    }
    return "UNKNOWN";
}
void setState(int state) {
    if(state == yystate()) { return; }
    if(state == INDENT) { stateBeforeIndent = yystate(); }
    log("Setting state to: " + getStateName(state));
    if(getCurrentIndentLevel() == yycolumn && getStateAtIndent() != state) {
        resetLastIndentInfo(yycolumn, state);
    }
    yybegin(state);
}
IElementType returnElement(IElementType element) {
    startOfLine = (element == OMTTypes.NEW_LINE || (startOfLine && element == TokenType.WHITE_SPACE));
    if(element != OMTTypes.NEW_LINE) {
        log("Returning " + yytext() + " as " + element.toString());
    }
    return element;
}
boolean backtick = false;
void setBacktick(boolean state) {
    backtick = state;
}
%}

%state YAML_SCALAR
%state YAML_SEQUENCE
%state YAML_DICTIONARY

%state INDENT
%state ODT
%state CURIE
%state BACKTICK

%%
// when in initial and newline, start the indent counting
<YYINITIAL, YAML_SCALAR, YAML_SEQUENCE, INDENT, ODT> {NEWLINE}                 { current_line_indent = 0; setState(INDENT); return returnElement(OMTTypes.NEW_LINE); }
<YYINITIAL, YAML_SCALAR, YAML_SEQUENCE, YAML_DICTIONARY, ODT> {
    {WHITE_SPACE}                                                   { return TokenType.WHITE_SPACE; } // capture all whitespace
    {JAVADOCS}                                                      { return returnElement(OMTTypes.JAVA_DOCS); }
    {END_OF_LINE_COMMENT}                                           { return returnElement(OMTTypes.END_OF_LINE_COMMENT); }
}
<YYINITIAL, YAML_SCALAR, YAML_SEQUENCE, ODT> {
    {GLOBAL_VARIABLE}                                                          { setState(ODT); return OMTTypes.GLOBAL_VARIABLE_NAME; } // capture all whitespace
    {BOOLEAN}                                                                  { return returnElement(OMTTypes.BOOLEAN); }
    {NULL}                                                                     { return returnElement(OMTTypes.NULL); }
}
// INDENTATION
// Required for YAML like grouping of blocks based on indents
<INDENT>{WHITE_SPACE}+                                               { current_line_indent = yylength(); }
<INDENT>.                                                            { return firstAfterIndentation(); }

<<EOF>>                                                              { return finishIdentation(); }
// a block starts with a property name, defined as NAME:
// this block can contain a SCALAR BLOCK, a MAPPING BLOCK or a SEQUENCE BLOCK
<YYINITIAL, YAML_SCALAR, YAML_SEQUENCE, YAML_DICTIONARY> {
    // specific OMT Blocks, used by the grammar part
    "prefixes:"                                               { setState(YAML_SCALAR); return returnElement(OMTTypes.PREFIX_BLOCK_START); }
    "commands:"                                               { setState(YAML_SCALAR); return returnElement(OMTTypes.COMMAND_BLOCK_START); }
    "queries:"                                                { setState(YAML_SCALAR); return returnElement(OMTTypes.QUERY_BLOCK_START); }
    "import:"                                                 { setState(YAML_SCALAR); return returnElement(OMTTypes.IMPORT_START); }
    "model:"                                                  { setState(YAML_SCALAR); return returnElement(OMTTypes.MODEL_BLOCK_START); }
    "moduleName:"                                             { setState(YAML_SCALAR); return returnElement(OMTTypes.MODULE_NAME_START); }
    "export:"                                                 { setState(YAML_SCALAR); return returnElement(OMTTypes.EXPORT_START); }


    {IMPORT_PATH}                                             { setState(YAML_SCALAR); return returnElement(OMTTypes.IMPORT_PATH); }
    // the initial state is only used for the key parts and can result in a switch to the scalar or sequence node
    // when a new line is reached in the state of initial (consecutive new lines), scalar or mapping, the state is always returned
    // to initial. This way, the initial state will detect the "-" indicator of the sequence items
}
<YYINITIAL> {
    {NAME}":"                                                 { setState(YAML_SCALAR); return returnElement(OMTTypes.PROPERTY); }
}

// YAML SEQUENCE:
// YAML: A sequence node is a series of zero or more nodes, can contain the same node multiple times or even itself
// OMT: We can expect variables, imports etc
<YAML_SEQUENCE> {
    // Variables
    {NAME}":"                                                        {
              // the indentation level doesn't start at the start of line here, add it separately
              log("yaml seq: " + yycolumn + ", " + getCurrentIndentLevel() + ", " + yytext());
              if(yycolumn > getCurrentIndentLevel()) {
                    addIndentInfo(yycolumn, YAML_DICTIONARY);
                    setState(YAML_DICTIONARY);
                    yypushback(yylength(), "YAML_SEQUENCE");
              } else {
                    // same indentation level, just a new entry
                    setState(YAML_SCALAR); return returnElement(OMTTypes.PROPERTY);
              }
          }
}
<YAML_DICTIONARY> {
    {NAME}":"                                                        { return returnElement(OMTTypes.DICTIONARY_KEY); }
}

// YAML SCALAR
<YAML_SCALAR> {
    "!"{NAME}                                                        { return returnElement(OMTTypes.MODEL_ITEM_TYPE); }
    {NAME}":"                                                        { return returnElement(OMTTypes.PROPERTY); }
    {STRING}":"                                                      { return returnElement(OMTTypes.PROPERTY); }
}
// ODT BLOCK
<ODT> {
    // statements that identify specific elements within the ODT language
    "DEFINE"                                                        { return returnElement(OMTTypes.DEFINE_START); }
    "QUERY"                                                         { return returnElement(OMTTypes.DEFINE_QUERY); }
    "COMMAND"                                                       { return returnElement(OMTTypes.DEFINE_COMMAND); }
    "VAR"                                                           { return returnElement(OMTTypes.DECLARE_VAR); }
    "PREFIX"                                                        { return returnElement(OMTTypes.PREFIX_DEFINE_START); }
    ";"                                                             { return returnElement(OMTTypes.SEMICOLON); }
    "$"{NAME}                                                       { return returnElement(OMTTypes.VARIABLE_NAME); }
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
    "=>"                                                            { return returnElement(OMTTypes.LAMBDA); }
    "="                                                             { return returnElement(OMTTypes.EQUALS); }

    // ODT operators
    // certain operators are used for assertions and should be recognized. They can be used within querysteps (grammar part)
    // when making filters or other boolean assertions
    "AND" | "OR" | "NOT" | "IN" | ">=" | "<=" | "==" | ">" | "<"    { return returnElement(OMTTypes.CONDITIONAL_OPERATOR); }
    "IF"                                                            { return returnElement(OMTTypes.IF_OPERATOR); }
    "ELSE"                                                          { return returnElement(OMTTypes.ELSE_OPERATOR); }
    "CHOOSE"                                                        { return returnElement(OMTTypes.CHOOSE_OPERATOR); }
    "WHEN"                                                          { return returnElement(OMTTypes.WHEN_OPERATOR); }
    "OTHERWISE"                                                     { return returnElement(OMTTypes.OTHERWISE_OPERATOR); }
    "END"                                                           { return returnElement(OMTTypes.END_OPERATOR); }
    "RETURN"                                                        { return returnElement(OMTTypes.RETURN_OPERATOR); }
    {NAME}                                                          { return returnElement(OMTTypes.OPERATOR); }

    {IRI}                                                            { return returnElement(OMTTypes.IRI); }
    // start code block:
    "|"                                                             { return returnElement(OMTTypes.PIPE); }
    "*"                                                             { return returnElement(OMTTypes.ASTERIX); }
}
<CURIE> {
    ":"                                                             { return returnElement(OMTTypes.COLON); }
    {NAME} | {SYMBOL}                                               {
                                                                          setState(ODT);
                                                                          return returnElement(OMTTypes.NAMESPACE_MEMBER);
      }
}

// Common tokens
<YYINITIAL, ODT> {
    {STRING}                                                        { return returnElement(OMTTypes.STRING); }
    {INTEGER}                                                       { return returnElement(OMTTypes.INTEGER); }
    {DECIMAL}                                                       { return returnElement(OMTTypes.DECIMAL); }
    {TYPED_VALUE}                                                   { return returnElement(OMTTypes.TYPED_VALUE); }

    // all single characters that are resolved to special characters:
    // todo: some should be made more specifically available based on their lexer state
    ":"                                                             { return returnElement(OMTTypes.COLON); }
    "="                                                             { return returnElement(OMTTypes.EQUALS); }
    ","                                                             { return returnElement(OMTTypes.COMMA); }
    ";"                                                             { return returnElement(OMTTypes.SEMICOLON); }
    "{"                                                             { return returnElement(OMTTypes.CURLY_OPEN); }
    "}"                                                             {
          if(backtick) { setState(BACKTICK); }
          return returnElement(OMTTypes.CURLY_CLOSED); }
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
    "`"                                                             { setBacktick(true); setState(BACKTICK); return returnElement(OMTTypes.BACKTICK); }
    "$"                                                             { return returnElement(OMTTypes.DOLLAR); }
}
<BACKTICK> {
    "$"                                                             { return returnElement(OMTTypes.DOLLAR); }
    "{"                                                             { setState(ODT); return returnElement(OMTTypes.CURLY_OPEN); }
    {WHITE_SPACE}+                                                  { return returnElement(TokenType.WHITE_SPACE); }
    {NEWLINE}+                                                      { return returnElement(OMTTypes.NEW_LINE); }
    "`"                                                             { setBacktick(false); setState(ODT); return returnElement(OMTTypes.BACKTICK); }
    [^\$\`\{\}\ ]+                                                  { return returnElement(OMTTypes.STRING); }
}

<YAML_SCALAR, YYINITIAL, YAML_SEQUENCE, ODT> "-"                     { setState(YAML_SEQUENCE); return returnElement(OMTTypes.SEQUENCE_BULLET); }
// defer as much as possible to the ODT state
<YAML_SCALAR, YAML_SEQUENCE, YYINITIAL, YAML_DICTIONARY>     [^]     { yypushback(yylength(), "YAML_SEQUENCE"); setState(ODT); }
[^]                                                                  { return returnElement(TokenType.BAD_CHARACTER); }
