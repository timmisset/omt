package com.misset.opp.omt.psi.util;

import com.google.gson.JsonObject;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.impl.OMTBuiltInMember;
import com.misset.opp.omt.psi.impl.OMTQueryReverseStepImpl;
import com.misset.opp.omt.psi.named.OMTCall;
import com.misset.opp.omt.psi.support.BuiltInType;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.util.UtilManager.*;

public class VariableUtil {

    private static final String PARAMS = "params";
    private static final String VARIABLES = "variables";
    private static final String BASE = "base";
    private static final String BINDINGS = "bindings";
    private static final String NAME = "name";

    private static final String GLOBAL_VARIABLE_USERNAME = "$username";
    private static final String GLOBAL_VARIABLE_MEDEWERKER_GRAPH = "$medewerkerGraph";
    private static final String GLOBAL_VARIABLE_OFFLINE = "$offline";

    public Optional<OMTVariable> getFirstAppearance(OMTVariable variable, PsiElement container) {
        return PsiTreeUtil.findChildrenOfType(container, OMTVariable.class).stream()
                .filter(scriptVariable -> scriptVariable.textMatches(variable))
                .findFirst();
    }

    /**
     * Returns the declared variables available at the position of this specific element
     */
    public List<OMTVariable> getDeclaredVariables(PsiElement element) {
        // Get the declared variable from the script
        List<OMTVariable> variables = getScriptUtil().getAccessibleElements(element, OMTVariable.class).stream()
                .filter(OMTVariable::isDeclaredVariable).collect(Collectors.toCollection(ArrayList::new));

        // OR from the DEFINE statement
        Optional<OMTDefineParam> definedParameters = getDefinedParameters(element);
        definedParameters.ifPresent(omtDefineParam -> variables.addAll(omtDefineParam.getVariableList()));

        // OR from the Model
        variables.addAll(getBlockEntryDeclaredVariables(element));

        return variables;
    }

