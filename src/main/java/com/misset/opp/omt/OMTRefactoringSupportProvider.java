package com.misset.opp.omt;

import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTDefineName;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The element is the element that is actually refactored:
 * If a rename is triggered from a call, the element will be either the OMTDefineName or the OMTModelItemLabel etc.
 * <p>
 * The context is the leaf element that used to iniate the refactoring
 * In the example above, that would be the call
 */
public class OMTRefactoringSupportProvider extends RefactoringSupportProvider {

    @Override
    public boolean isMemberInplaceRenameAvailable(@NotNull PsiElement element, @Nullable PsiElement context) {
        return element instanceof OMTVariable ||
                element instanceof OMTNamespacePrefix;
    }

    @Override
    public boolean isAvailable(@NotNull PsiElement element) {
        return element instanceof OMTVariable ||
                element instanceof OMTNamespacePrefix ||
                element instanceof OMTModelItemLabel ||
                element instanceof OMTDefineName;
    }

}
