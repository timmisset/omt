package com.misset.opp.omt.psi.references;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.util.ImportUtil;
import com.misset.opp.omt.psi.util.MemberUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

class MemberReferenceTest extends LightJavaCodeInsightFixtureTestCase {

    @Mock
    MemberUtil memberUtil;

    @Mock
    ImportUtil importUtil;

    @Mock
    OMTMember member;

    @Mock
    OMTOperatorCall operatorCall;

    @Mock
    OMTCommandCall commandCall;

    @Mock
    OMTDefineName defineName;

    @Mock
    OMTModelItemLabel modelItemLabel;

    @Mock
    TextRange textRange;

    @Mock
    PsiElement psiElementMock;

    MemberReference importMemberReference;
    MemberReference exportMemberReference;
    MemberReference modelItemReference;
    MemberReference defineNameReference;
    MemberReference operatorCallReference;
    MemberReference commandCallReference;

    ExampleFiles exampleFiles;

    @BeforeEach
    void setUpSuite() throws Exception {
        super.setName("MemberReferenceTest");
        super.setUp();
        exampleFiles = new ExampleFiles(this);
        MockitoAnnotations.initMocks(this);
        importMemberReference = new MemberReference(member, textRange, NamedMemberType.ImportingMember, memberUtil, importUtil);
        exportMemberReference = new MemberReference(member, textRange, NamedMemberType.ExportingMember, memberUtil, importUtil);
        modelItemReference = new MemberReference(modelItemLabel, textRange, NamedMemberType.ModelItem, memberUtil, importUtil);
        defineNameReference = new MemberReference(defineName, textRange, NamedMemberType.DefineName, memberUtil, importUtil);
        operatorCallReference = new MemberReference(operatorCall, textRange, NamedMemberType.OperatorCall, memberUtil, importUtil);
        commandCallReference = new MemberReference(commandCall, textRange, NamedMemberType.CommandCall, memberUtil, importUtil);
    }

    @AfterEach
    void tearDownSuite() throws Exception {
        super.tearDown();
    }

    @Test
    void multiResolve_ImportMember_ReturnsSelfWhenNotResolved() {
        ResolveResult[] resolveResults = importMemberReference.multiResolve(false);
        ResolveResult resolveResult = resolveResults[0];
        assertEquals(member, resolveResult.getElement());
    }

    @Test
    void multiResolve_ImportMember_ReturnsResolvedMember() {

        doReturn(Optional.of(psiElementMock)).when(importUtil).resolveImportMember(member);
        ResolveResult[] resolveResults = importMemberReference.multiResolve(false);
        ResolveResult resolveResult = resolveResults[0];
        assertEquals(psiElementMock, resolveResult.getElement());
    }

    @Test
    void multiResolve_ExportMember_ReturnsSelfWhenNotResolved() {
        ResolveResult[] resolveResults = exportMemberReference.multiResolve(false);
        ResolveResult resolveResult = resolveResults[0];
        assertEquals(member, resolveResult.getElement());
    }

    @Test
    void multiResolve_ExportMember_ReturnsResolvedMember() {
        doReturn("exportMemberName").when(member).getName();
        doReturn(Optional.of(psiElementMock)).when(memberUtil).getDeclaringMemberFromImport(eq(member), eq("exportMemberName"));
        ResolveResult[] resolveResults = exportMemberReference.multiResolve(false);
        ResolveResult resolveResult = resolveResults[0];
        assertEquals(psiElementMock, resolveResult.getElement());
    }

    @Test
    void multiResolve_ModelItemReturnsSelf() {
        ResolveResult[] resolveResults = modelItemReference.multiResolve(false);
        ResolveResult resolveResult = resolveResults[0];
        assertEquals(modelItemLabel, resolveResult.getElement());
    }

    @Test
    void multiResolve_DefineNameReturnsSelf() {
        ResolveResult[] resolveResults = defineNameReference.multiResolve(false);
        ResolveResult resolveResult = resolveResults[0];
        assertEquals(defineName, resolveResult.getElement());
    }

