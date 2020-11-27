package com.misset.opp.omt.psi.impl;

import com.google.gson.JsonObject;
import com.misset.opp.omt.exceptions.CallCallableMismatchException;
import com.misset.opp.omt.exceptions.IncorrectFlagException;
import com.misset.opp.omt.exceptions.IncorrectSignatureArgument;
import com.misset.opp.omt.exceptions.NumberOfInputParametersMismatchException;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.support.OMTCall;
import com.misset.opp.omt.psi.support.OMTCallable;
import com.misset.opp.omt.psi.support.OMTParameter;
import com.misset.opp.omt.psi.util.ModelUtil;
import com.misset.opp.omt.psi.util.QueryUtil;
import com.misset.opp.omt.psi.util.VariableUtil;
import com.misset.opp.omt.util.ProjectUtil;
import com.misset.opp.omt.util.RDFModelUtil;
import org.apache.jena.rdf.model.Resource;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public abstract class OMTCallableImpl implements OMTCallable {

    private final HashMap<String, OMTParameter> parameters = new HashMap<>();
    private final List<OMTParameter> parameterList = new ArrayList<>();
    private final List<String> localVariables = new ArrayList<>();
    private final List<String> flags = new ArrayList<>();
    private final String returnType;
    private String name;
    private final String type;
    private final boolean isCommand;

    private String description;

    private ModelUtil modelUtil = ModelUtil.SINGLETON;
    private ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    private VariableUtil variableUtil = VariableUtil.SINGLETON;
    private QueryUtil queryUtil = QueryUtil.SINGLETON;
    private RDFModelUtil rdfModelUtil;
    public OMTCallableImpl(String type, boolean isCommand) {
        this.type = type;
        this.isCommand = isCommand;
        JsonObject parsedModel = projectUtil.getParsedModel();
        if (parsedModel.has(type)) {
            JsonObject modelItemType = parsedModel.getAsJsonObject(type);
            if (modelItemType.has("flags")) {
                modelItemType.getAsJsonArray("flags").forEach(
                        flag -> flags.add(flag.getAsString())
                );
            }
        }
        returnType = "any";
    }

    public OMTCallableImpl(String type, String name, List<OMTParameter> parameterList, boolean isCommand, List<String> localVariables, List<String> flags) {
        this(type, name, parameterList, isCommand, localVariables, flags, "any");
    }

    public OMTCallableImpl(String type, String name, List<OMTParameter> parameterList, boolean isCommand) {
        this(type, name, parameterList, isCommand, new ArrayList<>());
    }

    public OMTCallableImpl(String type, String name, List<OMTParameter> parameterList, boolean isCommand, List<String> localVariables) {
        this(type, name, parameterList, isCommand, localVariables, new ArrayList<>());
    }

    public OMTCallableImpl(String type,
                           String name,
                           List<OMTParameter> parameterList,
                           boolean isCommand,
                           List<String> localVariables,
                           List<String> flags,
                           String returnType
    ) {
        this.type = type;
        this.name = name;
        this.isCommand = isCommand;
        parameterList.forEach(this::addParameter);
        this.localVariables.addAll(localVariables);
        this.flags.addAll(flags);
        this.returnType = returnType;
    }

    private RDFModelUtil getModelUtil() {
        if (rdfModelUtil == null || !rdfModelUtil.isLoaded()) {
            rdfModelUtil = new RDFModelUtil(projectUtil.getOntologyModel());
        }
        return rdfModelUtil;
    }

    @Override
    public boolean hasFlags() {
        return !flags.isEmpty();
    }

    @Override
    public List<String> getFlags() {
        return flags;
    }

    @Override
    public boolean isOperator() {
        return !isCommand;
    }

    @Override
    public boolean isCommand() {
        return isCommand;
    }

    @Override
    public boolean hasRest() {
        return parameters.values().stream().anyMatch(OMTParameter::isRest);
    }

    @Override
    public boolean hasParameters() {
        return !parameterList.isEmpty();
    }

    @Override
    public int getMinExpected() {
        final long minRequired = parameters.values().stream().filter(OMTParameter::isRequired).count();
        return minRequired == 0 && hasRest() ? 1 : (int) minRequired;
    }

    @Override
    public int getMaxExpected() {
        return hasRest() ? -1 : parameters.size();
    }

    @Override
    public String[] getParameters() {
        return parameterList.stream()
                .map(OMTParameter::describe)
                .toArray(String[]::new);
    }

    @Override
    public String[] getParameterNames() {
        return parameterList.stream()
                .map(OMTParameter::getName)
                .toArray(String[]::new);
    }

    private void addParameter(OMTParameter parameter) {
        parameterList.add(parameter);
        parameters.put(parameter.getName(), parameter);
    }

    @Override
    public List<String> getLocalVariables() {
        return localVariables;
    }

    @Override
    public void setHTMLDescription(String description) {
        this.description = description;
    }

    void setParametersFromModelItem(OMTModelItemBlock block) {
        Optional<OMTBlockEntry> params = modelUtil.getModelItemBlockEntry(block, "params");
        if (!params.isPresent() || params.get().getSequence() == null) {
            return;
        }

        params.ifPresent(omtBlockEntry -> omtBlockEntry
                .getSequence()
                .getSequenceItemList()
                .stream()
                .map(OMTSequenceItem::getScalarValue)
                .filter(Objects::nonNull)
                .forEach(sequenceItemValue -> {
                    if (sequenceItemValue.getParameterWithType() != null) {
                        addParameter(
                                new OMTParameterImpl(sequenceItemValue.getParameterWithType())
                        );
                    }
                    if (sequenceItemValue.getQuery() instanceof OMTQueryPath) {
                        addParameter(new OMTParameterImpl((OMTQueryPath) sequenceItemValue.getQuery()));
                    }
                    if (sequenceItemValue.getVariableAssignment() != null) {
                        addParameter(new OMTParameterImpl(sequenceItemValue.getVariableAssignment()));
                    }
                })
        );
    }

    @Override
    public void validateSignature(OMTCall call) throws CallCallableMismatchException, NumberOfInputParametersMismatchException, IncorrectFlagException {
        if ((isOperator() && !call.canCallOperator()) || (isCommand() && !call.canCallCommand())) {
            throw new CallCallableMismatchException(this, call);
        }
        int intputParameters = call.getSignature() != null ? call.getSignature().numberOfParameters() : 0;
        if (intputParameters < getMinExpected() || (!hasRest() && intputParameters > getMaxExpected())) {
            throw new NumberOfInputParametersMismatchException(name, getMinExpected(), getMaxExpected(), intputParameters);
        }
        OMTFlagSignature flagSignature = call.getFlagSignature();
        if (flagSignature != null) {
            String flagName = flagSignature.getText().substring(1);
            if (!getFlags().contains(flagName)) {
                throw new IncorrectFlagException(flagName, getFlags());
            }
        }
    }

    @Override
    public void validateSignatureArgument(int index, OMTSignatureArgument argument) throws IncorrectSignatureArgument {
        OMTParameter parameter = index <= parameterList.size() - 1 ? parameterList.get(index) : null;
        if (parameter == null && hasRest()) {
            parameter = parameterList.get(parameterList.size() - 1);
        }
        if (parameter == null) {
            return;
        } // cannot determine parameter, the validateSignature will catch this

        if (parameter.getType() == null) {
            return;
        }
        final Resource parameterType = parameter.getType().getAsResource();
        if (parameterType == null) {
            return;
        } // could not resolve the type to a Resource, for now, just leave it

        OMTParameter finalParameter = parameter;
        AtomicReference<IncorrectSignatureArgument> exception = new AtomicReference<>();
        queryUtil.validateType(
                parameterType, argument.resolveToResource(),
                (acceptableTypes, argumentTypes) -> exception.set(new IncorrectSignatureArgument(finalParameter, acceptableTypes, argumentTypes))
        );
        if (exception.get() != null) {
            throw exception.get();
        }
    }

    void setParametersFromDefined(OMTDefineParam parameters) {
        parameters.getVariableList().stream()
                .map(OMTParameterImpl::new)
                .forEach(omtParameter -> {
                    variableUtil.getTypeFromAnnotation(omtParameter.getVariable(), parameters.getParent())
                            .ifPresent(omtParameterAnnotation -> omtParameter.setType(
                                    omtParameterAnnotation.getParameterWithType().getParameterType()
                            ));
                    addParameter(omtParameter);
                });
    }

    @Override
    public String htmlDescription() {
        if (description != null && description.length() > 0) {
            return description;
        }
        return String.format("<b>%s</b><br>Type: %s<br><br>Params:<br>%s",
                name, type, String.join("<br>", getParameters()));
    }

    @Override
    public String shortDescription() {
        return String.format("%s: %s", type, name);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name.endsWith(":") ? name.substring(0, name.length() - 1) : name;
    }

    @Override
    public String getAsSuggestion() {
        String exposedName = isCommand ? String.format("@%s", getName()) : getName();
        String exposedParameters = hasParameters() ?
                String.format("(%s)", String.join(", ", getParameterNames())) :
                "";
        if (isCommand && exposedParameters.isEmpty()) {
            exposedParameters = "()";
        }
        return exposedName + exposedParameters;
    }

    @Override
    public List<Resource> getReturnType() {
        return Collections.singletonList(getModelUtil().getPrimitiveTypeAsResource(returnType));
    }

    @Override
    public boolean returnsAny() {
        return returnType.equals("any");
    }

    @Override
    public String getCallableType() {
        return type;
    }

    @Override
    public HashMap<String, Resource> getCallArgumentTypes() {
        HashMap<String, Resource> callArgumentTypes = new HashMap<>();
        parameterList.stream()
                .filter(parameter -> parameter.getType() != null)
                .forEach(parameter -> callArgumentTypes.put(parameter.getName(), parameter.getType().getAsResource())
                );
        return callArgumentTypes;
    }
}
