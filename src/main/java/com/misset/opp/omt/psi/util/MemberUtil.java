package com.misset.opp.omt.psi.util;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.PsiJavaDirectoryImpl;
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
    public static Optional<PsiElement> getDeclaringMember(OMTCall call) {
        OMTDefinedStatement definedStatement = getDefinedStatement(call);
        if (definedStatement != null) {
            // check if the member is declared before it's used, which is a requirement for an OperatorCall
            if (!isCallBeforeDefine(call, definedStatement)) {
                return Optional.of(definedStatement);
            }
        }

        String callName = getCallName(call);

        // check if it's part of this page's exports:
        OMTFile currentFile = (OMTFile) call.getContainingFile();
        HashMap<String, OMTExportMember> currentFileMembers = currentFile.getExportedMembers();
        if (currentFileMembers.containsKey(callName)) {
            return Optional.of(currentFileMembers.get(callName).getResolvingElement());
        }

        // not found as a member of a defined block, check for imports:
        OMTFile containingFile = (OMTFile) call.getContainingFile();
        List<OMTMember> importedMembers = containingFile.getImportedMembers();
        Optional<OMTMember> importedMember = importedMembers.stream()
                .filter(member -> member.getText().trim().equals(callName))
                .findFirst();
        if (importedMember.isPresent()) {
            OMTImport omtImport = ImportUtil.getImport(importedMember.get());
            if (omtImport == null) {
                return Optional.of(importedMember.get());
            } // cast to generic PsiElement optional
            try {
                VirtualFile importedFile = ImportUtil.getImportedFile(omtImport);
                PsiFile psiFile = PsiManager.getInstance(omtImport.getProject()).findFile(importedFile);
                if (psiFile instanceof OMTFile) {
                    OMTFile omtFile = (OMTFile) psiFile;
                    HashMap<String, OMTExportMember> exportedMembers = omtFile.getExportedMembers();
                    if (exportedMembers.containsKey(callName)) {
                        OMTExportMember omtExportMember = exportedMembers.get(callName);
                        return Optional.of(omtExportMember.getResolvingElement());
                    } else {
                        throw new UnknownMappingException(omtImport.getImportSource().getImportLocation().getText());
                    }
                } else {
                    throw new UnknownMappingException(omtImport.getImportSource().getImportLocation().getText());
                }
            } catch (URISyntaxException | UnknownMappingException | FileNotFoundException e) {
                return Optional.of(importedMember.get());
            }
        } else {
            return Optional.empty();
        }
    }

    public static String getCallName(PsiElement call) {
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
    private static boolean isCallBeforeDefine(OMTCall call, PsiElement define) {
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

    private static PsiElement getCallContainingElement(PsiElement call) {
        // the call depth depends on the origin:
        PsiElement containingElement = PsiTreeUtil.getTopmostParentOfType(call, OMTDefineQueryStatement.class);
        if(containingElement == null) { containingElement = PsiTreeUtil.getTopmostParentOfType(call, OMTDefineCommandStatement.class); }
        if(containingElement == null) { containingElement = PsiTreeUtil.getTopmostParentOfType(call, OMTScriptLine.class); }
        if(containingElement == null) { containingElement = PsiTreeUtil.getTopmostParentOfType(call, OMTBlockEntry.class); }

        return containingElement;
    }

    /**
     * Returns the defined statement corresponding to the call name
     * It doesn't validate that the defined statement precedes the call
     *
     * @param call
     * @return
     */
    public static OMTDefinedStatement getDefinedStatement(OMTCall call) {

        PsiElement element = call.getParent();
        List<OMTDefinedBlock> processedBlocks = new ArrayList<>();
        while (element != null) {
            if (element instanceof OMTDefinedBlock) {
                Optional<OMTDefinedStatement> definedStatement = ((OMTDefinedBlock) element).getStatements().stream()
                        .filter(defineQueryStatement -> defineQueryStatement.getDefineName().textMatches(call.getFirstChild().getText()))
                        .findFirst();
                if (definedStatement.isPresent()) {
                    return definedStatement.get();
                }
                processedBlocks.add((OMTDefinedBlock) element);
            }
            element = element.getParent();
            if (element instanceof PsiJavaDirectoryImpl) {
                return null;
            }
            if (element != null) {
                OMTQueriesBlock childOfType = PsiTreeUtil.findChildOfType(element, OMTQueriesBlock.class);
                if (childOfType != null && !processedBlocks.contains(childOfType)) {
                    element = childOfType;
                }
            }

        }
        return null;
    }

    public static void annotateCall(@NotNull OMTCall call, @NotNull AnnotationHolder holder) {
        PsiElement resolved = call.getReference().resolve();

        if (resolved == null) {
            // command call not found in file or via import.
            BuiltInMember builtInMember = BuiltInUtil.getBuiltInMember(call.getName(), call.canCallCommand() ? BuiltInType.Command : BuiltInType.Operator);
            if (builtInMember != null) {
                // is a builtIn member, annotate:
                holder.createAnnotation(HighlightSeverity.INFORMATION, call.getNameIdentifier().getTextRange(), builtInMember.shortDescription(), builtInMember.htmlDescription());
                validateSignature(call, builtInMember, holder);
                return;
            }

            // check if local command:
            List<String> localCommands = ModelUtil.getLocalCommands(call);
            if (localCommands.contains(call.getName())) {
                holder.createInfoAnnotation(call, String.format("%s is available as local command", call.getName()));
                return;
            }

            // check attribute type:
            JsonObject json = ModelUtil.getJson(call);
            if (json.has("type") &&
                    json.get("type").getAsString().equals("interpolatedString") &&
                    json.get("type").getAsString().equals("string")) {
                return;
            }

            // unknown, annotate with error:
            Annotation errorAnnotation = holder.createErrorAnnotation(call.getNameIdentifier(), String.format("%s could not be resolved", call.getName()));
            List<IntentionAction> intentionActionList = MemberIntention.getImportMemberIntentions(call, omtExportMember ->
                    (omtExportMember.isCommand() && call.canCallCommand()) ||
                            (omtExportMember.isOperator() && call.canCallOperator())
            );
            intentionActionList.forEach(errorAnnotation::registerFix);
        } else {
            // callable is found as reference. Since the reference can be resolved to a PsiElement
            // the callable element is part of the project code, not a built-in or local command
            // the call will resolve to the label of the callable
            try {
                OMTExportMember asExportMember = memberToExportMember(resolved);
                if (asExportMember == null) {
                    throw new Exception("Could not resolve callable element to exported member, this is a bug");
                }
                holder.createAnnotation(HighlightSeverity.INFORMATION, call.getNameIdentifier().getTextRange(),
                        asExportMember.shortDescription(), asExportMember.htmlDescription());
                validateSignature(call, asExportMember, holder);
            } catch (Exception e) {
                holder.createErrorAnnotation(call.getNameIdentifier(), e.getMessage());
            }
        }
    }

    private static void validateSignature(@NotNull OMTCall call, @NotNull OMTCallable callable, @NotNull AnnotationHolder holder) {
        try {
            callable.validateSignature(call);
        } catch (NumberOfInputParametersMismatchException | CallCallableMismatchException e) {
            JsonObject attributes = ModelUtil.getJson(call);
            if (attributes.has("namedReference") && attributes.get("namedReference").getAsBoolean()) {
                holder.createErrorAnnotation(call, e.getMessage());
            }
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
    public static PsiElement getContainingElement(PsiElement resolvedToElement) {
        if (resolvedToElement instanceof OMTPropertyLabel) {
            Optional<OMTModelItemBlock> modelItemBlock = ModelUtil.getModelItemBlock(resolvedToElement);
            if (modelItemBlock.isPresent()) {
                return modelItemBlock.get();
            }
        }
        if (resolvedToElement instanceof OMTDefineName) {
            return resolvedToElement.getParent();
        }
        return resolvedToElement;
    }

    public static NamedMemberType getNamedMemberType(PsiElement element) {
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

    public static OMTExportMember memberToExportMember(PsiElement element) {
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
                switch (modelItemBlock.getModelItemLabel().getModelItemTypeElement().getText().toLowerCase()) {
                    case "!activity":
                        return new OMTExportMemberImpl(modelItemBlock, ExportMemberType.Activity);
                    case "!procedure":
                        return new OMTExportMemberImpl(modelItemBlock, ExportMemberType.Procedure);
                    case "!standalonequery":
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
