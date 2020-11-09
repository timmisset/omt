package com.misset.opp.omt.psi.util;

import com.google.gson.JsonObject;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.external.util.builtIn.BuiltInMember;
import com.misset.opp.omt.external.util.builtIn.BuiltInType;
import com.misset.opp.omt.external.util.builtIn.BuiltInUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.intentions.variables.AnnotateParameterIntention;
import com.misset.opp.omt.psi.intentions.variables.RenameVariableIntention;
import com.misset.opp.omt.psi.support.OMTCall;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class VariableUtil {

    public static final VariableUtil SINGLETON = new VariableUtil();
    private ModelUtil modelUtil = ModelUtil.SINGLETON;
    private ScriptUtil scriptUtil = ScriptUtil.SINGLETON;
    private AnnotationUtil annotationUtil = AnnotationUtil.SINGLETON;
    private BuiltInUtil builtInUtil = BuiltInUtil.SINGLETON;

    public Optional<OMTVariable> getFirstAppearance(OMTVariable variable, PsiElement container) {
        return PsiTreeUtil.findChildrenOfType(container, OMTVariable.class).stream()
                .filter(scriptVariable -> scriptVariable.getText().equals(variable.getText()))
                .findFirst();
    }

    /**
     * Returns the declared variables available at the position of this specific element
     *
     * @param element
     * @return
     */
    public List<OMTVariable> getDeclaredVariables(PsiElement element) {
        List<OMTVariable> variables = new ArrayList<>();
        // a variable can be declared within a script block using the VAR
        // this must happen before it's usage of course:
        PsiElement topContainer = PsiTreeUtil.getTopmostParentOfType(element, OMTScript.class);
        if (topContainer == null) {
            topContainer = PsiTreeUtil.getTopmostParentOfType(element, OMTCommandBlock.class);
        }
        if (topContainer != null) {
            // now check all the variable declarations that are part of this script or command block
            variables.addAll(
                    scriptUtil.getAccessibleElements(element, OMTVariable.class).stream()
                            .map(omtVariable -> (OMTVariable) omtVariable)
                            .filter(OMTVariable::isDeclaredVariable)
                            .collect(Collectors.toList())
            );
        }

        // or it can be declared by its containing method: DECLARE COMMAND($myVar) => $myVar
        Optional<OMTDefineParam> definedParameters = getDefinedParameters(element);
        definedParameters.ifPresent(omtDefineParam -> variables.addAll(omtDefineParam.getVariableList()));

        // and finally, by it's containing model item (!Activity, !Procedure etc)
        variables.addAll(getBlockEntryDeclaredVariables(element));

        return variables;
    }

    public Optional<OMTVariable> getDeclaredByVariable(OMTVariable variable) {
        if (variable.isDeclaredVariable()) {
            return Optional.of(variable);
        }

        List<OMTVariable> declaredVariables = getDeclaredVariables(variable);
        return declaredVariables.stream()
                .filter(declaredVariable -> declaredVariable.getText().equals(variable.getText()))
                .findFirst();
    }

    private Optional<OMTDefineParam> getDefinedParameters(PsiElement element) {
        // defined parameters can be part of query
        OMTDefineQueryStatement omtDefineQueryStatement = PsiTreeUtil.getTopmostParentOfType(element, OMTDefineQueryStatement.class);
        if (omtDefineQueryStatement != null && omtDefineQueryStatement.getDefineParam() != null) {
            return Optional.of(omtDefineQueryStatement.getDefineParam());
        }

        // or a command
        OMTDefineCommandStatement omtDefineCommandStatement = PsiTreeUtil.getTopmostParentOfType(element, OMTDefineCommandStatement.class);
        if (omtDefineCommandStatement != null && omtDefineCommandStatement.getDefineParam() != null) {
            return Optional.of(omtDefineCommandStatement.getDefineParam());
        }

        return Optional.empty();
    }

    private List<OMTVariable> getBlockEntryDeclaredVariables(PsiElement element) {
        List<OMTBlockEntry> connectedEntries = modelUtil.getConnectedEntries(element, Arrays.asList("params", "variables", "bindings", "base"));
        List<OMTVariable> variables = new ArrayList<>();
        connectedEntries.forEach(omtBlockEntry -> variables.addAll(
                PsiTreeUtil.findChildrenOfType(omtBlockEntry, OMTVariable.class).stream()
                        .filter(OMTVariable::isDeclaredVariable)
                        .collect(Collectors.toList())
        ));
        return variables;
    }

    public void annotateVariable(@NotNull final OMTVariable variable, @NotNull AnnotationHolder holder) {
        if (variable.isGlobalVariable()) {
            // the magic variables always exist
            holder.newAnnotation(HighlightSeverity.INFORMATION, String.format("%s is a global variable which is always available", variable.getName()))
                    .range(variable)
                    .create();
            return;
        }
        if (variable.isIgnoredVariable()) {
            // the magic variables always exist
            holder.newAnnotation(HighlightSeverity.INFORMATION, String.format("%s is used to indicate the variable ignored", variable.getName()))
                    .range(variable)
                    .create();
            return;
        }
        if (variable.isDeclaredVariable()) {
            // check that atleast 1 variable is using the declaration:
            AnnotationBuilder annotationBuilder = annotationUtil.annotateUsageGetBuilder(variable, OMTVariable.class, holder);
            if (annotationBuilder != null) {
                if (variable.getParent() instanceof OMTVariableAssignment &&
                        PsiTreeUtil.getNextSiblingOfType(variable, OMTVariable.class) != null) {
                    annotationBuilder.withFix(RenameVariableIntention.SINGLETON.getRenameVariableIntention(variable, "$_"));
                }
                annotationBuilder.create();
            }
        } else {
            // variable usage must have exactly 1 resolved value:
            if (variable.getReference() != null && variable.getReference().resolve() == null) {
                // check if it is a local variable:
                HashMap<String, String> localVariables = getLocalVariables(variable);
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
    }

    /**
     * Returns a list of variables that are locally available for the element
     *
     * @param element
     * @return
     */
    public HashMap<String, String> getLocalVariables(@NotNull PsiElement element) {
        HashMap<String, String> localVariables = new HashMap<>();
        while (element.getParent() != null && !(element instanceof PsiFile)) {
            // from builtIn members
            if (element instanceof OMTCall) {
                OMTCall call = (OMTCall) element;
                BuiltInMember builtInMember = builtInUtil.getBuiltInMember(call.getName(),
                        call.canCallCommand() ? BuiltInType.Command : BuiltInType.Operator);
                if (builtInMember != null) {
                    builtInMember.getLocalVariables().forEach(
                            variable -> localVariables.put(variable, builtInMember.getName())
                    );
                }
            }
            JsonObject attributes = modelUtil.getJson(element);
            if (attributes != null && attributes.has("variables")) {
                attributes.get("variables").getAsJsonArray().forEach(variable -> {
                    localVariables.put(variable.getAsString(), attributes.get("name").getAsString());
                });
            }
            element = element.getParent();
        }
        return localVariables;
    }

    public boolean isDeclaredVariable(OMTVariable variable) {
        PsiElement lookWith = variable;
        if (PsiTreeUtil.findFirstParent(lookWith, parent -> parent instanceof OMTVariableValue) != null) {
            return false;
        }
        if (lookWith.getParent() instanceof OMTParameterWithType) {
            lookWith = variable.getParent();
        }
        // check if direct parent is the VAR ... statement.
        if (lookWith.getParent() instanceof OMTDeclareVariable) {
            return true;
        }
        if (lookWith.getParent() instanceof OMTDefineParam) {
            return true;
        }
        // if part of an assignment: VAR $myVariable = 'test'; OR $myVariable = test (in the variables: block)
        if (lookWith.getParent() instanceof OMTVariableAssignment) {
            if (lookWith.getParent().getParent() instanceof OMTDeclareVariable) {
                return true;
            }
            if (lookWith.getParent().getParent() instanceof OMTScalarValue) {
                if (partOfBlockEntryLevel(lookWith, "variables")) {
                    return true;
                }
            }
        }
        // check if part of the base: property of a standalone query
        if (partOfBlockEntryLevel(lookWith, "base")) {
            return true;
        }
        // check if part of the bindings: property of a component
        if (partOfModelItemEntryLevel(lookWith, "bindings")) {
            return true;
        }

        OMTScalarValue asScalarValue = (OMTScalarValue) PsiTreeUtil.findFirstParent(lookWith, parent -> parent instanceof OMTScalarValue);
        return partOfBlockEntryLevel(asScalarValue, "variables") || partOfBlockEntryLevel(asScalarValue, "params");
    }

    private boolean partOfBlockEntryLevel(PsiElement element, String entryLevelLabel) {
        String blockEntryLabel = modelUtil.getEntryBlockLabel(element);
        return blockEntryLabel != null && blockEntryLabel.equals(entryLevelLabel);
    }

    private boolean partOfModelItemEntryLevel(PsiElement element, String entryLevelLabel) {
        String blockEntryLabel = modelUtil.getModelItemEntryLabel(element);
        return blockEntryLabel != null && blockEntryLabel.equals(entryLevelLabel);
    }

    public void annotateDefineParameter(OMTDefineParam defineParam, AnnotationHolder holder) {
        defineParam.getVariableList().forEach(omtVariable -> {
            final List<Resource> type = omtVariable.getType();
            if (type.isEmpty()) {
                holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "Annotate parameter with type")
                        .tooltip(String.format("Annotate parameter %s with a type, this help to resolve the query path%n%n" +
                                "/**" +
                                "%n* @param %s (pol:Classname)%n" +
                                "*/", omtVariable.getName(), omtVariable.getName()))
                        .range(omtVariable)
                        .withFix(AnnotateParameterIntention.SINGLETON.getAnnotateParameterIntention(defineParam, omtVariable.getName()))
                        .create();
            }
        });
    }
}
