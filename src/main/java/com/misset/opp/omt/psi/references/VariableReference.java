package com.misset.opp.omt.psi.references;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.misset.opp.omt.psi.OMTDeclaredVariable;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.util.VariableUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VariableReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private OMTVariable variable;
    public VariableReference(@NotNull OMTVariable declaredVariable, TextRange textRange) {
        super(declaredVariable, textRange);
        this.variable = declaredVariable;
        System.out.println("Creating reference " + declaredVariable.getText());
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {

        // get all the usages of this declared variable
        Optional<OMTVariable> declaredByVariable = VariableUtil.getDeclaredByVariable(variable);
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
}
