package com.misset.opp.omt.psi.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.psi.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;

class CurieUtilTest extends LightJavaCodeInsightFixtureTestCase {

    private static final CurieUtil curieUtil = CurieUtil.SINGLETON;
    private final ExampleFiles exampleFiles = new ExampleFiles(this);
    PsiElement rootBlock;

    @BeforeEach
    void setUpSuite() throws Exception {
        super.setName("CurieUtilTest");
        super.setUp();

        ApplicationManager.getApplication().runReadAction(() -> {
            rootBlock = exampleFiles.getActivityWithImportsPrefixesParamsVariablesGraphsPayload();
        });
    }

    @AfterEach
    void tearDownSuite() throws Exception {
        super.tearDown();
    }

    @Test
    void testGetDefinedByPrefixForOMTParameterType() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTParameterType parameterType = exampleFiles.getPsiElementFromRootDocument(OMTParameterType.class, rootBlock);
            Optional<OMTPrefix> definedByPrefix = curieUtil.getDefinedByPrefix(parameterType);

            assertTrue(definedByPrefix.isPresent());
            assertEquals(definedByPrefix.get().getNamespacePrefix().getName(), parameterType.getNamespacePrefix().getName());
        });
    }

    @Test
    void testGetDefinedByPrefixForOMTCurieElement() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCurieElement curieElement = exampleFiles.getPsiElementFromRootDocument(OMTCurieElement.class, rootBlock);
            Optional<OMTPrefix> definedByPrefix = curieUtil.getDefinedByPrefix(curieElement);

            assertTrue(definedByPrefix.isPresent());
            assertEquals(definedByPrefix.get().getNamespacePrefix().getName(), curieElement.getNamespacePrefix().getName());
        });
    }

    @Test
    void testGetDefinedByPrefixOMTCurieElementReturnsEmpty() {
        assertEquals(Optional.empty(), curieUtil.getDefinedByPrefix(null, mock(OMTCurieElement.class)));
    }

    @Test
    void testGetDefinedByPrefixOMTParameterTypeReturnsEmpty() {
        assertEquals(Optional.empty(), curieUtil.getDefinedByPrefix(null, mock(OMTParameterType.class)));
    }

    @Test
    void getPrefixBlockReturnsExisting() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCurieElement curieElement = exampleFiles.getPsiElementFromRootDocument(OMTCurieElement.class, rootBlock);
            OMTPrefixBlock prefixBlock = exampleFiles.getPsiElementFromRootDocument(OMTPrefixBlock.class, rootBlock);
            OMTPrefixBlock returnedPrefixBlock = curieUtil.getPrefixBlock(curieElement);
            assertEquals(prefixBlock, returnedPrefixBlock);
        });
    }

    @Test
    void getPrefixBlockReturnsExistingWhenNew() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCurieElement curieElement = exampleFiles.getPsiElementFromRootDocument(OMTCurieElement.class, rootBlock);
            OMTPrefixBlock prefixBlock = exampleFiles.getPsiElementFromRootDocument(OMTPrefixBlock.class, rootBlock);
            OMTPrefixBlock returnedPrefixBlock = curieUtil.getPrefixBlock(curieElement, true);
            assertEquals(prefixBlock, returnedPrefixBlock);
        });
    }

    @Test
    void getPrefixBlockReturnsNull() {
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement activityWithoutPrefixBlock = exampleFiles.getActivityWithQueryWatcher();
            OMTVariable variable = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, activityWithoutPrefixBlock);
            OMTPrefixBlock returnedPrefixBlock = curieUtil.getPrefixBlock(variable, false);
            assertNull(returnedPrefixBlock);
        });
    }

    @Test
    void getPrefixBlockReturnsNewPrefixBlock() {
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement activityWithoutPrefixBlock = exampleFiles.getActivityWithQueryWatcher();
            OMTVariable variable = exampleFiles.getPsiElementFromRootDocument(OMTVariable.class, activityWithoutPrefixBlock);
            OMTPrefixBlock returnedPrefixBlock = curieUtil.getPrefixBlock(variable, true);
            assertNotNull(returnedPrefixBlock);
        });
    }
}
