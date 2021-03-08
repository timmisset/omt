package com.misset.opp.omt.psi.resolvable;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTQuery;

public interface OMTQueryFilterResolvable extends PsiElement {

    OMTQuery getQuery();

    boolean isSubSelection();
}
