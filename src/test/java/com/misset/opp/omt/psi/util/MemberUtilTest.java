package com.misset.opp.omt.psi.util;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import com.misset.opp.omt.psi.support.OMTExportMember;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

class MemberUtilTest extends LightJavaCodeInsightFixtureTestCase {

    @Mock
    AnnotationHolder annotationHolder;
    @Mock
    AnnotationBuilder annotationBuilder;

    @Mock
    ImportUtil importUtil;
    @Mock
    PsiElement psiElement;
    @InjectMocks
    MemberUtil memberUtil;
    private ExampleFiles exampleFiles;

    PsiElement rootBlock;

    @BeforeEach
    void setUpSuite() throws Exception {
        super.setName("MemberUtilTest");
        super.setUp();

        MockitoAnnotations.initMocks(this);

        exampleFiles = new ExampleFiles(this);

        ApplicationManager.getApplication().runReadAction(() -> {
            rootBlock = exampleFiles.getActivityWithMembers();
        });
        doReturn(annotationBuilder).when(annotationHolder).newAnnotation(any(), anyString());
        doReturn(annotationBuilder).when(annotationBuilder).range(any(PsiElement.class));
        doReturn(annotationBuilder).when(annotationBuilder).withFix(any(IntentionAction.class));
    }

    @AfterEach
    void tearDownSuite() throws Exception {
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
    void getDeclaringMember_ReturnsExportingMember() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall commandCall = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    call -> Objects.equals(call.getName(), "MijnProcedure")
            );
            Optional<PsiElement> declaringMember = memberUtil.getDeclaringMember(commandCall);
            assertTrue(declaringMember.isPresent());
            assertEquals("MijnProcedure", ((OMTPropertyLabel) declaringMember.get()).getPropertyLabelName());
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
    void annotateCall() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall call = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    commandCall -> commandCall.getName().equals("myThirdCommand"));
            memberUtil.annotateCall(call, annotationHolder);
        });
    }

    @Test
    void getContainingElement() {
    }

    @Test
    void getNamedMemberType() {
        ApplicationManager.getApplication().runReadAction(() -> {
            assertEquals(NamedMemberType.CommandCall, memberUtil.getNamedMemberType(exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock)));
            assertEquals(NamedMemberType.OperatorCall, memberUtil.getNamedMemberType(exampleFiles.getPsiElementFromRootDocument(OMTOperatorCall.class, rootBlock)));
            assertEquals(NamedMemberType.ModelItem, memberUtil.getNamedMemberType(exampleFiles.getPsiElementFromRootDocument(OMTModelItemLabel.class, rootBlock)));
            assertEquals(NamedMemberType.ModelItem, memberUtil.getNamedMemberType(exampleFiles.getPsiElementFromRootDocument(OMTPropertyLabel.class, rootBlock)));
            assertEquals(NamedMemberType.DefineName, memberUtil.getNamedMemberType(exampleFiles.getPsiElementFromRootDocument(OMTDefineName.class, rootBlock)));
            assertEquals(NamedMemberType.ImportingMember, memberUtil.getNamedMemberType(exampleFiles.getPsiElementFromRootDocument(OMTMember.class, rootBlock)));

            assertNull(memberUtil.getNamedMemberType(exampleFiles.getPsiElementFromRootDocument(OMTImportBlock.class, rootBlock)));
        });

    }

    @Test
    void memberToExportMember_ReturnsExportMemberQuery() throws NoSuchMethodException {
        Method method = MemberUtil.class.getDeclaredMethod("memberToExportMember", PsiElement.class);
        method.setAccessible(true);
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTDefineQueryStatement queryStatement = exampleFiles.getPsiElementFromRootDocument(OMTDefineQueryStatement.class, rootBlock);
            OMTDefineName defineName = queryStatement.getDefineName();
            try {
                OMTExportMember exportMember = (OMTExportMember) method.invoke(memberUtil, defineName);
                assertTrue(exportMember.isOperator());
                assertEquals(defineName, exportMember.getResolvingElement());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void memberToExportMember_ReturnsExportMemberCommand() throws NoSuchMethodException {
        Method method = MemberUtil.class.getDeclaredMethod("memberToExportMember", PsiElement.class);
        method.setAccessible(true);
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTDefineCommandStatement commandStatement = exampleFiles.getPsiElementFromRootDocument(OMTDefineCommandStatement.class, rootBlock);
            OMTDefineName defineName = commandStatement.getDefineName();
            try {
                OMTExportMember exportMember = (OMTExportMember) method.invoke(memberUtil, defineName);
                assertTrue(exportMember.isCommand());
                assertEquals(defineName, exportMember.getResolvingElement());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void getContainingElement_ReturnsModelItemBlockForModelItemLabel() throws NoSuchMethodException {
        Method method = MemberUtil.class.getDeclaredMethod("getContainingElement", PsiElement.class);
        method.setAccessible(true);
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTModelItemLabel modelItemLabel = exampleFiles.getPsiElementFromRootDocument(OMTModelItemLabel.class, rootBlock);
            OMTPropertyLabel propertyLabel = modelItemLabel.getPropertyLabel();
            try {
                OMTModelItemBlock modelItemBlock = (OMTModelItemBlock) method.invoke(memberUtil, propertyLabel);
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

}
