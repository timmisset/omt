package com.misset.opp.omt;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.tree.TokenSet;
import com.misset.opp.omt.psi.OMTDefineName;
import com.misset.opp.omt.psi.OMTDefineQueryStatement;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.OMTPropertyLabel;
import com.misset.opp.omt.psi.OMTTypes;
import com.misset.opp.omt.psi.OMTVariable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
        return isVariableWithUsage(psiElement) ||
                isModelItemPropertyLabel(psiElement) ||
                psiElement instanceof OMTDefineName ||
                isNamespacePrefixWithUsage(psiElement);
    }

    @Override
    public @Nullable
    @NonNls
    String getHelpId(@NotNull PsiElement psiElement) {
        return "Finding an OMT Element";
    }

    private boolean isVariableWithUsage(PsiElement element) {
        return element instanceof OMTVariable &&
                ((OMTVariable) element).isDeclaredVariable();
    }

    private boolean isNamespacePrefixWithUsage(PsiElement element) {
        return element instanceof OMTNamespacePrefix &&
                element.getParent() instanceof OMTPrefix;
    }

    private boolean isModelItemPropertyLabel(PsiElement element) {
        return element instanceof OMTPropertyLabel &&
                element.getParent() instanceof OMTModelItemLabel;
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement element) {
        if (element instanceof OMTVariable) {
            return "Variable";
        } else if (element instanceof OMTDefineName) {
            return element.getParent() instanceof OMTDefineQueryStatement ?
                    "Query" : "Command";
        } else if (element instanceof OMTNamespacePrefix) {
            return "Prefix";
        } else if (isModelItemPropertyLabel(element)) {
            return ((OMTModelItemLabel) element.getParent()).getModelItemTypeElement().getText().substring(1);
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
        if (isModelItemPropertyLabel(element)) {
            return ((OMTModelItemLabel) element.getParent()).getName();
        }
        return element instanceof PsiNameIdentifierOwner ?
                Objects.requireNonNull(((PsiNameIdentifierOwner) element).getName()) :
                element.getText();
    }

}
