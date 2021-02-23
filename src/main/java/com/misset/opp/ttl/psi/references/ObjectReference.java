package com.misset.opp.ttl.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.FakePsiElement;
import com.misset.opp.ttl.psi.TTLIri;
import com.misset.opp.ttl.psi.TTLObject;
import com.misset.opp.ttl.psi.TTLSubject;
import com.misset.opp.ttl.psi.named.TTLObjectNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.misset.opp.util.UtilManager.getTTLUtil;

public class ObjectReference extends PsiReferenceBase<TTLObjectNamedElement> implements PsiPolyVariantReference {

    public ObjectReference(@NotNull TTLObjectNamedElement element, @NotNull TextRange textRange) {
        super(element, textRange);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        return new ResolveResult[]{};
    }

    @Override
    @Nullable
    public PsiElement resolve() {
        final TTLIri iri = ((TTLObject) getElement()).getIri();
        if (iri == null) {
            return null;
        }
        final List<FakePsiElement> ttlReference = getTTLUtil().getTTLReference(iri, Collections.emptyList());
        return ttlReference.isEmpty() ? null : ttlReference.get(0);
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        final TTLObject ttlObject = (TTLObject) getElement();
        return element instanceof TTLSubject &&
                ((TTLSubject) element).getIri() != null &&
                ttlObject.getIri() != null &&
                ((TTLSubject) element).getIri().getResourceAsString().equals(ttlObject.getIri().getResourceAsString());
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        return getElement().setName(newElementName);
    }
}
