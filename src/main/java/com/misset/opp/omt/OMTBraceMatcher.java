package com.misset.opp.omt;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.misset.opp.omt.psi.OMTTypes.*;

public class OMTBraceMatcher implements PairedBraceMatcher {

    private static final BracePair[] pairs = new BracePair[]{
            new BracePair(CURLY_OPEN, CURLY_CLOSED, true),
            new BracePair(BRACKET_OPEN, BRACKET_CLOSED, true),
            new BracePair(PARENTHESES_OPEN, PARENTHESES_CLOSE, true),
    };


    @NotNull
    @Override
    public BracePair[] getPairs() {
        return pairs;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
