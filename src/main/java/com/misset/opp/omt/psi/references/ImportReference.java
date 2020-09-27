package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.misset.opp.omt.psi.OMTImport;
import com.misset.opp.omt.psi.OMTImportSource;
import com.misset.opp.omt.psi.util.ImportUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ImportReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private static final ImportUtil importUtil = ImportUtil.SINGLETON;

    public ImportReference(@NotNull OMTImportSource importSource, TextRange textRange) {
        super(importSource, textRange);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        VirtualFile importedFile = importUtil.getImportedFile((OMTImport) myElement.getParent());
        PsiFile file = PsiManager.getInstance(myElement.getProject()).findFile(importedFile);
        return new ResolveResult[]{new PsiElementResolveResult(file)};
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
