package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.OMTQueryStep;
import com.misset.opp.omt.psi.impl.named.OMTCurieElementImpl;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.misset.opp.omt.util.UtilManager.getRDFModelUtil;

public abstract class OMTCurieElementResolvableImpl extends OMTCurieElementImpl implements OMTCurieElement {

    public OMTCurieElementResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<Resource> resolveToResource() {
        OMTQueryStep step = (OMTQueryStep) getParent();
        if (step == null || step.getParent() == null) {
            return Collections.singletonList(getAsResource());
        }

        return step.getParent().getFirstChild().equals(step) ?
                Collections.singletonList(getAsResource()) :
                getRDFModelUtil().getPredicateObjects(getAsResource());
    }

}
