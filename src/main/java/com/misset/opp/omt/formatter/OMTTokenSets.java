package com.misset.opp.omt.formatter;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.TokenSet;

import static com.misset.opp.omt.psi.OMTTypes.*;

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
            CONDITIONAL_OPERATOR
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
            TokenSet.create(SEQUENCE_ITEM, MEMBER_LIST_ITEM)
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
