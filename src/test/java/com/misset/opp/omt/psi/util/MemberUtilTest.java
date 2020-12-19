package com.misset.opp.omt.psi.util;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.impl.BuiltInMember;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.support.OMTCall;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import com.misset.opp.omt.psi.support.OMTExportMember;
import com.misset.opp.omt.util.BuiltInUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MemberUtilTest extends OMTTestSuite {

    private static String SHORT_DESCRIPTION = "Short description";
    private static String HTML_DESCRIPTION = "HTML description";

    @Mock
    AnnotationHolder annotationHolder;

    @Mock
    AnnotationBuilder annotationBuilder;

    @Mock
    ImportUtil importUtil;
    @Mock
    PsiElement psiElement;
    @Mock
    BuiltInUtil builtInUtil;
    @Mock
    BuiltInMember builtInMember;
    @Spy
    ModelUtil modelUtil;
    @Spy
    ScriptUtil scriptUtil;
    @InjectMocks
    MemberUtil memberUtil;
    private ExampleFiles exampleFiles;

    PsiElement rootBlock;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setName("MemberUtilTest");
        super.setUp();

        MockitoAnnotations.openMocks(this);
        setUtilMock(builtInUtil);
        setUtilMock(modelUtil);
        setUtilMock(scriptUtil);
        setUtilMock(importUtil);

        exampleFiles = new ExampleFiles(this, myFixture);
        rootBlock = exampleFiles.getActivityWithMembers();
        doReturn(SHORT_DESCRIPTION).when(builtInMember).shortDescription();
        doReturn(HTML_DESCRIPTION).when(builtInMember).htmlDescription();
        doReturn(annotationBuilder).when(annotationHolder).newAnnotation(any(HighlightSeverity.class), anyString());
        doReturn(annotationBuilder).when(annotationBuilder).range(any(PsiElement.class));
        doReturn(annotationBuilder).when(annotationBuilder).tooltip(anyString());
        doReturn(annotationBuilder).when(annotationBuilder).withFix(any(IntentionAction.class));
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void getDeclaringMember_ReturnsOperatorForOperatorCall() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTOperatorCall operatorCall = exampleFiles.getPsiElementFromRootDocument(OMTOperatorCall.class, rootBlock,
                    call -> Objects.equals(call.getName(), "myThirdQuery")
            );
            Optional<PsiElement> declaringMember = memberUtil.getDeclaringMember(operatorCall);
            assertTrue(declaringMember.isPresent());
            assertEquals("myThirdQuery", ((OMTDefineQueryStatement) declaringMember.get()).getDefineName().getName());
        });
    }

    @Test
    void getDeclaringMember_ReturnsEmptyWhenCallBeforeDefinedForOperatorCall() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTOperatorCall operatorCall = exampleFiles.getPsiElementFromRootDocument(OMTOperatorCall.class, rootBlock,
                    call -> Objects.equals(call.getName(), "myFourthQuery")
            );
            Optional<PsiElement> declaringMember = memberUtil.getDeclaringMember(operatorCall);
            assertFalse(declaringMember.isPresent());
        });
    }

    @Test
    void getDeclaringMember_ReturnsEmptyWhenCallOnDifferentModelItemDefinedStatement() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall commandCall = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    call -> Objects.equals(call.getName(), "myFirstCommand")
            );
            Optional<PsiElement> declaringMember = memberUtil.getDeclaringMember(commandCall);
            assertFalse(declaringMember.isPresent());
        });
    }

    @Test
    void getDeclaringMember_ReturnsWhenCallOnRootDefinedStatement() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall commandCall = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    call -> Objects.equals(call.getName(), "myRootCommand")
            );
            Optional<PsiElement> declaringMember = memberUtil.getDeclaringMember(commandCall);
            assertTrue(declaringMember.isPresent());
        });
    }

    @Test
    void getDeclaringMember_ReturnsExportingMember() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall commandCall = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    call -> Objects.equals(call.getName(), "MijnProcedure")
            );
            Optional<PsiElement> declaringMember = memberUtil.getDeclaringMember(commandCall);
            assertTrue(declaringMember.isPresent());
            assertEquals("MijnProcedure", ((OMTModelItemLabel) declaringMember.get()).getPropertyLabel().getPropertyLabelName());
        });
    }

    @Test
    void getDeclaringMember_ReturnsImportedMember() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall commandCall = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    call -> Objects.equals(call.getName(), "myImportedMethod")
            );
            doReturn(Optional.of(psiElement)).when(importUtil).resolveImportMember(any(OMTMember.class));
            Optional<PsiElement> declaringMember = memberUtil.getDeclaringMember(commandCall);

            assertTrue(declaringMember.isPresent());
            assertEquals(psiElement, declaringMember.get());
        });
    }

    @Test
    void getDeclaringMember_ReturnsOntology() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTOperatorCall operatorCall = exampleFiles.getPsiElementFromRootDocument(OMTOperatorCall.class, rootBlock,
                    call -> Objects.equals(call.getName(), "MijnOntology")
            );
            Optional<PsiElement> declaringMember = memberUtil.getDeclaringMember(operatorCall);
            assertTrue(declaringMember.isPresent());
        });
    }

    @Test
    void getNamedMemberType() {
        ApplicationManager.getApplication().runReadAction(() -> {
            assertEquals(NamedMemberType.CommandCall, memberUtil.getNamedMemberType(exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock)));
            assertEquals(NamedMemberType.OperatorCall, memberUtil.getNamedMemberType(exampleFiles.getPsiElementFromRootDocument(OMTOperatorCall.class, rootBlock)));
            assertEquals(NamedMemberType.ModelItem, memberUtil.getNamedMemberType(exampleFiles.getPsiElementFromRootDocument(OMTModelItemLabel.class, rootBlock)));
            assertEquals(NamedMemberType.ModelItem, memberUtil.getNamedMemberType(exampleFiles.getPsiElementFromRootDocument(OMTPropertyLabel.class, rootBlock)));
            assertEquals(NamedMemberType.DefineName, memberUtil.getNamedMemberType(exampleFiles.getPsiElementFromRootDocument(OMTDefineName.class, rootBlock)));
            assertEquals(NamedMemberType.ImportingMember, memberUtil.getNamedMemberType(exampleFiles.getPsiElementFromRootDocument(OMTMember.class, rootBlock, member -> ((OMTMember) member).getName().equals("myImportedMethod"))));
            assertEquals(NamedMemberType.ExportingMember, memberUtil.getNamedMemberType(exampleFiles.getPsiElementFromRootDocument(OMTMember.class, rootBlock, member -> ((OMTMember) member).getName().equals("myExportedMethod"))));

            assertNull(memberUtil.getNamedMemberType(exampleFiles.getPsiElementFromRootDocument(OMTImportBlock.class, rootBlock)));
        });

    }

    @Test
    void memberToExportMember_Procedure() throws NoSuchMethodException {
        Method method = MemberUtil.class.getDeclaredMethod("memberToExportMember", PsiElement.class);
        method.setAccessible(true);
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTModelItemLabel modelItemLabel = exampleFiles.getPsiElementFromRootDocument(OMTModelItemLabel.class, rootBlock, label -> label.getName().equals("MijnProcedure"));
            try {
                OMTExportMember exportMember = (OMTExportMember) method.invoke(memberUtil, modelItemLabel);
                assertTrue(exportMember.isCommand());
                assertEquals(modelItemLabel, exportMember.getResolvingElement());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void memberToExportMember_Activity() throws NoSuchMethodException {
        Method method = MemberUtil.class.getDeclaredMethod("memberToExportMember", PsiElement.class);
        method.setAccessible(true);
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTModelItemLabel modelItemLabel = exampleFiles.getPsiElementFromRootDocument(OMTModelItemLabel.class, rootBlock, label -> label.getName().equals("MijnActiviteitMetInterpolatedTitel"));
            try {
                OMTExportMember exportMember = (OMTExportMember) method.invoke(memberUtil, modelItemLabel);
                assertTrue(exportMember.isCommand());
                assertEquals(modelItemLabel, exportMember.getResolvingElement());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void memberToExportMember_StandaloneQuery() throws NoSuchMethodException {
        Method method = MemberUtil.class.getDeclaredMethod("memberToExportMember", PsiElement.class);
        method.setAccessible(true);
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTModelItemLabel modelItemLabel = exampleFiles.getPsiElementFromRootDocument(OMTModelItemLabel.class, rootBlock, label -> label.getName().equals("MijnStandaloneQuery"));
            try {
                OMTExportMember exportMember = (OMTExportMember) method.invoke(memberUtil, modelItemLabel);
                assertTrue(exportMember.isOperator());
                assertEquals(modelItemLabel, exportMember.getResolvingElement());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void memberToExportMember_ExportedMemberReturnsNullOnNoReference() throws NoSuchMethodException {
        Method method = MemberUtil.class.getDeclaredMethod("memberToExportMember", PsiElement.class);
        method.setAccessible(true);
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTMember exportingMember = exampleFiles.getPsiElementFromRootDocument(OMTMember.class, rootBlock, member -> member.getName().equals("myExportedMethod"));
            try {
                OMTMember spy = spy(exportingMember);
                doReturn(null).when(spy).getReference();
                assertNull(method.invoke(memberUtil, spy));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void memberToExportMember_ExportedMemberReturnsNullWhenReferenceResolvesToSelf() throws NoSuchMethodException {
        Method method = MemberUtil.class.getDeclaredMethod("memberToExportMember", PsiElement.class);
        method.setAccessible(true);
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTMember exportingMember = exampleFiles.getPsiElementFromRootDocument(OMTMember.class, rootBlock, member -> member.getName().equals("myExportedMethod"));
            try {
                OMTExportMember exportMember = (OMTExportMember) method.invoke(memberUtil, exportingMember);
                assertNull(exportMember);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void memberToExportMember_ExportedMember() throws NoSuchMethodException {
        Method method = MemberUtil.class.getDeclaredMethod("memberToExportMember", PsiElement.class);
        method.setAccessible(true);
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTMember exportingMember = exampleFiles.getPsiElementFromRootDocument(OMTMember.class, rootBlock, member -> member.getName().equals("myExportedMethod"));
            OMTModelItemLabel modelItemLabel = exampleFiles.getPsiElementFromRootDocument(OMTModelItemLabel.class, rootBlock, label -> label.getName().equals("MijnActiviteitMetInterpolatedTitel"));
            try {
                OMTMember spy = spy(exportingMember);
                PsiReference refSpy = spy(exportingMember.getReference());
                doReturn(refSpy).when(spy).getReference();
                doReturn(modelItemLabel).when(refSpy).resolve();
                OMTExportMember exportMember = (OMTExportMember) method.invoke(memberUtil, spy);
                assertTrue(exportMember.isCommand());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void memberToExportMember_ReturnsNullOnUnknownType() throws NoSuchMethodException {
        Method method = MemberUtil.class.getDeclaredMethod("memberToExportMember", PsiElement.class);
        method.setAccessible(true);
        ApplicationManager.getApplication().runReadAction(() -> {
            // ModelItemBlock is not a valid type
            OMTModelItemBlock modelItemBlock = exampleFiles.getPsiElementFromRootDocument(OMTModelItemBlock.class, rootBlock);
            try {
                assertNull(method.invoke(memberUtil, modelItemBlock));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void memberToExportMember_ReturnsNullOnImportedMember() throws NoSuchMethodException {
        Method method = MemberUtil.class.getDeclaredMethod("memberToExportMember", PsiElement.class);
        method.setAccessible(true);
        ApplicationManager.getApplication().runReadAction(() -> {
            // ModelItemBlock is not a valid type
            OMTMember member = exampleFiles.getPsiElementFromRootDocument(OMTMember.class, rootBlock, member1 -> member1.getName().equals("myImportedMethod"));
            try {
                assertNull(method.invoke(memberUtil, member));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void memberToExportMember_ReturnsNullOnCommandCall() throws NoSuchMethodException {
        Method method = MemberUtil.class.getDeclaredMethod("memberToExportMember", PsiElement.class);
        method.setAccessible(true);
        ApplicationManager.getApplication().runReadAction(() -> {
            // ModelItemBlock is not a valid type
            OMTCommandCall call = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock);
            try {
                assertNull(method.invoke(memberUtil, call));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void memberToExportMember_ReturnsNullOnOperatorCall() throws NoSuchMethodException {
        Method method = MemberUtil.class.getDeclaredMethod("memberToExportMember", PsiElement.class);
        method.setAccessible(true);
        ApplicationManager.getApplication().runReadAction(() -> {
            // ModelItemBlock is not a valid type
            OMTOperatorCall call = exampleFiles.getPsiElementFromRootDocument(OMTOperatorCall.class, rootBlock);
            try {
                assertNull(method.invoke(memberUtil, call));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void parseDefinedToCallable_CommandStatement() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTDefineCommandStatement commandStatement = exampleFiles.getPsiElementFromRootDocument(OMTDefineCommandStatement.class, rootBlock);
            OMTDefineName defineName = commandStatement.getDefineName();

            OMTExportMember exportMember = (OMTExportMember) memberUtil.parseDefinedToCallable(defineName);
            assertTrue(exportMember.isCommand());
            assertEquals(defineName, exportMember.getResolvingElement());
        });
    }

    @Test
    void parseDefinedToCallable_QueryStatement() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTDefineQueryStatement queryStatement = exampleFiles.getPsiElementFromRootDocument(OMTDefineQueryStatement.class, rootBlock);
            OMTDefineName defineName = queryStatement.getDefineName();

            OMTExportMember exportMember = (OMTExportMember) memberUtil.parseDefinedToCallable(defineName);
            assertTrue(exportMember.isOperator());
            assertEquals(defineName, exportMember.getResolvingElement());
        });
    }

    @Test
    void getContainingElement_ReturnsModelItemBlockForModelItemLabel() throws NoSuchMethodException {
        Method method = MemberUtil.class.getDeclaredMethod("getContainingElement", PsiElement.class);
        method.setAccessible(true);
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTModelItemLabel modelItemLabel = exampleFiles.getPsiElementFromRootDocument(OMTModelItemLabel.class, rootBlock);
            try {
                OMTModelItemBlock modelItemBlock = (OMTModelItemBlock) method.invoke(memberUtil, modelItemLabel);
                assertNotNull(modelItemBlock);
                assertEquals(modelItemBlock.getModelItemLabel(), modelItemLabel);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void getContainingElement_ReturnsDefinedStatementForDefineName() throws NoSuchMethodException {
        Method method = MemberUtil.class.getDeclaredMethod("getContainingElement", PsiElement.class);
        method.setAccessible(true);
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTDefineName omtDefineName = exampleFiles.getPsiElementFromRootDocument(OMTDefineName.class, rootBlock);
            try {
                OMTDefinedStatement statement = (OMTDefinedStatement) method.invoke(memberUtil, omtDefineName);
                assertNotNull(statement);
                assertEquals(omtDefineName.getParent(), statement);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void getCallName() {
        PsiElement firstChild = mock(PsiElement.class);

        OMTCall call = mock(OMTCall.class);
        doReturn(firstChild).when(call).getFirstChild();
        doReturn(true).when(call).isCommandCall();

        // with prefix:
        doReturn("@Call").when(firstChild).getText();
        assertEquals("Call", memberUtil.getCallName(call));

        // without prefix
        doReturn("Call").when(firstChild).getText();
        assertEquals("Call", memberUtil.getCallName(call));

    }

}
