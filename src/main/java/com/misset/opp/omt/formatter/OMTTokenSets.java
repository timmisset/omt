package com.misset.opp.omt.formatter;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.TokenSet;

import static com.misset.opp.omt.psi.OMTIgnored.END_OF_LINE_COMMENT;
import static com.misset.opp.omt.psi.OMTIgnored.MULTILINE_COMMENT;
import static com.misset.opp.omt.psi.OMTTypes.BOOLEAN_STATEMENT;
import static com.misset.opp.omt.psi.OMTTypes.CHOOSE_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.CHOOSE_OPERATOR;
import static com.misset.opp.omt.psi.OMTTypes.COMMANDS_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.CONDITIONAL_OPERATOR;
import static com.misset.opp.omt.psi.OMTTypes.DEDENT;
import static com.misset.opp.omt.psi.OMTTypes.DEDENT2;
import static com.misset.opp.omt.psi.OMTTypes.DEDENT_TOKEN;
import static com.misset.opp.omt.psi.OMTTypes.DEFINE_COMMAND_STATEMENT;
import static com.misset.opp.omt.psi.OMTTypes.DEFINE_QUERY_STATEMENT;
import static com.misset.opp.omt.psi.OMTTypes.EMPTY_ARRAY;
import static com.misset.opp.omt.psi.OMTTypes.END_PATH;
import static com.misset.opp.omt.psi.OMTTypes.END_TOKEN;
import static com.misset.opp.omt.psi.OMTTypes.EQUALS;
import static com.misset.opp.omt.psi.OMTTypes.EQUATION_STATEMENT;
import static com.misset.opp.omt.psi.OMTTypes.EXPORT_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.FORWARD_SLASH;
import static com.misset.opp.omt.psi.OMTTypes.GENERIC_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.IMPORT;
import static com.misset.opp.omt.psi.OMTTypes.IMPORT_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.IMPORT_LOCATION;
import static com.misset.opp.omt.psi.OMTTypes.INDENT;
import static com.misset.opp.omt.psi.OMTTypes.INDENT2;
import static com.misset.opp.omt.psi.OMTTypes.INDENT_TOKEN;
import static com.misset.opp.omt.psi.OMTTypes.INTERPOLATION_TEMPLATE;
import static com.misset.opp.omt.psi.OMTTypes.JAVADOCS_CONTENT;
import static com.misset.opp.omt.psi.OMTTypes.JAVADOCS_END;
import static com.misset.opp.omt.psi.OMTTypes.LAMBDA;
import static com.misset.opp.omt.psi.OMTTypes.MEMBER_LIST;
import static com.misset.opp.omt.psi.OMTTypes.MEMBER_LIST_ITEM;
import static com.misset.opp.omt.psi.OMTTypes.MODEL_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.MODEL_ITEM_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.MODEL_ITEM_LABEL;
import static com.misset.opp.omt.psi.OMTTypes.MODULE_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.OTHERWISE_PATH;
import static com.misset.opp.omt.psi.OMTTypes.PREFIX;
import static com.misset.opp.omt.psi.OMTTypes.PREFIX_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.PROPERTY_LABEL;
import static com.misset.opp.omt.psi.OMTTypes.QUERIES_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.QUERY_ARRAY;
import static com.misset.opp.omt.psi.OMTTypes.QUERY_FILTER;
import static com.misset.opp.omt.psi.OMTTypes.QUERY_PATH;
import static com.misset.opp.omt.psi.OMTTypes.QUERY_STEP;
import static com.misset.opp.omt.psi.OMTTypes.SCRIPT;
import static com.misset.opp.omt.psi.OMTTypes.SCRIPT_LINE;
import static com.misset.opp.omt.psi.OMTTypes.SEQUENCE;
import static com.misset.opp.omt.psi.OMTTypes.SEQUENCE_ITEM;
import static com.misset.opp.omt.psi.OMTTypes.SIGNATURE_ARGUMENT;
import static com.misset.opp.omt.psi.OMTTypes.START_TOKEN;
import static com.misset.opp.omt.psi.OMTTypes.STRING;
import static com.misset.opp.omt.psi.OMTTypes.SUB_QUERY;
import static com.misset.opp.omt.psi.OMTTypes.WHEN_PATH;

