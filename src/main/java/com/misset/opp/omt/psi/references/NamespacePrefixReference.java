package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.util.PsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.misset.opp.omt.psi.util.UtilManager.getCurieUtil;

/**
 * The curie reference resolves to the declaration of the curie prefix in either the prefixes: node or
 * a defined PREFIX statement when used in a script.
 * The CurieUtil will find the declaring statement of the prefix
 */
public class NamespacePrefixReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    public NamespacePrefixReference(@NotNull OMTNamespacePrefix namespacePrefix, TextRange textRange) {
        super(namespacePrefix, textRange);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        OMTNamespacePrefix namespacePrefix = (OMTNamespacePrefix) myElement;

        Optional<OMTPrefix> definedByPrefix = getCurieUtil().getDefinedByPrefix(namespacePrefix);
        return definedByPrefix
                .map(prefix -> new ResolveResult[]{new PsiElementResolveResult(prefix.getNamespacePrefix())})
                .orElseGet(() -> new ResolveResult[0]);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        return PsiImplUtil.setName((OMTNamespacePrefix) getElement(), newElementName);
    }
}
