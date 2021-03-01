package com.misset.opp.omt.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTJdComment;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.OMTVariableAssignment;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.misset.opp.omt.inspection.quickfixes.Remove.getRemoveQuickFix;
import static com.misset.opp.omt.inspection.quickfixes.Rename.getRenameQuickFix;

/**
 * Code inspection for all unused declarations
 */
public class OMTCodeInspectionUnused extends AbstractCodeInspection {

    @Override
    public @Nullable @Nls String getStaticDescription() {
        return "UnusedDeclarations";
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
                }
            }

            private void inspectPrefix(OMTNamespacePrefix namespacePrefix) {
                if (isUsed(namespacePrefix)) return;
                registerNeverUsed(namespacePrefix,
                        getRemoveQuickFix(PsiTreeUtil.getParentOfType(namespacePrefix, OMTPrefix.class)));
            }
            private void inspectVariable(OMTVariable variable) {
                if (!variable.isDeclaredVariable() || isUsed(variable) || variable.isIgnoredVariable()) return;
                if (variable.getParent() instanceof OMTVariableAssignment) {
                    if (PsiTreeUtil.getNextSiblingOfType(variable, OMTVariable.class) != null) {
                        // VAR $unused, $used = ... => should be renamed to VAR $_, $used = ...
                        registerNeverUsed(variable, getRenameQuickFix(variable, "$_"));
                    } else {
                        registerNeverUsed(variable, getRemoveQuickFix((OMTVariableAssignment) variable.getParent()));
                    }
                }
            }

            private void registerNeverUsed(PsiElement element, LocalQuickFix... fixes) {
                holder.registerProblem(element, String.format("%s is never used", element.getText()), ProblemHighlightType.LIKE_UNUSED_SYMBOL, fixes);
            }
        };
    }

    private boolean isUsed(PsiElement element) {
        return ReferencesSearch.search(element)
                .anyMatch(psiReference ->
                        psiReference.getElement().getContainingFile() instanceof OMTFile &&
                                PsiTreeUtil.getParentOfType(psiReference.getElement(), OMTJdComment.class) == null &&
                                element != psiReference.getElement());
    }

}
