package com.misset.opp.omt.psi.impl;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTDefineCommandStatement;
import com.misset.opp.omt.psi.OMTDefineQueryStatement;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.support.ExportMemberType;
import com.misset.opp.omt.psi.support.OMTExportMember;

/**
 * An exported member can be a wide variety of items, a Query or StandAlone query, both are considered Operator
 * A Command, Activity and Procedure are considered Commands
 */
public class OMTExportMemberImpl extends OMTCallableImpl implements OMTExportMember {

    private final PsiElement element;
    private final ExportMemberType type;

    public OMTExportMemberImpl(PsiElement exportMemberPsi, ExportMemberType type) {
        super(type.name(), type == ExportMemberType.Command || type == ExportMemberType.Procedure || type == ExportMemberType.Activity);
        element = exportMemberPsi;
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
    public boolean hasRest() {
        // rest parameters are currently only supported for builtIn types
        return false;
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


}
