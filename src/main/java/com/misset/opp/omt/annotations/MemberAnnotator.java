package com.misset.opp.omt.annotations;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.exceptions.CallCallableMismatchException;
import com.misset.opp.omt.exceptions.IncorrectFlagException;
import com.misset.opp.omt.exceptions.IncorrectSignatureArgument;
import com.misset.opp.omt.exceptions.NumberOfInputParametersMismatchException;
import com.misset.opp.omt.intentions.members.MemberIntention;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTSignature;
import com.misset.opp.omt.psi.OMTSignatureArgument;
import com.misset.opp.omt.psi.impl.OMTBuiltInMember;
import com.misset.opp.omt.psi.named.OMTCall;
import com.misset.opp.omt.psi.support.BuiltInType;
import com.misset.opp.omt.psi.support.OMTCallable;
import com.misset.opp.omt.psi.support.OMTExportMember;

import java.util.List;

import static util.UtilManager.getBuiltinUtil;
import static util.UtilManager.getMemberUtil;
import static util.UtilManager.getModelUtil;

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
                        annotationBuilder.highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
                    });
        } else {
            annotateReference(resolved);
        }
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
