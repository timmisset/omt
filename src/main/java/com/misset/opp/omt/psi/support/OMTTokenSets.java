package com.misset.opp.omt.psi.support;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.TokenSet;

import static com.misset.opp.omt.psi.OMTTypes.*;

public interface OMTTokenSets {
    TokenSet WHITESPACE = TokenSet.create(
            TokenType.WHITE_SPACE,
            INDENT_TOKEN,
            DEDENT_TOKEN,
            START_TOKEN,
            END_TOKEN,
            EMPTY_ARRAY
    );
    TokenSet SEQUENCES = TokenSet.create(
            IMPORT,
            PREFIX,
            DEFINE_COMMAND_STATEMENT,
            DEFINE_QUERY_STATEMENT
    );
    TokenSet QUERY_INDENTATION_PARENT = TokenSet.create(
            SUB_QUERY, FORWARD_SLASH, WHEN_PATH, OTHERWISE_PATH
    );
    TokenSet ASSIGNMENT_OPERATORS = TokenSet.create(
            EQUALS,
            CONDITIONAL_OPERATOR
    );
    TokenSet ENTRIES = TokenSet.create(
            MODEL_ITEM_BLOCK,
            BLOCK_ENTRY
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
}
