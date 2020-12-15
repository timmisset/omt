package com.misset.opp.omt.psi.util;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.exceptions.CallCallableMismatchException;
import com.misset.opp.omt.exceptions.IncorrectFlagException;
import com.misset.opp.omt.exceptions.IncorrectSignatureArgument;
import com.misset.opp.omt.exceptions.NumberOfInputParametersMismatchException;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.impl.BuiltInMember;
import com.misset.opp.omt.psi.impl.OMTExportMemberImpl;
import com.misset.opp.omt.psi.intentions.generic.RemoveIntention;
import com.misset.opp.omt.psi.intentions.members.MemberIntention;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.support.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.util.UtilManager.*;

public class MemberUtil {

    private final MemberIntention memberIntention = new MemberIntention();
    private final RemoveIntention removeIntention = new RemoveIntention();

    /**
     * Returns the PsiElement which contains the declaration for this call
     * This can be a DefineStatement somewhere upstream or an import statement
     * When the call points to an imported member it will try to resolve to it's original declaration in the external file,
     * otherwise it will resolve to the import statement.
     * The declaration of the operator must precede it's call to it, not only upstream but also within the same declaration block
     */
    public Optional<PsiElement> getDeclaringMember(OMTCall call) {
        String callName = getCallName(call);
        OMTDefinedStatement definedStatement = getAccessibleDefinedStatements(call)
                .stream()
                .filter(statement -> statement.getDefineName().getName().equals(callName))
                .filter(statement -> (statement.isCommand() && call.isCommandCall()) || (statement.isQuery() && call.isOperatorCall()))
                .findFirst()
                .orElse(null);
        if (definedStatement != null) {
            return Optional.of(definedStatement);
        }

        // check if it's part of this page's exports:
        OMTFile currentFile = (OMTFile) call.getContainingFile();
        HashMap<String, OMTExportMember> currentFileMembers = currentFile.getExportedMembers();
        if (currentFileMembers.containsKey(callName)) {
            return Optional.of(currentFileMembers.get(callName).getResolvingElement());
        }

        // or of locally available ontology declarations:
        Map<String, OMTModelItemBlock> declaredOntologies = currentFile.getDeclaredOntologies();
        if (declaredOntologies.containsKey(callName)) {
            return Optional.of(declaredOntologies.get(callName));
        }

        // not found as a member of a defined block, check for imports:
        return getDeclaringMemberFromImport(call, getCallName(call));
    }

    /**
     * Method that will look for any corresponding import member based on this elements containing file
     * and nameIdentifier
     */
    public Optional<PsiElement> getDeclaringMemberFromImport(PsiElement element, String nameIdentifier) {
        OMTFile containingFile = (OMTFile) element.getContainingFile();
        List<OMTMember> importedMembers = containingFile.getImportedMembers();
        Optional<OMTMember> importedMember = importedMembers.stream()
                .filter(member ->
                        member != null &&
                                member.getName() != null &&
                                member.getName().trim().equals(nameIdentifier))
                .findFirst();
        if (importedMember.isPresent()) {
            return getImportUtil().resolveImportMember(importedMember.get());
        } else {
            return Optional.empty();
        }
    }

    public String getCallName(OMTCall call) {
        String name = call.getFirstChild().getText();
        return call.isCommandCall() && name.startsWith("@") ? name.substring(1) : name;
    }

    private PsiElement getComparableContainer(PsiElement element) {
        PsiElement containingElement = PsiTreeUtil.getTopmostParentOfType(element, OMTDefinedStatement.class);
        if (containingElement == null) {
            containingElement = PsiTreeUtil.getTopmostParentOfType(element, OMTScriptLine.class);
        }
        if (containingElement == null) {
            containingElement = PsiTreeUtil.getTopmostParentOfType(element, OMTModelItemBlock.class);
        }
        if (containingElement == null) {
            containingElement = PsiTreeUtil.getTopmostParentOfType(element, OMTBlockEntry.class);
        }

        return containingElement;
    }

    private boolean isComparableContainer(PsiElement element) {
        return element instanceof OMTDefinedStatement ||
                element instanceof OMTScriptLine ||
                element instanceof OMTBlockEntry;
    }

    public List<OMTDefinedStatement> getAccessibleDefinedStatements(PsiElement element) {
        final Collection<OMTDefinedStatement> allDefinedStatements = PsiTreeUtil.findChildrenOfType(element.getContainingFile(), OMTDefinedStatement.class);
        // the container for this element that determines the accessibility level
        final PsiElement comparableContainer = getComparableContainer(element);

        return allDefinedStatements.stream().filter(
                definedStatement -> isAccessible(definedStatement, comparableContainer)
        ).collect(Collectors.toList());
    }

