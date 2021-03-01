package com.misset.opp.omt.inspection.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTDeclareVariable;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.OMTPrefixBlock;
import com.misset.opp.omt.psi.OMTScriptLine;
import com.misset.opp.omt.psi.OMTVariableAssignment;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Remove {
    public static LocalQuickFix getRemoveQuickFix(OMTPrefix prefix) {
        return new LocalQuickFix() {
            @Override
            public @IntentionFamilyName @NotNull String getFamilyName() {
                return "Remove";
            }

            @Override
            public @IntentionName @NotNull String getName() {
                return isLastPrefix() ? "Remove prefix block" : "Remove prefix";
            }

            private boolean isLastPrefix() {
                return ((OMTPrefixBlock) prefix.getParent()).getPrefixList().size() == 1;
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

    public static LocalQuickFix getRemoveQuickFix(OMTVariableAssignment variableAssignment) {
        OMTDeclareVariable declareVariable = (OMTDeclareVariable) variableAssignment.getParent();
        return new LocalQuickFix() {
            @Override
            public @IntentionFamilyName @NotNull String getFamilyName() {
                return "Remove";
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
}
