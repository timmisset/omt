package com.misset.opp.omt.psi.impl;

import com.misset.opp.omt.exceptions.CallCallableMismatchException;
import com.misset.opp.omt.exceptions.IncorrectFlagException;
import com.misset.opp.omt.exceptions.NumberOfInputParametersMismatchException;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.support.OMTCall;
import com.misset.opp.omt.psi.support.OMTCallable;
import com.misset.opp.omt.psi.support.OMTParameter;
import com.misset.opp.omt.psi.util.ModelUtil;

import java.util.*;

public abstract class OMTCallableImpl implements OMTCallable {

    private final HashMap<String, OMTParameter> parameters = new HashMap<>();
    private final List<OMTParameter> parameterList = new ArrayList<>();
    private final List<String> localVariables = new ArrayList<>();
    private final List<String> flags = new ArrayList<>();
    private String name;
    private final String type;
    private final boolean isCommand;

    private String description;

    private ModelUtil modelUtil = ModelUtil.SINGLETON;

    public OMTCallableImpl(String type, boolean isCommand) {
        this.type = type;
        this.isCommand = isCommand;
    }

    public OMTCallableImpl(String type, String name, List<OMTParameter> parameterList, boolean isCommand) {
        this(type, name, parameterList, isCommand, new ArrayList<>());
    }

    public OMTCallableImpl(String type, String name, List<OMTParameter> parameterList, boolean isCommand, List<String> localVariables) {
        this(type, name, parameterList, isCommand, localVariables, new ArrayList<>());
    }

    public OMTCallableImpl(String type, String name, List<OMTParameter> parameterList, boolean isCommand, List<String> localVariables, List<String> flags) {
        this.type = type;
        this.name = name;
        this.isCommand = isCommand;
        parameterList.forEach(this::addParameter);
        this.localVariables.addAll(localVariables);
        this.flags.addAll(flags);
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
        return (int) parameters.values().stream().filter(OMTParameter::isRequired).count();
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
                    if (sequenceItemValue.getQueryPath() != null) {
                        addParameter(new OMTParameterImpl(sequenceItemValue.getQueryPath()));
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
            if (!flags.contains(flagName)) {
                throw new IncorrectFlagException(flagName, flags);
            }
        }
    }

    void setParametersFromDefined(OMTDefineParam parameters) {
        parameters.getVariableList().stream()
                .map(OMTParameterImpl::new)
                .forEach(this::addParameter);
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

    public String getName() {
        return this.name;
    }

    public String asSuggestion() {
        String name = getName();
        if (isCommand) {
            name = "@" + name;
        }
        String params = hasParameters() ? String.format(
                "(%s)", String.join(", ", getParameterNames())
        ) : "";
        return name + params;
    }

    public void setName(String name) {
        this.name = name.endsWith(":") ? name.substring(0, name.length() - 1) : name;
    }
}
