package com.misset.opp.omt;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.misset.opp.omt.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OMTFindUsageProvider implements FindUsagesProvider {
    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(new OMTLexerAdapter("Word scanner"),
                TokenSet.create(
                        OMTTypes.VARIABLE_NAME,
                        OMTTypes.NAMESPACE_PREFIX,
                        OMTTypes.OPERATOR,
                        OMTTypes.COMMAND,
                        OMTTypes.OPERATOR_CALL,
                        OMTTypes.COMMAND_CALL
                ),
                TokenSet.EMPTY,
                TokenSet.EMPTY);
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return (psiElement instanceof OMTVariable) ||
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
            return "Variable";
        } else if (element instanceof OMTModelItemLabel) {
            return ((OMTModelItemLabel) element).getModelItemTypeElement().getText().substring(1);
        } else if (element instanceof OMTDefineName) {
            return element.getParent() instanceof OMTDefineQueryStatement ?
                    "Query" : "Command";
        } else if (element instanceof OMTNamespacePrefix) {
            return "Prefix";
        } else {
            return "";
        }
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        return getNodeText(element, false);
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        if (element instanceof OMTModelItemLabel) {
            return ((OMTModelItemLabel) element).getPropertyLabel().getName();
        }
        return element.getText();
    }
}
