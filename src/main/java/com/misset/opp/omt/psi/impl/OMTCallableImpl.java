package com.misset.opp.omt.psi.impl;

import com.google.gson.JsonObject;
import com.misset.opp.omt.exceptions.CallCallableMismatchException;
import com.misset.opp.omt.exceptions.IncorrectFlagException;
import com.misset.opp.omt.exceptions.IncorrectSignatureArgument;
import com.misset.opp.omt.exceptions.NumberOfInputParametersMismatchException;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTDefineParam;
import com.misset.opp.omt.psi.OMTFlagSignature;
import com.misset.opp.omt.psi.OMTGenericBlock;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.OMTQueryPath;
import com.misset.opp.omt.psi.OMTSequenceItem;
import com.misset.opp.omt.psi.OMTSignatureArgument;
import com.misset.opp.omt.psi.named.OMTCall;
import com.misset.opp.omt.psi.support.OMTCallable;
import com.misset.opp.omt.psi.support.OMTParameter;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.misset.opp.util.UtilManager.getModelUtil;
import static com.misset.opp.util.UtilManager.getProjectUtil;
import static com.misset.opp.util.UtilManager.getRDFModelUtil;
import static com.misset.opp.util.UtilManager.getVariableUtil;

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

    public OMTCallableImpl(String type, boolean isCommand) {
        this.type = type;
        this.isCommand = isCommand;
        JsonObject parsedModel = getProjectUtil().getParsedModel();
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
        Optional<OMTBlockEntry> params = getModelUtil().getModelItemBlockEntry(block, "params");
        if (params.isEmpty()) {
            return;
        }
        final OMTGenericBlock paramsBlock = (OMTGenericBlock) params.get();
        if (paramsBlock.getSequence() == null) {
            return;
        }

        paramsBlock.getSequence().getSequenceItemList()
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
                });
    }

    @Override
    public void validateSignature(OMTCall call) throws CallCallableMismatchException, NumberOfInputParametersMismatchException, IncorrectFlagException {
        if ((isOperator() && !call.canCallOperator()) || (isCommand() && !call.canCallCommand())) {
            throw new CallCallableMismatchException(this, call);
        }
        int intputParameters = call.getSignature() != null ? call.getSignature().getSignatureArgumentList().size() : 0;
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
        List<Resource> acceptableTypes = getAcceptableArgumentType(index);
        List<Resource> argumentTypes = getRDFModelUtil().appendAllSubclassesAndImplementations(argument.resolveToResource());
        if (!getRDFModelUtil().validateType(acceptableTypes, argumentTypes)) {
            throw new IncorrectSignatureArgument(getParameter(index), acceptableTypes, argumentTypes);
        }
    }

    @Override
    public boolean acceptsArgument(int index, OMTSignatureArgument argument) {
        return acceptsArgument(index, argument.resolveToResource());
    }

    @Override
    public boolean acceptsArgument(int index, List<Resource> resources) {
        List<Resource> acceptableTypes = getAcceptableArgumentType(index);
        List<Resource> argumentTypes = getRDFModelUtil().appendAllSubclassesAndImplementations(resources);
        return getRDFModelUtil().validateType(acceptableTypes, argumentTypes);
    }

    @Override
    public OMTParameter getParameter(int index) {
        OMTParameter parameter = index <= parameterList.size() - 1 ? parameterList.get(index) : null;
        if (parameter == null && hasRest()) {
            parameter = parameterList.get(parameterList.size() - 1);
        }
        return parameter;
    }

    @Override
    public Resource getParameterType(int index) {
        final OMTParameter parameter = getParameter(index);
        if (parameter == null || parameter.getType() == null || parameter.getType().getAsResource() == null) {
            return null;
        }
        return parameter.getType().getAsResource();
    }

    @Override
    public List<Resource> getAcceptableArgumentType(int index) {
        final Resource parameterType = getParameterType(index);
        if (parameterType == null) {
            return getRDFModelUtil().getAnyTypeAsList();
        }
        return getRDFModelUtil().appendAllSubclasses(parameterType);
    }

    void setParametersFromDefined(OMTDefineParam parameters) {
        parameters.getVariableList().stream()
                .map(OMTParameterImpl::new)
                .forEach(omtParameter -> {
                    getVariableUtil().getTypeFromAnnotation(omtParameter.getVariable(), parameters.getParent())
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
        return Collections.singletonList(getRDFModelUtil().getPrimitiveTypeAsResource(returnType));
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
