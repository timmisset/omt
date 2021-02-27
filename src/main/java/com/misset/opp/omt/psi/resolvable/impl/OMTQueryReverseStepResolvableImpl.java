package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.OMTQueryReverseStep;
import com.misset.opp.omt.psi.impl.OMTQueryStepImpl;
import com.misset.opp.omt.util.RDFModelUtil;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.misset.opp.util.UtilManager.getQueryUtil;
import static com.misset.opp.util.UtilManager.getRDFModelUtil;

public abstract class OMTQueryReverseStepResolvableImpl extends OMTQueryStepImpl implements OMTQueryReverseStep {

    public OMTQueryReverseStepResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<Resource> resolveToResource(boolean filter) {
        List<Resource> resources = getQueryUtil().getPreviousStepResources(this);
        final OMTCurieElement curieElement = getCurieElement();
        if (curieElement == null) {
            return resources;
        }
        final RDFModelUtil rdfModelUtil = getRDFModelUtil();
        final Resource asResource = curieElement.getAsResource();
        if (rdfModelUtil.isTypePredicate(asResource)) {
            if (!getQueryUtil().isPreviousStepAType(this)) {
                return new ArrayList<>(); // can only call ^rdf:type on Type or Class
            }
        } else if (rdfModelUtil.isSubclassOfPredicate(asResource)) {
            if (!getQueryUtil().isPreviousStepAType(this)) {
                return new ArrayList<>(); // can only call ^rdfs:subClass on Type or Class
            } else {
                resources.addAll(getRDFModelUtil().allSubClasses(resources));
                return resources;
            }
        } else {
            // If not ^rdf:type, also add the superClasses for all resources
            resources = rdfModelUtil.allSuperClasses(resources);
        }
        List<Resource> resolvedResources = resources.isEmpty() ?
                rdfModelUtil.getPredicateSubjects(asResource) : // only by predicate
                rdfModelUtil.listSubjectsWithPredicateObjectClass(asResource, resources);// by predicate and object
        return filter ? filter(resolvedResources) : resolvedResources;
    }

    @Override
    public boolean isType() {
        // a reverse step can never resolve to a type but always to an instance of the type
        return false;
    }
}
