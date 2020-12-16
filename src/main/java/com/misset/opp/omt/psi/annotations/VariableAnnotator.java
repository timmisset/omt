package com.misset.opp.omt.psi.annotations;

import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTParameterWithType;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.OMTVariableAssignment;
import com.misset.opp.omt.psi.intentions.variables.RenameVariableIntention;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static com.misset.opp.omt.psi.util.UtilManager.*;

public class VariableAnnotator extends OMTAnnotations {
    private static final String PARAMS = "params";

    private void annotateGlobalVariable(@NotNull final OMTVariable variable, @NotNull AnnotationHolder holder) {
        // the magic variables always exist
        holder.newAnnotation(HighlightSeverity.INFORMATION,
                String.format("%s is a global variable which is always available", variable.getName()))
                .create();
    }

    private void annotateIgnoredVariable(@NotNull final OMTVariable variable, @NotNull AnnotationHolder holder) {
        // ignored variable
        holder.newAnnotation(HighlightSeverity.INFORMATION, String.format("%s is used to indicate the variable ignored", variable.getName()))
                .create();
    }

    private void annotateDeclaredVariable(@NotNull final OMTVariable variable, @NotNull AnnotationHolder holder) {
        // check that atleast 1 variable is using the declaration:
        AnnotationBuilder annotationBuilder = getAnnotationUtil().annotateUsageGetBuilder(variable, holder);
        if (annotationBuilder != null) {
            if (variable.getParent() instanceof OMTVariableAssignment &&
                    PsiTreeUtil.getNextSiblingOfType(variable, OMTVariable.class) != null) {
                annotationBuilder = annotationBuilder.withFix(new RenameVariableIntention().getRenameVariableIntention(variable, "$_"));
            }
            annotationBuilder.create();
        }
        annotateUntypedParameter(variable, holder);
    }

    private void annotateUsageVariable(@NotNull final OMTVariable variable, @NotNull AnnotationHolder holder) {
        // variable usage must have exactly 1 resolved value:
        if (variable.getReference() != null && variable.getReference().resolve() == null) {
            // check if it is a local variable:
            HashMap<String, String> localVariables = getVariableUtil().getLocalVariables(variable);
            if (localVariables.containsKey(variable.getName())) {
                holder.newAnnotation(HighlightSeverity.INFORMATION, String.format("%s is locally available in %s", variable.getName(), localVariables.get(variable.getName())))
                        .range(variable)
                        .create();
            } else {
                holder.newAnnotation(HighlightSeverity.ERROR, String.format("%s is not declared", variable.getText()))
                        .range(variable)
                        .create();
            }
        }
    }

    public void annotateVariable(@NotNull final OMTVariable variable, @NotNull AnnotationHolder holder) {
        if (variable.isGlobalVariable()) {
            annotateGlobalVariable(variable, holder);
        } else if (variable.isIgnoredVariable()) {
            annotateIgnoredVariable(variable, holder);
        } else if (variable.isDeclaredVariable()) {
            annotateDeclaredVariable(variable, holder);
        } else {
            annotateUsageVariable(variable, holder);
        }
    }

    private void annotateUntypedParameter(OMTVariable variable, AnnotationHolder holder) {
        if (PsiTreeUtil.getParentOfType(variable, OMTParameterWithType.class) == null &&
                getModelUtil().getEntryBlockLabel(variable).equals(PARAMS)) {
            holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "Annotate parameter with a type").create();
        }
    }
}
