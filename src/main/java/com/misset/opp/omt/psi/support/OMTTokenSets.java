package com.misset.opp.omt.psi.support;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.TokenSet;

import static com.misset.opp.omt.psi.OMTTypes.*;

public interface OMTTokenSets {
    TokenSet BLOCKS = TokenSet.create(
            BLOCK,
            MODEL_ITEM_BLOCK,
//            SCALAR,
            SEQUENCE,
            SPECIFIC_BLOCK
    );
    TokenSet SPECIFIC_BLOCKS = TokenSet.create(
            MODEL_BLOCK,
            IMPORT_BLOCK,
            EXPORT_BLOCK,
            PREFIX_BLOCK,
            QUERIES_BLOCK,
            COMMANDS_BLOCK,
            MODEL_BLOCK,
            MODULE_BLOCK,
            SPECIFIC_BLOCK
    );
    TokenSet CONTAINERS = TokenSet.orSet(SPECIFIC_BLOCKS, TokenSet.create(
            BLOCK_ENTRY
    ));
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
            MEMBER_LIST
    );
    TokenSet ASSIGNMENT_OPERATORS = TokenSet.create(
            EQUALS,
            CONDITIONAL_OPERATOR
    );
    TokenSet ENTRIES = TokenSet.create(
            MODEL_ITEM_BLOCK,
            BLOCK_ENTRY
    );
}
