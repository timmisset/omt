package com.misset.opp.omt.annotations;

import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTMemberList;
import com.misset.opp.omt.psi.OMTMemberListItem;
import com.misset.opp.omt.psi.OMTSequence;
import com.misset.opp.omt.psi.OMTSequenceItem;
import com.misset.opp.omt.psi.util.ModelUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CollectionAnnotatorTest extends OMTAnnotationTest {
    @Mock
    OMTMemberList memberList;

    @Mock
    OMTMemberListItem memberListItem;

    @Mock
    OMTMemberListItem memberListItemSibling;

    @Mock
    OMTSequence sequence;

    @Mock
    OMTSequenceItem sequenceItem;

    @Mock
    OMTSequenceItem sequenceItemSibling;

    @Mock
    OMTBlock block;

    @Mock
    OMTBlockEntry blockEntry;

    @Mock
    OMTBlockEntry blockEntrySibling;

    @Mock
    ModelUtil modelUtil;

    @InjectMocks
    CollectionAnnotator collectionAnnotator;

    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("CollectionAnnotatorTest");
        super.setUp(false);
        MockitoAnnotations.openMocks(this);
        doReturn(memberList).when(memberListItem).getParent();
        doReturn(memberList).when(memberListItemSibling).getParent();
        doReturn(Arrays.asList(memberListItem, memberListItemSibling)).when(memberList).getMemberListItemList();

        doReturn(sequence).when(sequenceItem).getParent();
        doReturn(sequence).when(sequenceItemSibling).getParent();
        doReturn(Arrays.asList(sequenceItem, sequenceItemSibling)).when(sequence).getSequenceItemList();

        doReturn(block).when(blockEntry).getParent();
        doReturn(block).when(blockEntrySibling).getParent();
        doReturn(Arrays.asList(blockEntry, blockEntrySibling)).when(block).getBlockEntryList();

        setUtilMock(modelUtil);
        doReturn(false).when(modelUtil).isDuplicationAllowed(any(PsiElement.class));
    }

    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void annotateCollectionNoAnnotationWhenInvalidType() {
        collectionAnnotator.annotate(mock(PsiElement.class));
        verifyNoAnnotations();
    }

    @Test
    void annotateMemberListItemThrowsErrorWhenDuplicateName() {
        doReturn("Name").when(memberListItem).getName();
        doReturn("Name").when(memberListItemSibling).getName();
        collectionAnnotator.annotate(memberListItem);
        verify(getHolder()).newAnnotation(eq(HighlightSeverity.ERROR), eq("Duplication"));
    }

    @Test
    void annotateMemberListItemThrowsErrorOnBothEntriesWhenDuplicateName() {
        doReturn("Name").when(memberListItem).getName();
        doReturn("Name").when(memberListItemSibling).getName();
        collectionAnnotator.annotate(memberListItem);
        collectionAnnotator.annotate(memberListItemSibling);
        verify(getHolder(), times(2)).newAnnotation(eq(HighlightSeverity.ERROR), eq("Duplication"));
    }

    @Test
    void annotateMemberListItemThrowsNoErrorWhenDifferentName() {
        doReturn("Name").when(memberListItem).getName();
        doReturn("Name2").when(memberListItemSibling).getName();
        collectionAnnotator.annotate(memberListItem);
        verifyNoErrors();
    }

    @Test
    void annotateSequenceItemThrowsErrorWhenDuplicateName() {
        doReturn("Name").when(sequenceItem).getName();
        doReturn("Name").when(sequenceItemSibling).getName();
        collectionAnnotator.annotate(sequenceItem);
        verify(getHolder()).newAnnotation(eq(HighlightSeverity.ERROR), eq("Duplication"));
    }

    @Test
    void annotateSequenceItemThrowsErrorOnBothEntriesWhenDuplicateName() {
        doReturn("Name").when(sequenceItem).getName();
        doReturn("Name").when(sequenceItemSibling).getName();
        collectionAnnotator.annotate(sequenceItem);
        collectionAnnotator.annotate(sequenceItemSibling);
        verify(getHolder(), times(2)).newAnnotation(eq(HighlightSeverity.ERROR), eq("Duplication"));
    }

    @Test
    void annotateSequenceItemThrowsNoErrorWhenDifferentName() {
        doReturn("Name").when(sequenceItem).getName();
        doReturn("Name2").when(sequenceItemSibling).getName();
        collectionAnnotator.annotate(sequenceItem);
        verifyNoErrors();
    }

    @Test
    void annotateSequenceItemThrowsNoErrorWhenItemHasNoName() {
        doReturn(null).when(sequenceItem).getName();
        doReturn("Name2").when(sequenceItemSibling).getName();
        collectionAnnotator.annotate(sequenceItem);
        verifyNoErrors();
    }

    @Test
    void annotateSequenceItemThrowsNoErrorWhenSiblingItemHasNoName() {
        doReturn("Name").when(sequenceItem).getName();
        doReturn(null).when(sequenceItemSibling).getName();
        collectionAnnotator.annotate(sequenceItem);
        verifyNoErrors();
    }

    @Test
    void annotateBlockEntryThrowsErrorWhenDuplicateName() {
        doReturn("Name").when(blockEntry).getName();
        doReturn("Name").when(blockEntrySibling).getName();
        collectionAnnotator.annotate(blockEntry);
        verify(getHolder()).newAnnotation(eq(HighlightSeverity.ERROR), eq("Duplication"));
    }

    @Test
    void annotateBlockEntryThrowsErrorOnBothEntriesWhenDuplicateName() {
        doReturn("Name").when(blockEntry).getName();
        doReturn("Name").when(blockEntrySibling).getName();
        collectionAnnotator.annotate(blockEntry);
        collectionAnnotator.annotate(blockEntrySibling);
        verify(getHolder(), times(2)).newAnnotation(eq(HighlightSeverity.ERROR), eq("Duplication"));
    }

    @Test
    void annotateBlockEntryThrowsNoErrorWhenDifferentName() {
        doReturn("Name").when(blockEntry).getName();
        doReturn("Name2").when(blockEntrySibling).getName();
        collectionAnnotator.annotate(blockEntry);
        verifyNoErrors();
    }

    @Test
    void annotateBlockEntryThrowsNoErrorWhenItemHasNoName() {
        doReturn(null).when(blockEntry).getName();
        doReturn("Name2").when(blockEntrySibling).getName();
        collectionAnnotator.annotate(blockEntry);
        verifyNoErrors();
    }

    @Test
    void annotateBlockEntryThrowsNoErrorWhenSiblingItemHasNoName() {
        doReturn("Name").when(blockEntry).getName();
        doReturn(null).when(blockEntrySibling).getName();
        collectionAnnotator.annotate(blockEntry);
        verifyNoErrors();
    }

    @Test
    void annotateBlockEntryThrowsNoErrorWhenDuplicationsAreAllowed() {
        doReturn("Name").when(blockEntry).getName();
        doReturn("Name").when(blockEntrySibling).getName();

        doReturn(true).when(modelUtil).isDuplicationAllowed(blockEntry);
        doReturn(true).when(modelUtil).isDuplicationAllowed(blockEntrySibling);
        collectionAnnotator.annotate(blockEntry);
        verifyNoErrors();
    }
}
