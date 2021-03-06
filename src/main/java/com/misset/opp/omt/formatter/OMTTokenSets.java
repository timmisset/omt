package com.misset.opp.omt.formatter;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.TokenSet;

import static com.misset.opp.omt.psi.OMTTypes.ASTERIX;
import static com.misset.opp.omt.psi.OMTTypes.BOOLEAN_OPERATOR;
import static com.misset.opp.omt.psi.OMTTypes.BOOLEAN_STATEMENT;
import static com.misset.opp.omt.psi.OMTTypes.CARET;
import static com.misset.opp.omt.psi.OMTTypes.CHOOSE_OPERATOR;
import static com.misset.opp.omt.psi.OMTTypes.COMMANDS_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.CONDITIONAL_OPERATOR;
import static com.misset.opp.omt.psi.OMTTypes.CURIE_CONSTANT_ELEMENT;
import static com.misset.opp.omt.psi.OMTTypes.CURIE_CONSTANT_ELEMENT_PREFIX;
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
import static com.misset.opp.omt.psi.OMTTypes.INTERPOLATED_STRING;
import static com.misset.opp.omt.psi.OMTTypes.JAVADOCS_CONTENT;
import static com.misset.opp.omt.psi.OMTTypes.JAVADOCS_END;
import static com.misset.opp.omt.psi.OMTTypes.JAVADOCS_START;
import static com.misset.opp.omt.psi.OMTTypes.LAMBDA;
import static com.misset.opp.omt.psi.OMTTypes.MEMBER_LIST;
import static com.misset.opp.omt.psi.OMTTypes.MEMBER_LIST_ITEM;
import static com.misset.opp.omt.psi.OMTTypes.MODEL_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.MODEL_ITEM_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.MODEL_ITEM_LABEL;
import static com.misset.opp.omt.psi.OMTTypes.MODULE_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.NEGATED_STEP;
import static com.misset.opp.omt.psi.OMTTypes.NOT_OPERATOR;
import static com.misset.opp.omt.psi.OMTTypes.OTHERWISE_PATH;
import static com.misset.opp.omt.psi.OMTTypes.PIPE;
import static com.misset.opp.omt.psi.OMTTypes.PLUS;
import static com.misset.opp.omt.psi.OMTTypes.PREFIX;
import static com.misset.opp.omt.psi.OMTTypes.PREFIX_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.PROPERTY_LABEL;
import static com.misset.opp.omt.psi.OMTTypes.QUERIES_BLOCK;
import static com.misset.opp.omt.psi.OMTTypes.QUERY;
import static com.misset.opp.omt.psi.OMTTypes.QUERY_ARRAY;
import static com.misset.opp.omt.psi.OMTTypes.QUERY_FILTER;
import static com.misset.opp.omt.psi.OMTTypes.QUERY_PATH;
import static com.misset.opp.omt.psi.OMTTypes.QUERY_REVERSE_STEP;
import static com.misset.opp.omt.psi.OMTTypes.QUERY_STEP;
import static com.misset.opp.omt.psi.OMTTypes.SCALAR;
import static com.misset.opp.omt.psi.OMTTypes.SCALAR_VALUE;
import static com.misset.opp.omt.psi.OMTTypes.SCRIPT_LINE;
import static com.misset.opp.omt.psi.OMTTypes.SEQUENCE;
import static com.misset.opp.omt.psi.OMTTypes.SEQUENCE_ITEM;
import static com.misset.opp.omt.psi.OMTTypes.START_TOKEN;
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
    // SAME_LEVEL_ALIGNMENTS will be used to register an alignment if they are the first instance
    // in their group or otherwise use the sibling alignment of the same type
    TokenSet SAME_LEVEL_ALIGNMENTS = TokenSet.orSet(
            DEFINED_STATEMENTS,
            ENTRIES,
            TokenSet.create(SEQUENCE_ITEM, MEMBER_LIST_ITEM, SCRIPT_LINE)
    );
    TokenSet CONTAINERS = TokenSet.create(
            MEMBER_LIST
    );
    TokenSet CHOOSE_OUTER = TokenSet.create(
            CHOOSE_OPERATOR, END_PATH
    );
    TokenSet CHOOSE_INNER = TokenSet.create(
            WHEN_PATH, OTHERWISE_PATH
    );
    TokenSet CHOOSE = TokenSet.orSet(CHOOSE_INNER, CHOOSE_OUTER);
    TokenSet JAVADOCS = TokenSet.create(
            JAVADOCS_CONTENT, JAVADOCS_START, JAVADOCS_END
    );
    TokenSet SKIPPABLE_CONTAINERS = TokenSet.create(
            SCALAR_VALUE, SCALAR
    );
    // ALL QUERIES AND PARTS THAT CAN BE USED FOR ALIGNMENT
    TokenSet QUERIES = TokenSet.create(QUERY, QUERY_PATH, QUERY_ARRAY, BOOLEAN_STATEMENT, EQUATION_STATEMENT, NEGATED_STEP);
    TokenSet QUERY_TOKENS = TokenSet.create(FORWARD_SLASH, PIPE, BOOLEAN_OPERATOR, CONDITIONAL_OPERATOR, NOT_OPERATOR);
    TokenSet QUERYSTEPS = TokenSet.create(QUERY_STEP, CURIE_CONSTANT_ELEMENT, QUERY_REVERSE_STEP, SUB_QUERY, INTERPOLATED_STRING);
    TokenSet QUERYSTEP_TOKENS = TokenSet.create(CURIE_CONSTANT_ELEMENT_PREFIX, FORWARD_SLASH, CARET, ASTERIX, PLUS);
    TokenSet ALL_QUERY_TOKENS = TokenSet.orSet(QUERIES, QUERY_TOKENS, QUERYSTEPS, QUERYSTEP_TOKENS);
    TokenSet ALL_QUERY_TOKENS_AND_FILTER = TokenSet.orSet(QUERIES, QUERY_TOKENS, QUERYSTEPS, QUERYSTEP_TOKENS, TokenSet.create(QUERY_FILTER));
}
