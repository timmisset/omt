package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTPropertyLabel;
import com.misset.opp.omt.psi.OMTQueryStep;
import com.misset.opp.omt.psi.OMTSignatureArgument;
import com.misset.opp.omt.psi.impl.named.OMTCallImpl;
import com.misset.opp.omt.psi.resolvable.OMTCallResolvable;
import com.misset.opp.omt.psi.support.OMTCallable;
import com.misset.opp.omt.util.RDFModelUtil;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        final RDFModelUtil rdfModelUtil = getRDFModelUtil();
        if (callable != null) {
            if (operatorsThatReturnFirstArgumentAsType.contains(getName())) {
                return queryStep.filter(getFirstArgumentType());
            } else if (operatorsThatAppendFirstArgumentAsType.contains(getName())) {
                final ArrayList<Resource> resources = new ArrayList<>(previousStep);
                resources.addAll(
                        queryStep.filter(getFirstArgumentType()));
                return resources;
            } else if (operatorsThatReturnAnyAsType.contains(getName())) {
                return rdfModelUtil.getAnyTypeAsList();
            } else if (operatorsThatReturnsPreviousStepType.contains(getName())) {
                return previousStep;
            }
            return callable.returnsAny() ? previousStep : callable.getReturnType();
        } else {
            // check if this call is actually just a primitive value such as integer, string etc
            final Resource primitiveTypeAsResource = rdfModelUtil.getPrimitiveTypeAsResource(getText());
            if (rdfModelUtil.isKnownPrimitiveType(primitiveTypeAsResource)) {
                return Collections.singletonList(primitiveTypeAsResource);
            }

            // TODO: this is a workaround for ontology classes that use nested propertykey:value pairs for typings
            // check if this call is localname part of a curie that has been parsed as call
            final PsiElement prevContainer = PsiTreeUtil.prevVisibleLeaf(getPsi()).getParent();
            if (prevContainer instanceof OMTPropertyLabel && prevContainer.getReference() != null) {
                final OMTFile containingFile = (OMTFile) getContainingFile();
                final Resource resource = rdfModelUtil.createResource(containingFile.curieToIri(prevContainer.getText() + getText()));
                return Collections.singletonList(resource);
            }
        }
        return previousStep;
    }

    @Override
    public List<Resource> getFirstArgumentType() {
        if (getSignature() != null &&
                getSignature().getSignatureArgumentList() != null &&
                !getSignature().getSignatureArgumentList().isEmpty()) {
            final OMTSignatureArgument omtSignatureArgument = getSignature().getSignatureArgumentList().get(0);
            final List<Resource> resources = omtSignatureArgument.resolveToResource();
            return resources.isEmpty() ? getRDFModelUtil().getAnyTypeAsList() : resources;
        }
        return getRDFModelUtil().getAnyTypeAsList();
    }
}
