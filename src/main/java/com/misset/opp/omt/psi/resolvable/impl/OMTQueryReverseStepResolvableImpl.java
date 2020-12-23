package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.OMTQueryReverseStep;
import com.misset.opp.omt.psi.impl.OMTQueryStepImpl;
import com.misset.opp.omt.util.RDFModelUtil;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.getQueryUtil;
import static com.misset.opp.omt.psi.util.UtilManager.getRDFModelUtil;

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
        if (!rdfModelUtil.isTypePredicate(curieElement.getAsResource())) {
            // For rdf:type, only resolve for the provided resources. If not, also add the superClasses for all resources
            resources = rdfModelUtil.allSuperClasses(resources);
        }
        List<Resource> resolvedResources = resources.isEmpty() ?
                rdfModelUtil.getPredicateSubjects(curieElement.getAsResource()) : // only by predicate
                rdfModelUtil.listSubjectsWithPredicateObjectClass(curieElement.getAsResource(), resources);// by predicate and object
        return filter ? filter(resolvedResources) : resolvedResources;
    }

    @Override
    public boolean isType() {
        // a reverse step can never resolve to a type but always to an instance of the type
        return false;
    }
}
