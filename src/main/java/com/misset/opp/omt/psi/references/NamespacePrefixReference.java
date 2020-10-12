package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTParameterType;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.impl.OMTPsiImplUtil;
import com.misset.opp.omt.psi.util.CurieUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * The curie reference resolves to the declaration of the curie prefix in either the prefixes: node or
 * a defined PREFIX statement when used in a script.
 * The CurieUtil will find the declaring statement of the prefix
 */
public class NamespacePrefixReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    public NamespacePrefixReference(@NotNull OMTNamespacePrefix namespacePrefix, TextRange textRange) {
        super(namespacePrefix, textRange);
    }

    private static final CurieUtil curieUtil = CurieUtil.SINGLETON;

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        OMTNamespacePrefix namespacePrefix = (OMTNamespacePrefix) myElement;

        Optional<OMTPrefix> definedByPrefix = Optional.empty();
        if (namespacePrefix.getParent() instanceof OMTPrefix) {
            definedByPrefix = Optional.of((OMTPrefix) myElement.getParent());
        }
        if (namespacePrefix.getParent() instanceof OMTCurieElement) {
            definedByPrefix = curieUtil.getDefinedByPrefix((OMTCurieElement) myElement.getParent());
        }
        if (namespacePrefix.getParent() instanceof OMTParameterType) {
            definedByPrefix = curieUtil.getDefinedByPrefix((OMTParameterType) myElement.getParent());
        }

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

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        return OMTPsiImplUtil.setName((OMTNamespacePrefix) getElement(), newElementName);
    }
}
