package com.misset.opp.omt.annotations;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.intentions.variables.RenameVariableIntention;
import com.misset.opp.omt.psi.OMTParameterWithType;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.OMTVariableAssignment;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static com.misset.opp.omt.psi.util.UtilManager.getModelUtil;
import static com.misset.opp.omt.psi.util.UtilManager.getVariableUtil;

public class VariableAnnotator extends AbstractAnnotator {

    public VariableAnnotator(AnnotationHolder annotationHolder) {
        super(annotationHolder);
    }

    private static final String PARAMS = "params";

    private void annotateGlobalVariable(@NotNull final OMTVariable variable) {
        // the magic variables always exist
        setInformation(String.format("%s is a global variable which is always available", variable.getName()));
    }

    private void annotateIgnoredVariable(@NotNull final OMTVariable variable) {
        // ignored variable
        setInformation(String.format("%s is used to indicate the variable ignored", variable.getName()));
    }

    private void annotateDeclaredVariable(@NotNull final OMTVariable variable) {
        // check that atleast 1 variable is using the declaration:
        annotateUsage(variable, annotationBuilder ->
        {
            if (variable.getParent() instanceof OMTVariableAssignment &&
                    PsiTreeUtil.getNextSiblingOfType(variable, OMTVariable.class) != null) {
                annotationBuilder.withFix(new RenameVariableIntention().getRenameVariableIntention(variable, "$_"));
            }
        });
        annotateUntypedParameter(variable);
    }

    private void annotateUsageVariable(@NotNull final OMTVariable variable) {
        HashMap<String, String> localVariables = getVariableUtil().getLocalVariables(variable);
        if (localVariables.containsKey(variable.getName())) {
            setInformation(String.format("%s is locally available in %s", variable.getName(), localVariables.get(variable.getName())));
        } else {
            validateReference(variable, String.format("%s is not declared", variable.getText()), annotationBuilder -> annotationBuilder.highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL));
        }
    }

    public void annotate(@NotNull final PsiElement element) {
        if (element instanceof OMTVariable) {
            OMTVariable variable = (OMTVariable) element;
            if (variable.isGlobalVariable()) {
                annotateGlobalVariable(variable);
            } else if (variable.isIgnoredVariable()) {
                annotateIgnoredVariable(variable);
            } else if (variable.isDeclaredVariable()) {
                annotateDeclaredVariable(variable);
            } else {
                annotateUsageVariable(variable);
            }
        }
    }

    private void annotateUntypedParameter(OMTVariable variable) {
        if (PsiTreeUtil.getParentOfType(variable, OMTParameterWithType.class) == null &&
                getModelUtil().getEntryBlockLabel(variable).equals(PARAMS)) {
            setWeakWarning("Annotate parameter with a type");
        }
    }

}
