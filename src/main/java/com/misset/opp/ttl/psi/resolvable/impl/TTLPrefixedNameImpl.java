package com.misset.opp.ttl.psi.resolvable.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.ttl.psi.TTLFile;
import com.misset.opp.ttl.psi.TTLPrefixID;
import com.misset.opp.ttl.psi.named.TTLPrefixIDNamedElement;
import com.misset.opp.ttl.psi.resolvable.TTLPrefixedName;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static com.misset.opp.util.UtilManager.getRDFModelUtil;

public abstract class TTLPrefixedNameImpl extends ASTWrapperPsiElement implements TTLPrefixedName {
    private static final String DELIMITER = ":";

    public TTLPrefixedNameImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public Resource getAsResource() {
        return getRDFModelUtil().createResource(getNamespace() + getLocalName());
    }

    @Override
    public String getPrefix() {
        return getText().split(DELIMITER)[0];
    }

    @Override
    public String getLocalName() {
        final String[] split = getText().split(DELIMITER);
        return split.length == 2 ? split[1] : "";
    }

    private String getNamespace() {
        TTLFile ttlFile = (TTLFile) getContainingFile();
        final Collection<TTLPrefixID> prefixes = PsiTreeUtil.findChildrenOfType(ttlFile, TTLPrefixID.class);
        return prefixes
                .stream()
                .filter(ttlPrefixID -> ttlPrefixID.getPrefix().equals(getPrefix()))
                .map(TTLPrefixIDNamedElement::getNamespace)
                .findFirst()
                .orElse("");
    }
}
