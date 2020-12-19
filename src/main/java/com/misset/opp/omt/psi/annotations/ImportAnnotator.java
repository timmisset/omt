package com.misset.opp.omt.psi.annotations;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTImportSource;
import com.misset.opp.omt.psi.OMTMember;
import com.misset.opp.omt.psi.intentions.generic.RemoveIntention;

import static com.misset.opp.omt.psi.intentions.imports.UnwrapIntention.getUnwrapIntention;
import static com.misset.opp.omt.psi.util.UtilManager.getMemberUtil;

public class ImportAnnotator extends AbstractAnnotator {
    private static final RemoveIntention removeIntention = new RemoveIntention();

    public ImportAnnotator(AnnotationHolder annotationHolder) {
        super(annotationHolder);
    }

    public void annotate(PsiElement element) {
        if (element instanceof OMTImportSource) {
            annotate((OMTImportSource) element);
        } else if (element instanceof OMTMember && getMemberUtil().isImportedMember((OMTMember) element)) {
            annotate((OMTMember) element);
        }
    }

    private void annotate(OMTMember member) {
        validateReference(member, String.format("%s is not an exported member", member.getName()));
        annotateUsage(member,
                annotationBuilder -> annotationBuilder.withFix(removeIntention.getRemoveIntention(member.getParent())));
    }

    private void annotate(OMTImportSource importSource) {
        validateReference(importSource, String.format("%s could not be resolved to a file", importSource.getText()));

        final String name = importSource.getName();
        if (name != null && name.startsWith("'.")) {
            setWeakWarning("Unnecessary wrapping of import statement",
                    annotationBuilder -> annotationBuilder.withFix(getUnwrapIntention(importSource)));
        }
    }
}
