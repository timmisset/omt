package com.misset.opp.omt.psi.impl;


import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import com.misset.opp.omt.psi.util.ProjectUtil;
import com.misset.opp.omt.psi.util.TokenUtil;
import com.misset.opp.omt.psi.util.VariableUtil;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;
import util.RDFModelUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OMTPsiImplUtil {

    private static final VariableUtil variableUtil = VariableUtil.SINGLETON;
    private static final ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    private static final TokenUtil tokenUtil = TokenUtil.SINGLETON;
    private static final RDFModelUtil rdfModelUtil = new RDFModelUtil(projectUtil.getOntologyModel());

    // ////////////////////////////////////////////////////////////////////////////
    // Variable
    // ////////////////////////////////////////////////////////////////////////////
    public static String getName(OMTVariable variable) {
        return variable.getText();
    }

    public static OMTVariable setName(OMTVariable variable, String newName) {
        if (newName.startsWith("$")) {
            OMTVariable replacement = OMTElementFactory.createVariable(variable.getProject(), newName);
            variable.replace(replacement);
            return replacement;
        }
        return variable;
    }
    public static PsiElement getNameIdentifier(OMTVariable variable) {
        return variable;
    }
    public static boolean isDeclaredVariable(OMTVariable variable) {

        return variableUtil.isDeclaredVariable(variable);
    }

    public static boolean isGlobalVariable(OMTVariable variable) {
        return variable.getGlobalVariable() != null;
    }

    public static boolean isIgnoredVariable(OMTVariable variable) {
        return variable.getIgnoredVariable() != null;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Namespace prefixes
    // ////////////////////////////////////////////////////////////////////////////
    public static String getName(OMTNamespacePrefix curieElement) {
        String prefix = curieElement.getText();
        prefix = prefix.endsWith(":") ? prefix.substring(0, prefix.length() - 1) : prefix;

        return prefix;
    }

    public static OMTNamespacePrefix setName(OMTNamespacePrefix namespacePrefix, String newName) {
        OMTNamespacePrefix replacement = OMTElementFactory.createNamespacePrefix(namespacePrefix.getProject(), newName);
        return (OMTNamespacePrefix) namespacePrefix.replace(replacement);
    }

    public static PsiElement getNameIdentifier(OMTNamespacePrefix namespacePrefix) {
        return namespacePrefix;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // OMTDefineName
    // ////////////////////////////////////////////////////////////////////////////
    @NotNull
    public static String getName(OMTDefineName defineName) {
        return defineName.getText();
    }

    public static PsiElement setName(OMTDefineName defineName, String newName) {
        PsiElement replacement = OMTElementFactory.createOperator(defineName.getProject(), newName);
        defineName.replace(replacement);
        return replacement;
    }

    @NotNull
    public static PsiElement getNameIdentifier(OMTDefineName defineName) {
        return defineName;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // OMTDefinedBlocks
    // ////////////////////////////////////////////////////////////////////////////
    public static List<OMTDefinedStatement> getStatements(OMTQueriesBlock omtQueriesBlock) {
        return omtQueriesBlock.getDefineQueryStatementList().stream().map(statement -> (OMTDefinedStatement) statement).collect(Collectors.toList());
    }

    public static List<OMTDefinedStatement> getStatements(OMTCommandsBlock omtCommandsBlock) {
        return omtCommandsBlock.getDefineCommandStatementList().stream().map(statement -> (OMTDefinedStatement) statement).collect(Collectors.toList());
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Members
    // ////////////////////////////////////////////////////////////////////////////
    @NotNull
    public static String getName(OMTMember member) {
        return getNameIdentifier(member).getText();
    }

    public static PsiElement setName(OMTMember member, String newName) {
        OMTMember replacement = OMTElementFactory.createMember(member.getProject(), newName);
        member.getNameIdentifier().replace(replacement.getNameIdentifier());
        return replacement;
    }

    @NotNull
    public static PsiElement getNameIdentifier(OMTMember member) {
        return member.getStart().getNextSibling();
    }

    // ////////////////////////////////////////////////////////////////////////////
    // ModelItemLabel
    // ////////////////////////////////////////////////////////////////////////////
    public static String getName(OMTModelItemLabel itemLabel) {
        return itemLabel.getPropertyLabel().getPropertyLabelName();
    }

    public static PsiElement setName(OMTModelItemLabel itemLabel, String newName) {
        OMTModelItemLabel replacement = OMTElementFactory.createModelItemLabelPropertyLabel(itemLabel.getProject(), newName, itemLabel.getModelItemTypeElement().getText());
        itemLabel.replace(replacement);
        return replacement;
    }

    @NotNull
    public static PsiElement getNameIdentifier(OMTModelItemLabel itemLabel) {
        return itemLabel.getPropertyLabel();
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Import source
    // ////////////////////////////////////////////////////////////////////////////
    public static String getName(OMTImportSource importSource) {
        return importSource.getText();
    }

    public static PsiElement setName(OMTImportSource importSource, String newName) {
        PsiElement replacement = OMTElementFactory.createImportSource(importSource.getProject(), newName);
        importSource.replace(replacement);
        return replacement;
    }

    public static PsiElement getNameIdentifier(OMTImportSource importSource) {
        return importSource;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Operator call
    // ////////////////////////////////////////////////////////////////////////////
    public static OMTOperatorCall setName(OMTOperatorCall operatorCall, String newName) {
        OMTOperatorCall replacement = OMTElementFactory.createOperatorCall(
                operatorCall.getProject(),
                newName,
                operatorCall.getFlagSignature() != null ? operatorCall.getFlagSignature().getText() : "",
                operatorCall.getSignature() != null ? operatorCall.getSignature().getText() : "");
        operatorCall.replace(replacement);
        return replacement;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Operator call
    // ////////////////////////////////////////////////////////////////////////////
    public static PsiElement setName(OMTCommandCall commandCall, String newName) {
        PsiElement replacement = OMTElementFactory.createCommandCall(
                commandCall.getProject(),
                newName,
                commandCall.getFlagSignature() != null ? commandCall.getFlagSignature().getText() : "",
                commandCall.getSignature() != null ? commandCall.getSignature().getText() : "");
        commandCall.replace(replacement);
        return replacement;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Prefixes
    // ////////////////////////////////////////////////////////////////////////////
    public static PsiElement getPrefix(OMTCurieElement curieElement) {
        return curieElement.getFirstChild();
    }

    public static String getPrefixName(OMTCurieElement curieElement) {
        return getPrefix(curieElement).getText().replace(":", "");
    }

    public static boolean isDefinedByPrefix(OMTCurieElement curieElement, OMTPrefix prefix) {
        return curieElement.getText().startsWith(prefix.getNamespacePrefix().getText());
    }

    public static boolean isDefinedByPrefix(OMTParameterType parameterType, OMTPrefix prefix) {
        return parameterType.getText().startsWith(prefix.getNamespacePrefix().getText());
    }

    /**
     * Returns the curie resolved to the full iri as a Resource in the loaded ontology model
     *
     * @param curieElement
     * @return
     */
    public static Resource getAsResource(OMTCurieElement curieElement) {
        String resolvedIri = String.format("%s%s",
                ((OMTFile) curieElement.getContainingFile()).getPrefixIri(curieElement.getPrefixName()),
                curieElement.getPrefix().getNextSibling().getText()
        );

        Model ontologyModel = projectUtil.getOntologyModel();
        return ontologyModel.getResource(resolvedIri);
    }

    /**
     * Returns the curie resolved to the full iri as a Resource in the loaded ontology model
     *
     * @param parameterType
     * @return
     */
    public static Resource getAsResource(OMTParameterType parameterType) {
        String fullIri = parameterType.getNamespacePrefix() == null ?
                parameterType.getFirstChild().getText() :
                String.format("%s%s",
                        ((OMTFile) parameterType.getContainingFile()).getPrefixIri(parameterType.getNamespacePrefix().getName()),
                        parameterType.getNamespacePrefix().getNextSibling().getText()
                );

        Model ontologyModel = projectUtil.getOntologyModel();
        return ontologyModel.getResource(fullIri);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Signature
    // ////////////////////////////////////////////////////////////////////////////
    public static int numberOfParameters(OMTSignature signature) {
        return signature.getCommandBlockList().size() +
                signature.getQueryPathList().size() +
                signature.getCommandCallList().size() +
                signature.getOperatorCallList().size();
    }

    // ////////////////////////////////////////////////////////////////////////////
    // BlockEntry
    // ////////////////////////////////////////////////////////////////////////////
    public static String getName(OMTBlockEntry blockEntry) {
        PsiElement label = getLabel(blockEntry);
        return label instanceof OMTPropertyLabel ? getPropertyLabelName((OMTPropertyLabel) label) : getName(blockEntry.getSpecificBlock());
    }

    public static PsiElement getLabel(OMTBlockEntry blockEntry) {
        return blockEntry.getSpecificBlock() != null ? blockEntry.getSpecificBlock().getFirstChild().getFirstChild() : blockEntry.getPropertyLabel();
    }

    // ////////////////////////////////////////////////////////////////////////////
    // PropertyLabel
    // ////////////////////////////////////////////////////////////////////////////
    public static String getPropertyLabelName(OMTPropertyLabel propertyLabel) {
        String propertyLabelText = propertyLabel.getText();
        return propertyLabelText.endsWith(":") ?
                propertyLabelText.substring(0, propertyLabelText.length() - 1) :
                propertyLabelText;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Specific blocks
    // ////////////////////////////////////////////////////////////////////////////
    public static String getName(OMTSpecificBlock specificBlock) {
        if (specificBlock.getCommandsBlock() != null) {
            return "commands";
        }
        if (specificBlock.getExportBlock() != null) {
            return "export";
        }
        if (specificBlock.getImportBlock() != null) {
            return "import";
        }
        if (specificBlock.getModelBlock() != null) {
            return "model";
        }
        if (specificBlock.getPrefixBlock() != null) {
            return "prefixes";
        }
        if (specificBlock.getQueriesBlock() != null) {
            return "queries";
        }
        return "unknown";
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Query paths
    // ////////////////////////////////////////////////////////////////////////////
    public static List<Resource> resolveToResource(OMTQueryPath path) {
        return resolvePathPart(path.getLastChild());
    }

    public static List<Resource> resolveToResource(OMTQueryStep step) {
        // steps that do not include preceeding info
        if (step.getCurieConstantElement() != null) {
            // a curie constant is a fixed value and is indifferent to previous steps
            return Arrays.asList(step.getCurieConstantElement().getCurieElement().getAsResource());
        }
        if (step.getConstantValue() != null) {
            return Arrays.asList(projectUtil.getOntologyModel().createTypedLiteral(
                    tokenUtil.parseToTypedLiteral(step.getConstantValue())
            ).asResource());
        }

        // steps that require preceeding info
        List<Resource> previousStep = getPreviousStep(step);
        if (step.getCurieElement() != null) {
            // a regular curie element will always originate from a previous query step since
            return rdfModelUtil.listObjectsWithSubjectPredicate(previousStep, step.getCurieElement().getAsResource());
        }
        return new ArrayList<>();
    }

    public static List<Resource> resolveToResource(OMTQueryReverseStep step) {
        List<Resource> resources = getPreviousStep(step);
        return rdfModelUtil.listSubjectsWithPredicateObjectClass(step.getQueryStep().getCurieElement().getAsResource(), resources);
    }

    private static List<Resource> getPreviousStep(PsiElement step) {
        PsiElement previous = step.getPrevSibling();
        while (previous != null
                && !(previous instanceof OMTQueryPath)
                && !(previous instanceof OMTQueryStep)
                && !(previous instanceof OMTQueryReverseStep)) {
            previous = previous.getPrevSibling();
        }
        return previous == null ? new ArrayList<>() : resolvePathPart(previous);
    }

    private static List<Resource> resolvePathPart(PsiElement part) {
        if (part != null) {
            if (part instanceof OMTQueryStep) {
                return ((OMTQueryStep) part).resolveToResource();
            }
            if (part instanceof OMTQueryPath) {
                return ((OMTQueryPath) part).resolveToResource();
            }
            if (part instanceof OMTQueryReverseStep) {
                return ((OMTQueryReverseStep) part).resolveToResource();
            }
        }
        return new ArrayList<>();
    }
}