    public Optional<OMTVariable> getDeclaredByVariable(OMTVariable variable) {
        if (variable.isDeclaredVariable()) {
            return Optional.empty();
        }

        List<OMTVariable> declaredVariables = getDeclaredVariables(variable);
        return declaredVariables.stream()
                .filter(declaredVariable -> declaredVariable.getName().equals(variable.getName()))
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
        List<OMTVariable> variables = new ArrayList<>();
        Optional<OMTModelItemBlock> modelItemBlock = getModelUtil().getModelItemBlock(element);
        if (modelItemBlock.isEmpty()) {
            return variables;
        }
        final OMTModelItemBlock omtModelItemBlock = modelItemBlock.get();

        final List<PsiElement> blocks = PsiTreeUtil.collectParents(element, OMTBlock.class, false, parent -> parent == omtModelItemBlock);
        blocks.add(omtModelItemBlock);

        final List<String> entryLabels = Arrays.asList(PARAMS, VARIABLES, BASE, BINDINGS);
        blocks.forEach(block -> variables.addAll(
                PsiTreeUtil.findChildrenOfType(block, OMTVariable.class).stream()
                        .filter(variable -> variable.isDeclaredVariable() &&
                                (entryLabels.contains(getModelUtil().getEntryBlockLabel(variable)) ||
                                        entryLabels.contains(getModelUtil().getModelItemEntryLabel(variable))))
                        .collect(Collectors.toList())
        ));
        return variables.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Returns a list of variables that are locally available for the element
     */
    public HashMap<String, String> getLocalVariables(@NotNull PsiElement element) {
        HashMap<String, String> localVariables = new HashMap<>();
        while (element.getParent() != null && !(element instanceof PsiFile)) {
            // from builtIn members
            if (element instanceof OMTCall) {
                OMTCall call = (OMTCall) element;
                OMTBuiltInMember builtInMember = getBuiltinUtil().getBuiltInMember(call.getName(),
                        call.canCallCommand() ? BuiltInType.Command : BuiltInType.Operator);
                if (builtInMember != null) {
                    builtInMember.getLocalVariables().forEach(
                            variable -> localVariables.put(variable, builtInMember.getName())
                    );
                }
            }
            JsonObject attributes = getModelUtil().getJson(element);
            if (attributes != null && attributes.has(VARIABLES)) {
                attributes.get(VARIABLES).getAsJsonArray().forEach(variable ->
                        localVariables.put(variable.getAsString(), attributes.get(NAME).getAsString()));
            }
            element = element.getParent();
        }
        return localVariables;
    }

    public boolean isDeclaredVariable(OMTVariable variable) {
        if (PsiTreeUtil.findFirstParent(variable, parent -> parent instanceof OMTVariableValue) != null) {
            return false;
        }

        final PsiElement parent = variable.getParent();

        // ODT defined parameters
        if (parent instanceof OMTDeclareVariable ||
                parent instanceof OMTDefineParam ||
                parent.getParent() instanceof OMTDeclareVariable) {
            return true;
        }
        // OMT defined parameters
        final List<String> validDefinedEntries = Arrays.asList(VARIABLES, PARAMS, BINDINGS, BASE);
        String modelItemEntryLabel = getModelUtil().getModelItemEntryLabel(variable);
        String entryBlockLabel = getModelUtil().getEntryBlockLabel(variable);
        return validDefinedEntries.contains(modelItemEntryLabel) || validDefinedEntries.contains(entryBlockLabel);

    }

    public List<Resource> getTypeSuggestions(OMTDefineParam defineParam, OMTVariable variableToAnnotate) {
        List<Resource> types = new ArrayList<>();
        PsiTreeUtil.findChildrenOfType(defineParam.getParent(), OMTVariable.class).stream().filter(
                variable -> variable.getReference() != null &&
                        variable != variableToAnnotate &&
                        variable.getReference().isReferenceTo(variableToAnnotate)
        ).forEach(
                variable -> {
                    final OMTQuery query = (OMTQuery) PsiTreeUtil.findFirstParent(variable, parent -> parent instanceof OMTQuery);
                    if (query != null) {
                        if (query.getParent() instanceof OMTEquationStatement) {
                            final OMTEquationStatement equationStatement = (OMTEquationStatement) query.getParent();
                            final OMTQuery opposite = equationStatement.getOpposite(query);
                            types.addAll(new OMTQueryReverseStepImpl(opposite.getNode()).resolveToResource());
                        } else {
                            if (query instanceof OMTQueryPath) {
                                final OMTQueryStep queryStep = (OMTQueryStep) PsiTreeUtil.findFirstParent(variable, parent -> parent instanceof OMTQueryStep);
                                addTypeSuggestionsFromQueryStep((OMTQueryPath) query, queryStep, types);
                            }
                        }
                    }
                }
        );
        return getRDFModelUtil().getDistinctResources(types);
    }

    private void addTypeSuggestionsFromQueryStep(OMTQueryPath queryPath, OMTQueryStep queryStep, List<Resource> types) {
        final int stepIndex = queryPath.getQueryStepList().indexOf(queryStep);
        if (stepIndex >= 0 && queryPath.getQueryStepList().size() > stepIndex + 1) {
            // take the next step and try to resolve it:
            final OMTQueryStep omtQueryStep = queryPath.getQueryStepList().get(stepIndex + 1);
            if (omtQueryStep.getCurieElement() != null) {
                final Resource predicate = omtQueryStep.getCurieElement().getAsResource();
                types.addAll(getRDFModelUtil().getPredicateSubjects(predicate));
                return;
            }
            if (omtQueryStep instanceof OMTQueryReverseStep &&
                    omtQueryStep.getQueryStep() != null &&
                    omtQueryStep.getQueryStep().getCurieElement() != null) {
                final Resource predicate = omtQueryStep.getQueryStep().getCurieElement().getAsResource();
                types.addAll(getRDFModelUtil().getPredicateObjects(predicate));
            }
        }
    }

    /**
     * Retrieves the parameter type for the parameter define statement via the annotation (if available)
     */
    public List<Resource> getType(OMTDefineParam defineParam, OMTVariable variable) {
        final Optional<OMTParameterAnnotation> parameterAnnotation = getTypeFromAnnotation(variable, defineParam.getParent());
        return parameterAnnotation.isPresent() ? parameterAnnotation.get().getParameterWithType().getType() : new ArrayList<>();
    }

    public List<Resource> getType(OMTVariable variable, String propertyLabel) {
        final Optional<OMTBlockEntry> blockEntry = getModelUtil().getModelItemBlockEntry(variable, propertyLabel);
        if (blockEntry.isEmpty()) {
            return new ArrayList<>();
        }

        final Optional<OMTParameterAnnotation> parameterAnnotation = getTypeFromAnnotation(variable, blockEntry.get());
        return parameterAnnotation.isPresent() ? parameterAnnotation.get().getParameterWithType().getType() : new ArrayList<>();
    }

    public Optional<OMTParameterAnnotation> getTypeFromAnnotation(PsiElement target, PsiElement container) {
        final Collection<OMTParameterAnnotation> parameterAnnotations = PsiTreeUtil.findChildrenOfType(container, OMTParameterAnnotation.class);
        return parameterAnnotations.stream().filter(omtParameterAnnotation -> {
            final OMTParameterWithType parameterWithType = omtParameterAnnotation.getParameterWithType();
            final PsiReference reference = parameterWithType.getVariable().getReference();
            return reference != null && reference.isReferenceTo(target);
        }).findFirst();
    }

    public List<Resource> getType(OMTVariableAssignment variableAssignment) {
        return getType(variableAssignment, 0);
    }

    public List<Resource> getType(OMTVariableAssignment variableAssignment, int index) {
        // only use-case of a second index variable for now is the committed boolean
        if (index > 0) {
            return getRDFModelUtil().getPrimitiveTypeAsResourceList("boolean");
        }
        final OMTVariableValue variableValue = variableAssignment.getVariableValue();
        // commands that return the type passed into the first argument
        List<String> resolvableCommands = Arrays.asList("NEW", "COPY_IN_GRAPH", "ASSIGN");
        if (variableValue.getCommandCall() != null) {
            if (resolvableCommands.contains(variableValue.getCommandCall().getName()) &&
                    variableValue.getCommandCall().getSignature() != null
            ) {
                final List<OMTSignatureArgument> signatureArgumentList = variableValue.getCommandCall().getSignature().getSignatureArgumentList();
                if (!signatureArgumentList.isEmpty()) {
                    return Objects.requireNonNull(signatureArgumentList.get(0).getResolvableValue()).resolveToResource();
                }
            }
        }
        if (variableValue.getQuery() != null) {
            return variableValue.getQuery().resolveToResource();
        }
        return new ArrayList<>();
    }

    public List<Resource> getType(OMTVariable variable) {
        if (variable.isDeclaredVariable()) {
            // a declared variable
            if (variable.getParent() instanceof OMTParameterWithType) {
                return getType((OMTParameterWithType) variable.getParent());
            } else if (variable.getParent() instanceof OMTDefineParam) {
                return getType((OMTDefineParam) variable.getParent(), variable);
            } else if (variable.getParent() instanceof OMTVariableAssignment) {
                return getType((OMTVariableAssignment) variable.getParent());
            } else if (Arrays.asList(VARIABLES, PARAMS, BINDINGS).contains(getModelUtil().getEntryBlockLabel(variable))) {
                return getType(variable, getModelUtil().getEntryBlockLabel(variable));
            }
        } else {
            if (variable.isGlobalVariable()) {
                return new ArrayList<>();
            }
            if (variable.isIgnoredVariable()) {
                return new ArrayList<>();
            }
            OMTVariable declaredByVariable = getDeclaredByVariable(variable).orElse(null);
            final List<Resource> types = new ArrayList<>(declaredByVariable != null ? declaredByVariable.getType() : new ArrayList<>());
            // and all possible assignments:
            getScriptUtil().getRelatableElements(variable, OMTVariableAssignment.class, variableAssignment ->
                    variableAssignment.getVariableList().stream().anyMatch(
                            assignedVariable -> declaredByVariable == getDeclaredByVariable(assignedVariable).orElse(null)
                    )).forEach(
                    variableAssignment -> types.addAll(getType(variableAssignment))
            );
            return getProjectUtil().getRDFModelUtil().getDistinctResources(types);
        }
        return new ArrayList<>();
    }

    public List<Resource> getType(OMTParameterWithType parameterWithType) {
        if (parameterWithType.getParameterType() == null) {
            return new ArrayList<>();
        }
        return Collections.singletonList(parameterWithType.getParameterType().getAsResource());
    }

    /**
     * Returns the value of variable from the closest assignment
     */
    public OMTVariableValue getValue(OMTVariable variable) {
        final List<OMTVariableAssignment> assignments = getAssignments(variable);
        if (assignments.isEmpty()) {
            return null;
        }
        return assignments.get(assignments.size() - 1).getVariableValue();
    }

    public List<OMTVariableAssignment> getAssignments(OMTVariable variable) {
        List<OMTVariableAssignment> assignments = new ArrayList<>();
        // get it from the script:
        assignments.addAll(
                getScriptUtil().getAccessibleElements(variable, OMTVariableAssignment.class)
                        .stream().filter(variableAssignment -> variableAssignment.getVariableList().stream().anyMatch(
                        assignedVariable -> assignedVariable.getName().equals(variable.getName())
                )).collect(Collectors.toList())
        );
        // and finally, by it's containing model item (!Activity, !Procedure etc)
        assignments.addAll(getBlockEntryVariableAssignments(variable));

        return assignments;
    }

    private List<OMTVariableAssignment> getBlockEntryVariableAssignments(OMTVariable variable) {
        Optional<OMTModelItemBlock> modelItemBlock = getModelUtil().getModelItemBlock(variable);
        List<OMTVariableAssignment> assignments = new ArrayList<>();
        modelItemBlock.ifPresent(omtModelItemBlock ->
                assignments.addAll(
                        PsiTreeUtil.findChildrenOfType(omtModelItemBlock, OMTVariableAssignment.class).stream()
                                .filter(variableAssignment ->
                                        variableAssignment.getVariableList().stream().anyMatch(
                                                assignedVariable -> assignedVariable.getName().equals(variable.getName())
                                        ) &&
                                                getModelUtil().getEntryBlockLabel(variableAssignment).equals(VARIABLES))
                                .collect(Collectors.toList())
                ));
        return assignments;
    }

    public List<String> getGlobalVariables() {
        return Arrays.asList(GLOBAL_VARIABLE_USERNAME, GLOBAL_VARIABLE_MEDEWERKER_GRAPH, GLOBAL_VARIABLE_OFFLINE);
    }

    public List<String> getGlobalVariables(Resource type) {
        if (type.equals(getRDFModelUtil().getAnyType())) {
            return Arrays.asList(GLOBAL_VARIABLE_USERNAME, GLOBAL_VARIABLE_MEDEWERKER_GRAPH, GLOBAL_VARIABLE_OFFLINE);
        }
        if (type.equals(getRDFModelUtil().getBooleanType())) {
            return Collections.singletonList(GLOBAL_VARIABLE_OFFLINE);
        }
        if (type.equals(getRDFModelUtil().getStringType())) {
            return Collections.singletonList(GLOBAL_VARIABLE_USERNAME);
        }
        return Collections.emptyList();
    }
}
