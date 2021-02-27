package com.misset.opp.omt;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.misset.opp.omt.psi.OMTDefineName;
import com.misset.opp.omt.psi.OMTDefineQueryStatement;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.OMTPropertyLabel;
import com.misset.opp.omt.psi.OMTTypes;
import com.misset.opp.omt.psi.OMTVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This method is also used by the refactor-rename dialog to determine the displayname and type
 */
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
                        OMTTypes.COMMAND_CALL,
                        OMTTypes.PROPERTY_LABEL
                ),
                TokenSet.EMPTY,
                TokenSet.EMPTY);
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return variableWithUsage(psiElement) ||
                psiElement instanceof OMTModelItemLabel ||
                psiElement instanceof OMTDefineName ||
                namespacePrefixWithUsage(psiElement);
    }

    private boolean variableWithUsage(PsiElement element) {
        return element instanceof OMTVariable &&
                ((OMTVariable) element).isDeclaredVariable();
    }

    private boolean namespacePrefixWithUsage(PsiElement element) {
        return element instanceof OMTNamespacePrefix &&
                element.getParent() instanceof OMTPrefix;
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
        } else if (isModelItemPropertyLabel(element)) {
            return getType(element.getParent());
        }
        return "";
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
        } else if (isModelItemPropertyLabel(element)) {
            return getNodeText(element.getParent(), useFullName);
        }
        return element.getText();
    }

    private boolean isModelItemPropertyLabel(PsiElement element) {
        return element instanceof OMTPropertyLabel &&
                element.getParent() instanceof OMTModelItemLabel;
    }
}