    @Test
    void multiResolve_OperatorCallReturnsResolved() {
        doReturn(Optional.of(psiElementMock)).when(memberUtil).getDeclaringMember(eq(operatorCall));
        ResolveResult[] resolveResults = operatorCallReference.multiResolve(false);
        ResolveResult resolveResult = resolveResults[0];
        assertEquals(psiElementMock, resolveResult.getElement());
    }

    @Test
    void multiResolve_CommandCallReturnsResolved() {
        doReturn(Optional.of(psiElementMock)).when(memberUtil).getDeclaringMember(eq(commandCall));
        ResolveResult[] resolveResults = commandCallReference.multiResolve(false);
        ResolveResult resolveResult = resolveResults[0];
        assertEquals(psiElementMock, resolveResult.getElement());
    }

    @Test
    void getVariants() {
        assertEmpty(commandCallReference.getVariants());
    }

    @Test
    void handleElementRename_CommandCall() {
        String newName = "newCommand";
        PsiElement[] result = handleElementRename(OMTCommandCall.class, NamedMemberType.CommandCall, newName);
        assertNotEquals(result[0], result[1]);
        assertNotEquals(newName, ((OMTCommandCall) result[0]).getName());
        assertEquals(newName, ((OMTCommandCall) result[1]).getName());
    }

    @Test
    void handleElementRename_OperatorCall() {
        String newName = "newQuery";
        PsiElement[] result = handleElementRename(OMTOperatorCall.class, NamedMemberType.OperatorCall, newName);
        assertNotEquals(result[0], result[1]);
        assertNotEquals(newName, ((OMTOperatorCall) result[0]).getName());
        assertEquals(newName, ((OMTOperatorCall) result[1]).getName());
    }

    @Test
    void handleElementRename_DefinedName() {
        String newName = "newQuery";
        PsiElement[] result = handleElementRename(OMTDefineName.class, NamedMemberType.DefineName, newName);
        assertNotEquals(result[0], result[1]);
        ApplicationManager.getApplication().runReadAction(() -> {
            assertNotEquals(newName, ((OMTDefineName) result[0]).getName());
            assertEquals(newName, ((OMTDefineName) result[1]).getName());
        });
    }

    @Test
    void handleElementRename_Import() {
        String newName = "newImport";
        PsiElement[] result = handleElementRename(OMTMember.class, NamedMemberType.ImportingMember, newName);
        // the Member replaces the NameIdentifier token itself, keeping the Psi intact therefore the original element
        // will also be renamed
        // only need to assert the new name on the replaced item (which is not really replaced)
        assertEquals(newName, ((OMTMember) result[1]).getName());
    }

    @Test
    void handleElementRename_ModelItem() {
        String newName = "newModelItem";
        PsiElement[] result = handleElementRename(OMTModelItemLabel.class, NamedMemberType.ModelItem, newName);
        assertNotEquals(result[0], result[1]);
        ApplicationManager.getApplication().runReadAction(() -> {
            assertNotEquals(newName, ((OMTModelItemLabel) result[0]).getName());
            assertEquals(newName, ((OMTModelItemLabel) result[1]).getName());
        });
    }

    // returns the element before (0) and after (1) rename. During a rename the element is actually replaced
    // this method will return the new element in the parent container
    PsiElement[] handleElementRename(Class<? extends PsiElement> targetClass, NamedMemberType namedMemberType, String newName) {
        AtomicReference<PsiElement> originalElement = new AtomicReference<>();
        AtomicReference<PsiElement> replacedElement = new AtomicReference<>();
        ;
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement root = exampleFiles.getActivityWithMembers();
            originalElement.set(exampleFiles.getPsiElementFromRootDocument(targetClass, root));

            PsiElement parent = originalElement.get().getParent();
            MemberReference memberReference = new MemberReference(originalElement.get(), textRange, namedMemberType);
            memberReference.handleElementRename(newName);
            replacedElement.set(exampleFiles.getPsiElementFromRootDocument(targetClass, parent));
        });
        return new PsiElement[]{originalElement.get(), replacedElement.get()};
    }
}
