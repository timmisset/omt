package com.misset.opp.omt.psi.util;

import com.google.gson.JsonObject;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.impl.BuiltInMember;
import com.misset.opp.omt.psi.impl.OMTQueryReverseStepImpl;
import com.misset.opp.omt.psi.intentions.variables.AnnotateParameterIntention;
import com.misset.opp.omt.psi.intentions.variables.RenameVariableIntention;
import com.misset.opp.omt.psi.support.BuiltInType;
import com.misset.opp.omt.psi.support.OMTCall;
import com.misset.opp.omt.util.BuiltInUtil;
import com.misset.opp.omt.util.ProjectUtil;
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
    private ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    private static final String PARAMS = "params";
    private static final String VARIABLES = "variables";
    private static final String BASE = "base";
    private static final String BINDINGS = "bindings";
    private static final String NAME = "name";
    public static final String NO_TYPE_SPECIFIED = "No type specified";

    public Optional<OMTVariable> getFirstAppearance(OMTVariable variable, PsiElement container) {
        return PsiTreeUtil.findChildrenOfType(container, OMTVariable.class).stream()
                .filter(scriptVariable -> scriptVariable.getText().equals(variable.getText()))
                .findFirst();
    }

    /**
     * Returns the declared variables available at the position of this specific element
     * @param element
     * @return
     */
    public List<OMTVariable> getDeclaredVariables(PsiElement element) {
        // Get the declared variable from the script
        List<OMTVariable> variables = scriptUtil.getAccessibleElements(element, OMTVariable.class).stream()
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
            return Optional.of(variable);
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
        Optional<OMTModelItemBlock> modelItemBlock = modelUtil.getModelItemBlock(element);
        if (!modelItemBlock.isPresent()) {
            return variables;
        }
        final OMTModelItemBlock omtModelItemBlock = modelItemBlock.get();

        final List<PsiElement> blocks = PsiTreeUtil.collectParents(element, OMTBlock.class, false, parent -> parent == omtModelItemBlock);
        blocks.add(omtModelItemBlock);

        final List<String> entryLabels = Arrays.asList(PARAMS, VARIABLES, BASE, BINDINGS);
        blocks.forEach(block -> variables.addAll(
                PsiTreeUtil.findChildrenOfType(block, OMTVariable.class).stream()
                        .filter(variable -> variable.isDeclaredVariable() &&
                                (entryLabels.contains(modelUtil.getEntryBlockLabel(variable)) ||
                                        entryLabels.contains(modelUtil.getModelItemEntryLabel(variable))))
                        .collect(Collectors.toList())
        ));
        return variables.stream().distinct().collect(Collectors.toList());
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
                    annotationBuilder = annotationBuilder.withFix(RenameVariableIntention.SINGLETON.getRenameVariableIntention(variable, "$_"));
                }
                annotationBuilder.create();
            }
            annotateUntypedParameter(variable, holder);
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

    private void annotateUntypedParameter(OMTVariable variable, AnnotationHolder holder) {
        if (PsiTreeUtil.getParentOfType(variable, OMTParameterWithType.class) == null &&
                modelUtil.getEntryBlockLabel(variable).equals(PARAMS)) {
            holder.newAnnotation(HighlightSeverity.WARNING, "Annotate parameter with a type").create();
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
            if (attributes != null && attributes.has(VARIABLES)) {
                attributes.get(VARIABLES).getAsJsonArray().forEach(variable ->
                        localVariables.put(variable.getAsString(), attributes.get("name").getAsString()));
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
        String modelItemEntryLabel = modelUtil.getModelItemEntryLabel(variable);
        String entryBlockLabel = modelUtil.getEntryBlockLabel(variable);
        return validDefinedEntries.contains(modelItemEntryLabel) || validDefinedEntries.contains(entryBlockLabel);

    }
    public void annotateDefineParameter(OMTDefineParam defineParam, AnnotationHolder holder) {
        defineParam.getVariableList().forEach(omtVariable -> {
            final List<Resource> type = omtVariable.getType();
            if (type.isEmpty()) {
                final List<Resource> typeSuggestions = getTypeSuggestions(defineParam, omtVariable);
                AnnotationBuilder annotate_parameter_with_type = holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "Annotate parameter with type")
                        .tooltip(String.format("Annotate parameter %s with a type, this help to resolve the query path%n%n" +
                                "/**" +
                                "%n* @param %s (pol:Classname)%n" +
                                "*/", omtVariable.getName(), omtVariable.getName()))
                        .range(omtVariable);
                final List<String> suggetions = typeSuggestions.stream().map(resource -> ((OMTFile) omtVariable.getContainingFile()).resourceToCurie(resource)).collect(Collectors.toList());
                if (typeSuggestions.isEmpty()) {
                    suggetions.add("prefix:Class");
                }

                final AnnotateParameterIntention annotateParameterIntention = AnnotateParameterIntention.SINGLETON;
                for (String suggestion : suggetions) {
                    annotate_parameter_with_type = annotate_parameter_with_type.withFix(
                            annotateParameterIntention.getAnnotateParameterIntention(
                                    defineParam, omtVariable.getName(), suggestion
                            )
                    );
                }
                annotate_parameter_with_type.create();
            }
        });
    }

    private List<Resource> getTypeSuggestions(OMTDefineParam defineParam, OMTVariable variableToAnnotate) {
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
        return projectUtil.getRDFModelUtil().getDistinctResources(types);
    }

    private void addTypeSuggestionsFromQueryStep(OMTQueryPath queryPath, OMTQueryStep queryStep, List<Resource> types) {
        final int stepIndex = queryPath.getQueryStepList().indexOf(queryStep);
        if (stepIndex >= 0 && queryPath.getQueryStepList().size() > stepIndex + 1) {
            // take the next step and try to resolve it:
            final OMTQueryStep omtQueryStep = queryPath.getQueryStepList().get(stepIndex + 1);
            if (omtQueryStep.getCurieElement() != null) {
                final Resource predicate = omtQueryStep.getCurieElement().getAsResource();
                types.addAll(projectUtil.getRDFModelUtil().getPredicateSubjects(predicate));
                return;
            }
            if (omtQueryStep instanceof OMTQueryReverseStep &&
                    omtQueryStep.getQueryStep() != null &&
                    omtQueryStep.getQueryStep().getCurieElement() != null) {
                final Resource predicate = omtQueryStep.getQueryStep().getCurieElement().getAsResource();
                types.addAll(projectUtil.getRDFModelUtil().getPredicateObjects(predicate));
            }
        }
    }

    /**
     * Retrieves the parameter type for the parameter define statement via the annotation (if available)
     *
     * @param defineParam
     * @return
     */
    public List<Resource> getType(OMTDefineParam defineParam, OMTVariable variable) {
        final Optional<OMTParameterAnnotation> parameterAnnotation = getTypeFromAnnotation(variable, defineParam.getParent());
        return parameterAnnotation.isPresent() ? parameterAnnotation.get().getParameterWithType().getType() : new ArrayList<>();
    }

    public List<Resource> getType(OMTVariable variable, String propertyLabel) {
        final Optional<OMTBlockEntry> blockEntry = modelUtil.getModelItemBlockEntry(variable, propertyLabel);
        if (!blockEntry.isPresent()) {
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
            return projectUtil.getRDFModelUtil().getPrimitiveTypeAsResourceList("boolean");
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
            } else if (Arrays.asList(VARIABLES, PARAMS, BINDINGS).contains(modelUtil.getEntryBlockLabel(variable))) {
                return getType(variable, modelUtil.getEntryBlockLabel(variable));
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
            scriptUtil.getRelatableElements(variable, OMTVariableAssignment.class, variableAssignment ->
                    variableAssignment.getVariableList().stream().anyMatch(
                            assignedVariable -> declaredByVariable == getDeclaredByVariable(assignedVariable).orElse(null)
                    )).forEach(
                    variableAssignment -> types.addAll(getType(variableAssignment))
            );
            return projectUtil.getRDFModelUtil().getDistinctResources(types);
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
     *
     * @param variable
     * @return
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
                scriptUtil.getAccessibleElements(variable, OMTVariableAssignment.class)
                        .stream().filter(variableAssignment -> variableAssignment.getVariableList().stream().anyMatch(
                        assignedVariable -> assignedVariable.getName().equals(variable.getName())
                )).collect(Collectors.toList())
        );
        // and finally, by it's containing model item (!Activity, !Procedure etc)
        assignments.addAll(getBlockEntryVariableAssignments(variable));

        return assignments;
    }

    private List<OMTVariableAssignment> getBlockEntryVariableAssignments(OMTVariable variable) {
        Optional<OMTModelItemBlock> modelItemBlock = modelUtil.getModelItemBlock(variable);
        List<OMTVariableAssignment> assignments = new ArrayList<>();
        modelItemBlock.ifPresent(omtModelItemBlock ->
                assignments.addAll(
                        PsiTreeUtil.findChildrenOfType(omtModelItemBlock, OMTVariableAssignment.class).stream()
                                .filter(variableAssignment ->
                                        variableAssignment.getVariableList().stream().anyMatch(
                                                assignedVariable -> assignedVariable.getName().equals(variable.getName())
                                        ) &&
                                                modelUtil.getEntryBlockLabel(variableAssignment).equals(VARIABLES))
                                .collect(Collectors.toList())
                ));
        return assignments;
    }

    public void annotateParameterWithType(OMTParameterWithType parameterWithType, AnnotationHolder holder) {
        if (parameterWithType.getParameterType() == null) {
            holder.newAnnotation(HighlightSeverity.ERROR, "No type specified").create();
        }
    }
}
