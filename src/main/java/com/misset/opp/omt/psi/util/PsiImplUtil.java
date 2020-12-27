package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.util.UtilManager.*;

public class PsiImplUtil {

    // ////////////////////////////////////////////////////////////////////////////
    // OMTDefineName
    // ////////////////////////////////////////////////////////////////////////////
    @NotNull
    public static String getName(OMTDefineName defineName) {
        return defineName.getText();
    }

    public static PsiElement setName(OMTDefineName defineName, String newName) {
        PsiElement replacement = OMTElementFactory.createOperator(defineName.getProject(), newName);
        if (replacement != null) {
            defineName.replace(replacement);
        }
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
        return member.getFirstChild();
    }

    public static String getName(OMTMemberListItem memberListItem) {
        return memberListItem.getMember() == null ? "" : memberListItem.getMember().getName();
    }

    public static String getName(OMTSequenceItem sequenceItem) {
        if (sequenceItem.getScalarValue() != null) {
            final OMTScalarValue scalarValue = sequenceItem.getScalarValue();
            if (scalarValue.getParameterWithType() != null) {
                return scalarValue.getParameterWithType().getVariable().getName();
            }
            if (scalarValue.getVariableAssignment() != null) {
                return scalarValue.getVariableAssignment().getVariableList().get(0).getName();
            }
            if (scalarValue.getQuery() != null) {
                return scalarValue.getQuery().getText();
            }
            if (scalarValue.getIndentedBlock() != null) {
                // a name can only be returned for a scalar value when the block contains the name: property
                return scalarValue.getIndentedBlock().getBlockEntryList()
                        .stream().filter(
                                blockEntry ->
                                        blockEntry instanceof OMTGenericBlock &&
                                                blockEntry.getName().equals("name"))
                        .map(blockEntry -> ((OMTGenericBlock) blockEntry).getScalar())
                        .filter(Objects::nonNull)
                        .map(omtScalar -> omtScalar.getText().trim())
                        .findFirst()
                        .orElse(null);
            }
        }
        return null;
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

    public static String getName(OMTNamespaceIri namespaceIri) {
        String name = namespaceIri.getText();
        return name.substring(name.indexOf("<"), name.indexOf(">") + 1);
    }

    public static String getNamespace(OMTNamespaceIri namespaceIri) {
        String name = namespaceIri.getText();
        return name.substring(name.indexOf("<") + 1, name.indexOf(">"));
    }

    public static String getName(OMTPrefix prefix) {
        return String.format("%s:%s", prefix.getNamespacePrefix().getName(), prefix.getNamespaceIri().getNamespace());
    }

    /**
     * Returns the curie resolved to the full iri as a Resource in the loaded ontology model
     */
    public static Resource getAsResource(OMTCurieElement curieElement) {
        String resolvedIri = String.format("%s%s",
                ((OMTFile) curieElement.getContainingFile()).getPrefixIri(curieElement.getPrefixName()),
                curieElement.getPrefix().getNextSibling().getText()
        );

        Model ontologyModel = getProjectUtil().getOntologyModel();
        return ontologyModel.getResource(resolvedIri);
    }

    /**
     * Returns the curie resolved to the full iri as a Resource in the loaded ontology model
     */
    public static Resource getAsResource(OMTParameterType parameterType) {
        if (parameterType.getNamespacePrefix() == null) {
            final String type = parameterType.getFirstChild().getText().trim();
            return getRDFModelUtil().getPrimitiveTypeAsResource(type);
        } else {
            final String iri = ((OMTFile) parameterType.getContainingFile()).curieToIri(parameterType.getText().trim());
            return getRDFModelUtil().getResource(iri);
        }
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Signature
    // ////////////////////////////////////////////////////////////////////////////
    public static int numberOfParameters(OMTSignature signature) {
        return signature.getSignatureArgumentList().size();
    }

    // ////////////////////////////////////////////////////////////////////////////
    // BlockEntry
    // ////////////////////////////////////////////////////////////////////////////
    public static String getName(OMTBlockEntry blockEntry) {
        return getPropertyLabelName(getLabel(blockEntry));
    }

    public static PsiElement getLabel(OMTBlockEntry blockEntry) {
        if (blockEntry instanceof OMTSpecificBlock) {
            return blockEntry.getFirstChild() instanceof OMTLeading ?
                    getSibling(blockEntry.getFirstChild(),
                            element -> element.getNode().getElementType() != TokenType.WHITE_SPACE) :
                    blockEntry.getFirstChild();
        }
        if (blockEntry instanceof OMTModelItemBlock) {
            return ((OMTModelItemBlock) blockEntry).getModelItemLabel().getPropertyLabel();
        }
        return ((OMTGenericBlock) blockEntry).getPropertyLabel();
    }

    public static PsiElement getSibling(@NotNull PsiElement element, Predicate<PsiElement> condition) {
        element = element.getNextSibling();
        while (element != null) {
            if (condition.test(element)) {
                return element;
            }
            element = element.getNextSibling();
        }
        return null;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // PropertyLabel
    // ////////////////////////////////////////////////////////////////////////////
    public static String getPropertyLabelName(OMTPropertyLabel propertyLabel) {
        return getPropertyLabelName((PsiElement) propertyLabel);
    }

    private static String getPropertyLabelName(PsiElement element) {
        String propertyLabelText = element.getText();
        return propertyLabelText.endsWith(":") ?
                propertyLabelText.substring(0, propertyLabelText.length() - 1) :
                propertyLabelText;
    }
    public static String getType(OMTModelItemBlock modelItemBlock) {
        final OMTModelItemTypeElement modelItemTypeElement = modelItemBlock.getModelItemLabel().getModelItemTypeElement();
        return modelItemTypeElement.getText().substring(1); // return type without flag token
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Queries
    // ////////////////////////////////////////////////////////////////////////////
    public static String getDefinedName(OMTQuery query) {
        final OMTDefineQueryStatement definedQueryStatement = (OMTDefineQueryStatement) PsiTreeUtil.findFirstParent(query, parent -> parent instanceof OMTDefineQueryStatement);
        return definedQueryStatement != null ? definedQueryStatement.getDefineName().getName() : "";
    }

    public static OMTQuery getOpposite(OMTEquationStatement equationStatement, OMTQuery query) {
        return equationStatement.getQueryList().stream().filter(
                query1 -> query != query1
        ).findFirst().orElse(null);
    }

    public static PsiElement getPreviousSibling(PsiElement element, Class<? extends PsiElement>... ofTypes) {
        return getElementOrContinueWith(element, PsiElement::getPrevSibling, ofTypes);
    }

    public static PsiElement getParent(PsiElement element, Class<? extends PsiElement>... ofTypes) {
        return getElementOrContinueWith(element, PsiElement::getParent, ofTypes);
    }

    private static PsiElement getElementOrContinueWith(PsiElement element, UnaryOperator<PsiElement> continueWith, Class<? extends PsiElement>... ofTypes) {
        if (element == null) {
            return null;
        }
        PsiElement continueWithElement = continueWith.apply(element);
        while (continueWithElement != null && !isAssignableFrom(continueWithElement, ofTypes)) {
            continueWithElement = continueWith.apply(continueWithElement);
        }
        return continueWithElement;
    }

    private static boolean isAssignableFrom(PsiElement element, Class<? extends PsiElement>... ofTypes) {
        for (Class<? extends PsiElement> clazz : ofTypes) {
            if (clazz.isAssignableFrom(element.getClass())) {
                return true;
            }
        }
        return false;
    }


    public static Resource getTypeAsResource(String type, PsiElement element) {
        if (type.contains(":")) {
            final String prefixIri = ((OMTFile) element.getContainingFile()).getPrefixIri(type.split(":")[0]);
            final String iriAsString = String.format("%s%s", prefixIri, type.split(":")[1]);
            return getRDFModelUtil().createResource(iriAsString);
        } else {
            return getRDFModelUtil().getPrimitiveTypeAsResource(type);
        }
    }

    public static List<Resource> getType(OMTParameterWithType parameterWithType) {
        return getVariableUtil().getType(parameterWithType);
    }

    public static List<OMTBlockEntry> getBlockEntryList(OMTBlock block) {
        if (block instanceof OMTIndentedBlock) {
            return ((OMTIndentedBlock) block).getBlockEntryList();
        }
        if (block instanceof OMTRootBlock) {
            return ((OMTRootBlock) block).getBlockEntryList();
        }
        return new ArrayList<>();
    }

    public static OMTBlock getBlock(OMTGenericBlock genericBlock) {
        return genericBlock.getIndentedBlock();
    }

    public static OMTBlock getBlock(OMTModelItemBlock modelItemBlock) {
        return modelItemBlock.getIndentedBlock();
    }

}
