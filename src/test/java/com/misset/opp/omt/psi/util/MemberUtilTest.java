package com.misset.opp.omt.psi.util;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.exceptions.CallCallableMismatchException;
import com.misset.opp.omt.exceptions.IncorrectFlagException;
import com.misset.opp.omt.exceptions.NumberOfInputParametersMismatchException;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.impl.BuiltInMember;
import com.misset.opp.omt.psi.intentions.members.MemberIntention;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.support.BuiltInType;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MemberUtilTest extends LightJavaCodeInsightFixtureTestCase {

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
    @Mock
    IntentionAction intentionAction;
    @Mock
    MemberIntention memberIntention;
    @Spy
    ModelUtil modelUtil;
    @Spy
    ScriptUtil scriptUtil;
    @InjectMocks
    MemberUtil memberUtil;
    private ExampleFiles exampleFiles;

    PsiElement rootBlock;

    @BeforeEach
    void setUpSuite() throws Exception {
        super.setName("MemberUtilTest");
        super.setUp();

        MockitoAnnotations.initMocks(this);

        exampleFiles = new ExampleFiles(this, myFixture);
        rootBlock = exampleFiles.getActivityWithMembers();
        doReturn(SHORT_DESCRIPTION).when(builtInMember).shortDescription();
        doReturn(HTML_DESCRIPTION).when(builtInMember).htmlDescription();
        doReturn(annotationBuilder).when(annotationHolder).newAnnotation(any(HighlightSeverity.class), anyString());
        doReturn(annotationBuilder).when(annotationBuilder).range(any(PsiElement.class));
        doReturn(annotationBuilder).when(annotationBuilder).tooltip(anyString());
        doReturn(annotationBuilder).when(annotationBuilder).withFix(any(IntentionAction.class));
        doReturn(Arrays.asList(intentionAction)).when(memberIntention).getImportMemberIntentions(any());
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
    void annotateCall() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall call = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    commandCall -> commandCall.getName().equals("myThirdCommand"));
            memberUtil.annotateCall(call, annotationHolder);
            verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.ERROR), anyString());
        });
    }

    @Test
    void annotateCall_MyThirdActivity() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTOperatorCall call = exampleFiles.getPsiElementFromRootDocument(OMTOperatorCall.class, rootBlock,
                    operatorCall -> operatorCall.getName().equals("titleQuery"));
            memberUtil.annotateCall(call, annotationHolder);
            verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.ERROR), anyString());
        });
    }

    @Test
    void annotateCall_annotateAsBuiltInMember() {
        doReturn(builtInMember).when(builtInUtil).getBuiltInMember(anyString(), eq(BuiltInType.Command));
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall call = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    commandCall -> commandCall.getName().equals("myBuiltInMethod"));
            memberUtil.annotateCall(call, annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.INFORMATION), eq(SHORT_DESCRIPTION));
            verify(annotationBuilder).tooltip(eq(HTML_DESCRIPTION));
            verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.ERROR), anyString());
        });
    }

    @Test
    void annotateCall_annotateAsBuiltInMemberThrowsNumberOfInputParametersMismatchException() {
        doReturn(builtInMember).when(builtInUtil).getBuiltInMember(anyString(), eq(BuiltInType.Command));
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall call = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    commandCall -> commandCall.getName().equals("myBuiltInMethod"));
            try {
                doThrow(new NumberOfInputParametersMismatchException("myBuiltInMethod", 0, 1, 2)).when(builtInMember).validateSignature(eq(call));
            } catch (CallCallableMismatchException | NumberOfInputParametersMismatchException | IncorrectFlagException e) {
                e.printStackTrace();
            }
            memberUtil.annotateCall(call, annotationHolder);
            verify(annotationHolder, times(1)).newAnnotation(eq(HighlightSeverity.ERROR), eq("myBuiltInMethod expects between 0 and 1 parameters, found 2"));
        });
    }

    @Test
    void annotateCall_annotateAsBuiltInMemberDoesNotThrowValidateSignatureErrorsWhenNamedReference() {
        doReturn(builtInMember).when(builtInUtil).getBuiltInMember(anyString(), eq(BuiltInType.Command));
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall call = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    commandCall -> commandCall.getName().equals("myBuiltInMethod"));
            try {
                doThrow(new NumberOfInputParametersMismatchException("myBuiltInMethod", 0, 1, 2)).when(builtInMember).validateSignature(eq(call));
            } catch (CallCallableMismatchException | NumberOfInputParametersMismatchException | IncorrectFlagException e) {
                e.printStackTrace();
            }

            JsonObject attributes = new JsonObject();
            attributes.addProperty("namedReference", true);
            doReturn(attributes).when(modelUtil).getJson(call);

            memberUtil.annotateCall(call, annotationHolder);
            verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.ERROR), anyString());
        });
    }

    @Test
    void annotateCall_annotateAsBuiltInMemberDoesThrowValidateSignatureErrorsWhenNamedReferenceFalse() {
        doReturn(builtInMember).when(builtInUtil).getBuiltInMember(anyString(), eq(BuiltInType.Command));
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall call = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    commandCall -> commandCall.getName().equals("myBuiltInMethod"));
            try {
                doThrow(new NumberOfInputParametersMismatchException("myBuiltInMethod", 0, 1, 2)).when(builtInMember).validateSignature(eq(call));
            } catch (CallCallableMismatchException | NumberOfInputParametersMismatchException | IncorrectFlagException e) {
                e.printStackTrace();
            }

            JsonObject attributes = new JsonObject();
            attributes.addProperty("namedReference", false);
            doReturn(attributes).when(modelUtil).getJson(call);

            memberUtil.annotateCall(call, annotationHolder);
            verify(annotationHolder, times(1)).newAnnotation(eq(HighlightSeverity.ERROR), eq("myBuiltInMethod expects between 0 and 1 parameters, found 2"));
        });
    }

    @Test
    void annotateCall_annotateAsBuiltInMemberThrowsNumberInvalidFlagSignature() {
        doReturn(builtInMember).when(builtInUtil).getBuiltInMember(anyString(), eq(BuiltInType.Command));
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall call = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    commandCall -> commandCall.getName().equals("myBuiltInMethodWithFlag"));
            try {
                doThrow(new IncorrectFlagException("someFlag", Arrays.asList("flagA, flagB"))).when(builtInMember).validateSignature(eq(call));
            } catch (CallCallableMismatchException | NumberOfInputParametersMismatchException | IncorrectFlagException e) {
                e.printStackTrace();
            }
            memberUtil.annotateCall(call, annotationHolder);
            verify(annotationHolder, times(1)).newAnnotation(eq(HighlightSeverity.ERROR), eq("Incorrect flag 'someFlag' used, acceptable flags are: 'flagA, flagB'"));
        });
    }

    @Test
    void annotateCall_annotateAsLocalCommand() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall call = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    commandCall -> commandCall.getName().equals("LOCALCOMMAND"));
            doReturn(Arrays.asList("LOCALCOMMAND")).when(modelUtil).getLocalCommands(eq(call));
            memberUtil.annotateCall(call, annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.INFORMATION), eq("LOCALCOMMAND is available as local command"));
            verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.ERROR), anyString());
        });
    }

    @Test
    void annotateCall_annotateAsLocalCommandCallsAnnotateFinalStatement() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall call = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    commandCall -> commandCall.getName().equals("DONE"));
            doReturn(Arrays.asList("DONE")).when(modelUtil).getLocalCommands(eq(call));
            memberUtil.annotateCall(call, annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.INFORMATION), eq("DONE is available as local command"));
            verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.ERROR), anyString());
            verify(scriptUtil, times(1)).annotateFinalStatement(eq(call), eq(annotationHolder));
        });
    }

    @Test
    void annotateCall_annotateAsLocalCommandCallsDoesNotAnnotateWhenOperatorCall() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall call = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    commandCall -> commandCall.getName().equals("DONE"));
            OMTCommandCall callAsSpy = spy(call);
            doReturn(false).when(callAsSpy).isCommandCall();
            doReturn(Arrays.asList("DONE")).when(modelUtil).getLocalCommands(eq(call));
            memberUtil.annotateCall(callAsSpy, annotationHolder);
            verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.INFORMATION), eq("DONE is available as local command"));
        });
    }

    @Test
    void annotateCall_annotateAsLocalCommandCallsDoesNotAnnotateWhenUnknownLocalCommand() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall call = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    commandCall -> commandCall.getName().equals("DONE"));
            OMTCommandCall callAsSpy = spy(call);
            doReturn(new ArrayList<>()).when(modelUtil).getLocalCommands(eq(callAsSpy));
            memberUtil.annotateCall(callAsSpy, annotationHolder);
            verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.INFORMATION), eq("DONE is available as local command"));
        });
    }

    @Test
    void annotateCall_annotateByAttributeString() {
        JsonObject attributes = new JsonObject();
        attributes.addProperty("type", "string");
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTOperatorCall call = exampleFiles.getPsiElementFromRootDocument(OMTOperatorCall.class, rootBlock,
                    operatorCall -> operatorCall.getName().equals("Mijn"));
            doReturn(attributes).when(modelUtil).getJson(eq(call));

            memberUtil.annotateCall(call, annotationHolder);
            verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.ERROR), anyString());
        });
    }

    @Test
    void annotateCall_annotateByAttributeInterpolatedString() {
        JsonObject attributes = new JsonObject();
        attributes.addProperty("type", "interpolatedString");
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTOperatorCall call = exampleFiles.getPsiElementFromRootDocument(OMTOperatorCall.class, rootBlock,
                    operatorCall -> operatorCall.getName().equals("Mijn"));
            doReturn(attributes).when(modelUtil).getJson(eq(call));

            memberUtil.annotateCall(call, annotationHolder);
            verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.ERROR), anyString());
        });
    }

    @Test
    void annotateCall_annotateByAttributeInterpolatedStringThrowsError() {
        JsonObject attributes = new JsonObject();
        attributes.addProperty("type", "interpolatedString");
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTOperatorCall call = exampleFiles.getPsiElementFromRootDocument(OMTOperatorCall.class, rootBlock,
                    operatorCall -> operatorCall.getName().equals("mijnInterpolatedTitel"));
            doReturn(attributes).when(modelUtil).getJson(eq(call));

            memberUtil.annotateCall(call, annotationHolder);
            verify(annotationHolder, times(1)).newAnnotation(eq(HighlightSeverity.ERROR), eq("mijnInterpolatedTitel could not be resolved"));
        });
    }

    @Test
    void annotateCall_annotateByAttributeShortcut() {
        JsonObject attributes = new JsonObject();
        attributes.addProperty("shortcut", "bladiebla");
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTOperatorCall call = exampleFiles.getPsiElementFromRootDocument(OMTOperatorCall.class, rootBlock,
                    operatorCall -> operatorCall.getName().equals("Mijn"));
            doReturn(attributes).when(modelUtil).getJson(eq(call));

            memberUtil.annotateCall(call, annotationHolder);
            verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.ERROR), anyString());
        });
    }

    @Test
    void annotateCall_annotateByAttributeNoJsonThrows() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTOperatorCall call = exampleFiles.getPsiElementFromRootDocument(OMTOperatorCall.class, rootBlock,
                    operatorCall -> operatorCall.getName().equals("Mijn"));
            doReturn(null).when(modelUtil).getJson(eq(call));

            memberUtil.annotateCall(call, annotationHolder);

            // since there is no valid way to check that the operator should not be annotated with an error, it gets annotated
            verify(annotationHolder, times(1)).newAnnotation(eq(HighlightSeverity.ERROR), eq("Mijn could not be resolved"));
        });
    }

    @Test
    void annotateCall_annotateByAttributeThrowsError() {
        JsonObject attributes = new JsonObject();
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTOperatorCall call = exampleFiles.getPsiElementFromRootDocument(OMTOperatorCall.class, rootBlock,
                    operatorCall -> operatorCall.getName().equals("Mijn"));
            doReturn(attributes).when(modelUtil).getJson(eq(call));

            memberUtil.annotateCall(call, annotationHolder);
            verify(annotationHolder, times(1)).newAnnotation(eq(HighlightSeverity.ERROR), eq("Mijn could not be resolved"));
            verify(memberIntention, times(1)).getImportMemberIntentions(eq(call));
        });
    }

    @Test
    void annotateCall_annotateCallToProcedure() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall call = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    commandCall -> commandCall.getName().equals("MijnProcedure"));
            memberUtil.annotateCall(call, annotationHolder);
            verify(annotationHolder).newAnnotation(eq(HighlightSeverity.INFORMATION), eq("Procedure: MijnProcedure"));
            verify(annotationHolder, times(0)).newAnnotation(eq(HighlightSeverity.ERROR), anyString());
        });
    }

    @Test
    void annotateCall_annotateCallToProcedureThrowsCannotBeResolved() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall call = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    commandCall -> commandCall.getName().equals("MijnProcedure"));
            doReturn("WrongType").when(modelUtil).getModelItemType(any(PsiElement.class));
            memberUtil.annotateCall(call, annotationHolder);
            verify(annotationHolder, times(1))
                    .newAnnotation(eq(HighlightSeverity.ERROR), eq("Could not resolve callable element to exported member, this might be an issue with the imported file"));
        });
    }

    @Test
    void annotateCall_annotateCallToProcedureThrowsCannotBeResolvedEscapesViaOntology() {
        ApplicationManager.getApplication().runReadAction(() -> {
            OMTCommandCall call = exampleFiles.getPsiElementFromRootDocument(OMTCommandCall.class, rootBlock,
                    commandCall -> commandCall.getName().equals("MijnProcedure"));
            doReturn("WrongType").when(modelUtil).getModelItemType(any(PsiElement.class));
            doReturn(true).when(modelUtil).isOntology(any(PsiElement.class));
            memberUtil.annotateCall(call, annotationHolder);
            verify(annotationHolder, times(0))
                    .newAnnotation(eq(HighlightSeverity.ERROR), anyString());
        });
    }

    @Test
    void annotateCall_StopsOnNullReference() {
        OMTCall mockCall = mock(OMTCall.class);
        memberUtil.annotateCall(mockCall, annotationHolder);
        verify(mockCall, times(1)).getReference();
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
