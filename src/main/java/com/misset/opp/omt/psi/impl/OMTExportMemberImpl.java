package com.misset.opp.omt.psi.impl;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.support.ExportMemberType;
import com.misset.opp.omt.psi.support.OMTExportMember;
import com.misset.opp.omt.psi.support.OMTParameter;
import com.misset.opp.omt.psi.util.ModelUtil;

import java.util.HashMap;
import java.util.Optional;

/**
 * An exported member can be a wide variety of items, a Query or StandAlone query, both are considered Operators
 * A Command, Activity and Procedure are considered Commands
 */
public class OMTExportMemberImpl implements OMTExportMember {

    private PsiElement element;
    private ExportMemberType type;
    private HashMap<String, OMTParameter> parameters = new HashMap<>();
    private String name;

    public OMTExportMemberImpl(PsiElement exportMember, ExportMemberType type) {
        element = exportMember;
        this.type = type;
        set();
    }

    @Override
    public boolean isOperator() {
        return type == ExportMemberType.Query || type == ExportMemberType.StandaloneQuery;
    }

    @Override
    public boolean isCommand() {
        return !isOperator();
    }

    @Override
    public int getMinExpected() {
        return 0;
    }

    @Override
    public int getMaxExpected() {
        return 0;
    }

    @Override
    public void validateSignature(OMTCommandCall call) throws CallCallableMismatchException {
        if (isOperator()) {
            throw new CallCallableMismatchException(call, type);
        }
    }

    @Override
    public void validateSignature(OMTOperatorCall call) throws CallCallableMismatchException {
        if (isCommand()) {
            throw new CallCallableMismatchException(call, type);
        }
    }

    @Override
    public String[] getParameters() {
        return new String[0];
    }

    @Override
    public String getName() {
        return name;
    }

    private void setName(String name) {
        if (name.endsWith(":")) {
            name = name.substring(0, name.length() - 1);
        }
        this.name = name;
    }

    @Override
    public PsiElement getElement() {
        return element;
    }

    @Override
    public PsiElement getResolvingElement() {
        switch (type) {
            case Activity:
            case StandaloneQuery:
            case Procedure:
                return ((OMTModelItemBlock) element).getModelItemLabel().getPropertyLabel();

            case Command:
                return ((OMTDefineCommandStatement) element).getDefineName();
            case Query:
                return ((OMTDefineQueryStatement) element).getDefineName();
            default:
                return element;
        }
    }

    private void set() {
        switch (type) {
            case Activity:
            case StandaloneQuery:
            case Procedure:
                OMTModelItemBlock modelItemBlock = (OMTModelItemBlock) this.element;
                setName(modelItemBlock.getModelItemLabel().getPropertyLabel().getText());
                setParametersFromModelItem(modelItemBlock);
                break;

            case Command:
                OMTDefineCommandStatement defineCommandStatement = (OMTDefineCommandStatement) this.element;
                setName(defineCommandStatement.getDefineName().getText());
                if (defineCommandStatement.getDefineParam() != null) {
                    setParametersFromDefined(defineCommandStatement.getDefineParam());
                }
                break;
            case Query:
                OMTDefineQueryStatement defineQueryStatement = (OMTDefineQueryStatement) this.element;
                setName(defineQueryStatement.getDefineName().getText());
                if (defineQueryStatement.getDefineParam() != null) {
                    setParametersFromDefined(defineQueryStatement.getDefineParam());
                }
                break;

            default:
        }
    }

    private void addParameter(OMTParameter parameter) {
        parameters.put(parameter.getName(), parameter);
    }

    private void setParametersFromModelItem(OMTModelItemBlock block) {
        Optional<OMTBlockEntry> params = ModelUtil.getModelItemBlockEntry(block, "params");
        params.ifPresent(omtBlockEntry -> omtBlockEntry
                .getSequence()
                .getSequenceItemList()
                .stream()
                .map(OMTSequenceItem::getSequenceItemValue)
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

    private void setParametersFromDefined(OMTDefineParam parameters) {
        parameters.getVariableList().stream()
                .map(omtVariable -> new OMTParameterImpl(omtVariable))
                .forEach(this::addParameter);
    }
}
