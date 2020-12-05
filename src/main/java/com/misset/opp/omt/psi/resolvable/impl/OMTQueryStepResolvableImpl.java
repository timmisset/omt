package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.OMTQueryFilter;
import com.misset.opp.omt.psi.OMTQueryStep;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.getQueryUtil;
import static com.misset.opp.omt.psi.util.UtilManager.getRDFModelUtil;

public abstract class OMTQueryStepResolvableImpl extends ASTWrapperPsiElement implements OMTQueryStep {

    private static final String BOOLEAN = "boolean";

    public OMTQueryStepResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<Resource> filter(List<Resource> resources) {
        for (OMTQueryFilter filter : getQueryFilterList()) {
            resources = filter.getQuery().filter(resources);
        }
        return resources;
    }

    @Override
    public List<Resource> resolveToResource() {
        return resolveToResource(true);
    }

    @Override
    public List<Resource> resolveToResource(boolean lookBack) {
        return resolveToResource(lookBack, true);
    }

    @Override
    public List<Resource> resolveToResource(boolean lookBack, boolean filter) {
        // steps that do not include preceeding info
        List<Resource> resources = new ArrayList<>();
        List<Resource> previousStep = getQueryUtil().getPreviousStep(this);

        if (getConstantValue() != null) {
            resources = getConstantValue().resolveToResource();
        } else if (getVariable() != null) {
            resources = getVariable().getType();
        } else if (getNegatedStep() != null) {
            resources = Collections.singletonList(getRDFModelUtil().getPrimitiveTypeAsResource(BOOLEAN));
        } else if (getCurieElement() != null) {
            final OMTCurieElement curieElement = getCurieElement();
            if (lookBack && !previousStep.isEmpty()) {
                return getRDFModelUtil().listObjectsWithSubjectPredicate(previousStep, curieElement.getAsResource());
            }
            return curieElement.resolveToResource();
        } else if (getOperatorCall() != null) {
            return getOperatorCall().resolveToResource();
        }
        return filter ? filter(resources) : resources;
    }

}
