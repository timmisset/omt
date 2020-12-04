package com.misset.opp.omt.psi.resolvable;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTQueryStep;
import com.misset.opp.omt.psi.OMTSignatureArgument;
import com.misset.opp.omt.psi.impl.OMTCallImpl;
import com.misset.opp.omt.psi.support.OMTCallable;
import com.misset.opp.omt.psi.util.MemberUtil;
import com.misset.opp.omt.psi.util.QueryUtil;
import com.misset.opp.omt.util.ProjectUtil;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class OMTCallResolvableImpl extends OMTCallImpl implements OMTCallResolvable {

    private static final List<String> operatorsThatReturnFirstArgumentAsType = Arrays.asList("CAST", "PLUS", "MINUS");
    private static final List<String> operatorsThatAppendFirstArgumentAsType = Arrays.asList("IF_EMPTY", "CATCH");

    private static ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    private static QueryUtil queryUtil = QueryUtil.SINGLETON;
    private static MemberUtil memberUtil = MemberUtil.SINGLETON;

    public OMTCallResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<Resource> resolveToResource() {
        final OMTCallable callable = memberUtil.getCallable(this);
        OMTQueryStep queryStep = (OMTQueryStep) getParent();
        List<Resource> previousStep = queryUtil.getPreviousStep(queryStep);
        if (callable != null) {
            if (operatorsThatReturnFirstArgumentAsType.contains(getName())) {
                return queryStep.filter(getFirstArgumentType());
            } else if (operatorsThatAppendFirstArgumentAsType.contains(getName())) {
                final ArrayList<Resource> resources = new ArrayList<>(previousStep);
                resources.addAll(
                        queryStep.filter(getFirstArgumentType()));
                return resources;
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
            return resources.isEmpty() ? projectUtil.getRDFModelUtil().getAnyTypeAsList() : resources;
        }
        return projectUtil.getRDFModelUtil().getAnyTypeAsList();
    }
}
