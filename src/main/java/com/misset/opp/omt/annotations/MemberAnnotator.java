package com.misset.opp.omt.annotations;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.exceptions.CallCallableMismatchException;
import com.misset.opp.omt.exceptions.IncorrectFlagException;
import com.misset.opp.omt.exceptions.IncorrectSignatureArgument;
import com.misset.opp.omt.exceptions.NumberOfInputParametersMismatchException;
import com.misset.opp.omt.intentions.members.MemberIntention;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTInterpolationTemplate;
import com.misset.opp.omt.psi.OMTSignature;
import com.misset.opp.omt.psi.OMTSignatureArgument;
import com.misset.opp.omt.psi.impl.OMTBuiltInMember;
import com.misset.opp.omt.psi.named.OMTCall;
import com.misset.opp.omt.psi.support.BuiltInType;
import com.misset.opp.omt.psi.support.OMTCallable;
import com.misset.opp.omt.psi.support.OMTExportMember;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.*;

public class MemberAnnotator extends AbstractAnnotator {

    public MemberAnnotator(AnnotationHolder holder) {
        super(holder);
    }

    public void annotate(PsiElement element) {
        if (element instanceof OMTCall) {
            annotate((OMTCall) element);
        } else if (element instanceof OMTSignatureArgument) {
            annotate((OMTSignatureArgument) element);
        } else if (element instanceof OMTSignature) {
            annotate((OMTSignature) element);
        }
    }

    private void annotate(OMTSignatureArgument signatureArgument) {
        OMTSignature signature = (OMTSignature) signatureArgument.getParent();
        OMTCall call = (OMTCall) signature.getParent();
        try {
            final OMTCallable callable = call.getCallable();
            if (callable != null) {
                callable.validateSignatureArgument(
                        signature.getSignatureArgumentList().indexOf(signatureArgument),
                        signatureArgument
                );
            }
        } catch (IncorrectSignatureArgument exception) {
            setError(exception.getMessage());
        }
    }

    private void annotate(OMTSignature signature) {
        OMTCall call = (OMTCall) signature.getParent();
        JsonObject attributes = getModelUtil().getJson(call);
        // if the call is in a scalar that is considered a namedReference, it's not actually a call
        if (attributes.has("namedReference") && attributes.get("namedReference").getAsBoolean()) return;

        // validate the call itself, number of arguments, required etc
        try {
            final OMTCallable callable = call.getCallable();
            if (callable != null) callable.validateSignature(call);
        } catch (NumberOfInputParametersMismatchException | CallCallableMismatchException | IncorrectFlagException exception) {
            setError(exception.getMessage());
        }
    }

    private void annotate(OMTCall call) {
        PsiElement resolved = call.getReference() != null ? call.getReference().resolve() : null;

        if (annotateByAttribute(call)) return;

        if (resolved == null) {
            if (annotateAsBuiltInMember(call)) return;
            if (annotateAsLocalCommand(call)) return;

            // do not error on module files
            // TODO: create specific handler for Module files
            if (call.getContainingFile() != null && ((OMTFile) call.getContainingFile()).isModuleFile()) return;

            // unknown, annotate with error:
            setError(String.format("%s could not be resolved", call.getName()),
                    annotationBuilder -> {
                        // add importing possibilities
                        for (IntentionAction action : MemberIntention.getImportMemberIntentions(call)) {
                            annotationBuilder = annotationBuilder.withFix(action);
                        }
                    });
        } else {
            annotateReference(resolved);
        }
    }

    private boolean annotateByAttribute(@NotNull OMTCall call) {
        // this method will check if based on the attribute the unresolved reference has to be annotated with an Error
        // when this method returns true, there is no need for the error annotation due to something in the attributes.

        // examples are the title: field in the model which is a string or interpolatedString (IS)
        // in YAML, it's not required to encapsulate the string with quotes or backticks but can simply be a plain scalar value
        // this will make the lexer parse these names as Operator calls which can usually not be resolved

        // The parsed OMT Model determines if the fields are classified as string or IS and will simply ignore them for annotation
        // except when the call is part of a template / placeholder block in the interpolated string
        // in which case the call must be resolved

        // check attribute type:
        JsonObject json = getModelUtil().getJson(call);
        String type = json != null && json.has("type") ? json.get("type").getAsString() : "unknown";
        switch (type) {
            case "string":
                // plain scalar string is always ignored, any value should be accepted here since resolved to a string
                return true;
            case "interpolatedString":
                // if the call is NOT part of an interpolation template block, it can be ignored
                return PsiTreeUtil.findFirstParent(call, parent -> parent instanceof OMTInterpolationTemplate) == null;
            default:
        }

        // finally, a scalar value can be used as a shortcut to populate a complex structure
        // for example, the parameters are actually a structure with a name and type but are usually provided
        // as a shortcut using the $varName (pol:Pol) syntax.
        // TODO: properly parse the shortcut structure
        return json != null && json.has("shortcut");
    }

    private boolean annotateAsLocalCommand(OMTCall call) {
        if (!call.isCommandCall()) return false;

        List<String> localCommands = getModelUtil().getLocalCommands(call);
        if (localCommands.contains(call.getName())) {
            setInformation(String.format("%s is available as local command", call.getName()));
            return true;
        }
        return false;
    }

    private boolean annotateAsBuiltInMember(OMTCall call) {
        OMTBuiltInMember builtInMember = getBuiltinUtil().getBuiltInMember(call.getName(), call.isCommandCall() ? BuiltInType.Command : BuiltInType.Operator);
        if (builtInMember != null) {
            setInformation(builtInMember.shortDescription(),
                    annotationBuilder -> annotationBuilder.tooltip(builtInMember.htmlDescription()));
            return true;
        }
        return false;
    }

    private void annotateReference(PsiElement resolved) {
        OMTExportMember asExportMember = getMemberUtil().memberToExportMember(resolved);
        if (asExportMember == null) {
            if (getModelUtil().isOntology(resolved)) return;
            setError("Could not resolve callable element to exported member, this might be an issue with the imported file");
        } else {
            setInformation(asExportMember.shortDescription(),
                    annotationBuilder -> annotationBuilder.tooltip(asExportMember.htmlDescription()));
        }
    }

}
