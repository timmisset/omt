package com.misset.opp.omt.completion;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.misset.opp.omt.psi.OMTQueryPath;
import com.misset.opp.omt.psi.OMTQueryStep;

public abstract class QueryCompletion extends RDFCompletion {

    protected static final PsiElementPattern queryPathPattern = PlatformPatterns.psiElement(OMTQueryPath.class);
    protected static final PsiElementPattern firstQueryStepPattern =
            PlatformPatterns.psiElement(OMTQueryStep.class).insideStarting(queryPathPattern);

}
