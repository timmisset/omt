package com.misset.opp.omt.psi.util;

import com.google.gson.JsonObject;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTCommandBlock;
import com.misset.opp.omt.psi.OMTDeclareVariable;
import com.misset.opp.omt.psi.OMTDefineCommandStatement;
import com.misset.opp.omt.psi.OMTDefineParam;
import com.misset.opp.omt.psi.OMTDefineQueryStatement;
import com.misset.opp.omt.psi.OMTEquationStatement;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.OMTParameterAnnotation;
import com.misset.opp.omt.psi.OMTParameterWithType;
import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.OMTQueryPath;
import com.misset.opp.omt.psi.OMTQueryReverseStep;
import com.misset.opp.omt.psi.OMTQueryStep;
import com.misset.opp.omt.psi.OMTScript;
import com.misset.opp.omt.psi.OMTSignatureArgument;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.OMTVariableAssignment;
import com.misset.opp.omt.psi.OMTVariableValue;
import com.misset.opp.omt.psi.impl.OMTBuiltInMember;
import com.misset.opp.omt.psi.impl.OMTQueryReverseStepImpl;
import com.misset.opp.omt.psi.named.OMTCall;
import com.misset.opp.omt.psi.named.OMTVariableNamedElement;
import com.misset.opp.omt.psi.support.BuiltInType;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.misset.opp.util.UtilManager.getBuiltinUtil;
import static com.misset.opp.util.UtilManager.getModelUtil;
import static com.misset.opp.util.UtilManager.getProjectUtil;
import static com.misset.opp.util.UtilManager.getRDFModelUtil;
import static com.misset.opp.util.UtilManager.getScriptUtil;

public class VariableUtil {

    private static final String PARAMS = "params";
    private static final String VARIABLES = "variables";
    private static final String BASE = "base";
    private static final String BINDINGS = "bindings";
    private static final String NAME = "name";
    private static final String ACTIONS = "actions";

    private static final String GLOBAL_VARIABLE_USERNAME = "$username";
    private static final String GLOBAL_VARIABLE_MEDEWERKER_GRAPH = "$medewerkerGraph";
    private static final String GLOBAL_VARIABLE_OFFLINE = "$offline";

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
                .filter(declaredVariable -> canBeDefinedVariable(variable, declaredVariable))
                .min((o1, o2) -> getModelDepthPosition(o1, o2, variable));
    }

    private int getModelDepthPosition(OMTVariable declaredVariable1, OMTVariable declaredVariable2, OMTVariable targetVariable) {
        final PsiElement commonParentVariable1 = PsiTreeUtil.findCommonParent(declaredVariable1, targetVariable); // for example, the model item
        final PsiElement commonParentVariable2 = PsiTreeUtil.findCommonParent(declaredVariable2, targetVariable); // for example, the model item
        return PsiTreeUtil.getDepth(targetVariable, commonParentVariable1) <
                PsiTreeUtil.getDepth(targetVariable, commonParentVariable2) ? 0 : 1;
    }

    private boolean canBeDefinedVariable(OMTVariable variable, OMTVariable declaredVariable) {
        return declaredVariable.getName().equals(variable.getName()) &&
                (declaredVariable.isDeclaredByOMTModel() || isDeclaredStatementAccessible(variable, declaredVariable));
    }

    private boolean isDeclaredStatementAccessible(OMTVariable variable, OMTVariable declaredVariable) {
        final PsiElement commonParent = PsiTreeUtil.findCommonParent(variable, declaredVariable);
        final PsiElement declaredBlock = PsiTreeUtil.findFirstParent(declaredVariable, parent -> parent instanceof OMTCommandBlock ||
                parent instanceof OMTScript);
        // a variable declare statement and usage common parent must be the block where the variable is declared
        // to make it accessible for the usage
        // moreover, the position of the declare statement must precede the usage
        return commonParent == declaredBlock &&
                declaredVariable.getTextOffset() < variable.getTextOffset();
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

        // return all the accessible declared variables by moving up the tree
        // determining if a variable is declared or usage is determined by position in the model
        blocks.forEach(block -> variables.addAll(
                PsiTreeUtil.findChildrenOfType(block, OMTVariable.class).stream()
                        .filter(OMTVariableNamedElement::isDeclaredVariable)
                        .filter(variable -> !(variable.getParent() instanceof OMTDefineParam))
                        .collect(Collectors.toList())
        ));
        return variables.stream().distinct().collect(Collectors.toList());
    }

    public boolean isLocalVariable(OMTVariable variable) {
        return getLocalVariables(variable).containsKey(variable.getName());
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
        if (PsiTreeUtil.getParentOfType(variable, OMTVariableValue.class) != null) {
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
        final List<String> validDefinedEntries = Arrays.asList(VARIABLES, PARAMS, BINDINGS, BASE, ACTIONS);
        final List<String> validEntryBlockLabels = Arrays.asList(VARIABLES, PARAMS, BINDINGS, BASE, NAME);
        String modelItemEntryLabel = getModelUtil().getModelItemEntryLabel(variable);
        String entryBlockLabel = getModelUtil().getEntryBlockLabel(variable);
        return validDefinedEntries.contains(modelItemEntryLabel) &&
                (modelItemEntryLabel.equals(BINDINGS) ||
                        validEntryBlockLabels.contains(entryBlockLabel));
    }

    public List<Resource> getTypeSuggestions(OMTDefineParam defineParam, OMTVariable variableToAnnotate) {
        List<Resource> types = new ArrayList<>();
        PsiTreeUtil.findChildrenOfType(defineParam.getParent(), OMTVariable.class).stream().filter(
                variable -> variable.getReference() != null &&
                        variable != variableToAnnotate &&
                        variable.getReference().isReferenceTo(variableToAnnotate)
        ).forEach(
                variable -> {
                    final OMTQuery query = PsiTreeUtil.getParentOfType(variable, OMTQuery.class);
                    if (query != null) {
                        if (query.getParent() instanceof OMTEquationStatement) {
                            final OMTEquationStatement equationStatement = (OMTEquationStatement) query.getParent();
                            final OMTQuery opposite = equationStatement.getOpposite(query);
                            types.addAll(new OMTQueryReverseStepImpl(opposite.getNode()).resolveToResource());
                        } else {
                            if (query instanceof OMTQueryPath) {
                                final OMTQueryStep queryStep = PsiTreeUtil.getParentOfType(variable, OMTQueryStep.class);
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
            } else if (Arrays.asList(VARIABLES, PARAMS).contains(getModelUtil().getEntryBlockLabel(variable))) {
                return getType(variable, getModelUtil().getEntryBlockLabel(variable));
            }
        } else {
            if (variable.isGlobalVariable()) {
                return new ArrayList<>();
            }
            if (variable.isIgnoredVariable()) {
                return new ArrayList<>();
            }
            if (isLocalVariable(variable)) {
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
        // make sure the parameter with type is actually considered a type
        final JsonObject json = getModelUtil().getJson(parameterWithType);
        if (parameterWithType.getParameterType() == null ||
                json.has("name") &&
                        !json.get("name").getAsString().equals("Param")) {
            return Collections.emptyList();
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
