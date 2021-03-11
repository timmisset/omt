package com.misset.opp.omt;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTDefineName;
import com.misset.opp.omt.psi.OMTDefineQueryStatement;
import com.misset.opp.omt.psi.OMTImport;
import com.misset.opp.omt.psi.OMTImportLocation;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.OMTTypes;
import com.misset.opp.omt.psi.OMTVariable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.misset.opp.util.UtilManager.getImportUtil;

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
                        OMTTypes.NAME,
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
                isModelItemLabel(psiElement) ||
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

    private boolean isModelItemLabel(PsiElement element) {
        return isPropertyLabelInParentOrParentClass(element, OMTModelItemLabel.class);
    }

    private boolean isImportLocationLabel(PsiElement element) {
        return isPropertyLabelInParentOrParentClass(element, OMTImportLocation.class);
    }

    private boolean isPropertyLabelInParentOrParentClass(PsiElement element, Class<? extends PsiElement> clazz) {
        return clazz.isAssignableFrom(element.getClass()) ||
                (element.getParent() != null && clazz.isAssignableFrom(element.getParent().getClass()));
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
        } else if (isModelItemLabel(element)) {
            // get parent or self (non-strict lookup)
            final OMTModelItemLabel modelItemLabel = PsiTreeUtil.getParentOfType(element, OMTModelItemLabel.class, false);
            return modelItemLabel != null ? modelItemLabel.getModelItemType() : "Unknown";
        } else if (isImportLocationLabel(element)) {
            return "OMT File";
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
        if (isModelItemLabel(element)) {
            // get parent or self (non-strict lookup)
            final OMTModelItemLabel modelItemLabel = PsiTreeUtil.getParentOfType(element, OMTModelItemLabel.class, false);
            return modelItemLabel != null ? modelItemLabel.getName() : "";
        } else if (isImportLocationLabel(element)) {
            final OMTImport omtImport = PsiTreeUtil.getParentOfType(element, OMTImport.class);
            if (omtImport == null) return "";

            final VirtualFile importedFile = getImportUtil().getImportedFile(omtImport);
            return importedFile != null ? importedFile.getPresentableUrl() : "";
        }
        return element instanceof PsiNameIdentifierOwner ?
                Objects.requireNonNull(((PsiNameIdentifierOwner) element).getName()) :
                element.getText();
    }

}
