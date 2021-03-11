package com.misset.opp.omt.psi.util;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTCommandCall;
import com.misset.opp.omt.psi.OMTDefineCommandStatement;
import com.misset.opp.omt.psi.OMTDefineName;
import com.misset.opp.omt.psi.OMTDefineQueryStatement;
import com.misset.opp.omt.psi.OMTImportBlock;
import com.misset.opp.omt.psi.OMTMember;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.OMTOperatorCall;
import com.misset.opp.omt.psi.OMTPropertyLabel;
import com.misset.opp.omt.psi.impl.OMTBuiltInMember;
import com.misset.opp.omt.psi.named.NamedMemberType;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

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
    OMTBuiltInMember builtInMember;
    @Spy
    ModelUtil modelUtil;
    @Spy
    ScriptUtil scriptUtil;
    @InjectMocks
    MemberUtil memberUtil;

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

        setExampleFileActivityWithMembers();
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
        ReadAction.run(() -> {
            OMTOperatorCall operatorCall = getElement(OMTOperatorCall.class,
                    call -> Objects.equals(call.getName(), "myThirdQuery")
            );
            Optional<PsiElement> declaringMember = memberUtil.getDeclaringMember(operatorCall);
            assertTrue(declaringMember.isPresent());
            assertEquals("myThirdQuery", ((OMTDefineQueryStatement) declaringMember.get()).getDefineName().getName());
        });
    }

    @Test
    void getDeclaringMember_ReturnsEmptyWhenCallBeforeDefinedForOperatorCall() {
        ReadAction.run(() -> {
            OMTOperatorCall operatorCall = getElement(OMTOperatorCall.class,
                    call -> Objects.equals(call.getName(), "myFourthQuery")
            );
            Optional<PsiElement> declaringMember = memberUtil.getDeclaringMember(operatorCall);
            assertFalse(declaringMember.isPresent());
        });
    }

    @Test
    void getDeclaringMember_ReturnsEmptyWhenCallOnDifferentModelItemDefinedStatement() {
        ReadAction.run(() -> {
            OMTCommandCall commandCall = getElement(OMTCommandCall.class,
                    call -> Objects.equals(call.getName(), "myFirstCommand")
            );
            Optional<PsiElement> declaringMember = memberUtil.getDeclaringMember(commandCall);
            assertFalse(declaringMember.isPresent());
        });
    }

    @Test
    void getDeclaringMember_ReturnsWhenCallOnRootDefinedStatement() {
        ReadAction.run(() -> {
            OMTCommandCall commandCall = getElement(OMTCommandCall.class,
                    call -> Objects.equals(call.getName(), "myRootCommand")
            );
            Optional<PsiElement> declaringMember = memberUtil.getDeclaringMember(commandCall);
            assertTrue(declaringMember.isPresent());
        });
    }

    @Test
    void getDeclaringMember_ReturnsExportingMember() {
        ReadAction.run(() -> {
            OMTCommandCall commandCall = getElement(OMTCommandCall.class,
                    call -> Objects.equals(call.getName(), "MijnProcedure")
            );
            Optional<PsiElement> declaringMember = memberUtil.getDeclaringMember(commandCall);
            assertTrue(declaringMember.isPresent());
            assertEquals("MijnProcedure", ((OMTModelItemLabel) declaringMember.get()).getName());
        });
    }

    @Test
    void getDeclaringMember_ReturnsImportedMember() {
        ReadAction.run(() -> {
            OMTCommandCall commandCall = getElement(OMTCommandCall.class,
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
        ReadAction.run(() -> {
            OMTOperatorCall operatorCall = getElement(OMTOperatorCall.class,
                    call -> Objects.equals(call.getName(), "MijnOntology")
            );
            Optional<PsiElement> declaringMember = memberUtil.getDeclaringMember(operatorCall);
            assertTrue(declaringMember.isPresent());
        });
    }

    @Test
    void getNamedMemberType() {
        ReadAction.run(() -> {
            assertEquals(NamedMemberType.CommandCall, memberUtil.getNamedMemberType(getElement(OMTCommandCall.class)));
            assertEquals(NamedMemberType.OperatorCall, memberUtil.getNamedMemberType(getElement(OMTOperatorCall.class)));
            assertEquals(NamedMemberType.ModelItem, memberUtil.getNamedMemberType(getElement(OMTModelItemLabel.class)));
            assertEquals(NamedMemberType.ModelItem, memberUtil.getNamedMemberType(getElement(OMTPropertyLabel.class)));
            assertEquals(NamedMemberType.DefineName, memberUtil.getNamedMemberType(getElement(OMTDefineName.class)));
            assertEquals(NamedMemberType.ImportingMember, memberUtil.getNamedMemberType(getElement(OMTMember.class, member -> member.getName().equals("myImportedMethod"))));
            assertEquals(NamedMemberType.ExportingMember, memberUtil.getNamedMemberType(getElement(OMTMember.class, member -> member.getName().equals("myExportedMethod"))));

            assertNull(memberUtil.getNamedMemberType(getElement(OMTImportBlock.class)));
        });

    }

    @Test
    void memberToExportMember_Procedure() throws NoSuchMethodException {
        Method method = MemberUtil.class.getDeclaredMethod("memberToExportMember", PsiElement.class);
        method.setAccessible(true);
        ReadAction.run(() -> {
            OMTModelItemLabel modelItemLabel = getElement(OMTModelItemLabel.class, label -> label.getName().equals("MijnProcedure"));
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
        ReadAction.run(() -> {
            OMTModelItemLabel modelItemLabel = getElement(OMTModelItemLabel.class, label -> label.getName().equals("MijnActiviteitMetInterpolatedTitel"));
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
        ReadAction.run(() -> {
            OMTModelItemLabel modelItemLabel = getElement(OMTModelItemLabel.class, label -> label.getName().equals("MijnStandaloneQuery"));
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
        ReadAction.run(() -> {
            OMTMember exportingMember = getElement(OMTMember.class, member -> member.getName().equals("myExportedMethod"));
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
        ReadAction.run(() -> {
            OMTMember exportingMember = getElement(OMTMember.class, member -> member.getName().equals("myExportedMethod"));
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
        ReadAction.run(() -> {
            OMTMember exportingMember = getElement(OMTMember.class, member -> member.getName().equals("myExportedMethod"));
            OMTModelItemLabel modelItemLabel = getElement(OMTModelItemLabel.class, label -> label.getName().equals("MijnActiviteitMetInterpolatedTitel"));
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
        ReadAction.run(() -> {
            // ModelItemBlock is not a valid type
            OMTModelItemBlock modelItemBlock = getElement(OMTModelItemBlock.class);
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
        ReadAction.run(() -> {
            // ModelItemBlock is not a valid type
            OMTMember member = getElement(OMTMember.class, member1 -> member1.getName().equals("myImportedMethod"));
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
        ReadAction.run(() -> {
            // ModelItemBlock is not a valid type
            OMTCommandCall call = getElement(OMTCommandCall.class);
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
        ReadAction.run(() -> {
            // ModelItemBlock is not a valid type
            OMTOperatorCall call = getElement(OMTOperatorCall.class);
            try {
                assertNull(method.invoke(memberUtil, call));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void parseDefinedToCallable_CommandStatement() {
        ReadAction.run(() -> {
            OMTDefineCommandStatement commandStatement = getElement(OMTDefineCommandStatement.class);
            OMTDefineName defineName = commandStatement.getDefineName();

            OMTExportMember exportMember = (OMTExportMember) memberUtil.parseDefinedToCallable(defineName);
            assertTrue(exportMember.isCommand());
            assertEquals(defineName, exportMember.getResolvingElement());
        });
    }

    @Test
    void parseDefinedToCallable_QueryStatement() {
        ReadAction.run(() -> {
            OMTDefineQueryStatement queryStatement = getElement(OMTDefineQueryStatement.class);
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
        ReadAction.run(() -> {
            OMTModelItemLabel modelItemLabel = getElement(OMTModelItemLabel.class);
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
        ReadAction.run(() -> {
            OMTDefineName omtDefineName = getElement(OMTDefineName.class);
            try {
                OMTDefinedStatement statement = (OMTDefinedStatement) method.invoke(memberUtil, omtDefineName);
                assertNotNull(statement);
                assertEquals(omtDefineName.getParent(), statement);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

}
