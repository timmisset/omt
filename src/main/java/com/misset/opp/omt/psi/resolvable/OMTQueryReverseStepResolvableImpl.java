package com.misset.opp.omt.psi.resolvable;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.OMTQueryReverseStep;
import com.misset.opp.omt.psi.impl.OMTQueryStepImpl;
import com.misset.opp.omt.psi.util.QueryUtil;
import com.misset.opp.omt.util.ProjectUtil;
import com.misset.opp.omt.util.RDFModelUtil;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class OMTQueryReverseStepResolvableImpl extends OMTQueryStepImpl implements OMTQueryReverseStep {

    private static final QueryUtil queryUtil = QueryUtil.SINGLETON;
    private static final ProjectUtil projectUtil = ProjectUtil.SINGLETON;

    public OMTQueryReverseStepResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }


    @Override
    public List<Resource> resolveToResource(boolean lookBack, boolean filter) {
        List<Resource> resources = queryUtil.getPreviousStep(this);
        final OMTCurieElement curieElement = getCurieElement();
        if (curieElement == null) {
            return resources;
        }
        final RDFModelUtil rdfModelUtil = projectUtil.getRDFModelUtil();
        if (!rdfModelUtil.isTypePredicate(curieElement.getAsResource())) { // for a type predicate, resolve only to the given class
            resources = rdfModelUtil.allSuperClasses(resources);
        }
        List<Resource> resolvedResources = resources.isEmpty() ?
                rdfModelUtil.getPredicateSubjects(curieElement.getAsResource()) : // only by predicate
                rdfModelUtil.listSubjectsWithPredicateObjectClass(curieElement.getAsResource(), resources);// by predicate and object
        return filter ? filter(resolvedResources) : resolvedResources;
    }

    @Override
    public List<Resource> resolveToResource() {
        return resolveToResource(true, true);
    }
}
