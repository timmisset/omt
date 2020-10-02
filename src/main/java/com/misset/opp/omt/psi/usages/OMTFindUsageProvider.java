package com.misset.opp.omt.psi.usages;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.misset.opp.omt.OMTLexerAdapter;
import com.misset.opp.omt.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OMTFindUsageProvider implements FindUsagesProvider {
    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(new OMTLexerAdapter(),
                TokenSet.create(
                        OMTTypes.VARIABLE_NAME,
                        OMTTypes.NAMESPACE_PREFIX,
                        OMTTypes.OPERATOR,
                        OMTTypes.COMMAND
                ),
                TokenSet.EMPTY,
                TokenSet.EMPTY);
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return (psiElement instanceof OMTVariable) ||
                (psiElement instanceof OMTPropertyLabel) ||
                (psiElement instanceof OMTModelItemLabel) ||
                (psiElement instanceof OMTDefineName) ||
                (psiElement instanceof OMTNamespacePrefix);
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
            return element.getClass().getSimpleName();
        }
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        if (element instanceof OMTVariable) {
            return element.getText();
        } else {
            return element.getText();
        }
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        if (element instanceof OMTVariable) {
            return element.getText();
        } else {
            return element.getText();
        }
    }
}
