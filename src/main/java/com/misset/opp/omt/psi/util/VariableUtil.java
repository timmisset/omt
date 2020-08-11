package com.misset.opp.omt.psi.util;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.misset.opp.omt.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class VariableUtil {

    public static boolean isVariableDeclare(OMTVariable variable) {
        return variable.getParent() instanceof OMTSequenceItemValue ||
                variable.getParent() instanceof OMTDefineParam ||
                variable.getParent() instanceof OMTVariableAssignment && isVariableDeclare((OMTVariableAssignment)variable.getParent());

    }
    public static boolean isVariableDeclare(OMTVariableAssignment variableAssignment) {
        return variableAssignment.getParent() instanceof OMTDeclareVariable;
    }
    public static boolean isVariableAssignment(OMTVariable variable) {
        return variable.getParent() instanceof OMTVariableAssignment;
    }
    public static OMTVariable getVariable(OMTSequenceItemValue sequenceItemValue) {
        PsiElement element = sequenceItemValue.getQueryPath() != null ? sequenceItemValue.getQueryPath().getFirstChild() : sequenceItemValue.getFirstChild();
        if(element == null) { return null; }
        if(element instanceof OMTVariableAssignment) { return getVariable((OMTVariableAssignment) element); }
        if(element instanceof OMTQueryStep) { return ((OMTQueryStep) element).getVariable(); }
        if(element instanceof OMTQueryReverseStep) { return ((OMTQueryReverseStep) element).getQueryStep().getVariable(); }
        if(element instanceof OMTVariable) { return (OMTVariable) element; }
        if(element instanceof OMTParameterWithType) { return ((OMTParameterWithType) element).getVariable(); }
        return null;
    }
    public static OMTVariable getVariable(OMTVariableAssignment variableAssignment) {
        return variableAssignment.getVariable();
    }
    public static List<OMTVariable> getAllVariableInstances(OMTVariable variableToFind, PsiElement container) {
        return PsiTreeUtil.findChildrenOfType(container, OMTVariable.class).stream()
                .filter(variable -> variable.getText().equals(variableToFind.getText()))
                .collect(Collectors.toList());
    }

    /**
     * Checks if this variable is part of the OMT model directly and not via a script
     * @param variableInstance
     * @return
     */
    public static boolean isPartOfModel(OMTVariable variableInstance) {
        return PsiTreeUtil.getTopmostParentOfType(variableInstance, OMTScript.class) == null &&
                PsiTreeUtil.getTopmostParentOfType(variableInstance, OMTQueryPath.class) != null &&
                ModelUtil.getModelItemBlock(variableInstance).isPresent();
    }
    public static boolean isVariableAssignmentValueUsed(OMTVariableValue variableAssignmentValue) {
        // first check if the assignmentvalue is part of script or listitem
        if(variableAssignmentValue.getParent() instanceof OMTSequenceItem) {
            return isVariableAssignmentValueUsedInModel(variableAssignmentValue);
        }
        OMTScriptContent scriptContent = PsiTreeUtil.getTopmostParentOfType(variableAssignmentValue, OMTScriptContent.class);
        if(scriptContent != null) {
            OMTScriptContent sibling = PsiTreeUtil.getNextSiblingOfType(scriptContent, OMTScriptContent.class);
            while(sibling != null) {
                Optional<OMTVariable> firstAppearance = getFirstAppearance(
                        getVariable((OMTVariableAssignment)variableAssignmentValue.getParent()
                        ), sibling);
                if(firstAppearance.isPresent()) {
                    return !isVariableAssignment(firstAppearance.get());
                }
                sibling = PsiTreeUtil.getNextSiblingOfType(sibling, OMTScriptContent.class);
            }
        }
        return true;
    }
    public static boolean isVariableAssignmentValueUsedInModel(OMTVariableValue variableAssignmentValue) {
        OMTVariable variable = getVariable((OMTVariableAssignment) variableAssignmentValue.getParent());
        // variable can be used somewhere else in the model:
        Optional<OMTModelItemBlock> optionalOMTBlock = ModelUtil.getModelItemBlock(variableAssignmentValue);
        if(optionalOMTBlock.isPresent()) {
            OMTModelItemBlock modelItemBlock = optionalOMTBlock.get();
            // get the variable usages
            List<OMTVariable> allVariableInstances = getAllVariableInstances(variable, modelItemBlock);
            for(OMTVariable variableInstance : allVariableInstances) {
                // other appearances in the model can only be usages
                // for example, payload or query assignment
                if(variable != variableInstance && isPartOfModel(variableInstance)) { return true; }
            }

            // get the script block(s) in this model:
            Collection<OMTScript> scripts = PsiTreeUtil.findChildrenOfType(modelItemBlock, OMTScript.class);

            // check the appearance in the script, if the variable is used before it's reassigned, it's ok
            for(OMTScript script : scripts) {
                // get the first appearance of this variable:
                Optional<OMTVariable> firstAppearance = getFirstAppearance(variable, script);
                if(firstAppearance.isPresent() && !isVariableAssignment(firstAppearance.get())) {
                    return true;
                }
            }
        }
        return false;
    }
    public static Optional<OMTVariable> getFirstAppearance(OMTVariable variable, PsiElement container) {
        return PsiTreeUtil.findChildrenOfType(container, OMTVariable.class).stream()
                .filter(scriptVariable -> scriptVariable.getText().equals(variable.getText()))
                .findFirst();
    }
    public static List<OMTVariable> getVariableUsage(OMTDeclaredVariable declaredVariable) {
        List<OMTVariable> variables = new ArrayList<>();

        // We first need to determine where the variable declare is part of:
        // a query
        OMTDefineQueryStatement omtDefineQueryStatement = PsiTreeUtil.getTopmostParentOfType(declaredVariable, OMTDefineQueryStatement.class);
        if(omtDefineQueryStatement != null) {
            variables.addAll(
                    PsiTreeUtil.findChildrenOfType(omtDefineQueryStatement.getQueryPath(), OMTVariable.class).stream()
                    .filter(variable -> variable.getText().equals(declaredVariable.getText()))
                    .collect(Collectors.toList())
            );
        }

        // a command
        OMTDefineCommandStatement omtDefineCommandStatement = PsiTreeUtil.getTopmostParentOfType(declaredVariable, OMTDefineCommandStatement.class);
        if(omtDefineCommandStatement != null) {
            variables.addAll(
            PsiTreeUtil.findChildrenOfType(omtDefineCommandStatement.getCommandBlock(), OMTVariable.class).stream()
                    .filter(variable -> variable.getText().equals(declaredVariable.getText()))
                    .collect(Collectors.toList()));
        }

        // a script line:
        Optional<OMTScript> script = ScriptUtil.getScript(declaredVariable);
        script.ifPresent(omtScript -> variables.addAll(
                PsiTreeUtil.findChildrenOfType(omtScript, OMTVariable.class).stream()
                        .filter(variable ->
                                !variable.isDeclaredVariable() &&
                                ScriptUtil.isBefore(variable, declaredVariable) &&
                                variable.getText().equals(declaredVariable.getText()))
                        .collect(Collectors.toList())
        ));

        // in the model item
        Optional<OMTModelItemBlock> modelItem = ModelUtil.getModelItemBlock(declaredVariable);
        modelItem.ifPresent(omtBlock -> variables.addAll(
                PsiTreeUtil.findChildrenOfType(omtBlock, OMTVariable.class).stream()
                    .filter(variable -> variable.getText().equals(declaredVariable.getText()) && !variable.isDeclaredVariable())
                .collect(Collectors.toList())
        ));

        return variables;
    }

    /**
     * Returns the declared variables available at the position of this specific element
     * @param element
     * @return
     */
    public static List<OMTVariable> getDeclaredVariables(PsiElement element) {
        List<OMTVariable> variables = new ArrayList<>();
        // a variable can be declared within a script block using the VAR
        // this must happen before it's usage of course:
        OMTScript script = PsiTreeUtil.getTopmostParentOfType(element, OMTScript.class);
        if(script != null) {
            // now check all the variable declarations that are part of this script:
            final Collection<OMTDeclaredVariable> declaredVariables = PsiTreeUtil.findChildrenOfType(script, OMTDeclaredVariable.class);
            variables.addAll(
                    declaredVariables.stream()
                    .filter(declaredVariable -> ScriptUtil.isBefore(declaredVariable, element))
                    .map(declaredVariable -> (OMTVariable)declaredVariable.getParent())
                    .collect(Collectors.toList())
            );
        }

        // or it can be declared by its containing method: DECLARE COMMAND($myVar) => $myVar
        Optional<OMTDefineParam> definedParameters = getDefinedParameters(element);
        definedParameters.ifPresent(omtDefineParam -> variables.addAll(omtDefineParam.getVariableList()));

        // and finally, by it's containing model item (!Activity, !Procedure etc)
        variables.addAll(getModelItemEntryVariables(element, "params"));
        variables.addAll(getModelItemEntryVariables(element, "variables"));

        return variables;
    }
    public static Optional<OMTVariable> getDeclaredByVariable(OMTVariable variable) {
        if(isVariableDeclare(variable)) { return Optional.of(variable); }

        List<OMTVariable> declaredVariables = getDeclaredVariables(variable);
        return declaredVariables.stream()
                .filter(declaredVariable -> declaredVariable.getText().equals(variable.getText()))
                .findFirst();
    }

    public static Optional<OMTDefineParam> getDefinedParameters(PsiElement element) {
        // defined parameters can be part of query
        OMTDefineQueryStatement omtDefineQueryStatement = PsiTreeUtil.getTopmostParentOfType(element, OMTDefineQueryStatement.class);
        if(omtDefineQueryStatement != null && omtDefineQueryStatement.getDefineParam() != null) {
            return Optional.of(omtDefineQueryStatement.getDefineParam());
        }

        // or a command
        OMTDefineCommandStatement omtDefineCommandStatement = PsiTreeUtil.getTopmostParentOfType(element, OMTDefineCommandStatement.class);
        if(omtDefineCommandStatement != null && omtDefineCommandStatement.getDefineParam() != null) {
            return Optional.of(omtDefineCommandStatement.getDefineParam());
        }

        return Optional.empty();
    }

    public static List<OMTVariable> getModelItemEntryVariables(PsiElement element, String propertyLabel) {
        Optional<OMTBlockEntry> modelItemBlockEntry = ModelUtil.getModelItemBlockEntry(element, propertyLabel);
        if(modelItemBlockEntry.isPresent()) {
            return modelItemBlockEntry.get().getSequence().getSequenceItemList().stream()
                    .map(OMTSequenceItem::getSequenceItemValue)
                    .map(VariableUtil::getVariable)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public static void annotateVariable(@NotNull final OMTVariable variable, @NotNull AnnotationHolder holder) {
        if(variable.isGlobalVariable()) {
            // the magic variables always exist
            holder.createInfoAnnotation(variable, String.format("%s is a global variable which is always available", variable.getName()));
            return;
        }
        if(variable.isDeclaredVariable()) {
            // check that atleast 1 variable is using the declaration:
            AnnotationUtil.annotateUsage(variable, OMTVariable.class, holder);
        } else {
            // variable usage must have exactly 1 resolved value:
            AnnotationUtil.annotateOrigin(variable, holder);
        }
    }
}
