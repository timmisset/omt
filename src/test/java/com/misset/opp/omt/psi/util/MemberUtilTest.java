package com.misset.opp.omt.psi.util;

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class MemberUtilTest extends LightJavaCodeInsightFixtureTestCase {

    private final ExampleFiles exampleFiles = new ExampleFiles(this);
    private final MemberUtil memberUtil = MemberUtil.SINGLETON;
    PsiElement rootBlock;

    @BeforeEach
    void setUpSuite() throws Exception {
        super.setName("CurieUtilTest");
        super.setUp();

        ApplicationManager.getApplication().runReadAction(() -> {
            rootBlock = exampleFiles.getActivityWithMembers();
        });
    }

    @AfterEach
    void tearDownSuite() throws Exception {
        super.tearDown();
    }

    @Test
    void getDeclaringMember() {

    }

    @Test
    void getCallName() {
    }

    @Test
    void annotateCall() {
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