public interface OMTTokenSets {
    // tokenset that is used to determine next child indentation
    TokenSet INCOMPLETE = TokenSet.create(
            MODEL_ITEM_LABEL,
            MODEL_ITEM_BLOCK,
            PROPERTY_LABEL,
            SEQUENCE,
            MEMBER_LIST,
            IMPORT_LOCATION
    );
    TokenSet ROOTBLOCK_ENTRIES = TokenSet.create(
            IMPORT_BLOCK,
            PREFIX_BLOCK,
            QUERIES_BLOCK,
            COMMANDS_BLOCK,
            MODEL_BLOCK
    );

    TokenSet WHITESPACE = TokenSet.create(
            TokenType.WHITE_SPACE,
            INDENT_TOKEN,
            DEDENT_TOKEN,
            INDENT2,
            INDENT,
            DEDENT2,
            DEDENT,
            START_TOKEN,
            END_TOKEN,
            EMPTY_ARRAY
    );
    TokenSet DEFINED_STATEMENTS = TokenSet.create(
            DEFINE_COMMAND_STATEMENT,
            DEFINE_QUERY_STATEMENT
    );

    TokenSet ASSIGNMENT_OPERATORS = TokenSet.create(
            EQUALS,
            CONDITIONAL_OPERATOR,
            LAMBDA
    );
    TokenSet ENTRIES = TokenSet.create(
            MODEL_ITEM_BLOCK,
            GENERIC_BLOCK,
            IMPORT_BLOCK,
            EXPORT_BLOCK,
            PREFIX_BLOCK,
            QUERIES_BLOCK,
            COMMANDS_BLOCK,
            MODEL_BLOCK,
            MODULE_BLOCK,
            IMPORT,
            PREFIX
    );
    TokenSet COMMENTS = TokenSet.create(
            END_OF_LINE_COMMENT,
            MULTILINE_COMMENT
    );

    TokenSet CHOOSE_INNER = TokenSet.create(
            WHEN_PATH, OTHERWISE_PATH
    );
    TokenSet CHOOSE_OUTER = TokenSet.create(
            CHOOSE_OPERATOR, END_PATH
    );
    TokenSet INDENTED_QUERY_STEPS = TokenSet.orSet(CHOOSE_OUTER,
            TokenSet.create(QUERY_FILTER, QUERY_PATH, SUB_QUERY, QUERY_STEP, CHOOSE_BLOCK, FORWARD_SLASH, EQUATION_STATEMENT,
                    QUERY_ARRAY, BOOLEAN_STATEMENT));
    TokenSet INDENTED_SCRIPT_PARTS = TokenSet.create(SCRIPT, SIGNATURE_ARGUMENT);
    TokenSet INDENTED_JAVA_DOCS_PARTS = TokenSet.create(JAVADOCS_CONTENT, JAVADOCS_END);

    // SAME_LEVEL_ALIGNMENTS will be used to register an alignment if they are the first instance
    // in their group or otherwise use the sibling alignment of the same type
    TokenSet SAME_LEVEL_ALIGNMENTS = TokenSet.orSet(
            DEFINED_STATEMENTS,
            ENTRIES,
            CHOOSE_OUTER,
            TokenSet.create(SEQUENCE_ITEM, MEMBER_LIST_ITEM, SCRIPT_LINE, JAVADOCS_CONTENT)
    );

    TokenSet INTERPOLATED_STRING_COMPONENTS = TokenSet.create(
            STRING, INTERPOLATION_TEMPLATE
    );
//    TokenSet JAVADOCS = TokenSet.create(
//            JAVADOCS_CONTENT, JAVADOCS_START, JAVADOCS_END
//    );
}
