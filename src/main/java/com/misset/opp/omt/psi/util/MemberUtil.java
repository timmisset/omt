package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTCommandCall;
import com.misset.opp.omt.psi.OMTDefineName;
import com.misset.opp.omt.psi.OMTDefineQueryStatement;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTImportBlock;
import com.misset.opp.omt.psi.OMTMember;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.OMTOperatorCall;
import com.misset.opp.omt.psi.OMTPropertyLabel;
import com.misset.opp.omt.psi.OMTScriptLine;
import com.misset.opp.omt.psi.impl.OMTBuiltInMember;
import com.misset.opp.omt.psi.impl.OMTExportMemberImpl;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.named.OMTCall;
import com.misset.opp.omt.psi.support.BuiltInType;
import com.misset.opp.omt.psi.support.ExportMemberType;
import com.misset.opp.omt.psi.support.OMTCallable;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import com.misset.opp.omt.psi.support.OMTExportMember;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static util.UtilManager.getBuiltinUtil;
import static util.UtilManager.getImportUtil;
import static util.UtilManager.getModelUtil;

public class MemberUtil {

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

        // check if it's part of this page's modelItems:
        OMTFile currentFile = (OMTFile) call.getContainingFile();
        final Map<String, OMTModelItemLabel> modelItemLabels = PsiTreeUtil.findChildrenOfType(currentFile, OMTModelItemLabel.class).stream().collect(Collectors.toMap(
                OMTModelItemLabel::getName, modelItemLabel -> modelItemLabel
        ));
        if (modelItemLabels.containsKey(callName)) {
            return Optional.of(modelItemLabels.get(callName));
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

    public OMTCallable getCallable(OMTCall call) {
        OMTBuiltInMember builtInMember = getBuiltinUtil().getBuiltInMember(call.getName(), call.canCallCommand() ? BuiltInType.Command : BuiltInType.Operator);
        if (builtInMember != null) {
            return builtInMember;
        }

        if (call.getReference() != null) {
            return memberToExportMember(call.getReference().resolve());
        }
        return null;
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
            return PsiTreeUtil.getParentOfType(element, OMTImportBlock.class) != null ? NamedMemberType.ImportingMember : NamedMemberType.ExportingMember;
        }
        return null;
    }

    public OMTCallable parseDefinedToCallable(OMTDefineName defineName) {
        return memberToExportMember(defineName);
    }

    public OMTExportMember memberToExportMember(PsiElement element) {
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

    public boolean isImportedMember(OMTMember member) {
        return member.getType() == NamedMemberType.ImportingMember;
    }

}
