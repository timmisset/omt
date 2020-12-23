package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTQueryPath;
import com.misset.opp.omt.psi.OMTQueryStep;
import com.misset.opp.omt.psi.impl.OMTQueryImpl;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.getTokenUtil;

public abstract class OMTQueryPathResolvableImpl extends OMTQueryImpl implements OMTQueryPath {

    public OMTQueryPathResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean isBooleanType() {
        if (getQueryStepList().size() == 1) {
            return getQueryStepList().get(0).getNegatedStep() != null;
        } else {
            return getTokenUtil().isNotOperator(getLastChild().getFirstChild());
        }
    }

    private boolean isEmpty() {
        return getQueryStepList().isEmpty();
    }

    private OMTQueryStep getLastStep() {
        if (isEmpty()) {
            return null;
        }
        return getQueryStepList().get(getQueryStepList().size() - 1);
    }

    private OMTQueryStep getFirstStep() {
        if (isEmpty()) {
            return null;
        }
        return getQueryStepList().get(0);
    }

    @Override
    public List<Resource> resolveToResource() {
        final OMTQueryStep queryStep = getLastStep();
        if (queryStep == null) {
            return new ArrayList<>();
        }
        return queryStep.resolveToResource();
    }

    @Override
    public List<Resource> filter(List<Resource> resources) {
        final OMTQueryStep queryStep = getFirstStep();
        if (queryStep == null) {
            return new ArrayList<>();
        }
        if (queryStep.getNegatedStep() != null) {
            return queryStep.getNegatedStep().filter(resources);
        }
        return resources;
    }

    @Override
    public boolean isType() {
        return getLastStep() != null && getLastStep().isType();
    }
}
