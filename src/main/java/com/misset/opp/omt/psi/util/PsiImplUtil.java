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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PsiImplUtil {

    private static final VariableUtil variableUtil = VariableUtil.SINGLETON;
    private static final ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    private static final TokenUtil tokenUtil = TokenUtil.SINGLETON;
    private static final MemberUtil memberUtil = MemberUtil.SINGLETON;
    private static RDFModelUtil rdfModelUtil;

    private static RDFModelUtil getRdfModelUtil() {
        if (rdfModelUtil == null) {
            rdfModelUtil = new RDFModelUtil(projectUtil.getOntologyModel());
        }
        if (!rdfModelUtil.isLoaded()) {
            rdfModelUtil = new RDFModelUtil(projectUtil.getOntologyModel());
        }
        return rdfModelUtil;
    }

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
                signature.getQueryList().size() +
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
    public static OMTQueryPath getValidQueryPath(OMTQuery query) {
        if (query.getQueryPath() != null) {
            return query.getQueryPath();
        }
        if (query.getBooleanStatement() != null) {
            if (query.getBooleanStatement().getEquationStatementList().size() == 1 &&
                    query.getBooleanStatement().getEquationStatementList().get(0).getQueryPathList().size() == 1) {
                return query.getBooleanStatement().getEquationStatementList().get(0).getQueryPathList().get(0);
            }
        }
        return null;
    }

    public static OMTBooleanStatement getValidBooleanStatement(OMTQuery query) {
        if (query.getValidQueryPath() != null) {
            return null;
        }
        return query.getBooleanStatement();
    }


    public static List<Resource> resolveToResource(OMTQueryPath path) {
        return resolvePathPart(path.getLastChild());
    }

    public static List<Resource> resolveToResource(OMTQuery query) {
        if (query.getValidBooleanStatement() != null) {
            return Collections.singletonList(getRdfModelUtil().getPrimitiveTypeAsResource("boolean"));
        }
        if (query.getValidQueryPath() != null) {
            return query.getValidQueryPath().resolveToResource();
        }
        return new ArrayList();
    }

    public static List<Resource> filter(OMTBooleanStatement statement, List<Resource> resources) {
        return resources;
    }

    public static List<Resource> filter(OMTQuery statement, List<Resource> resources) {
        return resources;
    }

    public static List<Resource> filter(OMTQueryPath path, List<Resource> resources) {
        return filter((OMTQueryStep) path.getLastChild(), resources);
    }


    public static List<Resource> filter(OMTQueryStep step, List<Resource> resources) {
//        List<Resource> previousStep = getPreviousStep(step);
//        if (step.getEquationStatement() == null || previousStep.stream().noneMatch(resource -> getRdfModelUtil().isTypePredicate(resource))) {
//            // no type filter, return all:
//            return resources;
//        }
//        List<Resource> acceptableResources = resolvePathPart(step.getEquationStatement().getLastChild());
//        if (acceptableResources.isEmpty()) {
//            // cannot resolve filter, return all:
//            return resources;
//        }
//        boolean negativeAssertion = step.getEquationStatement().getText().contains("NOT");
//        return negativeAssertion ?
//                // negative assertions are to complex to process for now, only support simple filter statement
//                // like [rdf:type == /ont:Something]
//                resources :
//                resources.stream().filter(resource -> acceptableResources.stream().anyMatch(
//                        matchingResource -> matchingResource.toString().equals(resource.toString())
//                )).collect(Collectors.toList());
        return resources;
    }

    public static List<Resource> resolveToResource(OMTQueryStep step) {
        // steps that do not include preceeding info
        if (step.getCurieConstantElement() != null) {
            // a curie constant is a fixed value and is indifferent to previous steps
            return Collections.singletonList(step.getCurieConstantElement().getCurieElement().getAsResource());
        }
        if (step.getConstantValue() != null) {
            Model ontologyModel = projectUtil.getOntologyModel();
            return Collections.singletonList(
                    ontologyModel.createResource(ontologyModel.createTypedLiteral(
                            tokenUtil.parseToTypedLiteral(step.getConstantValue())
                    ).getDatatypeURI())
            );
        }
        if (step.getVariable() != null) {
            return step.getVariable().getType();
        }

        // steps that require preceeding info
        List<Resource> previousStep = getPreviousStep(step);
        if (step.getCurieElement() != null) {
            // list based on the preceeding data
            if (!previousStep.isEmpty()) {
                return getRdfModelUtil().listObjectsWithSubjectPredicate(previousStep, step.getCurieElement().getAsResource());
            }
            // only resolve the curie at the current location, for example [ rdf:type == /ont:ClassA ]
            return Collections.singletonList(step.getCurieElement().getAsResource());
        }
        if (step.getSubQuery() != null) {
            return step.getSubQuery().getQuery().resolveToResource();
        }
        if (step.getQueryFilter() != null) {
            return step.getQueryFilter().getQuery().filter(previousStep);
        }
        if (step.getOperatorCall() != null) {
            final OMTCallable callable = memberUtil.getCallable(step.getOperatorCall());
            if (callable.returnsAny()) {
                return previousStep;
            } // return the existing types from the previous step
            else {
                return callable.getReturnType();
            }
        }
        return new ArrayList<>();
    }

    public static List<Resource> resolveToResource(OMTQueryReverseStep step) {
        List<Resource> resources = getPreviousStep(step);
        return getRdfModelUtil().listSubjectsWithPredicateObjectClass(step.getQueryStep().getCurieElement().getAsResource(), resources);
    }

    public static List<Resource> resolveToResource(OMTEquationStatement step) {
        return Collections.singletonList(getRdfModelUtil().getPrimitiveTypeAsResource("boolean"));
    }

    public static List<Resource> getPreviousStep(PsiElement step) {
        PsiElement previous = step.getPrevSibling();
        while (previous != null
                && !(previous instanceof OMTQueryPath)
                && !(previous instanceof OMTQueryStep)
                && !(previous instanceof OMTQueryReverseStep)) {
            previous = previous.getPrevSibling();
        }
        if (previous == null) {
            // retrieve the previous value via the parent
            final PsiElement containingQueryStep = PsiTreeUtil.findFirstParent(step, parent -> parent != step &&
                    (parent instanceof OMTQueryStep || parent instanceof OMTQueryFilter));
            // if the first container is also a query step. If this is a filter, ignore it
            if (containingQueryStep instanceof OMTQueryStep) {
                return getPreviousStep(containingQueryStep);
            }
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
            if (part instanceof OMTEquationStatement) {
                return ((OMTEquationStatement) part).resolveToResource();
            }
        }
        return new ArrayList<>();
    }

    public static List<Resource> getType(OMTVariable variable) {
        if (variable.isDeclaredVariable()) {
            // a declared variable
            if (variable.getParent() instanceof OMTParameterWithType) {
                return getType((OMTParameterWithType) variable.getParent());
            } else if (variable.getParent() instanceof OMTDefineParam) {
                // query or command statement
                OMTDefinedStatement statement = (OMTDefinedStatement) variable.getParent().getParent();
                Pattern pattern = Pattern.compile(String.format("@param \\%s \\((.*)\\)", variable.getName()));
                Matcher matcher = pattern.matcher(
                        statement.getLeading() != null ?
                                statement.getLeading().getText() : "");
                if (matcher.find()) {
                    return Collections.singletonList(getTypeAsResource(matcher.group(1), variable));
                }
            }
        } else {
            if (variable.isGlobalVariable()) {
                return new ArrayList<>();
            }
            if (variable.isIgnoredVariable()) {
                return new ArrayList<>();
            }
            OMTVariable declaredByVariable = variableUtil.getDeclaredByVariable(variable).orElse(null);
            return declaredByVariable != null && declaredByVariable.isDeclaredVariable() ? declaredByVariable.getType() : new ArrayList<>();
        }
        return new ArrayList<>();
    }

    public static Resource getTypeAsResource(String type, PsiElement element) {
        if (type.contains(":")) {
            final String prefixIri = ((OMTFile) element.getContainingFile()).getPrefixIri(type.split(":")[0]);
            final String iriAsString = String.format("%s%s", prefixIri, type.split(":")[1]);
            return getRdfModelUtil().createResource(iriAsString);
        } else {
            return getRdfModelUtil().getPrimitiveTypeAsResource(type);
        }
    }

    public static List<Resource> getType(OMTParameterWithType parameterWithType) {
        if (parameterWithType.getParameterType() != null) {
            return Collections.singletonList(parameterWithType.getParameterType().getAsResource());
        } else {
            // defined as 'operator', which means a shortname like 'string':
            Pattern pattern = Pattern.compile("\\(([a-z]*)\\)");
            Matcher matcher = pattern.matcher(parameterWithType.getText());
            matcher.find();
            if (matcher.group(1) != null) {
                return Collections.singletonList(getRdfModelUtil().getPrimitiveTypeAsResource(matcher.group(1)));
            } else {
                return new ArrayList<>();
            }
        }
    }
}
