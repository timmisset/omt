package com.misset.opp.ttl;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.misset.opp.ttl.psi.TTLTypes;

%%

%class TTLLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%column
%line

%eof{  return;
%eof}

IRIREF	                                =	"<"([^#x00-#x20<>\"{} | \^\`\\] | {UCHAR})* ">" /* #x00=NULL #01-#x1F=control codes #x20=space */
PNAME_NS                                =	{PN_PREFIX}? ":"
PNAME_LN                                =	{PNAME_NS} {PN_LOCAL}
BLANK_NODE_LABEL                        =	"_:" ({PN_CHARS_U} | [0-9]) (({PN_CHARS} | '.')* {PN_CHARS})?
LANGTAG                                 =	'@' [a-zA-Z]+ ('-' [a-zA-Z0-9]+)*
INTEGER                                 =	[+-]? [0-9]+
DECIMAL                                 =	[+-]? [0-9]* '.' [0-9]+
DOUBLE                                  =	[+-]? ([0-9]+ '.' [0-9]* {EXPONENT} | '.' [0-9]+ {EXPONENT} | [0-9]+ {EXPONENT})
EXPONENT                                =	[eE] [+-]? [0-9]+
STRING_LITERAL_QUOTE                    =	"\"" ([^#x22#x5C#xA#xD] | {ECHAR} | {UCHAR})* "\"" /* #x22=" #x5C=\ #xA=new line #xD=carriage return */
STRING_LITERAL_SINGLE_QUOTE             =	"\'" ([^#x27#x5C#xA#xD] | {ECHAR} | {UCHAR})* "\'" /* #x27=' #x5C=\ #xA=new line #xD=carriage return */
STRING_LITERAL_LONG_SINGLE_QUOTE        =	"\'\'\'" (("\'" | "\'\'")? ([^\'\\] | {ECHAR} | {UCHAR}))* "\'\'\'"
STRING_LITERAL_LONG_QUOTE               =	"\"\"\"" (("\"" | "\"\"\"")? ([^\"\\] | {ECHAR} | {UCHAR}))* "\"\"\""
UCHAR                                   =	"\u" {HEX} {HEX} {HEX} {HEX} | "\U" {HEX} {HEX} {HEX} {HEX} {HEX} {HEX} {HEX} {HEX}
ECHAR                                   =	"\\" [tbnrf\"\'\\]
WS                                      =	#x20 | #x9 | #xD | #xA /* #x20=space #x9=character tabulation #xD=carriage return #xA=new line */
ANON                                    =	"[" {WS}* "]"
PN_CHARS_BASE                           =	[A-Z] | [a-z] | [#x00C0-#x00D6] | [#x00D8-#x00F6] | [#x00F8-#x02FF] | [#x0370-#x037D] | [#x037F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
PN_CHARS_U                              =	{PN_CHARS_BASE} | "_"
PN_CHARS                                =	{PN_CHARS_U} | "-" | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040]
PN_PREFIX                               =	{PN_CHARS_BASE} (({PN_CHARS} | '.')* {PN_CHARS})?
PN_LOCAL                                =	({PN_CHARS_U} | ":" | [0-9] | PLX) (({PN_CHARS} | "." | ":" | {PLX})* ({PN_CHARS} | ":" | {PLX}))?
PLX                                     =	{PERCENT} | {PN_LOCAL_ESC}
PERCENT                                 =	"%" {HEX} {HEX}
HEX                                     =	[0-9] | [A-F] | [a-f]
PN_LOCAL_ESC                            =	'\' ('_' | '~' | '.' | '-' | '!' | '$' | '&' | "'" | '(' | ')' | '*' | '+' | ',' | ';' | '=' | '/' | '?' | '#' | '@' | '%')

WHITE_SPACE                             =   [\ \f\t]
NEWLINE                                 =   (\r\n) | (\r) | (\n)

%%
<YYINITIAL> {
    {WHITE_SPACE}                       { return TTLTypes.WHITE_SPACE; }
    '@prefix'                           { return TTLTypes.atPrefix; }
    '@base'                             { return TTLTypes.atBase; }
}
