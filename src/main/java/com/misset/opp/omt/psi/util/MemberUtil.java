package com.misset.opp.omt.psi.util;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.PsiJavaDirectoryImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.exceptions.UnknownMappingException;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.support.OMTExportMember;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.misset.opp.omt.psi.intentions.members.MemberIntention.getImportIntentions;

public class MemberUtil {

    /**
     * Returns the PsiElement which contains the declaration for this OperatorCall
     * This can be a DefineQueryStatement somewhere upstream or an import statement
     * When the call points to an imported member it will try to resolve to it's original declaration in the external file,
     * otherwise it will resolve to the import statement.
     * The declaration of the operator must precede it's call to it, not only upstream but also within the same declaration block
     *
     * @param operatorCall
     * @return
     */
    public static Optional<PsiElement> getDeclaringMember(OMTOperatorCall operatorCall) {
        return getDeclaringMember(getDefinedQueryStatement(operatorCall), operatorCall);
    }

    /**
     * Returns the PsiElement which contains the declaration for this CommandCall
     * This can be a DefineCommandStatement somewhere upstream or an import statement.
     * When the call points to an imported member it will try to resolve to it's original declaration in the external file,
     * otherwise it will resolve to the import statement.
     * The declaration of the command must precede it's call to it, not only upstream but also within the same declaration block
     * @param commandCall
     * @return
     */
    public static Optional<PsiElement> getDeclaringMember(OMTCommandCall commandCall) {
        return getDeclaringMember(getDefinedCommandStatement(commandCall), commandCall);
    }

    private static Optional<PsiElement> getDeclaringMember(PsiElement definedStatement, PsiElement call) {
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
                .filter(member -> member.textMatches(callName))
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
    private static boolean isCallBeforeDefine(PsiElement call, PsiElement define) {
        PsiElement callContainingElement = getCallContainingElement(call);

        if(callContainingElement.getParent() == define.getParent()) {
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
     * Returns the defined query statement corresponding to the operator call name
     * It doesn't validate that the defined statement precedes the call
     * @param operatorCall
     * @return
     */
    public static OMTDefineQueryStatement getDefinedQueryStatement(OMTOperatorCall operatorCall) {

        PsiElement element = operatorCall.getParent();
        List<OMTQueriesBlock> processedBlocks = new ArrayList<>();
        while(element != null) {
            if(element instanceof OMTQueriesBlock) {
                Optional<OMTDefineQueryStatement> queryDefined = ((OMTQueriesBlock) element).getDefineQueryStatementList().stream()
                        .filter(defineQueryStatement -> defineQueryStatement.getDefineName().textMatches(operatorCall.getFirstChild().getText()))
                        .findFirst();
                if(queryDefined.isPresent()) { return queryDefined.get(); }
                processedBlocks.add((OMTQueriesBlock) element);
            }
            element = element.getParent();
            if(element instanceof PsiJavaDirectoryImpl) {
                return null;
            }
            if(element != null) {
                OMTQueriesBlock childOfType = PsiTreeUtil.findChildOfType(element, OMTQueriesBlock.class);
                if(childOfType != null && !processedBlocks.contains(childOfType)) { element = childOfType; }
            }

        }
        return null;
    }

    /**
     * Returns the defined command statement corresponding to the command call name
     * It doesn't validate that the defined statement precedes the call
     * @param commandCall
     * @return
     */
    public static OMTDefineCommandStatement getDefinedCommandStatement(OMTCommandCall commandCall) {
        PsiElement element = commandCall.getParent();
        List<OMTCommandsBlock> processedBlocks = new ArrayList<>();
        while(element != null) {
            if(element instanceof OMTCommandsBlock) {
                Optional<OMTDefineCommandStatement> commandDefined = ((OMTCommandsBlock) element).getDefineCommandStatementList().stream()
                        .filter(definedCommandStatement -> definedCommandStatement.getDefineName().textMatches(commandCall.getFirstChild().getText()))
                        .findFirst();
                if(commandDefined.isPresent()) { return commandDefined.get(); }
                processedBlocks.add((OMTCommandsBlock) element);
            }
            element = element.getParent();
            if(element instanceof PsiJavaDirectoryImpl) {
                return null;
            }
            if (element != null) {
                OMTCommandsBlock childOfType = PsiTreeUtil.findChildOfType(element, OMTCommandsBlock.class);
                if (childOfType != null && !processedBlocks.contains(childOfType)) {
                    element = childOfType;
                }
            }

        }
        return null;
    }

    public static void annotateCommandCall(@NotNull OMTCommandCall commandCall, @NotNull AnnotationHolder holder) {
        if (commandCall.getReference().resolve() == null) {
            // command call not found in file or via import.
            // TODO: check if it is a build-in or local command

            // unknown, annotate with error:
            Annotation errorAnnotation = holder.createErrorAnnotation(commandCall.getNameIdentifier(), String.format("%s could not be resolved", commandCall.getName()));
            List<IntentionAction> intentionActionList = getImportIntentions(commandCall);
            intentionActionList.forEach(intentionAction -> errorAnnotation.registerFix(intentionAction));
        }
    }

    public static void annotateOperatorCall(@NotNull OMTOperatorCall operatorCall, @NotNull AnnotationHolder holder) {
        if (operatorCall.getReference().resolve() == null) {
            // command call not found in file or via import.
            // TODO: check if it is a build-in operator

            // unknown, annotate with error:
            Annotation errorAnnotation = holder.createErrorAnnotation(operatorCall.getNameIdentifier(), String.format("%s could not be resolved", operatorCall.getName()));
            List<IntentionAction> intentionActionList = getImportIntentions(operatorCall);
            intentionActionList.forEach(intentionAction -> errorAnnotation.registerFix(intentionAction));
        }
    }

}
