package com.misset.opp.omt.psi.util;

import com.google.gson.JsonObject;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.external.util.builtIn.BuiltInMember;
import com.misset.opp.omt.external.util.builtIn.BuiltInType;
import com.misset.opp.omt.external.util.builtIn.BuiltInUtil;
import com.misset.opp.omt.external.util.rdf.RDFModelUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.impl.OMTQueryReverseStepImpl;
import com.misset.opp.omt.psi.intentions.variables.AnnotateParameterIntention;
import com.misset.opp.omt.psi.intentions.variables.RenameVariableIntention;
import com.misset.opp.omt.psi.support.OMTCall;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VariableUtil {

    public static final VariableUtil SINGLETON = new VariableUtil();
    private ModelUtil modelUtil = ModelUtil.SINGLETON;
    private ScriptUtil scriptUtil = ScriptUtil.SINGLETON;
    private AnnotationUtil annotationUtil = AnnotationUtil.SINGLETON;
    private BuiltInUtil builtInUtil = BuiltInUtil.SINGLETON;
    private ProjectUtil projectUtil = ProjectUtil.SINGLETON;

    public Optional<OMTVariable> getFirstAppearance(OMTVariable variable, PsiElement container) {
        return PsiTreeUtil.findChildrenOfType(container, OMTVariable.class).stream()
                .filter(scriptVariable -> scriptVariable.getText().equals(variable.getText()))
                .findFirst();
    }

    private RDFModelUtil rdfModelUtil;

    private RDFModelUtil getRdfModelUtil() {
        if (rdfModelUtil == null) {
            rdfModelUtil = new RDFModelUtil(projectUtil.getOntologyModel());
        }
        if (!rdfModelUtil.isLoaded()) {
            rdfModelUtil = new RDFModelUtil(projectUtil.getOntologyModel());
        }
        return rdfModelUtil;
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
                suggetions.forEach(suggestion -> annotate_parameter_with_type
                        .withFix(AnnotateParameterIntention.SINGLETON.getAnnotateParameterIntention(
                                defineParam, omtVariable.getName(), suggestion)));
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
                    if (query.getParent() instanceof OMTEquationStatement) {
                        final OMTEquationStatement equationStatement = (OMTEquationStatement) query.getParent();
                        final OMTQuery opposite = equationStatement.getOpposite(query);
                        types.addAll(new OMTQueryReverseStepImpl(opposite.getNode()).resolveToResource());
                    } else {
                        if (query instanceof OMTQueryPath) {
                            final OMTQueryStep queryStep = (OMTQueryStep) PsiTreeUtil.findFirstParent(variable, parent -> parent instanceof OMTQueryStep);
                            final OMTQueryPath queryPath = (OMTQueryPath) query;
                            final int stepIndex = queryPath.getQueryStepList().indexOf(queryStep);
                            if (stepIndex >= 0 && queryPath.getQueryStepList().size() > stepIndex + 1) {
                                // take the next step and try to resolve it:
                                final OMTQueryStep omtQueryStep = queryPath.getQueryStepList().get(stepIndex + 1);
                                if (omtQueryStep.getCurieElement() != null) {
                                    final Resource predicate = omtQueryStep.getCurieElement().getAsResource();
                                    types.addAll(getRdfModelUtil().getPredicateSubjects(predicate));
                                }
                                if (omtQueryStep instanceof OMTQueryReverseStep &&
                                        ((OMTQueryReverseStep) omtQueryStep).getQueryStep().getCurieElement() != null) {
                                    final Resource predicate = ((OMTQueryReverseStep) omtQueryStep).getQueryStep().getCurieElement().getAsResource();
                                    types.addAll(getRdfModelUtil().getPredicateSubjects(predicate));
                                }
                            }
                        }
                    }
                }
        );
        return getRdfModelUtil().getDistinctResources(types);
    }

    /**
     * Retrieves the parameter type for the parameter define statement via the annotation (if available)
     *
     * @param defineParam
     * @return
     */
    public List<Resource> getType(OMTDefineParam defineParam) {
        final Optional<OMTParameterAnnotation> parameterAnnotation = getTypeFromAnnotation(defineParam.getVariableList().get(0), defineParam.getParent());
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

    private Optional<OMTParameterAnnotation> getTypeFromAnnotation(PsiElement target, PsiElement container) {
        final Collection<OMTParameterAnnotation> parameterAnnotations = PsiTreeUtil.findChildrenOfType(container, OMTParameterAnnotation.class);
        return parameterAnnotations.stream().filter(omtParameterAnnotation -> {
            final OMTParameterWithType parameterWithType = omtParameterAnnotation.getParameterWithType();
            final PsiReference reference = parameterWithType.getVariable().getReference();
            return reference != null && reference.isReferenceTo(target);
        }).findFirst();
    }

    public List<Resource> getType(OMTVariableAssignment variableAssignment) {
        final OMTVariableValue variableValue = variableAssignment.getVariableValue();
        // commands that return the type passed into the first argument
        List<String> resolvableCommands = Arrays.asList("NEW", "COPYINGRAPH", "ASSIGN");
        if (variableValue.getCommandCall() != null && resolvableCommands.contains(variableValue.getCommandCall().getName())) {
            final List<OMTSignatureArgument> signatureArgumentList = variableValue.getCommandCall().getSignature().getSignatureArgumentList();
            if (!signatureArgumentList.isEmpty() && signatureArgumentList.get(0).getQuery() != null) {
                return Objects.requireNonNull(signatureArgumentList.get(0).getQuery()).resolveToResource();
            }
        }
        return new ArrayList<>();
    }

    public List<Resource> getType(OMTVariable variable) {
        if (variable.isDeclaredVariable()) {
            // a declared variable
            if (variable.getParent() instanceof OMTParameterWithType) {
                return getType((OMTParameterWithType) variable.getParent());
            } else if (variable.getParent() instanceof OMTDefineParam) {
                return getType((OMTDefineParam) variable.getParent());
            } else if (variable.getParent() instanceof OMTVariableAssignment) {
                return getType((OMTVariableAssignment) variable.getParent());
            } else if (Arrays.asList("variables", "params", "bindings").contains(modelUtil.getEntryBlockLabel(variable))) {
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
            return declaredByVariable != null && declaredByVariable.isDeclaredVariable() ? declaredByVariable.getType() : new ArrayList<>();
        }
        return new ArrayList<>();
    }

    public List<Resource> getType(OMTParameterWithType parameterWithType) {
        if (parameterWithType.getParameterType() != null) {
            return Collections.singletonList(parameterWithType.getParameterType().getAsResource());
        } else {
            // defined as 'operator', which means a shortname like 'string':
            Pattern pattern = Pattern.compile("\\(([a-z]*)\\)");
            Matcher matcher = pattern.matcher(parameterWithType.getText());
            if (matcher.find() && matcher.group(1) != null) {
                return Collections.singletonList(getRdfModelUtil().getPrimitiveTypeAsResource(matcher.group(1)));
            } else {
                return new ArrayList<>();
            }
        }
    }
}
