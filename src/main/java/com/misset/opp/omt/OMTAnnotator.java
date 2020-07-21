package com.misset.opp.omt;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.util.CurieUtil;
import com.misset.opp.omt.psi.util.VariableUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static com.misset.opp.omt.psi.intentions.prefix.registerPrefixIntention.getRegisterPrefixIntention;
import static com.misset.opp.omt.psi.intentions.prefix.removePrefixIntention.getRemovePrefixIntention;


public class OMTAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        if(element instanceof OMTCurieElement) {
            annotateCurie(new OMTCurie((OMTCurieElement)element), holder);
        }
        else if(element instanceof OMTCurieConstantElement) {
            annotateCurie(new OMTCurie((OMTCurieConstantElement)element), holder);
        }
        else if(element instanceof OMTPrefix) {
            annotatePrefix((OMTPrefix)element, holder);
        }
        else if(element instanceof OMTVariable) {
            annotateVariable((OMTVariable)element, holder);
        }
        else if(element instanceof OMTVariableAssignmentValue) {
            annotateVariableAssignment((OMTVariableAssignmentValue)element, holder);
        }
    }

    private void annotateVariableAssignment(@NotNull OMTVariableAssignmentValue variableAssignmentValue, @NotNull AnnotationHolder holder) {
        // check if the assigned value is ever used:
        if(!VariableUtil.isVariableAssignmentValueUsed(variableAssignmentValue)) {
            holder.createErrorAnnotation(variableAssignmentValue, "Value is never used");
        }
    }

    private void annotateVariable(@NotNull OMTVariable variable, @NotNull AnnotationHolder holder) {
        // first, check if we are dealing with a variable declare:
        if(VariableUtil.isVariableDeclare(variable)) {
            List<OMTVariable> variableUsage = VariableUtil.getVariableUsage(variable);
            if(variableUsage.isEmpty()) {
                holder.createErrorAnnotation(variable, "Variable is declared but it's never used");
            }
        } else {
            // variable is used, check if it has been declared
            Optional<OMTVariable> declaredByVariable = VariableUtil.getDeclaredByVariable(variable);
            if(!declaredByVariable.isPresent()) {
                holder.createErrorAnnotation(variable, String.format("Variable %s is not defined", variable.getText()));
            }
        }
    }

    private void annotateCurie(@NotNull OMTCurie curie, @NotNull AnnotationHolder holder) {
        Optional<OMTPrefix> definedByPrefix = CurieUtil.getDefinedByPrefix(curie);
        if(!definedByPrefix.isPresent()) {
            Annotation prefix_is_not_defined = holder.createErrorAnnotation(curie.getElement(), "Prefix is not defined");
            prefix_is_not_defined.registerFix(getRegisterPrefixIntention(curie));
        }
    }
    private void annotatePrefix(@NotNull OMTPrefix prefix, @NotNull AnnotationHolder holder) {
        if(CurieUtil.isPrefixedDefinedMoreThanOnce(prefix)) {
            Annotation prefix_is_defined_more_than_once = holder.createErrorAnnotation(prefix, "Prefix is defined more than once");
            prefix_is_defined_more_than_once.registerFix(getRemovePrefixIntention(prefix));
        }
        if(!CurieUtil.isPrefixUsed(prefix)) {
            Annotation prefix_is_not_used = holder.createWarningAnnotation(prefix, "Prefix is defined but it's value is never used");
            prefix_is_not_used.registerFix(getRemovePrefixIntention(prefix));
        }
    }
}

