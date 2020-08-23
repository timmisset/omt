// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.misset.opp.omt;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.misset.opp.omt.psi.OMTElementType;import com.misset.opp.omt.psi.OMTTokenType;import com.misset.opp.omt.psi.OMTTypes;
import com.intellij.psi.TokenType;import jdk.nashorn.internal.parser.Token;
import java.util.Stack;

%%

%class OMTLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%column

%eof{  return;
%eof}

WHITE_SPACE=                    [\ \f\t]
NOT_WHITE_SPACE=                [^\ \f\t\r\n]
ALPHA=                          [A-Za-z]
NEWLINE=                        (\r\n) | (\r) | (\n)
UNDERSCORE=                     [_]
DIGIT=                          [0-9]
STRING=                         (\"[^\"]*\")|(\'[^\']*\')
INTEGER=                        \-?([1-9][0-9]+|[0-9])
DECIMAL=                        {INTEGER}\.[0-9]+
BOOLEAN=                        "true"|"false"
NULL=                           "null"
GLOBAL_VARIABLE=                \$username|\$medewerkerGraph|\$offline|\$mockvalue[0-9]+

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
PROPERTY_KEY=                   {IMPORT_PATH} | (({STRING}|{NAME})({WHITE_SPACE}*)":"({WHITE_SPACE}+ | {NEWLINE}))

%{
/* globals to track current indentation */
int yycolumn;
Stack<Integer> indents = new Stack();
void log(String message) {
    if(logging) { System.out.println(message); }
}
boolean logging = false;
OMTLexer(java.io.Reader in, boolean enableLogging) {
    this.zzReader = in;
    this.logging = enableLogging;
}
void yypushback(int number, String id) {
    int position = zzMarkedPos;
    log("resetting " + number + " of " + yylength() + "'" + yytext() + "'" + " due to: " + id);
    yypushback(number);
    log("token moved back from " + position + " to " + zzMarkedPos);
}
boolean inScalarBlock = false;
IElementType startScalarBlock() {
    inScalarBlock = true;
    yypushback(yylength() - 1, "starting scalar");
    return returnElement(OMTTypes.PIPE);
}
String getStateName(int state) {
    switch(state) {
        case 0: return "YYINITIAL";
        case 2: return "YAML_SCALAR";
//        case 4: return "YAML_SEQUENCE";
//        case 6: return "YAML_DICTIONARY";
//        case 8: return "INDENT";
//        case 10: return "ODT";
        case 4: return "CURIE";
        case 6: return "BACKTICK";
    }
    return "UNKNOWN";
}
void setState(int state) {
    log("Setting state to: " + getStateName(state));
    yybegin(state);
}
IElementType lastDedent;
IElementType resetIndent() {
    if(!indents.isEmpty() && indents.peek() > yylength()) {
        lastDedent = lastDedent == null || lastDedent == OMTTypes.DEDENT2 ? OMTTypes.DEDENT : OMTTypes.DEDENT2;
        indents.pop();
        return returnElement(lastDedent);
    }
    return null;
}
IElementType lastIndent;
IElementType getIndent() {
    lastIndent = lastIndent == null || lastIndent == OMTTypes.INDENT2 ? OMTTypes.INDENT : OMTTypes.INDENT2;
    return returnElement(lastIndent);
}
IElementType setIndent() {
    if(yycolumn > 0 && (indents.isEmpty() || indents.peek() < yycolumn)) {
        log((indents.isEmpty() ? "empty" : indents.peek()) + " vs " + yycolumn);
        indents.push(yycolumn);
        return getIndent();
    }
    if(yycolumn == 0 && indents.isEmpty()) { indents.push(0); }
    log("indent level = " + indents.peek());
    return null;
}
IElementType retryInInitial(String reason) {
    return retryInInitial(true, reason);
}
IElementType retryInInitial(boolean pushback, String reason) {
    if(pushback) { yypushback(yylength(), "retry in initial: " + reason); }
    inScalarBlock = false;
    setState(YYINITIAL);
    log(reason + ", remaining buffer = '" + yytext() + "'");
    return returnElement(OMTTypes.END_TOKEN);
}
void trim(IElementType element) {
    if(element != TokenType.WHITE_SPACE &&
        element != OMTTypes.START_TOKEN &&
        element != OMTTypes.END_TOKEN &&
        element != OMTTypes.INDENT &&
        element != OMTTypes.INDENT2 &&
        element != OMTTypes.DEDENT &&
        element != OMTTypes.DEDENT2
        ) {
        int trimSize = yylength() - yytext().toString().trim().length();
        if(trimSize > 0){
             yypushback(trimSize, "trim");
        }
    }
}
IElementType toSpecificBlockLabel() {
    switch(yytext().toString()) {
        case "prefixes:": return logAndReturn(OMTTypes.PREFIX_BLOCK_START);
        case "commands:": return logAndReturn(OMTTypes.COMMAND_BLOCK_START);
        case "queries:": return logAndReturn(OMTTypes.QUERY_BLOCK_START);
        case "import:": return logAndReturn(OMTTypes.IMPORT_START);
        case "model:": return logAndReturn(OMTTypes.MODEL_BLOCK_START);
        case "moduleName:": return logAndReturn(OMTTypes.MODULE_NAME_START);
        case "export:": return logAndReturn(OMTTypes.EXPORT_START);
        default: return logAndReturn(OMTTypes.PROPERTY);
    }
}
int totalReturned = 0;
IElementType returnElement(IElementType element) {
    trim(element);

    if(element == OMTTypes.PROPERTY) { return toSpecificBlockLabel(); }
    if(totalReturned > 100) {
        //throw new RuntimeException("Done");
    }
    totalReturned++;
    return logAndReturn(element);
}
IElementType logAndReturn(IElementType element) {
    if(element != TokenType.WHITE_SPACE) {
        log("Returning " + yytext() + " as " + element.toString());
    }
    return element;
}
boolean backtick = false;
void setBacktick(boolean state) {
    backtick = state;
}
%}

// YYINITIAL == MAP
%state YAML_SCALAR
%state CURIE
%state BACKTICK

%%
<YYINITIAL> {
    ^{WHITE_SPACE}*{NEWLINE}                                  { return returnElement(TokenType.WHITE_SPACE); }
    {NEWLINE}*                                                { return returnElement(TokenType.WHITE_SPACE); }

    {PROPERTY_KEY}                                            {
                                                                IElementType indent = setIndent();
                                                                if(indent == null) {
                                                                    return returnElement(OMTTypes.PROPERTY);
                                                                } else {
                                                                    yypushback(yylength(), "setting indent");
                                                                    return indent;
                                                                }
                                                              }
    "!"{NAME}                                                 { return returnElement(OMTTypes.MODEL_ITEM_TYPE); }
    "- "                                                      {
                                                                  IElementType indent = setIndent();
                                                                  if(indent == null) {
                                                                      return returnElement(OMTTypes.SEQUENCE_BULLET);
                                                                  } else {
                                                                      yypushback(yylength(), "setting indent");
                                                                      return indent;
                                                                  }
                                                                }
    ^{WHITE_SPACE}+                                            {
                                                                   if(yylength() == 0 || yylength() <= indents.peek()) {
                                                                       IElementType dedent = resetIndent();
                                                                       if(dedent == null) {
                                                                           return returnElement(TokenType.WHITE_SPACE);
                                                                       } else {
                                                                           yypushback(yylength(), "resetting indent, indent < peek");
                                                                           return dedent;
                                                                       }
                                                                   } else {
                                                                       return returnElement(TokenType.WHITE_SPACE);
                                                                   }
                                                              }
    {NEWLINE}{NOT_WHITE_SPACE}                                {
                                                                    IElementType dedent = resetIndent();
                                                                    if(dedent == null) {
                                                                        yypushback(1, "resetting indent, new line no whitespace, returning whitespace");
                                                                       return returnElement(TokenType.WHITE_SPACE);
                                                                   } else {
                                                                        yypushback(yylength(), "resetting indent, new line no whitespace, returning dedent");
                                                                       return dedent;
                                                                   }
                                                              }
    {WHITE_SPACE}+                                            { return TokenType.WHITE_SPACE; } // capture all whitespace
    {JAVADOCS}                                                { return returnElement(OMTTypes.JAVA_DOCS); }
    {END_OF_LINE_COMMENT}                                     { return returnElement(OMTTypes.END_OF_LINE_COMMENT); }
    <<EOF>>                                                   { return resetIndent(); }
    [^]                                                       { yypushback(yylength(), "initial"); setState(YAML_SCALAR); return returnElement(OMTTypes.START_TOKEN); }
}
<YAML_SCALAR> {
    ^{WHITE_SPACE}+                                           {
                                                                    if(yylength() == 0 || yylength() <= indents.peek()) {
                                                                        return retryInInitial("indent < peek");
                                                                    } else {
                                                                        return returnElement(TokenType.WHITE_SPACE);
                                                                    }
                                                              }
    {WHITE_SPACE}+                                            { return TokenType.WHITE_SPACE; } // capture all whitespace
    {JAVADOCS}                                                { return returnElement(OMTTypes.JAVA_DOCS); }
    {END_OF_LINE_COMMENT}                                     { return returnElement(OMTTypes.END_OF_LINE_COMMENT); }
    {NEWLINE}                                                 { return returnElement(TokenType.WHITE_SPACE); }
    {NEWLINE}{NOT_WHITE_SPACE}                                {
                                                                    return retryInInitial("newline starts with non-whitespace");
                                                              }
    {NAME}":"                                                 {
                                                                    if(inScalarBlock) {
                                                                        yypushback(1, "yaml_scalar");
                                                                        return returnElement(OMTTypes.OPERATOR); }
                                                                    else { return retryInInitial("propetry key outside scalar block"); }
                                                              }
    "- "                                                      {
                                                                    if(inScalarBlock) {
                                                                        yypushback(1, "yaml_scalar");
                                                                        return returnElement(OMTTypes.PIPE); }
                                                                    else { return retryInInitial("sequence bullet outside scalar block"); }
                                                              }
    {GLOBAL_VARIABLE}                                         { return returnElement(OMTTypes.GLOBAL_VARIABLE_NAME); } // capture all whitespace
    {BOOLEAN}                                                 { return returnElement(OMTTypes.BOOLEAN); }
    {NULL}                                                    { return returnElement(OMTTypes.NULL); }
     "/"{CURIE}                                               {
                                                                  yypushback(yylength() - 1);
                                                                  return returnElement(OMTTypes.CURIE_CONSTANT_ELEMENT_PREFIX);
                                                              }
    {CURIE}                                                   {
                                                                  setState(CURIE);
                                                                  yypushback(yylength() - yytext().toString().indexOf(":"));
                                                                  return returnElement(OMTTypes.NAMESPACE);
                                                             }
    "|"{WHITE_SPACE}*{NEWLINE}                               { return startScalarBlock(); }
    "|"                                                      { return returnElement(OMTTypes.PIPE); }

    "TRUE" | "FALSE"                                                { return returnElement(OMTTypes.BOOLEAN); }
    "DEFINE"                                                        { return returnElement(OMTTypes.DEFINE_START); }
    "QUERY"                                                         { return returnElement(OMTTypes.DEFINE_QUERY); }
    "COMMAND"                                                       { return returnElement(OMTTypes.DEFINE_COMMAND); }
    "VAR"                                                           { return returnElement(OMTTypes.DECLARE_VAR); }
    "PREFIX"                                                        { return returnElement(OMTTypes.PREFIX_DEFINE_START); }
    ";"                                                             { return returnElement(OMTTypes.SEMICOLON); }
    "$"{NAME}                                                       { return returnElement(OMTTypes.VARIABLE_NAME); }
    "@"{NAME}                                                       { return returnElement(OMTTypes.COMMAND); }
    "!"{NAME}                                                       { return returnElement(OMTTypes.FLAG); }

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

    {IRI}                                                            { return returnElement(OMTTypes.IRI); }
    {STRING}                                                        { return returnElement(OMTTypes.STRING); }
    {INTEGER}                                                       { return returnElement(OMTTypes.INTEGER); }
    {DECIMAL}                                                       { return returnElement(OMTTypes.DECIMAL); }
    {TYPED_VALUE}                                                   { return returnElement(OMTTypes.TYPED_VALUE); }
    {NAME}                                                          { return returnElement(OMTTypes.OPERATOR); }
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
    "*"                                                             { return returnElement(OMTTypes.ASTERIX); }
    <<EOF>>                                                         { setState(YYINITIAL); return returnElement(OMTTypes.END_TOKEN); }
}
<CURIE> {
    ":"                                                             { return returnElement(OMTTypes.COLON); }
    {NAME} | {SYMBOL}                                               { return returnElement(OMTTypes.NAMESPACE_MEMBER); }
    [^]                                                             { yypushback(1, "ending curie"); setState(YAML_SCALAR); }
}
<BACKTICK> {
    "$"                                                             { return returnElement(OMTTypes.DOLLAR); }
    "{"                                                             { setState(YAML_SCALAR); return returnElement(OMTTypes.CURLY_OPEN); }
    {WHITE_SPACE}+                                                  { return returnElement(TokenType.WHITE_SPACE); }
    {NEWLINE}+                                                      { return returnElement(TokenType.WHITE_SPACE); }
    "`"                                                             { setBacktick(false); setState(YAML_SCALAR); return returnElement(OMTTypes.BACKTICK); }
    [^\$\`\{\}\ ]+                                                  { return returnElement(OMTTypes.STRING); }
}

[^]                                                                  { return returnElement(TokenType.BAD_CHARACTER); }
