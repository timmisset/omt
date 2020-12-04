package com.misset.opp.omt.psi.resolvable;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.OMTQueryStep;
import com.misset.opp.omt.util.ProjectUtil;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public abstract class OMTCurieElementResolvableImpl extends ASTWrapperPsiElement implements OMTCurieElement {

    private static ProjectUtil projectUtil = ProjectUtil.SINGLETON;

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
                projectUtil.getRDFModelUtil().getPredicateObjects(getAsResource());
    }

}
