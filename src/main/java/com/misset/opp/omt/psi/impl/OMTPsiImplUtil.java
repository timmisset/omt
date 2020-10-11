package com.misset.opp.omt.psi.impl;


import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import com.misset.opp.omt.psi.util.ProjectUtil;
import com.misset.opp.omt.psi.util.VariableUtil;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class OMTPsiImplUtil {

    private static final VariableUtil variableUtil = VariableUtil.SINGLETON;
    private static final ProjectUtil projectUtil = ProjectUtil.SINGLETON;

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
        return curieElement.getText();
    }

    public static OMTCurieElement setName(OMTNamespacePrefix curieElement, String newName) {
        OMTCurieElement replacement = OMTElementFactory.createCurieElement(curieElement.getProject(), newName);
        curieElement.replace(replacement);
        return replacement;
    }

    public static PsiElement getNameIdentifier(OMTNamespacePrefix namespacePrefix) {
        return namespacePrefix;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // OMTDefineName
    // ////////////////////////////////////////////////////////////////////////////
    public static String getName(OMTDefineName defineName) {
        return defineName.getText();
    }

    public static PsiElement setName(OMTDefineName defineName, String newName) {
        PsiElement replacement = OMTElementFactory.createOperator(defineName.getProject(), newName);
        defineName.replace(replacement);
        return replacement;
    }

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
    public static String getName(OMTMember member) {
        return getNameIdentifier(member).getText();
    }

    public static PsiElement setName(OMTMember member, String newName) {
        PsiElement replacement = OMTElementFactory.createMember(member.getProject(), newName);
        member.replace(replacement);
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
        PsiElement replacement = OMTElementFactory.createModelItemLabelPropertyLabel(itemLabel.getProject(), newName, itemLabel.getModelItemTypeElement().getText());
        itemLabel.getPropertyLabel().replace(replacement);
        return replacement;
    }

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
                ((OMTFile) curieElement.getContainingFile()).getPrefixIri(curieElement.getPrefix().getText()),
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

    public static int numberOfParameters(OMTSignature signature) {
        return signature.getCommandBlockList().size() +
                signature.getQueryPathList().size() +
                signature.getCommandCallList().size() +
                signature.getOperatorCallList().size();
    }

    public static String getPropertyLabelName(OMTPropertyLabel propertyLabel) {
        String propertyLabelText = propertyLabel.getText();
        return propertyLabelText.endsWith(":") ?
                propertyLabelText.substring(0, propertyLabelText.length() - 1) :
                propertyLabelText;
    }

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
}
