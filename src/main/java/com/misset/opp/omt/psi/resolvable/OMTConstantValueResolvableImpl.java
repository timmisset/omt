package com.misset.opp.omt.psi.resolvable;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTConstantValue;
import com.misset.opp.omt.psi.util.TokenUtil;
import com.misset.opp.omt.util.ProjectUtil;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class OMTConstantValueResolvableImpl extends ASTWrapperPsiElement implements OMTConstantValue {

    private static final ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    private static final TokenUtil tokenUtil = TokenUtil.SINGLETON;

    public OMTConstantValueResolvableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<Resource> resolveToResource() {
        Model ontologyModel = projectUtil.getOntologyModel();
        final Object typedLiteral = tokenUtil.parseToTypedLiteral(this);
        if (typedLiteral == null) {
            return new ArrayList<>();
        }
        return Collections.singletonList(
                ontologyModel
                        .createResource(ontologyModel.createTypedLiteral(typedLiteral)
                                .getDatatypeURI())
        );
    }
}
