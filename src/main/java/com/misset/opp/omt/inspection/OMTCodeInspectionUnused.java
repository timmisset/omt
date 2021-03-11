package com.misset.opp.omt.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTDefineName;
import com.misset.opp.omt.psi.OMTDefineParam;
import com.misset.opp.omt.psi.OMTJdComment;
import com.misset.opp.omt.psi.OMTMember;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.OMTVariableAssignment;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static com.misset.opp.omt.inspection.quickfixes.Remove.getRemoveQuickFix;
import static com.misset.opp.omt.inspection.quickfixes.Rename.getRenameQuickFix;
import static com.misset.opp.util.UtilManager.getModelUtil;

/**
 * Code inspection for all unused declarations
 */
public class OMTCodeInspectionUnused extends AbstractCodeInspection {

    // The model items for which the variables can be removed when unused via a QuickFix
    private static final List<String> validModelItemEntries = Arrays.asList("variables", "params");

    @Override
    public @Nullable @Nls String getStaticDescription() {
        return "Unused declarations of prefixes, variables and defined statements (queries and commands).\n" +
                "These should be removed when no longer used.\n";
    }

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        setHolder(holder);
        if (isTestFile(holder)) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        return new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (element instanceof OMTNamespacePrefix &&
                        element.getParent() instanceof OMTPrefix) {
                    inspectPrefix((OMTNamespacePrefix) element);
                } else if (element instanceof OMTVariable) {
                    inspectVariable((OMTVariable) element);
                } else if (element instanceof OMTDefineName) {
                    inspectDefineName((OMTDefineName) element);
                } else if (element instanceof OMTModelItemLabel) {
                    inspectModelItem((OMTModelItemLabel) element);
                } else if (element instanceof OMTMember) {
                    inspectImportMember((OMTMember) element);
                }
            }
        };
    }

    private void inspectImportMember(OMTMember member) {
        if (!isUsed(member)) {
            registerNeverUsed(member);
        }
    }

    private void inspectModelItem(OMTModelItemLabel modelItemLabel) {
        if (!isUsed(modelItemLabel)) {
            if (modelItemLabel.getModelItemType().equals("Procedure") ||
                    modelItemLabel.getModelItemType().equals("StandaloneQuery")) {
                registerNeverUsed(modelItemLabel, getRemoveQuickFix(modelItemLabel));
            } else {
                registerNeverUsed(modelItemLabel);
            }
        }
    }

    private void inspectPrefix(@NotNull OMTNamespacePrefix namespacePrefix) {
        if (isUsed(namespacePrefix)) return;
        registerNeverUsed(namespacePrefix,
                getRemoveQuickFix((OMTPrefix) namespacePrefix.getParent()));
    }

    private void inspectVariable(@NotNull OMTVariable variable) {
        if (!variable.isDeclaredVariable() || variable.isIgnoredVariable() || isUsed(variable)) return;
        if (variable.getParent() instanceof OMTVariableAssignment) {
            if (PsiTreeUtil.getNextSiblingOfType(variable, OMTVariable.class) != null) {
                // VAR $unused, $used = ... => should be renamed to VAR $_, $used = ...
                registerNeverUsed(variable, getRenameQuickFix(variable, "$_"));
            } else {
                registerNeverUsed(variable, getRemoveQuickFix((OMTVariableAssignment) variable.getParent()));
            }
        } else if (variable.getParent() instanceof OMTDefineParam) {
            registerNeverUsed(variable, getRemoveQuickFix(variable, "defined"));
        } else {
            final String modelItemEntryLabel = getModelUtil().getModelItemEntryLabel(variable);

            if (validModelItemEntries.contains(modelItemEntryLabel)) {
                registerNeverUsed(variable, getRemoveQuickFix(variable, modelItemEntryLabel));
            } else {
                registerNeverUsed(variable);
            }
        }
    }

    private void inspectDefineName(@NotNull OMTDefineName defineName) {
        if (isUsed(defineName)) return;
        registerNeverUsed(defineName, getRemoveQuickFix(defineName));
    }

    private void registerNeverUsed(@NotNull PsiElement element, LocalQuickFix... fixes) {
        holder.registerProblem(element, String.format("%s is never used", element.getText()), ProblemHighlightType.LIKE_UNUSED_SYMBOL, fixes);
    }

    private boolean isUsed(@NotNull PsiElement element) {
        // The ReferencesSearch will target all known references in its range and do a isReferenceTo check
        // if not specified, the isReferenceTo will call resolve() to obtain the references referred element
        // and checks if it equal
        // to customize, modify the isReferenceTo method in the different PsiReference elements itself
        return ReferencesSearch.search(element)
                .anyMatch(psiReference ->
                        PsiTreeUtil.getParentOfType(psiReference.getElement(), OMTJdComment.class) == null &&
                                element != psiReference.getElement());
    }

}
