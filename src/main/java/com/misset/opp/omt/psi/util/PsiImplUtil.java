package com.misset.opp.omt.psi.util;


import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.external.util.rdf.RDFModelUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.support.OMTCallable;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class PsiImplUtil {

    private static final String BOOLEAN = "boolean";

    private static final VariableUtil variableUtil = VariableUtil.SINGLETON;
    private static final ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    private static final TokenUtil tokenUtil = TokenUtil.SINGLETON;
    private static final MemberUtil memberUtil = MemberUtil.SINGLETON;

    // ////////////////////////////////////////////////////////////////////////////
    // Variable
    // ////////////////////////////////////////////////////////////////////////////
    @NotNull
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
        if (parameterType.getNamespacePrefix() == null) {
            final String type = parameterType.getFirstChild().getText().trim();
            return projectUtil.getRDFModelUtil().getPrimitiveTypeAsResource(type);
        } else {
            final String iri = ((OMTFile) parameterType.getContainingFile()).curieToIri(parameterType.getText().trim());
            return projectUtil.getRDFModelUtil().getResource(iri);
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
    // Queries
    // ////////////////////////////////////////////////////////////////////////////
    public static String getDefinedName(OMTQuery query) {
        final OMTDefineQueryStatement definedQueryStatement = (OMTDefineQueryStatement) PsiTreeUtil.findFirstParent(query, parent -> parent instanceof OMTDefineQueryStatement);
        return definedQueryStatement != null ? definedQueryStatement.getDefineName().getName() : "";
    }

    public static boolean isBooleanType(OMTQuery query) {
        if (query instanceof OMTQueryPath) {
            final OMTQueryPath queryPath = (OMTQueryPath) query;
            if (queryPath.getQueryStepList().size() == 1) {
                return queryPath.getQueryStepList().get(0).getNegatedStep() != null;
            } else {
                return tokenUtil.isNotOperator(query.getLastChild().getFirstChild());
            }
        }
        return query instanceof OMTBooleanStatement || query instanceof OMTEquationStatement;
    }

    public static List<Resource> resolveToResource(OMTQuery query) {
        if (query.isBooleanType()) {
            return Collections.singletonList(projectUtil.getRDFModelUtil().getPrimitiveTypeAsResource(BOOLEAN));
        }
        return new ArrayList<>();
    }

    public static List<Resource> resolveToResource(OMTBooleanStatement ignored) {
        return Collections.singletonList(projectUtil.getRDFModelUtil().getPrimitiveTypeAsResource(BOOLEAN));
    }

    public static List<Resource> resolveToResource(OMTEquationStatement ignored) {
        return Collections.singletonList(projectUtil.getRDFModelUtil().getPrimitiveTypeAsResource(BOOLEAN));
    }

    public static OMTQuery getOpposite(OMTEquationStatement equationStatement, OMTQuery query) {
        if (equationStatement.getQueryList().get(0) != query) {
            return query;
        }
        return equationStatement.getQueryList().get(0);
    }

    public static List<Resource> resolveToResource(OMTQueryPath query) {
        return resolveToResource(query, true);
    }

    public static List<Resource> resolveToResource(OMTQueryPath query, boolean lookBack) {
        final List<OMTQueryStep> queryStepList = query.getQueryStepList();
        final OMTQueryStep queryStep = queryStepList.get(queryStepList.size() - 1);
        if (queryStep instanceof OMTCurieConstantElement) {
            return resolveToResource((OMTCurieConstantElement) queryStep);
        }
        if (queryStep instanceof OMTQueryReverseStep) {
            return resolveToResource((OMTQueryReverseStep) queryStep);
        }
        if (queryStep instanceof OMTSubQuery) {
            return resolveToResource((OMTSubQuery) queryStep);
        }
        if (queryStep instanceof OMTNegatedStep) {
            return Collections.singletonList(projectUtil.getRDFModelUtil().getPrimitiveTypeAsResource(BOOLEAN));
        }
        return queryStep.resolveToResource(lookBack);
    }

    public static List<Resource> resolveToResource(OMTQueryArray query) {
        final List<Resource> resources = query.getQueryList().stream()
                .map(OMTQuery::resolveToResource)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return projectUtil.getRDFModelUtil().getDistinctResources(resources);
    }

    public static List<Resource> filter(OMTBooleanStatement booleanStatement, List<Resource> resources) {
        return booleanStatement.getQueryList().stream().map(
                query -> query.filter(resources)
        ).min(Comparator.comparingInt(List::size))
                .orElse(resources);
    }

    public static List<Resource> filter(OMTQuery query, List<Resource> resources) {
        if (!query.isBooleanType()) {
            return resources;
        }
        if (query instanceof OMTQueryPath) {
            final OMTQueryStep queryStep = ((OMTQueryPath) query).getQueryStepList().get(0);
            if (queryStep.getNegatedStep() != null) {
                return queryStep.getNegatedStep().getQuery().filter(resources);
            }
        }
        return resources;
    }

    public static List<Resource> filter(OMTEquationStatement equationStatement, List<Resource> resources) {
        final OMTQuery query = equationStatement.getQueryList().get(0);
        final List<Resource> leftHand = query instanceof OMTQueryPath ? ((OMTQueryPath) query).resolveToResource(false) : query.resolveToResource();

        if (!leftHand.isEmpty() && projectUtil.getRDFModelUtil().isTypePredicate(leftHand.get(0))) {
            // [rdf:type == ...]
            // now filter the resources based on the type
            PsiElement parent = equationStatement.getParent();
            boolean isNegated = parent instanceof OMTNegatedStep;
            List<Resource> rightHand = equationStatement.getQueryList().get(1).resolveToResource();
            final List<String> resourcesToCheck = rightHand.stream().map(Resource::toString).collect(Collectors.toList());
            return resources.stream().filter(
                    resource -> isNegated != resourcesToCheck.contains(resource.toString())
            ).collect(Collectors.toList());
        }
        return resources;
    }

    public static List<Resource> resolveToResource(OMTQueryStep step) {
        return resolveToResource(step, true);
    }

    public static List<Resource> resolveToResource(OMTQueryStep step, boolean lookBack) {
        // steps that do not include preceeding info
        if (step.getConstantValue() != null) {
            Model ontologyModel = projectUtil.getOntologyModel();
            final Object typedLiteral = tokenUtil.parseToTypedLiteral(step.getConstantValue());
            if (typedLiteral == null) {
                return new ArrayList<>();
            }
            return Collections.singletonList(
                    ontologyModel
                            .createResource(ontologyModel.createTypedLiteral(typedLiteral)
                                    .getDatatypeURI())
            );
        }
        if (step.getVariable() != null) {
            return step.getVariable().getType();
        }
        if (step.getNegatedStep() != null) {
            return Collections.singletonList(projectUtil.getRDFModelUtil().getPrimitiveTypeAsResource(BOOLEAN));
        }

        // steps that require preceeding info
        List<Resource> previousStep = getPreviousStep(step);
        if (step.getCurieElement() != null) {
            // list based on the preceeding data
            if (lookBack && !previousStep.isEmpty()) {
                return projectUtil.getRDFModelUtil().listObjectsWithSubjectPredicate(previousStep, step.getCurieElement().getAsResource());
            }
            // No initial information to use, continue from this position with the curie as it should be resolvable
            // to a predicate which can point to class or datatype
            return step.getParent().getFirstChild().equals(step) ?
                    Collections.singletonList(step.getCurieElement().getAsResource()) :
                    projectUtil.getRDFModelUtil().getPredicateObjects(step.getCurieElement().getAsResource());
        }
        if (step instanceof OMTQueryFilter) {
            return ((OMTQueryFilter) step).getQuery().filter(previousStep);
        }
        if (step.getOperatorCall() != null) {
            final OMTCallable callable = memberUtil.getCallable(step.getOperatorCall());
            if (callable != null) {
                return callable.returnsAny() ? previousStep : callable.getReturnType();
            }
        }
        return new ArrayList<>();
    }

    public static List<Resource> resolveToResource(OMTCurieConstantElement step) {
        return Collections.singletonList(step.getCurieElement().getAsResource());
    }

    public static List<Resource> resolveToResource(OMTSubQuery step) {
        return step.getQuery().resolveToResource();
    }

    public static List<Resource> resolveToResource(OMTQueryReverseStep step) {
        List<Resource> resources = getPreviousStep(step);
        final OMTCurieElement curieElement = step.getQueryStep().getCurieElement();
        final RDFModelUtil rdfModelUtil = projectUtil.getRDFModelUtil();
        if (curieElement != null && !rdfModelUtil.isTypePredicate(curieElement.getAsResource()) && !resources.isEmpty()) {
            return rdfModelUtil.listSubjectsWithPredicateObjectClass(curieElement.getAsResource(),
                    rdfModelUtil.allSuperClasses(resources));
        }
        return resources.isEmpty() && curieElement != null ? rdfModelUtil.getPredicateSubjects(curieElement.getAsResource()) : resources;
    }

    public static List<Resource> getPreviousStep(PsiElement step) {
        PsiElement previous = step.getPrevSibling();
        while (previous != null && !(previous instanceof OMTQueryPath) && !(previous instanceof OMTQueryStep)) {
            previous = previous.getPrevSibling();
        }
        if (previous == null) {
            // retrieve the previous value via the parent
            final PsiElement containingQueryStep = PsiTreeUtil.findFirstParent(
                    step, parent -> parent != step && (parent instanceof OMTSubQuery || parent instanceof OMTQueryFilter));
            // retrieve via the subQuery:
            return containingQueryStep != null ? getPreviousStep(containingQueryStep) : new ArrayList<>();
        }
        List<Resource> typesForStep = new ArrayList<>(resolvePathPart(previous));
        typesForStep.addAll(projectUtil.getRDFModelUtil().allSubClasses(typesForStep));
        typesForStep = projectUtil.getRDFModelUtil().getDistinctResources(typesForStep);
        return typesForStep;
    }

    private static List<Resource> resolvePathPart(PsiElement part) {
        if (part != null) {
            if (part instanceof OMTQueryStep) {
                return ((OMTQueryStep) part).resolveToResource();
            }
            if (part instanceof OMTQueryPath) {
                return ((OMTQueryPath) part).resolveToResource();
            }
            if (part instanceof OMTEquationStatement) {
                return ((OMTEquationStatement) part).resolveToResource();
            }
        }
        return new ArrayList<>();
    }

    public static Resource getTypeAsResource(String type, PsiElement element) {
        if (type.contains(":")) {
            final String prefixIri = ((OMTFile) element.getContainingFile()).getPrefixIri(type.split(":")[0]);
            final String iriAsString = String.format("%s%s", prefixIri, type.split(":")[1]);
            return projectUtil.getRDFModelUtil().createResource(iriAsString);
        } else {
            return projectUtil.getRDFModelUtil().getPrimitiveTypeAsResource(type);
        }
    }

    public static List<Resource> getType(OMTVariable variable) {
        return variableUtil.getType(variable);
    }

    public static List<Resource> getType(OMTParameterWithType parameterWithType) {
        return variableUtil.getType(parameterWithType);
    }

    public static OMTVariableValue getValue(OMTVariable variable) {
        return variableUtil.getValue(variable);
    }

    public static List<OMTVariableAssignment> getAssignments(OMTVariable variable) {
        return variableUtil.getAssignments(variable);
    }

    public static List<Resource> resolveToResource(OMTSignatureArgument signatureArgument) {
        if (signatureArgument.getCommandCall() != null) {
            final OMTCallable callable = memberUtil.getCallable(signatureArgument.getCommandCall());
            return callable.getReturnType();
        }
        if (signatureArgument.getOperatorCall() != null) {
            final OMTCallable callable = memberUtil.getCallable(signatureArgument.getOperatorCall());
            return callable.getReturnType();
        }
        if (signatureArgument.getQuery() != null) {
            return signatureArgument.getQuery().resolveToResource();
        }
        final Resource resource = projectUtil.getRDFModelUtil().getPrimitiveTypeAsResource("any");
        return Collections.singletonList(resource);
    }
}
