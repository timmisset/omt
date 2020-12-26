package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.misset.opp.omt.psi.OMTImport;
import com.misset.opp.omt.psi.OMTImportSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.misset.opp.omt.psi.util.UtilManager.getImportUtil;

public class ImportReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    public ImportReference(@NotNull OMTImportSource importSource, TextRange textRange) {
        super(importSource, textRange);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        VirtualFile importedFile = getImportUtil().getImportedFile((OMTImport) myElement.getParent());
        if (importedFile == null) {
            return ResolveResult.EMPTY_ARRAY;
        }
        PsiFile file = PsiManager.getInstance(myElement.getProject()).findFile(importedFile);
        if (file == null) {
            return ResolveResult.EMPTY_ARRAY;
        }
        return new ResolveResult[]{new PsiElementResolveResult(file)};
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

}
