package com.misset.opp.omt.inspection.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTDeclareVariable;
import com.misset.opp.omt.psi.OMTDefineName;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.OMTPrefixBlock;
import com.misset.opp.omt.psi.OMTScriptLine;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.OMTVariableAssignment;
import com.misset.opp.omt.psi.named.OMTCall;
import com.misset.opp.omt.psi.support.OMTDefinedBlock;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import com.misset.opp.omt.psi.support.OMTModifiableContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.misset.opp.util.UtilManager.getModelUtil;

public class Remove {
    private static final String FAMILY_NAME = "Remove";

    public static LocalQuickFix getRemoveQuickFix(OMTPrefix prefix) {
        return new LocalQuickFix() {
            @Override
            public @IntentionFamilyName @NotNull String getFamilyName() {
                return FAMILY_NAME;
            }

            @Override
            public @IntentionName @NotNull String getName() {
                return isLastPrefix() ? "Remove prefix block" : "Remove prefix";
            }

            private boolean isLastPrefix() {
                return
                        ReadAction.compute(() -> ((OMTPrefixBlock) prefix.getParent()).getPrefixList().size() == 1);
            }

            @Override
            public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
                if (isLastPrefix()) {
                    prefix.getParent().delete();
                } else {
                    prefix.delete();
                }
            }
        };
    }

    public static LocalQuickFix getRemoveQuickFix(OMTVariable variable, String container) {
        // a declared variable at this point will be declared in the OMT scope itself
        // removing them via a QuickFix will depend on the ability to remove them from any call to the container
        // for example, all unused parameters
        final OMTModifiableContainer modifiableContainer = PsiTreeUtil.getParentOfType(variable, OMTModifiableContainer.class);
        if (modifiableContainer == null) {
            return null;
        }
        int numberOfChildren = modifiableContainer.numberOfChildren();
        int targetPosition = modifiableContainer.getChildPosition(variable);
        return new LocalQuickFix() {
            @Override
            public @IntentionFamilyName @NotNull String getFamilyName() {
                return FAMILY_NAME;
            }

            @Override
            public @IntentionName @NotNull String getName() {
                if ("variables".equals(container)) {
                    return "Remove variable";
                } else if ("params".equals(container)) {
                    return "Remove parameter and refactor call signatures to this " + getModelUtil().getModelItemType(variable);
                } else if ("defined".equals(container)) {
                    return "Remove parameter and refactor call signatures to this " + getDefinedType();
                } else {
                    return "Unsupported type for quickfix";
                }
            }

            private String getDefinedType() {
                final OMTDefinedStatement parentOfType = ReadAction.compute(() -> PsiTreeUtil.getParentOfType(variable, OMTDefinedStatement.class));
                if (parentOfType == null) {
                    return "Unknown";
                }
                return parentOfType.isQuery() ? "Query" : "Command";
            }

            private void cleanUpReferencesToModelItem(OMTModelItemBlock modelItemBlock) {
                cleanUpCallReferences(modelItemBlock.getModelItemLabel());
            }

            private void cleanUpReferencesToDefinedStatement(OMTDefinedStatement definedStatement) {
                if (definedStatement == null) {
                    return;
                }
                cleanUpCallReferences(definedStatement.getDefineName());
            }

            private void cleanUpCallReferences(PsiElement psiElement) {
                ReferencesSearch.search(psiElement)
                        .mapping(PsiReference::getElement)
                        .filtering(element -> element instanceof OMTCall)
                        .mapping(element -> (OMTCall) element)
                        .forEach(this::cleanUpCallReference);
            }

            private void cleanUpCallReference(OMTCall call) {
                if (call.getSignature() != null &&
                        call.getSignature().getSignatureArgumentList().size() == numberOfChildren) {
                    call.getSignature().removeChildAtPosition(targetPosition);
                }
            }

            @Override
            public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
                if ("params".equals(container)) {
                    // remove any signature arguments to this block, only calls to model-items are currently supported:
                    getModelUtil().getModelItemBlock(variable).ifPresent(this::cleanUpReferencesToModelItem);
                } else if ("defined".equals(container)) {
                    this.cleanUpReferencesToDefinedStatement(PsiTreeUtil.getParentOfType(variable, OMTDefinedStatement.class));
                }
                modifiableContainer.removeChild(variable);
            }
        };
    }

    /**
     * Remove quickfix for OMTVariableAssignment (VAR $variable = '';)
     *
     * @param variableAssignment
     * @return
     */
    public static LocalQuickFix getRemoveQuickFix(OMTVariableAssignment variableAssignment) {
        OMTDeclareVariable declareVariable = (OMTDeclareVariable) variableAssignment.getParent();
        return new LocalQuickFix() {
            @Override
            public @IntentionFamilyName @NotNull String getFamilyName() {
                return FAMILY_NAME;
            }

            @Override
            public @IntentionName @NotNull String getName() {
                return keepAssignment() ? "Remove variable (keep command)" : "Remove variable assignment";
            }

            private boolean keepAssignment() {
                return variableAssignment.getVariableValue().getCommandCall() != null;
            }

            @Override
            public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
                if (keepAssignment()) {
                    declareVariable.replace(
                            Objects.requireNonNull(variableAssignment.getVariableValue().getCommandCall())
                    );
                } else {
                    final OMTScriptLine scriptLine = PsiTreeUtil.getParentOfType(declareVariable, OMTScriptLine.class);
                    if (scriptLine != null) {
                        scriptLine.delete();
                    } else {
                        declareVariable.delete();
                    }
                }
            }
        };
    }

    public static LocalQuickFix getRemoveQuickFix(OMTDefineName defineName) {
        final OMTDefinedStatement definedStatement = (OMTDefinedStatement) defineName.getParent();
        if (definedStatement == null) {
            return null;
        }
        return new LocalQuickFix() {
            @Override
            public @IntentionFamilyName @NotNull String getFamilyName() {
                return FAMILY_NAME;
            }

            @Override
            public @IntentionName @NotNull String getName() {
                return "Remove " + (definedStatement.isQuery() ? "Query" : "Command");
            }

            @Override
            public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
                OMTDefinedBlock block = (OMTDefinedBlock) definedStatement.getParent();
                definedStatement.delete();
                if (block.getStatements().isEmpty()) {
                    block.delete();
                }
            }
        };
    }
}
