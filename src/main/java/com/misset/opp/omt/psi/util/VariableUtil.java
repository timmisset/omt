package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;
import org.apache.maven.model.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VariableUtil {

    public static boolean isVariableDeclare(OMTVariable variable) {
        return variable.getParent() instanceof OMTListItemParameter ||
                variable.getParent() instanceof OMTDefineParam ||
                variable.getParent() instanceof OMTVariableAssignment && isVariableDeclare((OMTVariableAssignment)variable.getParent());

    }
    public static boolean isVariableDeclare(OMTVariableAssignment variableAssignment) {
        return variableAssignment.getParent() instanceof OMTDeclareVariable;
    }
    public static boolean isVariableAssignment(OMTVariable variable) {
        return variable.getParent() instanceof OMTVariableAssignment;
    }
    public static OMTVariable getVariable(OMTListItemVariable listItemVariable) {
        return listItemVariable.getListItemParameter().getVariable();
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
    public static boolean isVariableAssignmentValueUsed(OMTVariableAssignmentValue variableAssignmentValue) {
        // first check if the assignmentvalue is part of script or listitem
        if(variableAssignmentValue.getParent() instanceof OMTListItemVariable) {
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
    public static boolean isVariableAssignmentValueUsedInModel(OMTVariableAssignmentValue variableAssignmentValue) {
        OMTVariable variable = getVariable((OMTListItemVariable)variableAssignmentValue.getParent());
        // variable can be used somewhere else in the model:
        Optional<OMTBlock> optionalOMTBlock = ModelUtil.getModelItemBlock(variableAssignmentValue);
        if(optionalOMTBlock.isPresent()) {
            OMTBlock modelItemBlock = optionalOMTBlock.get();
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
    public static List<OMTVariable> getVariableUsage(OMTVariable variable) {
        List<OMTVariable> variables = new ArrayList<>();

        // We first need to determine where the variable declare is part of:
        // a query
        OMTDefineQueryStatement omtDefineQueryStatement = PsiTreeUtil.getTopmostParentOfType(variable, OMTDefineQueryStatement.class);
        if(omtDefineQueryStatement != null) {
            variables.addAll(
                    PsiTreeUtil.findChildrenOfType(omtDefineQueryStatement.getQueryPath(), OMTVariable.class).stream()
                    .filter(omtVariable -> omtVariable.getText().equals(variable.getText()))
                    .collect(Collectors.toList())
            );
        }

        // a command
        OMTDefineCommandStatement omtDefineCommandStatement = PsiTreeUtil.getTopmostParentOfType(variable, OMTDefineCommandStatement.class);
        if(omtDefineCommandStatement != null) {
            // TODO: OMT Commands
            // variables.addAll(PsiTreeUtil.findChildrenOfType(omtDefineCommandStatement.(), OMTVariable.class));
        }

        // a script line:
        Optional<OMTScript> script = ScriptUtil.getScript(variable);
        script.ifPresent(omtScript -> variables.addAll(
                PsiTreeUtil.findChildrenOfType(omtScript, OMTVariable.class).stream()
                        .filter(omtVariable ->
                                !isVariableAssignment(variable) &&
                                ScriptUtil.isBefore(variable, omtVariable) &&
                                variable.getText().equals(omtVariable.getText()))
                        .collect(Collectors.toList())
        ));

        // in the model item
        Optional<OMTBlock> modelItem = ModelUtil.getModelItemBlock(variable);
        modelItem.ifPresent(omtBlock -> variables.addAll(
                PsiTreeUtil.findChildrenOfType(omtBlock, OMTVariable.class).stream()
                    .filter(omtVariable -> omtVariable.getText().equals(variable.getText()) &&
                            !VariableUtil.isVariableDeclare(omtVariable))
                .collect(Collectors.toList())
        ));

        return variables;
    }

    public static Optional<OMTVariable> getDeclaredByVariable(OMTVariable variable) {
        if(isVariableDeclare(variable)) { return Optional.of(variable); }

        // a variable can be declared within a script block using the VAR
        // this must happen before it's usage of course:
        OMTScript script = PsiTreeUtil.getTopmostParentOfType(variable, OMTScript.class);
        if(script != null) {
            // now check all the variable declarations that are part of this script:
            final Collection<OMTDeclareVariable> declaredVariables = PsiTreeUtil.findChildrenOfType(script, OMTDeclareVariable.class);
            Optional<OMTVariable> omtDeclaredVariable = declaredVariables.stream().map(OMTDeclareVariable::getVariableAssignmentList)
                    .flatMap(Collection::stream)
                    .filter(variableAssignment ->
                            ScriptUtil.isBefore(variableAssignment, variable) &&
                            variableAssignment.getVariable().getText().equals(variable.getText()))
                    .map(OMTVariableAssignment::getVariable)
                    .findFirst();
            if(omtDeclaredVariable.isPresent()) { return omtDeclaredVariable; }
        }

        // or it can be declared by its containing method: DECLARE COMMAND($myVar) => $myVar
        Optional<OMTDefineParam> definedParameters = getDefinedParameters(variable);
        if(definedParameters.isPresent()) {
            Optional<OMTVariable> omtDeclaredVariable = definedParameters.get().getVariableList().stream()
                    .filter(definedParameter -> definedParameter.getText().equals(variable.getText()))
                    .findFirst();
            if(omtDeclaredVariable.isPresent()) { return omtDeclaredVariable; }
        }

        // and finally, by it's containing model item (!Activity, Procedure etc)
        Optional<OMTBlock> modelItemBlock = ModelUtil.getModelItemBlock(variable);
        if(modelItemBlock.isPresent()) {
            // check all variables and parameters declared as listitem parameters:
            Collection<OMTListItemParameter> variables = PsiTreeUtil.findChildrenOfType(modelItemBlock.get(), OMTListItemParameter.class);
            Optional<OMTVariable> omtDeclaredVariable = variables.stream()
                    .map(OMTListItemParameter::getVariable)
                    .filter(omtVariable -> omtVariable.getText().equals(variable.getText()))
                    .findFirst();
            if(omtDeclaredVariable.isPresent()) { return omtDeclaredVariable; }
        }

        return Optional.empty();
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

}
