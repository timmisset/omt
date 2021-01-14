package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTConstantValue;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.getProjectUtil;
import static com.misset.opp.omt.psi.util.UtilManager.getTokenUtil;

public abstract class OMTConstantValueResolvableImpl extends ASTWrapperPsiElement implements OMTConstantValue {

    public OMTConstantValueResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<Resource> resolveToResource() {
        final Object typedLiteral = getTokenUtil().parseToTypedLiteral(this);
        if (typedLiteral == null) {
            return Collections.emptyList();
        }
        Model ontologyModel = getProjectUtil().getOntologyModel();
        if (ontologyModel == null) {
            return Collections.emptyList();
        }
        final String dataType = ontologyModel.createTypedLiteral(typedLiteral).getDatatype().getURI();
        return Collections.singletonList(ontologyModel.createResource(dataType));
    }
}
