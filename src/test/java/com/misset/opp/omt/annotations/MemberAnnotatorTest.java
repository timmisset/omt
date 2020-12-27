package com.misset.opp.omt.annotations;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.exceptions.CallCallableMismatchException;
import com.misset.opp.omt.exceptions.IncorrectFlagException;
import com.misset.opp.omt.exceptions.IncorrectSignatureArgument;
import com.misset.opp.omt.exceptions.NumberOfInputParametersMismatchException;
import com.misset.opp.omt.intentions.members.MemberIntention;
import com.misset.opp.omt.psi.OMTInterpolationTemplate;
import com.misset.opp.omt.psi.OMTSignature;
import com.misset.opp.omt.psi.OMTSignatureArgument;
import com.misset.opp.omt.psi.impl.OMTBuiltInMember;
import com.misset.opp.omt.psi.named.OMTCall;
import com.misset.opp.omt.psi.support.OMTCallable;
import com.misset.opp.omt.psi.support.OMTExportMember;
import com.misset.opp.omt.psi.support.OMTParameter;
import com.misset.opp.omt.psi.util.MemberUtil;
import com.misset.opp.omt.psi.util.ModelUtil;
import com.misset.opp.omt.util.BuiltInUtil;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class MemberAnnotatorTest extends OMTAnnotationTest {

    @Mock
    OMTCall call;

    @Mock
    OMTCallable callable;

    @Mock
    OMTExportMember exportMember;

    @Mock
    OMTBuiltInMember builtInMember;

    @Mock
    OMTSignature signature;

    @Mock
    OMTSignatureArgument signatureArgument;

    JsonObject attributes;

    @Mock
    ModelUtil modelUtil;

    @Mock
    BuiltInUtil builtInUtil;

    @Mock
    MemberUtil memberUtil;

    @InjectMocks
    MemberAnnotator memberAnnotator;

    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("MemberAnnotatorTest");
        super.setUp();
        MockitoAnnotations.openMocks(this);

        setUtilMock(modelUtil);
        setUtilMock(builtInUtil);
        setUtilMock(memberUtil);

        doReturn("Callable").when(callable).getName();
        doReturn(true).when(callable).isCommand();
        doReturn("Call").when(call).getName();
        doReturn(true).when(call).isCommandCall();

        doReturn(signature).when(signatureArgument).getParent();
        doReturn(call).when(signature).getParent();
        doReturn(callable).when(call).getCallable();
        doReturn(Arrays.asList(signatureArgument)).when(signature).getSignatureArgumentList();

        attributes = new JsonObject();
        attributes.addProperty("namedReference", false);
        doReturn(attributes).when(modelUtil).getJson(eq(call));

        doReturn(null).when(builtInUtil).getBuiltInMember(anyString(), any());

    }

    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void annotateMemberNoAnnotationWhenInvalidType() {
        memberAnnotator.annotate(mock(PsiElement.class));
        verifyNoAnnotations();
    }

    @Test
    void annotateSignatureArgumentThrowsErrorsWhenExceptionOnValidation() throws IncorrectSignatureArgument {
        setOntologyModel();
        final OMTParameter parameter = mock(OMTParameter.class);
        doReturn("$parameter").when(parameter).getName();
        List<Resource> acceptableTypes = classesAsResourceList("ClassA");
        List<Resource> argumentTypes = classesAsResourceList("ClassC");
        doThrow(new IncorrectSignatureArgument(parameter, acceptableTypes, argumentTypes))
                .when(callable).validateSignatureArgument(eq(0), eq(signatureArgument));

        memberAnnotator.annotate(signatureArgument);
        verifyError("Incorrect type, $parameter expects type ClassA but value is of type: ClassC");
    }

    @Test
    void annotateSignatureArgumentThrowsNoErrorsWhenNoException() {
        memberAnnotator.annotate(signatureArgument);
        verifyNoErrors();
    }

    @Test
    void annotateSignatureThrowsNoErrorsWhenNamedReferenceIsTrue() {
        attributes.addProperty("namedReference", true);
        memberAnnotator.annotate(signature);

        verifyNoErrors();
    }

    @Test
    void annotateSignatureThrowsNoErrorWhenHasNoAttributesMemberNamedReference() {
        attributes.remove("namedReference");
        memberAnnotator.annotate(signature);
        verifyNoErrors();
    }

    @Test
    void annotateSignatureThrowsNumberOfInputParametersMismatchExceptionError() throws NumberOfInputParametersMismatchException, IncorrectFlagException, CallCallableMismatchException {
        doThrow(
                new NumberOfInputParametersMismatchException("Member", 0, 1, 2)
        ).when(callable).validateSignature(call);
        memberAnnotator.annotate(signature);
        verifyError("Member expects between 0 and 1 parameters, found 2");
    }

    @Test
    void annotateSignatureThrowsCallCallableMismatchExceptionExceptionError() throws NumberOfInputParametersMismatchException, IncorrectFlagException, CallCallableMismatchException {
        doThrow(
                new CallCallableMismatchException(callable, call)
        ).when(callable).validateSignature(call);

        memberAnnotator.annotate(signature);
        verifyError("Callable is a Command and cannot be called using Call");
    }

    @Test
    void annotateSignatureThrowsIncorrectFlagExceptionExceptionError() throws NumberOfInputParametersMismatchException, IncorrectFlagException, CallCallableMismatchException {
        doThrow(
                new IncorrectFlagException("illegalFlag", Arrays.asList("FlagA", "FlagB"))
        ).when(callable).validateSignature(call);
        memberAnnotator.annotate(signature);
        verifyError("Incorrect flag 'illegalFlag' used, acceptable flags are: 'FlagA', 'FlagB'");
    }

    @Test
    void annotateCallByAttributeThrowsNoErrorWhenIsStringField() {
        attributes.addProperty("type", "string");
        memberAnnotator.annotate(call);
        verifyNoErrors();
    }

    @Test
    void annotateCallByAttributeThrowsNoErrorWhenIsInterpolatedStringAndNotInTemplateField() {
        attributes.addProperty("type", "interpolatedString");
        setPsiTreeUtilMockWhenThenReturn(() -> PsiTreeUtil.findFirstParent(eq(call), any()), null);
        memberAnnotator.annotate(call);
        verifyNoErrors();
    }

    @Test
    void annotateCallByAttributeThrowsNoErrorWhenIsShortcut() {
        attributes.addProperty("shortcut", "");
        setPsiTreeUtilMockWhenThenReturn(() -> PsiTreeUtil.findFirstParent(eq(call), any()), null);
        memberAnnotator.annotate(call);
        verifyNoErrors();
    }

    @Test
    void annotateCallByAttributeThrowsErrorWhenIsInterpolatedStringAndInTemplateField() {
        attributes.addProperty("type", "interpolatedString");
        doReturn(mock(OMTInterpolationTemplate.class)).when(call).getParent();
        memberAnnotator.annotate(call);
        verifyError("Call could not be resolved");
    }

    @Test
    void annotateCallByAttributeThrowsErrorWhenNoAttributes() {
        doReturn(null).when(modelUtil).getJson(eq(call));
        memberAnnotator.annotate(call);
        verifyError("Call could not be resolved");
    }

    @Test
    void addsImportIntentions() {
        doReturn(null).when(modelUtil).getJson(eq(call));
        List<IntentionAction> intentionActionList = new ArrayList<>();
        intentionActionList.add(mock(IntentionAction.class));
        intentionActionList.add(mock(IntentionAction.class));

        try (MockedStatic<MemberIntention> mockedStatic = mockStatic(MemberIntention.class)) {
            mockedStatic.when(() -> MemberIntention.getImportMemberIntentions(
                    eq(call)
            )).thenReturn(intentionActionList);
            memberAnnotator.annotate(call);
        }

        verifyError("Call could not be resolved");
        verify(getBuilder(), times(1)).withFix(eq(intentionActionList.get(0)));
        verify(getBuilder(), times(1)).withFix(eq(intentionActionList.get(1)));
    }

    @Test
    void annotateReferenceThrowsUnresolvableErrorWhenNoExportMemberAndNoOntology() {
        final PsiReference reference = mock(PsiReference.class);
        final PsiElement resolved = mock(PsiElement.class);
        doReturn(reference).when(call).getReference();
        doReturn(resolved).when(reference).resolve();
        doReturn(null).when(memberUtil).memberToExportMember(eq(resolved));
        doReturn(false).when(modelUtil).isOntology(eq(resolved));

        memberAnnotator.annotate(call);
        verifyError("Could not resolve callable element to exported member, this might be an issue with the imported file");
    }

    @Test
    void annotateReferenceThrowsNoUnresolvableErrorWhenNoExportMemberButIsOntology() {
        final PsiReference reference = mock(PsiReference.class);
        final PsiElement resolved = mock(PsiElement.class);
        doReturn(reference).when(call).getReference();
        doReturn(resolved).when(reference).resolve();
        doReturn(null).when(memberUtil).memberToExportMember(eq(resolved));
        doReturn(true).when(modelUtil).isOntology(eq(resolved));

        memberAnnotator.annotate(call);
        verifyNoErrors();
    }

    @Test
    void annotateReferenceThrowsNoUnresolvableErrorWhenExportMember() {
        final PsiReference reference = mock(PsiReference.class);
        final PsiElement resolved = mock(PsiElement.class);
        doReturn(reference).when(call).getReference();
        doReturn(resolved).when(reference).resolve();

        doReturn("short description").when(exportMember).shortDescription();
        doReturn("html description").when(exportMember).htmlDescription();
        doReturn(exportMember).when(memberUtil).memberToExportMember(eq(resolved));

        memberAnnotator.annotate(call);

        verifyNoErrors();
        verifyInfo("short description");
        verify(getBuilder()).tooltip(eq("html description"));
    }

    @Test
    void annotateAsLocalCommandThrowsErrorWhenIsNotCommandCall() {
        doReturn(false).when(call).isCommandCall();
        memberAnnotator.annotate(call);
        verifyError("Call could not be resolved");
    }

    @Test
    void annotateAsLocalCommandThrowsErrorWhenCommandCallIsNotLocalCommand() {
        doReturn("NotALocalCommand").when(call).getName();
        doReturn(Collections.singletonList("LocalCommand")).when(modelUtil).getLocalCommands(eq(call));
        memberAnnotator.annotate(call);
        verifyError("NotALocalCommand could not be resolved");
    }

    @Test
    void annotateAsLocalCommandThrowsNoErrorWhenCommandCallIsLocalCommand() {
        doReturn("LocalCommand").when(call).getName();
        doReturn(Collections.singletonList("LocalCommand")).when(modelUtil).getLocalCommands(eq(call));
        memberAnnotator.annotate(call);
        verifyInfo("LocalCommand is available as local command");
    }

    @Test
    void annotateAsBuiltinCommandThrowsNoErrorWhenIsBuiltCommandCall() {
        doReturn("short description").when(builtInMember).shortDescription();
        doReturn("html description").when(builtInMember).htmlDescription();
        doReturn(builtInMember).when(builtInUtil).getBuiltInMember(anyString(), any());
        memberAnnotator.annotate(call);
        verifyNoErrors();
        verifyInfo("short description");
        verify(getBuilder()).tooltip(eq("html description"));
    }
}
