// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.misset.opp.omt;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.misset.opp.omt.psi.OMTTypes;
import com.misset.opp.omt.psi.OMTIgnored;
import com.intellij.psi.TokenType;
import java.util.Stack;

%%

%class OMTLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%column
%line

%eof{  return;
%eof}

WHITE_SPACE=                    [\ \f\t]
NOT_WHITE_SPACE=                [^\ \f\t\r\n]
ALPHA=                          [A-Za-z]
NEWLINE=                        (\r\n) | (\r) | (\n)
UNDERSCORE=                     [_]
DIGIT=                          [0-9]
STRING=                         (\"[^\"\\]*(\\.[^\"\\]*)*\")|(\'[^\'\\]*(\\.[^\'\\]*)*\')
INTEGER=                        \-?([1-9][0-9]+|[0-9])
DECIMAL=                        {INTEGER}\.[0-9]+
BOOLEAN=                        "true"|"false"|"TRUE"|"FALSE"|"True"|"False"
NULL=                           "null"
GLOBAL_VARIABLE=                \$username|\$medewerkerGraph|\$offline|\$mockvalue[0-9]+|\$heeftPreviewRol

LATIN_EXT_A=                    [\u0100-\u017F] // Zie: http://en.wikipedia.org/wiki/Latin_script_in_Unicode
SYMBOL=                         ({ALPHA}|{DIGIT}|{LATIN_EXT_A}|[_@\-])+
SCHEME=                         {ALPHA}({ALPHA}|{DIGIT}|[+.-])*
IRI=                            "<"{SCHEME}":"({SYMBOL}|[?&#/+*.-])+">"
INCOMPLETE_IRI=                 "<"{SCHEME}":"({SYMBOL}|[?&#/+*.-])+
END_OF_LINE_COMMENT=            ("#" | "\/\/")[^\r\n]*
JDCOMMENTLINE=                  ("*")[^\*\@\r\n]*
JDSTART=                         \/\*\*
JDEND=                           \*\/ // all between /** and */
MULTILINECOMMENT=                \/\*\s*\n([^\*]|(\*[^\/]))+\*\/
NAME=                           {ALPHA}({ALPHA}|{DIGIT}|{UNDERSCORE})*
CURIE=                          ({NAME})?":"{SYMBOL}
TYPED_VALUE=                    {STRING}"^^"({IRI}|{CURIE})
IMPORT_PATH=                    (\.{1,2}\/[^:\n \[\]]+:)
PROPERTY_KEY=                   {IMPORT_PATH} | (({STRING}|{NAME})({WHITE_SPACE}*)":")
VARIABLENAME=                   "$"{NAME}
// used to capture a valid parameter annotation in the Javadocs
// makes sure incomplete annotations are parsed as regular javadocs comments
ANNOTATE_PARAM=                 @param[\s]+\$[^ ]*[\s*]\([^)]*\)

// YYINITIAL state can only have a limited selection of tokens that can trigger indentation
INITIAL_TOKENS=                {PROPERTY_KEY} | "-" | {JDSTART}    // the valid tokens for

%{
/* globals to track current indentation */
int yycolumn;
int yyline;
Stack<Integer> indents = new Stack();
void log(String message) {
    if(logging) { System.out.println(message); }
}
void error(String error) {
    System.out.println(error);
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
String getStateName(int state) {
    switch(state) {
        case 0: return "YYINITIAL";
        case 2: return "YAML_SCALAR";
        case 4: return "CURIE";
        case 6: return "BACKTICK";
        case 8: return "JAVADOCS";
        case 10: return "PARAM_ANNOTATION";
        case 12: return "STRING";
        case 14: return "INTERPOLATED_STRING";
    }
    return "UNKNOWN";
}
int previousState = 0;
void setState(int state) {
    setState(state, true);
}
void setState(int state, boolean assignPreviousState) {
    log("Setting state to: " + getStateName(state));
    if(assignPreviousState) { previousState = yystate(); }
    yybegin(state);
}
IElementType lastDedent;
IElementType getDedent() {
    lastDedent = lastDedent == null || lastDedent == OMTTypes.DEDENT2 ? OMTTypes.DEDENT : OMTTypes.DEDENT2;
    return lastDedent;
}
IElementType lastIndent;
IElementType getIndent() {
    lastIndent = lastIndent == null || lastIndent == OMTTypes.INDENT2 ? OMTTypes.INDENT : OMTTypes.INDENT2;
    return lastIndent;
}
public int getScalarState() {
    return getScalarState(currentBlockLabel);
}
public static int getScalarState(String entry) {
    if(entry == null) { return YAML_SCALAR; }
    switch(entry) {
        case "title:":
        case "description:":
            return INTERPOLATED_STRING;
        case "reason:":
        case "moduleName:":
        case "icon:":
            return STRING;
        default:
            return YAML_SCALAR;
    }
}
public String currentBlockLabel;
IElementType toSpecificBlockLabel() {
    currentBlockLabel = yytext().toString();
    switch(yytext().toString()) {
        case "prefixes:": return logAndReturn(OMTTypes.PREFIX_BLOCK_START);
        case "commands:": return logAndReturn(OMTTypes.COMMAND_BLOCK_START);
        case "queries:": return logAndReturn(OMTTypes.QUERY_BLOCK_START);
        case "import:": return logAndReturn(OMTTypes.IMPORT_START);
        case "model:": return logAndReturn(OMTTypes.MODEL_BLOCK_START);
        case "moduleName:": return logAndReturn(OMTTypes.MODULE_NAME_START);
        case "export:": return logAndReturn(OMTTypes.EXPORT_START);
        case "module:": return logAndReturn(OMTTypes.MODULE_START);
        default:
            if(yytext().toString().endsWith(":")) { yypushback(1); }
            return logAndReturn(OMTTypes.PROPERTY);
    }
}
IElementType returnElement(IElementType element) {
    if(element == OMTTypes.PROPERTY) { return toSpecificBlockLabel(); }
    return logAndReturn(element);
}
String lastResponse = null;
IElementType logAndReturn(IElementType element) {
  String elementName = element == null ? "null" : element.toString();
    String response = yystate() + "." + elementName + "." + zzMarkedPos;
    log("Returning " + yytext() + " as " + elementName + " --> " + response);
    if(response.equals(lastResponse)) {
        error("===============> Error, lexer not advancing!");
        error("Returning " + yytext() + " as " + elementName + " --> " + response);
    }
    lastResponse = response;
    return element;
}
boolean templateInBacktick = false;
void setTemplateInBacktick(boolean state) {
    templateInBacktick = state;
}
boolean templateInScalar = false;
void setTemplateInScalar(boolean state) {
    templateInScalar = state;
}

/**
* Returns an indent, dedent or whitespace for the current leading space
* @return
*/
int lastIndentLine = -1;
IElementType dent(IElementType returnType) {
    int indentAtToken = indentAtToken();
    int currentIndentation = currentIndentation();
    int nonWhitespaceSize = currentNonWhiteSpaceSize();

    if(indentAtToken > currentIndentation && yyline > lastIndentLine) {
        yypushback(nonWhitespaceSize, "nwst");                          // pushback the non-whitespace token
        indents.push(indentAtToken);                                    // register the indentation
        lastIndentLine = yyline;                                        // keep track of the last indented line, no supported for multiple indentations on the same line
        return returnElement(getIndent());                              // return whitespace as indent
    } else if (indentAtToken < currentIndentation) {
        indents.pop();                                                  // remove the current indentation
        yypushback(nonWhitespaceSize, "nwst");                          // pushback the non-whitespace token
        if(!indents.isEmpty() &&
            indents.peek() > indentAtToken &&
            yylength() >= indentAtToken)                                // check if only one dedent is required
        {
            yypushback(indentAtToken, "another dedent required");       // pushback all whitespace, will return to this method
        }                                                               // for another dedent token to be returned
        return returnElement(getDedent());                              // return whitespace as dedent
    } else {
        if(returnType == OMTTypes.SEQUENCE_BULLET) {
            // only for sequence items, an additional indentation is accepted for destructed elements
            // -    property:
            //      anotherProperty:
            lastIndentLine -= 1;
        }
        return returnLeadingWhitespaceFirst(returnType);                // return the actual element
    }
}
IElementType finishLexer() {
    if(!indents.isEmpty()) {
        indents.pop();
        return returnElement(getDedent());                              // returns as many as dedents as required to close the document
    }
    return null;
}
IElementType returnLeadingWhitespaceFirst(IElementType ifNoWhitespaceLeft) {
    if(yycolumn < indentAtToken()) {
      // first return the whitespace, then reprocess the non-whitespace
      yypushback(currentNonWhiteSpaceSize(), "return leading whitespace first");
      return returnElement(TokenType.WHITE_SPACE);
    }
    return returnElement(ifNoWhitespaceLeft);
}
int currentNonWhiteSpaceSize() {
    return yytext().toString().trim().length();
}
int currentIndentation() {
    return indents.isEmpty() ? 0 : indents.peek();
}
int indentAtToken() {
    return Math.max(0, yycolumn + yylength() - currentNonWhiteSpaceSize());
}
boolean shouldExitScalar() {
    return currentIndentation() >= indentAtToken();
}
IElementType startScalar() {
    IElementType elementType = returnLeadingWhitespaceFirst(OMTIgnored.START_TOKEN);
    if(elementType == OMTIgnored.START_TOKEN) {
        yypushback(currentNonWhiteSpaceSize(), "Starting Scalar");
        setState(getScalarState());
    }
    return elementType;
}
IElementType exitScalar() {
    setState(YYINITIAL);
    yypushback(yylength());
    return returnElement(OMTIgnored.END_TOKEN);
}
IElementType startBacktick() {
    setTemplateInBacktick(true);
    setState(BACKTICK);
    return returnElement(OMTTypes.BACKTICK);
}
IElementType closeBracket() {
    // a template block: ${} can be part of a backtick statement
    // or be used stand-alone in a scalar value
    // whichever state started the template should be returned to
    if(templateInBacktick) {
        setState(BACKTICK);
        return returnElement(OMTTypes.TEMPLATE_CLOSED);
    }
    // allow template parsing in the scalar values, annotation of the model tree should determine if their usage is legal
    if(templateInScalar) {
        setTemplateInScalar(false);
        setState(getScalarState());
        return returnElement(OMTTypes.TEMPLATE_CLOSED);
    }
    return returnElement(OMTTypes.CURLY_CLOSED); }
%}

// YYINITIAL == MAP
%state YAML_SCALAR
%state CURIE
%state BACKTICK
%state JAVADOCS
%state PARAM_ANNOTATION
%state STRING
%state INTERPOLATED_STRING

%%
<YYINITIAL> {
    // The YYINITIAL block is mainly responsible for the indentation and encapsulation of blocks
    // in the comment discriptions, underscore is used to indicate leading spaces

    // ____something:
    // take the whitespace and return it as indent, dedent or whitespace depending on previous indentations
    // will be resolved when the whitespace is gone, will always pushback the single not_white_space character
    ^{WHITE_SPACE}+{INITIAL_TOKENS}                         { return dent(TokenType.WHITE_SPACE); }

    // something:
    // OR
    //     something:
    //     -    somethingElse:
    //          aSecondEntry:
    // Especially the latter requires the indentation to be adjusted to the position of somethingElse to link
    // aSecondEntry: accordingly.
    {PROPERTY_KEY}                                            { return dent(OMTTypes.PROPERTY); }
    ":"                                                       { return OMTTypes.COLON; }

    // The YAML flag in OMT is only used to typecast the modelitem
    "!"+{NAME}                                                 { return returnElement(OMTTypes.MODEL_ITEM_TYPE); }

    // https://yaml-multiline.info/
    // these decorators are used in YAML to determine how linebreaks are processed and preserved
    "|" | ">" | "|+" | "|-" | ">+" | ">-"                    {
          setState(YAML_SCALAR);
          return returnElement(OMTTypes.YAML_MULTILINE_DECORATOR);
      }

    // A sequence bullet to identify a sequence item
    // the parser will collect them into a sequence
    "-"                                                       { return dent(OMTTypes.SEQUENCE_BULLET); }

    // JavaDocs can start from both the INITIAL state and SCALAR state
    // when part of the INITIAL state, it can be the start of the indentation
    {JDSTART}                                                 {
                                                                  IElementType element = dent(OMTTypes.JAVADOCS_START);
                                                                  if(element == OMTTypes.JAVADOCS_START) {
                                                                    setState(JAVADOCS); // indentation was resolved, change state
                                                                  }
                                                                  return element; // can be an indent/dedent token or JAVADOCS_START
                                                              }
    // When EOF is reached, resolve all open indentations with dedent tokens and close with null
    <<EOF>>                                                   { return finishLexer(); }
}
<YAML_SCALAR> {
    // Firstly, we need to check if the SCALAR state is still applicable
    // Only the indentation of the INITIAL state is recorded, so when the indentation in the SCALAR state is
    // <= than the last recorded indentation we can exit the scalar
    ^{WHITE_SPACE}+{NOT_WHITE_SPACE}                           {
          if(shouldExitScalar()) { return exitScalar(); }           // exit scalar if required
          // or else, continue in scalar
          yypushback(currentNonWhiteSpaceSize());                   // pushback all to be processed in the INITIAL state
          return returnElement(TokenType.WHITE_SPACE);              // and return the END token
      }
    {PROPERTY_KEY}                                           {    return exitScalar(); } // property_key at the start of a line can only be an exit of the Scalar

    {GLOBAL_VARIABLE}                                         { return returnElement(OMTTypes.GLOBAL_VARIABLE_NAME); } // capture all whitespace
    {BOOLEAN}                                                 { return returnElement(OMTTypes.BOOLEAN); }
    {NULL}                                                    { return returnElement(OMTTypes.NULL); }
    {CURIE}                                                   {
                                                                  setState(CURIE);
                                                                  yypushback(yylength() - yytext().toString().indexOf(":"));
                                                                  return returnElement(OMTTypes.PROPERTY);
                                                                }

    "DEFINE"                                                        { return returnElement(OMTTypes.DEFINE_START); }
    "QUERY"                                                         { return returnElement(OMTTypes.DEFINE_QUERY); }
    "COMMAND"                                                       { return returnElement(OMTTypes.DEFINE_COMMAND); }
    "VAR"                                                           { return returnElement(OMTTypes.DECLARE_VAR); }
    "PREFIX"                                                        { return returnElement(OMTTypes.PREFIX_DEFINE_START); }
    ";"                                                             { return returnElement(OMTTypes.SEMICOLON); }
    {VARIABLENAME}                                                  { return returnElement(OMTTypes.VARIABLE_NAME); }
    "$_"                                                            { return returnElement(OMTTypes.IGNORE_VARIABLE_NAME); }
    "@"{NAME}                                                       { return returnElement(OMTTypes.COMMAND); }
    "!"{NAME}                                                       { return returnElement(OMTTypes.FLAG); }

    // the lambda is used for assigning the actual query/command block to it's constructor
    // and to assign a path to case condition
    "=>"                                                            { return returnElement(OMTTypes.LAMBDA); }
    "="                                                             { return returnElement(OMTTypes.EQUALS); }

    // ODT operators
    // certain operators are used for assertions and should be recognized. They can be used within querysteps (grammar part)
    // when making filters or other boolean assertions
    "AND(" | "OR(" | "NOT("                                         { yypushback(1, "CONDITIONAL_OPERATOR"); return returnElement(OMTTypes.OPERATOR); }
    "AND" | "OR"                                                    { return returnElement(OMTTypes.BOOLEAN_OPERATOR); }
    "NOT"                                                           { return returnElement(OMTTypes.NOT_OPERATOR); }
    ">=" | "<=" | "==" | ">" | "<"                                  { return returnElement(OMTTypes.CONDITIONAL_OPERATOR); }
    "IF"                                                            { return returnElement(OMTTypes.IF_OPERATOR); }
    "ELSE"                                                          { return returnElement(OMTTypes.ELSE_OPERATOR); }
    "CHOOSE"                                                        { return returnElement(OMTTypes.CHOOSE_OPERATOR); }
    "WHEN"                                                          { return returnElement(OMTTypes.WHEN_OPERATOR); }
    "OTHERWISE"                                                     { return returnElement(OMTTypes.OTHERWISE_OPERATOR); }
    "END"                                                           { return returnElement(OMTTypes.END_OPERATOR); }
    "RETURN"                                                        { return returnElement(OMTTypes.RETURN_OPERATOR); }

    {IRI}                                                           { return returnElement(OMTTypes.IRI); }
    {INCOMPLETE_IRI}                                                { return returnElement(TokenType.BAD_CHARACTER); }
    {STRING}                                                        { return returnElement(OMTTypes.STRING); }
    {INTEGER}                                                       { return returnElement(OMTTypes.INTEGER); }
    {DECIMAL}                                                       { return returnElement(OMTTypes.DECIMAL); }
    {TYPED_VALUE}                                                   { return returnElement(OMTTypes.TYPED_VALUE); }
    "<"{NAME}">"                                                    { return returnElement(OMTTypes.OWLPROPERTY); }
    {NAME}                                                          { return returnElement(OMTTypes.OPERATOR); }
    // A pipe is used in ODT as delimiter when constructing a collection of values
    "|"                                                            { return returnElement(OMTTypes.PIPE); }
    // all single characters that are resolved to special characters:
    // todo: some should be made more specifically available based on their lexer state
    ":"                                                             { return returnElement(OMTTypes.COLON); }
    "="                                                             { return returnElement(OMTTypes.EQUALS); }
    ","                                                             { return returnElement(OMTTypes.COMMA); }
    ";"                                                             { return returnElement(OMTTypes.SEMICOLON); }
    "{"                                                             { return returnElement(OMTTypes.CURLY_OPEN); }
    "}"                                                             { return closeBracket(); }
    "/"                                                             { return returnElement(OMTTypes.FORWARD_SLASH); }
    "^"                                                             { return returnElement(OMTTypes.CARET); }
    "[]"                                                            { return returnElement(OMTTypes.EMPTY_ARRAY); }
    "["                                                             { return returnElement(OMTTypes.BRACKET_OPEN); }
    "]"                                                             { return returnElement(OMTTypes.BRACKET_CLOSED); }
    "+"                                                             { return returnElement(OMTTypes.PLUS); }
    "("                                                             { return returnElement(OMTTypes.PARENTHESES_OPEN); }
    ")"                                                             { return returnElement(OMTTypes.PARENTHESES_CLOSE); }
    "\."                                                            { return returnElement(OMTTypes.DOT); }
    "+="                                                            { return returnElement(OMTTypes.ADD); }
    "-="                                                            { return returnElement(OMTTypes.REMOVE); }
    "`"                                                             { return startBacktick(); }
    "*"                                                             { return returnElement(OMTTypes.ASTERIX); }
    "?"                                                             { return returnElement(OMTTypes.QUESTION_MARK); }
    // Its not required to encapsulate an interpolated string with backticks. To support this, the SCALAR state itself can also
    // capture backtick template blocks ${...}
    "${"                                                            { setTemplateInScalar(true); return returnElement(OMTTypes.TEMPLATE_OPEN); }

    // Javadocs in the Scalar are not indented but are anchored directly as leading block to the next Psi element
    {JDSTART}                                                       {
                                                                        if(shouldExitScalar()) {
                                                                                return exitScalar();
                                                                        }
                                                                        setState(JAVADOCS);
                                                                        return returnElement(OMTTypes.JAVADOCS_START); // can be an indent/dedent token or JAVADOCS_START
                                                                    }
    // When continueing from a scalar value into the next sequence item
    "-"                                                             { return returnElement(OMTTypes.SEQUENCE_BULLET); }
    <<EOF>>                                                         { setState(YYINITIAL); return returnElement(OMTIgnored.END_TOKEN); }
}
<CURIE> {
    ":"                                                             { return returnElement(OMTTypes.COLON); }
    {NAME} | {SYMBOL}                                               { return returnElement(OMTTypes.NAMESPACE_MEMBER); }
}
<BACKTICK> {
    // Backtick templates are supported and anything parsed in them can be resolved to PSI elements
    "${"                                                            { setState(YAML_SCALAR); return returnElement(OMTTypes.TEMPLATE_OPEN); }
    "`"                                                             { setTemplateInBacktick(false); setState(YAML_SCALAR); return returnElement(OMTTypes.BACKTICK); }
    [^\$\`\ \n]+                                                    { return returnElement(OMTTypes.STRING); }
    "$"                                                             { return returnElement(OMTTypes.STRING); }
}
<STRING> {
    {PROPERTY_KEY}                                                  { return exitScalar(); }
    {VARIABLENAME}                                                  { return returnElement(OMTTypes.VARIABLE_NAME); }
    [^\n\ ]+                                                        { return returnElement(OMTTypes.STRING);  }
    <<EOF>>                                                         { return finishLexer(); }
}
<INTERPOLATED_STRING> {
    // Interpolated templates are supported and anything parsed in them can be resolved to PSI elements
    {PROPERTY_KEY}                                                  {    return exitScalar(); }
    {VARIABLENAME}                                                  { return returnElement(OMTTypes.VARIABLE_NAME); }
    "${"                                                            { setTemplateInScalar(true); setState(YAML_SCALAR); return returnElement(OMTTypes.TEMPLATE_OPEN); }
    "`"                                                             { setTemplateInBacktick(false); setState(YAML_SCALAR); return returnElement(OMTTypes.BACKTICK); }
    "$"                                                             { return returnElement(OMTTypes.STRING); }
    [^\$\`\ \n]+                                                    { return returnElement(OMTTypes.STRING);  }
    <<EOF>>                                                          { return finishLexer(); }
}
<JAVADOCS> {
    {JDEND}                                                         { setState(previousState); return returnElement(OMTTypes.JAVADOCS_END); }
    {JDCOMMENTLINE}                                                 { return returnElement(OMTTypes.JAVADOCS_CONTENT); }
    {ANNOTATE_PARAM}                            {
          yypushback(yylength()-6); // pushback anything but the @param
          setState(PARAM_ANNOTATION, false);
          return returnElement(OMTTypes.ANNOTATE_PARAMETER); }
}
<PARAM_ANNOTATION> {
    "$"{NAME}                                                       { return returnElement(OMTTypes.VARIABLE_NAME); }
    {NAME}":"                                                       {
          yypushback(1);
          return returnElement(OMTTypes.PROPERTY); }
    ":"                                                             { return returnElement(OMTTypes.COLON); }
    {NAME} | {SYMBOL}                                               { return returnElement(OMTTypes.NAMESPACE_MEMBER); }
    "("                                                             { return returnElement(OMTTypes.PARENTHESES_OPEN); }
    ")"                                                             { setState(JAVADOCS, false); return returnElement(OMTTypes.PARENTHESES_CLOSE); }
}
<YYINITIAL, YAML_SCALAR, JAVADOCS, BACKTICK, PARAM_ANNOTATION, STRING, INTERPOLATED_STRING> {

    // WHITE_SPACE and NEWLINE are not relevant in YAML except for indentation
    // which is handled above
    // the longest possible RegEx match will make sure that the leading whitespace at the start
    // of the line will be matched first and appropriate indentation is set
    // all remaining whitespace characters can be collected and returns
    {WHITE_SPACE}+ | {NEWLINE}+                               { return TokenType.WHITE_SPACE; } // capture all whitespace

    // END_OF_LINE_COMMENTs are ignored by the parser and can be added to any location in the document
    ^{WHITE_SPACE}*{END_OF_LINE_COMMENT}                      {
          if(yystate() == YAML_SCALAR) {
                return shouldExitScalar() ? exitScalar() : returnLeadingWhitespaceFirst(OMTIgnored.END_OF_LINE_COMMENT);
          }
          return dent(OMTIgnored.END_OF_LINE_COMMENT);
                                                                     }
    {END_OF_LINE_COMMENT}                                     { return returnElement(OMTIgnored.END_OF_LINE_COMMENT); }
    ^{WHITE_SPACE}*{MULTILINECOMMENT}                      {
          if(yystate() == YAML_SCALAR) {
                return shouldExitScalar() ? exitScalar() : returnLeadingWhitespaceFirst(OMTIgnored.MULTILINE_COMMENT);
          }
          return dent(OMTIgnored.MULTILINE_COMMENT);
                                                                     }
    {MULTILINECOMMENT}                                     { return returnElement(OMTIgnored.MULTILINE_COMMENT); }
}

// Anything not matches by the above blocks is capted using the [^] regex. Per state a certain response is required:
// These cannot be added to their respective blocks since it would not allow the capturing of the generic items (whitespace, newline and EOL comments)
<YYINITIAL> {
    // Anything not resolving to a property(key) or a sequence is automatically a scalar which will be resolved
    // by the YAML_SCALAR state
    // SCALAR values are encapsulated by a <START> and <END> token to easier parse them and support the many
    // variations of how they can be entered in the YAML format.
    [^]                                                             { return startScalar(); }
}
<CURIE> {
    // The curie will be closed and always returns to the scalar
    [^]                                                             { yypushback(1, "ending curie"); setState(YAML_SCALAR); }
}
<JAVADOCS> {
    // Javadocs can contain anything. The only thing actually able to close the Javadocs and returning to the previous
    // lexical state is JDEND
    [^]                                                             { return returnElement(OMTTypes.JAVADOCS_CONTENT); }
}
<PARAM_ANNOTATION> {
    // Parameter annotations are part of the JavaDocs block and closure should always result to returning to the JAVADOCS state
    // usually the Param annotation is closed by the closing parenthesis, however when this is not done the lexer should
    // simply continue in the JAVADOCS state and consider everything a comment
    [^]                                                             { setState(JAVADOCS, false); return returnElement(OMTTypes.JAVADOCS_CONTENT); }
}

// Not all states have an escape. When the SCALAR hits a bad character it should result in a syntax error
[^]                                                                  { return returnElement(TokenType.BAD_CHARACTER); }
