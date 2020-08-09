package com.misset.opp.omt.psi.usages;

import com.intellij.lang.cacheBuilder.*;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.*;
import com.intellij.psi.tree.TokenSet;
import com.misset.opp.omt.OMTLexerAdapter;
import com.misset.opp.omt.psi.OMTTypes;
import com.misset.opp.omt.psi.OMTVariable;
import org.jetbrains.annotations.*;

public class VariablesFindUsageProvider implements FindUsagesProvider {
    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(new OMTLexerAdapter(),
                TokenSet.create(OMTTypes.VARIABLE_NAME),
                TokenSet.EMPTY,
                TokenSet.EMPTY);
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return psiElement instanceof PsiNamedElement;
    }

    @Nullable
    @Override
    public String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement element) {
        if (element instanceof OMTVariable) {
            return "variable";
        } else {
            return "";
        }
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        if (element instanceof OMTVariable) {
            return ((OMTVariable) element).getText();
        } else {
            return "";
        }
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        if (element instanceof OMTVariable) {
            return element.getText();
        } else {
            return "";
        }
    }
}
