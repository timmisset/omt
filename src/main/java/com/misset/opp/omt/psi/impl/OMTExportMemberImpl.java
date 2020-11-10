package com.misset.opp.omt.psi.impl;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.external.util.rdf.RDFModelUtil;
import com.misset.opp.omt.psi.OMTDefineCommandStatement;
import com.misset.opp.omt.psi.OMTDefineQueryStatement;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.support.ExportMemberType;
import com.misset.opp.omt.psi.support.OMTExportMember;
import com.misset.opp.omt.psi.util.ProjectUtil;
import org.apache.jena.rdf.model.Resource;

import java.util.List;

/**
 * An exported member can be a wide variety of items, a Query or StandAlone query, both are considered Operator
 * A Command, Activity and Procedure are considered Commands
 */
public class OMTExportMemberImpl extends OMTCallableImpl implements OMTExportMember {

    private final PsiElement element;
    private final ExportMemberType type;

    private ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    private RDFModelUtil rdfModelUtil;

    private RDFModelUtil getRDFModelUtil() {
        if (rdfModelUtil == null || !rdfModelUtil.isLoaded()) {
            rdfModelUtil = new RDFModelUtil(projectUtil.getOntologyModel());
        }
        return rdfModelUtil;
    }

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
                return ((OMTModelItemBlock) element).getModelItemLabel();

            case Command:
                return ((OMTDefineCommandStatement) element).getDefineName();
            case Query:
                return ((OMTDefineQueryStatement) element).getDefineName();
            default:
                return element;
        }
    }

    @Override
    public boolean returnsAny() {
        switch (type) {
            case Activity:
            case StandaloneQuery:
            case Procedure:
            case Command:
            default:
                return true;

            case Query:
                return
                        getReturnType().isEmpty() || getReturnType().get(0).getURI().equals(
                                getRDFModelUtil().getPrimitiveTypeAsResource("any").toString()
                        );
        }
    }

    @Override
    public List<Resource> getReturnType() {
        switch (type) {
            case Activity:
            case StandaloneQuery:
            case Procedure:
            case Command:
            default:
                return super.getReturnType();

            case Query:
                final OMTQuery query = ((OMTDefineQueryStatement) element).getQuery();
                return query.resolveToResource();
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
