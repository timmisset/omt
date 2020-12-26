package com.misset.opp.omt.annotations;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.*;

public class CollectionAnnotator extends AbstractAnnotator {
    private static final String DUPLICATION = "Duplication";

    public CollectionAnnotator(AnnotationHolder annotationHolder) {
        super(annotationHolder);
    }

    public void annotate(PsiElement element) {
        if (element instanceof OMTMemberListItem) {
            annotate((OMTMemberListItem) element);
        } else if (element instanceof OMTSequenceItem) {
            annotate((OMTSequenceItem) element);
        } else if (element instanceof OMTBlockEntry) {
            annotate((OMTBlockEntry) element);
        }
    }

    private void annotate(OMTMemberListItem memberListItem) {
        final OMTMemberList memberList = (OMTMemberList) memberListItem.getParent();
        final boolean duplication = memberList.getMemberListItemList().stream()
                .anyMatch(
                        element -> element != memberListItem &&
                                element.getName().equals(memberListItem.getName())
                );
        if (duplication) {
            setError(DUPLICATION);
        }
    }

    private void annotate(OMTSequenceItem sequenceItem) {
        if (sequenceItem.getName() == null) {
            return;
        }
        final OMTSequence sequence = (OMTSequence) sequenceItem.getParent();
        if (sequence.getSequenceItemList().stream()
                .anyMatch(
                        element ->
                                element.getName() != null &&
                                        element != sequenceItem &&
                                        element.getName().equals(sequenceItem.getName())
                )
        ) {
            setError(DUPLICATION);
        }
    }

    private void annotate(OMTBlockEntry blockEntry) {
        if (blockEntry.getName() == null) {
            return;
        }
        final OMTBlock block = (OMTBlock) blockEntry.getParent();
        if (block.getBlockEntryList().stream()
                .anyMatch(
                        element ->
                                element.getName() != null &&
                                        element != blockEntry &&
                                        element.getName().equals(blockEntry.getName())
                )
        ) {
            setError(DUPLICATION);
        }
    }
}
