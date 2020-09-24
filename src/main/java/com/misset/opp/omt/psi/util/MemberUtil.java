package com.misset.opp.omt.psi.util;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.exceptions.CallCallableMismatchException;
import com.misset.opp.omt.exceptions.NumberOfInputParametersMismatchException;
import com.misset.opp.omt.exceptions.UnknownMappingException;
import com.misset.opp.omt.external.util.builtIn.BuiltInMember;
import com.misset.opp.omt.external.util.builtIn.BuiltInType;
import com.misset.opp.omt.external.util.builtIn.BuiltInUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.impl.OMTExportMemberImpl;
import com.misset.opp.omt.psi.intentions.members.MemberIntention;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.support.*;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MemberUtil {

    static final ModelUtil modelUtil = ModelUtil.SINGLETON;
    static final ProjectUtil projectUtil = ProjectUtil.SINGLETON;

    public static final MemberUtil SINGLETON = new MemberUtil();

    /**
     * Returns the PsiElement which contains the declaration for this call
     * This can be a DefineStatement somewhere upstream or an import statement
     * When the call points to an imported member it will try to resolve to it's original declaration in the external file,
     * otherwise it will resolve to the import statement.
     * The declaration of the operator must precede it's call to it, not only upstream but also within the same declaration block
     *
     * @param call
     * @return
     */
    public Optional<PsiElement> getDeclaringMember(OMTCall call) {
        OMTDefinedStatement definedStatement = getDefinedStatement(call);
        if (definedStatement != null) {
            // check if the member is declared before it's used, which is a requirement for an OperatorCall
            return isCallBeforeDefine(call, definedStatement) ? Optional.empty() : Optional.of(definedStatement);
        }

        String callName = getCallName(call);

        // check if it's part of this page's exports:
        OMTFile currentFile = (OMTFile) call.getContainingFile();
        HashMap<String, OMTExportMember> currentFileMembers = currentFile.getExportedMembers();
        if (currentFileMembers.containsKey(callName)) {
            return Optional.of(currentFileMembers.get(callName).getResolvingElement());
        }

        // or of locally available ontology declarations:
        HashMap<String, OMTModelItemBlock> declaredOntologies = currentFile.getDeclaredOntologies();
        if (declaredOntologies.containsKey(callName)) {
            return Optional.of(declaredOntologies.get(callName));
        }

        // not found as a member of a defined block, check for imports:
        return getDeclaringMemberFromImport(call);
    }

    private Optional<PsiElement> getDeclaringMemberFromImport(OMTCall call) {

        String callName = getCallName(call);
        OMTFile containingFile = (OMTFile) call.getContainingFile();
        List<OMTMember> importedMembers = containingFile.getImportedMembers();
        Optional<OMTMember> importedMember = importedMembers.stream()
                .filter(member -> member.getText().trim().equals(callName))
                .findFirst();
        if (importedMember.isPresent()) {
            // resolve the import member to an import
            OMTImport omtImport = ImportUtil.getImport(importedMember.get());
            if (omtImport == null) {
                return Optional.of(importedMember.get());
            }

            try {
                VirtualFile importedFile = ImportUtil.getImportedFile(omtImport);
                PsiFile psiFile = PsiManager.getInstance(omtImport.getProject()).findFile(importedFile);
                if (psiFile instanceof OMTFile) {
                    OMTFile omtFile = (OMTFile) psiFile;
                    HashMap<String, OMTExportMember> exportedMembers = omtFile.getExportedMembers();
                    if (exportedMembers.containsKey(callName)) {
                        OMTExportMember omtExportMember = exportedMembers.get(callName);
                        return Optional.of(omtExportMember.getResolvingElement());
                    }
                }
                throw new UnknownMappingException(omtImport.getImportSource().getImportLocation().getText());
            } catch (URISyntaxException | UnknownMappingException | FileNotFoundException e) {
                return Optional.of(importedMember.get());
            }
        } else {
            return Optional.empty();
        }
    }

    public String getCallName(PsiElement call) {
        if (call instanceof OMTOperatorCall) {
            return call.getFirstChild().getText();
        }
        return call.getFirstChild().getText().substring(1);
    }

    /**
     * Checks if the call is made before the operator is defined
     *
     * @param call   - operatorCall or commandCall
     * @param define - definedQueryStatement or definedCommandStatement
     * @return
     */
    private boolean isCallBeforeDefine(OMTCall call, PsiElement define) {
        PsiElement callContainingElement = getCallContainingElement(call);

        if (callContainingElement.getParent() == define.getParent()) {
            // call is part of the same block as the parent:
            return callContainingElement.getStartOffsetInParent() <= define.getStartOffsetInParent();
        } else {
            // different containers. For example:
            //
            // title: getTitle()                            <<-- Block entry (Scalar)
            // queries: |
            //      DEFINE QUERY getTitle() => 'title';     <<-- DefineQueryStatement

            // in this case, the parent define statement, must have a lower or equal depth to the
            // the block entry that's using it.
            return PsiTreeUtil.getDepth(call, call.getContainingFile()) < PsiTreeUtil.getDepth(define.getParent(), define.getContainingFile());
        }
    }

    private PsiElement getCallContainingElement(PsiElement call) {
        // the call depth depends on the origin:
        PsiElement containingElement = PsiTreeUtil.getTopmostParentOfType(call, OMTDefineQueryStatement.class);
        if (containingElement == null) {
            containingElement = PsiTreeUtil.getTopmostParentOfType(call, OMTDefineCommandStatement.class);
        }
        if (containingElement == null) {
            containingElement = PsiTreeUtil.getTopmostParentOfType(call, OMTScriptLine.class);
        }
        if (containingElement == null) {
            containingElement = PsiTreeUtil.getTopmostParentOfType(call, OMTBlockEntry.class);
        }

        return containingElement;
    }

    /**
     * Returns the defined statement corresponding to the call name
     * It doesn't validate that the defined statement precedes the call
     *
     * @param call
     * @return
     */
    private OMTDefinedStatement getDefinedStatement(OMTCall call) {

        PsiFile containingFile = call.getContainingFile();
        ArrayList<OMTDefinedStatement> omtDefinedStatements = new ArrayList<>(PsiTreeUtil.findChildrenOfType(containingFile, OMTDefinedStatement.class));
        return omtDefinedStatements.stream()
                .filter(omtDefinedStatement ->
                        omtDefinedStatement.getDefineName().getName() != null &&
                                omtDefinedStatement.getDefineName().getName().equals(getCallName(call))
                ).findFirst().orElse(null);
    }

    public void annotateCall(@NotNull OMTCall call, @NotNull AnnotationHolder holder) {
        if (call.getNameIdentifier() == null || call.getReference() == null) {
            return;
        }
        PsiElement resolved = call.getReference().resolve();

        if (resolved == null) {
            if (annotateAsBuiltInMember(call, holder)) {
                return;
            }
            if (annotateAsLocalCommand(call, holder)) {
                return;
            }
            if (annotateByAttribute(call)) {
                return;
            }

            // unknown, annotate with error:
            AnnotationBuilder annotationBuilder = holder.newAnnotation(HighlightSeverity.ERROR, String.format("%s could not be resolved", call.getName()))
                    .range(call.getNameIdentifier());

            List<IntentionAction> intentionActionList = MemberIntention.getImportMemberIntentions(call, omtExportMember ->
                    (omtExportMember.isCommand() && call.canCallCommand()) ||
                            (omtExportMember.isOperator() && call.canCallOperator())
            );
            intentionActionList.forEach(annotationBuilder::newFix);
            annotationBuilder.create();
        } else {
            annotateReference(resolved, call, holder);
        }

    }

    private boolean annotateByAttribute(@NotNull OMTCall call) {
        // this method will check if based on the attribute the unresolved reference has to be annotated with an Error
        // when this method returns true, there is no need for the error annotation due to something in the attributes.

        // check attribute type:
        JsonObject json = modelUtil.getJson(call);
        if (json != null && json.has("type") &&
                (json.get("type").getAsString().equals("interpolatedString") ||
                        json.get("type").getAsString().equals("string"))) {
            return true;
        }
        // check shortcut:
        return json != null && json.has("shortcut");
    }

    private boolean annotateAsLocalCommand(@NotNull OMTCall call, @NotNull AnnotationHolder holder) {
        List<String> localCommands = modelUtil.getLocalCommands(call);
        if (localCommands.contains(call.getName())) {
            holder.newAnnotation(HighlightSeverity.INFORMATION, String.format("%s is available as local command", call.getName())).range(call).create();

            // check if final statement:
            if (call.isCommandCall() && getCallName(call).equals("DONE") || getCallName(call).equals("CANCEL")) {
                ScriptUtil.annotateFinalStatement(call, holder);
            }
            return true;
        }
        return false;
    }

    private boolean annotateAsBuiltInMember(@NotNull OMTCall call, @NotNull AnnotationHolder holder) {
        if (call.getNameIdentifier() == null) {
            return false;
        }
        BuiltInMember builtInMember = BuiltInUtil.getBuiltInMember(call.getName(), call.canCallCommand() ? BuiltInType.Command : BuiltInType.Operator);
        if (builtInMember != null) {
            // is a builtIn member, annotate:
            holder.newAnnotation(HighlightSeverity.INFORMATION, builtInMember.shortDescription())
                    .range(call.getNameIdentifier().getTextRange())
                    .tooltip(builtInMember.htmlDescription())
                    .create();
            validateSignature(call, builtInMember, holder);
            return true;
        } else {
            // check if the builtIn members are loaded:
            if (!BuiltInUtil.hasLoaded()) {
                projectUtil.loadBuiltInMembers(call.getProject());
                return annotateAsBuiltInMember(call, holder);
            }
        }
        return false;
    }

    private void annotateReference(@NotNull PsiElement resolved, @NotNull OMTCall call, @NotNull AnnotationHolder holder) {
        if (call.getNameIdentifier() == null) {
            return;
        }
        try {
            OMTExportMember asExportMember = memberToExportMember(resolved);
            if (asExportMember == null) {
                if (modelUtil.isOntology(resolved)) {
                    return;
                }
                throw new Exception("Could not resolve callable element to exported member, this is a bug");
            }
            holder.newAnnotation(HighlightSeverity.INFORMATION, asExportMember.shortDescription())
                    .range(call.getNameIdentifier().getTextRange())
                    .tooltip(asExportMember.htmlDescription())
                    .create();
            validateSignature(call, asExportMember, holder);
        } catch (Exception e) {
            holder.newAnnotation(HighlightSeverity.ERROR, e.getMessage()).range(call.getNameIdentifier()).create();
        }
    }

    private void validateSignature(@NotNull OMTCall call, @NotNull OMTCallable callable, @NotNull AnnotationHolder holder) {
        try {
            callable.validateSignature(call);
        } catch (NumberOfInputParametersMismatchException | CallCallableMismatchException e) {
            JsonObject attributes = modelUtil.getJson(call);
            if (attributes.has("namedReference") && attributes.get("namedReference").getAsBoolean()) {
                return;
            }
            holder.newAnnotation(HighlightSeverity.ERROR, e.getMessage()).range(call).create();
        }
    }

    /**
     * A call resolves to a label of the callable element, i.e. a call to a procedure will resolve to the name
     * of the procedure since that is were we want to navigation to go to.
     * This method helps to obtain the containing element of the name. For a modelItem it will get the ModelItem from the label.
     * For a query or command statement it will return the statement from the definedNamed
     *
     * @param resolvedToElement
     * @return
     */
    private PsiElement getContainingElement(PsiElement resolvedToElement) {
        if (resolvedToElement instanceof OMTPropertyLabel) {
            Optional<OMTModelItemBlock> modelItemBlock = modelUtil.getModelItemBlock(resolvedToElement);
            if (modelItemBlock.isPresent()) {
                return modelItemBlock.get();
            }
        }
        if (resolvedToElement instanceof OMTDefineName) {
            return resolvedToElement.getParent();
        }
        return resolvedToElement;
    }

    public NamedMemberType getNamedMemberType(PsiElement element) {
        if (element instanceof OMTOperatorCall) {
            return NamedMemberType.OperatorCall;
        }
        if (element instanceof OMTDefineName) {
            return NamedMemberType.DefineName;
        }
        if (element instanceof OMTCommandCall) {
            return NamedMemberType.CommandCall;
        }
        if (element instanceof OMTModelItemLabel || element instanceof OMTPropertyLabel) {
            return NamedMemberType.ModelItem;
        }
        if (element instanceof OMTMember && element.getParent().getParent() instanceof OMTImport) {
            return NamedMemberType.ImportingMember;
        }
        return null;
    }

    private OMTExportMember memberToExportMember(PsiElement element) {
        NamedMemberType namedMemberType = getNamedMemberType(element);
        if (namedMemberType == null) {
            return null;
        }

        switch (namedMemberType) {
            case DefineName:
                // operator or command, get via parent:
                PsiElement callableDefine = getContainingElement(element);
                if (callableDefine instanceof OMTDefineQueryStatement) {
                    return new OMTExportMemberImpl(callableDefine, ExportMemberType.Query);
                } else if (callableDefine instanceof OMTDefineCommandStatement) {
                    return new OMTExportMemberImpl(callableDefine, ExportMemberType.Command);
                }
                break;

            case ModelItem:
                OMTModelItemBlock modelItemBlock = (OMTModelItemBlock) getContainingElement(element);
                switch (modelUtil.getModelItemType(modelItemBlock)) {
                    case "Activity":
                        return new OMTExportMemberImpl(modelItemBlock, ExportMemberType.Activity);
                    case "Procedure":
                        return new OMTExportMemberImpl(modelItemBlock, ExportMemberType.Procedure);
                    case "StandaloneQuery":
                        return new OMTExportMemberImpl(modelItemBlock, ExportMemberType.StandaloneQuery);
                    default:
                        return null;
                }

            case ImportingMember:
            case CommandCall:
            case OperatorCall:
            default:
                return null;
        }
        return null;
    }
}
