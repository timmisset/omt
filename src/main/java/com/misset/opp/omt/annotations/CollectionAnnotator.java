package com.misset.opp.omt.annotations;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTImport;
import com.misset.opp.omt.psi.OMTImportBlock;
import com.misset.opp.omt.psi.OMTImportSource;
import com.misset.opp.omt.psi.OMTMemberList;
import com.misset.opp.omt.psi.OMTMemberListItem;
import com.misset.opp.omt.psi.OMTSequence;
import com.misset.opp.omt.psi.OMTSequenceItem;
import com.misset.opp.omt.psi.util.ImportUtil;
import org.jetbrains.annotations.NotNull;

import static util.UtilManager.getImportUtil;

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
        } else if (element instanceof OMTImportSource) {
            annotate((OMTImportSource) element);
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

    private void annotate(OMTImportSource importSource) {
        final ImportUtil importUtil = getImportUtil();

        OMTImportBlock block = (OMTImportBlock) importSource.getParent().getParent();
        final OMTImport currentImport = (OMTImport) importSource.getParent();

        final VirtualFile importedFile = importUtil.getImportedFile(currentImport);
        if (importedFile == null || !importedFile.exists()) {
            return;
        }

        block.getImportList()
                .stream()
                .filter(omtImport -> omtImport != currentImport)
                .filter(omtImport -> {
                    final VirtualFile thisImport = importUtil.getImportedFile(omtImport);
                    if (thisImport == null || !thisImport.exists()) {
                        return false;
                    }
                    return thisImport.equals(importedFile);
                })
                .forEach(omtImport -> setWarning(
                        String.format("%s and %s refer to the same file", omtImport.getImportSource().getText(), importSource.getText()),
                        annotationBuilder ->
                                annotationBuilder
                                        .withFix(getMergeIntention(currentImport, omtImport))
                                        .withFix(getMergeIntention(omtImport, currentImport))
                ));
    }

    private IntentionAction getMergeIntention(OMTImport omtImport, OMTImport obsolete) {
        return new IntentionAction() {
            @Override
            public @IntentionName @NotNull String getText() {
                return String.format("Merge into %s", omtImport.getImportSource().getText());
            }

            @Override
            public @NotNull @IntentionFamilyName String getFamilyName() {
                return "Merge imports";
            }

            @Override
            public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
                return true;
            }

            @Override
            public void invoke(@NotNull Project project, Editor editor, PsiFile file) {

                final OMTMemberList memberList = omtImport.getMemberList();
                if (obsolete.getMemberList() == null || memberList == null) return;

                obsolete.getMemberList().getMemberListItemList().forEach(
                        omtMemberListItem -> {
                            if (memberList.getMemberListItemList().stream().noneMatch(
                                    existingItem -> existingItem.getName().equals(omtMemberListItem.getName())
                            )) {
                                memberList.addBefore(omtMemberListItem, memberList.getDedentToken());
                            }
                        }
                );
                obsolete.delete();
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }
}
