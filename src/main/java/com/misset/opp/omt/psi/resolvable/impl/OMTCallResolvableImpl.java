package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTQueryStep;
import com.misset.opp.omt.psi.OMTSignatureArgument;
import com.misset.opp.omt.psi.impl.named.OMTCallImpl;
import com.misset.opp.omt.psi.resolvable.OMTCallResolvable;
import com.misset.opp.omt.psi.support.OMTCallable;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.misset.opp.util.UtilManager.getMemberUtil;
import static com.misset.opp.util.UtilManager.getQueryUtil;
import static com.misset.opp.util.UtilManager.getRDFModelUtil;

public abstract class OMTCallResolvableImpl extends OMTCallImpl implements OMTCallResolvable {

    private static final List<String> operatorsThatReturnFirstArgumentAsType = Arrays.asList("CAST", "PLUS", "MINUS");
    private static final List<String> operatorsThatAppendFirstArgumentAsType = Arrays.asList("IF_EMPTY", "CATCH");
    private static final List<String> operatorsThatReturnAnyAsType = Arrays.asList("TRAVERSE");
    private static final List<String> operatorsThatReturnsPreviousStepType = Arrays.asList("PICK", "ORDER_BY");

    public OMTCallResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<Resource> resolveToResource() {
        final OMTCallable callable = getMemberUtil().getCallable(this);
        OMTQueryStep queryStep = (OMTQueryStep) getParent();
        List<Resource> previousStep = getQueryUtil().getPreviousStepResources(queryStep);
        if (callable != null) {
            if (operatorsThatReturnFirstArgumentAsType.contains(getName())) {
                return queryStep.filter(getFirstArgumentType());
            } else if (operatorsThatAppendFirstArgumentAsType.contains(getName())) {
                final ArrayList<Resource> resources = new ArrayList<>(previousStep);
                resources.addAll(
                        queryStep.filter(getFirstArgumentType()));
                return resources;
            } else if (operatorsThatReturnAnyAsType.contains(getName())) {
                return getRDFModelUtil().getAnyTypeAsList();
            } else if (operatorsThatReturnsPreviousStepType.contains(getName())) {
                return previousStep;
            }
            return callable.returnsAny() ? previousStep : callable.getReturnType();
        }
        return previousStep;
    }

    @Override
    public List<Resource> getFirstArgumentType() {
        if (getSignature() != null) {
            final OMTSignatureArgument omtSignatureArgument = getSignature().getSignatureArgumentList().get(0);
            final List<Resource> resources = omtSignatureArgument.resolveToResource();
            return resources.isEmpty() ? getRDFModelUtil().getAnyTypeAsList() : resources;
        }
        return getRDFModelUtil().getAnyTypeAsList();
    }
}
