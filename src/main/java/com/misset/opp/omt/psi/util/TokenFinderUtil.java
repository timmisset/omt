package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTImport;

import java.util.Optional;

/**
 * Additional util functions to find tokens based on the position of a provider element
 */
public class TokenFinderUtil {

    public static Optional<OMTImport> findImport(PsiElement sibling) {
        while (sibling != null && !(sibling instanceof OMTImport)) {
            sibling = sibling.getPrevSibling();
        }
        if (sibling != null) {
            return Optional.of((OMTImport) sibling);
        }
        return Optional.empty();
    }

}