    private boolean isAccessible(OMTDefinedStatement definedStatement, PsiElement comparableContainer) {
        assert isComparableContainer(comparableContainer);

        if (definedStatement.getParent() == comparableContainer.getParent()) {
            // comparable container and defined statement are part of the same block
            // DEFINE QUERY A => ...
            // DEFINE QUERY B => ...
            // A is accessible for B, but B not for A.
            return definedStatement.getStartOffsetInParent() < comparableContainer.getStartOffsetInParent();
        }
        // comparable container and defined statement are not part of the same block
        if (((OMTFile) definedStatement.getContainingFile()).isPartOfRootBlock(definedStatement)) {
            // if the defined statement is part of the root block,
            return true;
        }
        // finally, check if the container and defined statement are part of the same model item (Activity, Component etc)
        // in which case the defined statement is available to all members of that model item
        return getModelUtil().getModelItemBlock(definedStatement).orElse(null) == getModelUtil().getModelItemBlock(comparableContainer).orElse(null);
    }

    public void annotateCall(@NotNull OMTCall call, @NotNull AnnotationHolder holder) {
        if (call.getReference() == null) {
            return;
        }
        PsiElement resolved = call.getReference().resolve();
        if (annotateByAttribute(call)) {
            return;
        }

        if (resolved == null) {
            if (annotateAsBuiltInMember(call, holder)) {
                return;
            }
            if (annotateAsLocalCommand(call, holder)) {
                return;
            }

            // unknown, annotate with error:
            AnnotationBuilder annotationBuilder = holder.newAnnotation(HighlightSeverity.ERROR, String.format("%s could not be resolved", call.getName()))
                    .range(call.getNameIdentifier());

            for (IntentionAction action : memberIntention.getImportMemberIntentions(call)) {
                annotationBuilder = annotationBuilder.withFix(action);
            }
            annotationBuilder.create();
        } else {
            annotateReference(resolved, call, holder);
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

    private boolean annotateAsLocalCommand(@NotNull OMTCall call, @NotNull AnnotationHolder holder) {
        if (!call.isCommandCall()) {
            return false;
        }

        List<String> localCommands = getModelUtil().getLocalCommands(call);
        if (localCommands.contains(call.getName())) {
            holder.newAnnotation(HighlightSeverity.INFORMATION, String.format("%s is available as local command", call.getName())).range(call).create();

            // check if final statement:
            if (Arrays.asList("DONE", "CANCEL").contains(getCallName(call))) {
                getScriptUtil().annotateFinalStatement(call, holder);
            }
            return true;
        }
        return false;
    }

    public OMTCallable getCallable(OMTCall call) {
        BuiltInMember builtInMember = getBuiltinUtil().getBuiltInMember(call.getName(), call.canCallCommand() ? BuiltInType.Command : BuiltInType.Operator);
        if (builtInMember != null) {
            return builtInMember;
        }

        if (call.getReference() != null) {
            return memberToExportMember(call.getReference().resolve());
        }
        return null;
    }

    private boolean annotateAsBuiltInMember(@NotNull OMTCall call, @NotNull AnnotationHolder holder) {
        BuiltInMember builtInMember = getBuiltinUtil().getBuiltInMember(call.getName(), call.canCallCommand() ? BuiltInType.Command : BuiltInType.Operator);
        if (builtInMember != null) {
            // is a builtIn member, annotate:
            holder.newAnnotation(HighlightSeverity.INFORMATION, builtInMember.shortDescription())
                    .range(call.getNameIdentifier())
                    .tooltip(builtInMember.htmlDescription())
                    .create();
            validateSignature(call, builtInMember, holder);
            return true;
        }
        return false;
    }

    private void annotateReference(@NotNull PsiElement resolved, @NotNull OMTCall call, @NotNull AnnotationHolder holder) {
        PsiElement nameIdentifier = call.getNameIdentifier();
        OMTExportMember asExportMember = memberToExportMember(resolved);
        if (asExportMember == null) {
            if (getModelUtil().isOntology(resolved)) {
                return;
            }
            holder.newAnnotation(HighlightSeverity.ERROR,
                    "Could not resolve callable element to exported member, this might be an issue with the imported file")
                    .range(nameIdentifier).create();
        } else {
            holder.newAnnotation(HighlightSeverity.INFORMATION, asExportMember.shortDescription())
                    .range(nameIdentifier)
                    .tooltip(asExportMember.htmlDescription())
                    .create();
            validateSignature(call, asExportMember, holder);
        }
    }

    private void validateSignature(@NotNull OMTCall call, @NotNull OMTCallable callable, @NotNull AnnotationHolder holder) {

        // validate the call itself, number of arguments, required etc
        try {
            callable.validateSignature(call);
        } catch (NumberOfInputParametersMismatchException | CallCallableMismatchException e) {
            JsonObject attributes = getModelUtil().getJson(call);
            if (attributes.has("namedReference") && attributes.get("namedReference").getAsBoolean()) {
                return;
            }
            holder.newAnnotation(HighlightSeverity.ERROR, e.getMessage()).range(call).create();
        } catch (IncorrectFlagException exception) {
            holder.newAnnotation(HighlightSeverity.ERROR, exception.getMessage())
                    .range(call.getFlagSignature())
                    .create();
        }
        // if passed, validate the individual arguments
        if (call.getSignature() != null) {
            call.getSignature().getSignatureArgumentList().forEach(
                    signatureArgument -> {
                        try {
                            callable.validateSignatureArgument(
                                    call.getSignature().getSignatureArgumentList().indexOf(signatureArgument),
                                    signatureArgument
                            );
                        } catch (IncorrectSignatureArgument exception) {
                            holder.newAnnotation(HighlightSeverity.ERROR, exception.getMessage())
                                    .range(signatureArgument)
                                    .create();
                        }
                    }
            );
        }

    }

    /**
     * A call resolves to a label of the callable element, i.e. a call to a procedure will resolve to the name
     * of the procedure since that is were we want to navigation to go to.
     * This method helps to obtain the containing element of the name. For a modelItem it will get the ModelItem from the label.
     * For a query or command statement it will return the statement from the definedNamed
     */
    private PsiElement getContainingElement(PsiElement resolvedToElement) {
        // there are 2 options, either the call resolves to a modelItem or to a defined statement
        return resolvedToElement instanceof OMTModelItemLabel ?
                getModelUtil().getModelItemBlock(resolvedToElement).orElse(null) :
                resolvedToElement.getParent();
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
        if (element instanceof OMTMember) {
            return PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTImportBlock) != null ? NamedMemberType.ImportingMember : NamedMemberType.ExportingMember;
        }
        return null;
    }

    public OMTCallable parseDefinedToCallable(OMTDefineName defineName) {
        return memberToExportMember(defineName);
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
                } else {
                    return new OMTExportMemberImpl(callableDefine, ExportMemberType.Command);
                }

            case ModelItem:
                OMTModelItemBlock modelItemBlock = (OMTModelItemBlock) getContainingElement(element);
                switch (getModelUtil().getModelItemType(modelItemBlock)) {
                    case "Activity":
                        return new OMTExportMemberImpl(modelItemBlock, ExportMemberType.Activity);
                    case "Procedure":
                        return new OMTExportMemberImpl(modelItemBlock, ExportMemberType.Procedure);
                    case "StandaloneQuery":
                        return new OMTExportMemberImpl(modelItemBlock, ExportMemberType.StandaloneQuery);
                    default:
                        return null;
                }

            case ExportingMember:
                // return the exporting member by resolving it
                if (element.getReference() == null) {
                    return null;
                }

                // resolve the exported member (probably via the import) to the original element
                PsiElement resolvedElement = element.getReference().resolve();
                return resolvedElement != element ? memberToExportMember(resolvedElement) : null;

            case ImportingMember:
            case CommandCall:
            case OperatorCall:
            default:
        }
        return null;
    }

    public void annotateImportedMember(OMTMember importedMember, AnnotationHolder holder) {
        annotateImportMemberUsage(importedMember, holder);
        annotateImportMemberDuplications(importedMember, holder);
    }

    private void annotateImportMemberDuplications(OMTMember importedMember, AnnotationHolder holder) {
        final OMTMemberList omtMemberList = PsiTreeUtil.getParentOfType(importedMember, OMTMemberList.class);
        if (omtMemberList == null) {
            return;
        }
        final List<OMTMemberListItem> memberListItemList = omtMemberList.getMemberListItemList();
        final OMTMemberListItem importedMemberParent = (OMTMemberListItem) importedMember.getParent();
        final boolean duplication = memberListItemList.stream().anyMatch(
                omtMemberListItem -> omtMemberListItem.getName().equals(importedMember.getName()) &&
                        memberListItemList.indexOf(omtMemberListItem) < memberListItemList.indexOf(importedMemberParent)
        );
        if (duplication) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Duplicate import")
                    .withFix(removeIntention.getRemoveIntention(importedMemberParent))
                    .create();
        }
    }

    private void annotateImportMemberUsage(OMTMember importedMember, AnnotationHolder holder) {
        if (importedMember.getReference() == null) {
            return;
        }
        final PsiFile currentFile = importedMember.getContainingFile();
        PsiElement resolvedElement = importedMember.getReference().resolve();
        if (resolvedElement == null) {
            return;
        }

        if (ReferencesSearch.search(resolvedElement)
                .anyMatch(
                        psiReference -> psiReference.getElement().getContainingFile() == currentFile &&
                                importedMember != psiReference.getElement()
                )) {
            return;
        }
        // import is not used:
        holder.newAnnotation(HighlightSeverity.WARNING, String.format(
                "%s is not used", importedMember.getName()
        )).withFix(removeIntention.getRemoveIntention(importedMember.getParent())).create();
    }
}
