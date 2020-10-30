package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.util.PsiImplUtil;
import com.misset.opp.omt.psi.util.VariableUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * The referencing part of IntelliJ is kind of vague in the tutorial. For now it appears to work when the concept of usage -> declaration
 * referencing is used. Meaning, a reference resolve always points to the declaration and not visa versa. Under the hood, IntelliJ will
 * provide a quick usages window when resolving the DeclaredVariable from the text editor.
 * The actual declaration should is resolved to itself, it then magically shows the usage instead of navigating to itself
 */
public class VariableReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {

    private static final VariableUtil variableUtil = VariableUtil.SINGLETON;

    private OMTVariable variable;

    /**
     * The reference created for this variable usage
     *
     * @param variable
     * @param textRange
     */
    public VariableReference(@NotNull OMTVariable variable, TextRange textRange) {
        super(variable, textRange);
        this.variable = variable;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        Optional<OMTVariable> declaredByVariable = variableUtil.getDeclaredByVariable(variable);
        return declaredByVariable
                .map(omtVariable -> new ResolveResult[]{new PsiElementResolveResult(omtVariable)})
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
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        return PsiImplUtil.setName(variable, newElementName);
    }
}
