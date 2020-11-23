package com.misset.opp.omt;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.misset.opp.omt.psi.OMTFile;
import org.intellij.sdk.language.parser.OMTParser;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.omt.psi.OMTIgnored.END_OF_LINE_COMMENT;
import static com.misset.opp.omt.psi.OMTTypes.Factory;
import static com.misset.opp.omt.psi.OMTTypes.STRING;

public class OMTParserDefinition implements ParserDefinition {
    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);

    public static final IFileElementType FILE = new IFileElementType(OMTLanguage.INSTANCE);

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new OMTLexerAdapter();
    }

    @NotNull
    @Override
    public TokenSet getWhitespaceTokens() {
        return TokenSet.create(
                TokenType.WHITE_SPACE
        );
//        return WHITE_SPACES;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return TokenSet.create(
                END_OF_LINE_COMMENT
        );
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return TokenSet.create(
                STRING
        );
    }

    @NotNull
    @Override
    public PsiParser createParser(final Project project) {
        return new OMTParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new OMTFile(viewProvider);
    }

    @Override
    public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode node) {
        return Factory.createElement(node);
    }
}
