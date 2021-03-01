package com.misset.opp.omt.inspection.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.OMTPrefixBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class RemovePrefixTest extends OMTTestSuite {
    @Mock
    OMTPrefixBlock prefixBlock;

    @Mock
    OMTPrefix prefix;

    List<OMTPrefix> prefixList = new ArrayList();

    @BeforeEach
    protected void setUp() {
        MockitoAnnotations.openMocks(this);
        doReturn(prefixBlock).when(prefix).getParent();
        doReturn(prefixList).when(prefixBlock).getPrefixList();
    }

    @Test
    void getRemoveQuickFixForPrefix() {
        prefixList.addAll(Arrays.asList(prefix, prefix)); // with 2 prefixes
        final LocalQuickFix removeQuickFix = Remove.getRemoveQuickFix(prefix);

        assertEquals("Remove", removeQuickFix.getFamilyName());
        assertEquals("Remove prefix", removeQuickFix.getName());

        removeQuickFix.applyFix(mock(Project.class), mock(ProblemDescriptor.class));

        verify(prefix).delete();
        verify(prefixBlock, never()).delete();
    }

    @Test
    void getRemoveQuickFixForLastPrefix() {
        prefixList.addAll(Arrays.asList(prefix)); // with 1 prefix
        final LocalQuickFix removeQuickFix = Remove.getRemoveQuickFix(prefix);

        assertEquals("Remove", removeQuickFix.getFamilyName());
        assertEquals("Remove prefix block", removeQuickFix.getName());

        removeQuickFix.applyFix(mock(Project.class), mock(ProblemDescriptor.class));

        verify(prefix, never()).delete();
        verify(prefixBlock).delete();
    }

}
