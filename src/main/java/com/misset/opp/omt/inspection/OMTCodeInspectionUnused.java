package com.misset.opp.omt.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTDeclareVariable;
import com.misset.opp.omt.psi.OMTDefineName;
import com.misset.opp.omt.psi.OMTDefineParam;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTGenericBlock;
import com.misset.opp.omt.psi.OMTImport;
import com.misset.opp.omt.psi.OMTImportSource;
import com.misset.opp.omt.psi.OMTJdComment;
import com.misset.opp.omt.psi.OMTMember;
import com.misset.opp.omt.psi.OMTMemberListItem;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.OMTSequenceItem;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.OMTVariableAssignment;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.omt.psi.OMTTypes.DEFINE_NAME;
import static com.misset.opp.omt.psi.OMTTypes.MEMBER;
import static com.misset.opp.omt.psi.OMTTypes.MODEL_ITEM_LABEL;
import static com.misset.opp.omt.psi.OMTTypes.NAMESPACE_PREFIX;
import static com.misset.opp.omt.psi.OMTTypes.VARIABLE;
import static com.misset.opp.util.UtilManager.getMemberUtil;

/**
 * Code inspection for all unused declarations
 */
public class OMTCodeInspectionUnused extends AbstractCodeInspection {

    private static final TokenSet ELEMENTS = TokenSet.create(
            VARIABLE, MEMBER, MODEL_ITEM_LABEL, DEFINE_NAME, NAMESPACE_PREFIX
    );

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        setHolder(holder);
        if (isTestFile(holder)) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        return new PsiElementVisitor() {

            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (ELEMENTS.contains(element.getNode().getElementType()) && isUnused(element)) {
                    if (element instanceof OMTVariable) {
                        inspectVariable((OMTVariable) element);
                    } else if (element instanceof OMTMember && getMemberUtil().isImportedMember((OMTMember) element)) {
                        inspectImportMember((OMTMember) element);
                    } else if (element instanceof OMTModelItemLabel) {
                        inspectModelItem((OMTModelItemLabel) element);
                    } else if (element instanceof OMTDefineName) {
                        registerNeverUsed(element,
                                getRemoveQuickFix(PsiTreeUtil.getParentOfType(element, OMTDefinedStatement.class)));
                    } else if (element instanceof OMTNamespacePrefix) {
                        inspectPrefix((OMTNamespacePrefix) element);
                    }
                }
            }

            private void inspectPrefix(OMTNamespacePrefix namespacePrefix) {
                if (!(namespacePrefix.getParent() instanceof OMTPrefix)) return;
                registerNeverUsed(namespacePrefix,
                        getRemoveQuickFix(PsiTreeUtil.getParentOfType(namespacePrefix, OMTPrefix.class)));
            }

            private void inspectModelItem(OMTModelItemLabel modelItemLabel) {
                // components are not used in OMT itself, only loaded into Angular component
                if (modelItemLabel.getModelItemTypeElement().getText().equals("!Component")) return;

                registerNeverUsed(modelItemLabel,
                        getRemoveQuickFix(PsiTreeUtil.getParentOfType(modelItemLabel, OMTModelItemBlock.class)));
            }

            private void inspectImportMember(OMTMember member) {
                // do an additional check for imports that are not used in the file itself, it might be that they
                // are used indirectly by files that import the current file
                if (hasDeferredImportReference(member)) return;
                registerNeverUsed(member, getRemoveQuickFix(PsiTreeUtil.getParentOfType(member, OMTMemberListItem.class)));
            }

            private boolean hasDeferredImportReference(OMTMember member) {
                return ReferencesSearch.search(member.getContainingFile()).anyMatch(
                        psiReference -> psiReference.getElement() instanceof OMTImportSource &&
                                ((OMTImport) psiReference.getElement().getParent()).getMemberList().getMemberListItemList().stream().anyMatch(
                                        omtMemberListItem -> omtMemberListItem.getName().equals(member.getName())
                                )
                );
            }

            private void inspectVariable(OMTVariable variable) {
                if (!variable.isIgnoredVariable() && variable.isDeclaredVariable()) {

                    if (isNonRemovableVariable(variable)) {
                        // only add fixes for variables that can be safely removed
                        registerNeverUsed(variable);
                    } else {
                        LocalQuickFix localQuickFix = variable.getParent() instanceof OMTVariableAssignment &&
                                PsiTreeUtil.getNextSiblingOfType(variable, OMTVariable.class) != null ?
                                getRenameQuickFix(variable, "$_") :
                                getRemoveQuickFix(
                                        PsiTreeUtil.findFirstParent(variable,
                                                parent -> parent instanceof OMTDeclareVariable ||
                                                        parent instanceof OMTSequenceItem));
                        registerNeverUsed(variable, localQuickFix);
                    }
                }
            }

            private boolean isNonRemovableVariable(OMTVariable variable) {
                return PsiTreeUtil.findFirstParent(variable, parent ->
                        parent instanceof OMTDefineParam ||
                                (parent instanceof OMTGenericBlock && !((OMTGenericBlock) parent).getPropertyLabel().getName().equals("variables"))
                ) != null;
            }

            private void registerNeverUsed(PsiElement element, LocalQuickFix... fixes) {
                holder.registerProblem(element, String.format("%s is never used", element.getText()), ProblemHighlightType.LIKE_UNUSED_SYMBOL, fixes);
            }
        };
    }

    private boolean isUnused(PsiElement element) {
        return !ReferencesSearch.search(element)
                .anyMatch(psiReference ->
                        psiReference.getElement().getContainingFile() instanceof OMTFile &&
                                PsiTreeUtil.getParentOfType(psiReference.getElement(), OMTJdComment.class) == null &&
                                element != psiReference.getElement());
    }

}
