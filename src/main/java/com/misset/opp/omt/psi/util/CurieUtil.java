package com.misset.opp.omt.psi.util;

import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.intentions.prefix.RegisterPrefixIntention;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;
import util.RDFModelUtil;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class CurieUtil {

    public static CurieUtil SINGLETON = new CurieUtil();

    private static final AnnotationUtil annotationUtil = AnnotationUtil.SINGLETON;
    private static final RegisterPrefixIntention registerPrefixIntention = RegisterPrefixIntention.SINGLETON;
    private ProjectUtil projectUtil = ProjectUtil.SINGLETON;

    public Optional<OMTPrefix> getDefinedByPrefix(OMTParameterType parameterType) {
        // Otherwise the main prefixes block:
        return getDefinedByPrefix(getPrefixBlock(parameterType), parameterType);
    }

    public Optional<OMTPrefix> getDefinedByPrefix(OMTCurieElement curieElement) {
        // First the script block:
        Optional<OMTPrefix> definedByScript = getDefinedByPrefix(PsiTreeUtil.getParentOfType(curieElement, OMTScript.class), curieElement);
        if (definedByScript.isPresent()) {
            return definedByScript;
        }

        // Otherwise the main prefixes block:
        return getDefinedByPrefix(getPrefixBlock(curieElement), curieElement);
    }

    public Optional<OMTPrefix> getDefinedByPrefix(PsiElement containingElement, OMTCurieElement curieElement) {
        if (containingElement == null) {
            return Optional.empty();
        }
        Collection<OMTPrefix> prefixes = PsiTreeUtil.findChildrenOfType(containingElement, OMTPrefix.class);
        return prefixes.stream()
                .filter(curieElement::isDefinedByPrefix)
                .findFirst();
    }

    public Optional<OMTPrefix> getDefinedByPrefix(PsiElement containingElement, OMTParameterType parameterType) {
        if (containingElement == null) {
            return Optional.empty();
        }
        Collection<OMTPrefix> prefixes = PsiTreeUtil.findChildrenOfType(containingElement, OMTPrefix.class);
        return prefixes.stream()
                .filter(parameterType::isDefinedByPrefix)
                .findFirst();
    }

    public OMTPrefixBlock getPrefixBlock(PsiElement element) {
        Optional<OMTBlockEntry> prefixes = ((OMTFile) element.getContainingFile()).getRootBlock("prefixes");
        return prefixes.map(omtBlockEntry -> omtBlockEntry.getSpecificBlock().getPrefixBlock()).orElse(null);
    }

    public void annotateNamespacePrefix(@NotNull OMTNamespacePrefix namespacePrefix, @NotNull AnnotationHolder holder) {
        if (namespacePrefix.getParent() instanceof OMTPrefix) {
            annotationUtil.annotateUsage(namespacePrefix, OMTNamespacePrefix.class, holder);
        } else {
            List<OMTPrefix> knownPrefixes = projectUtil.getKnownPrefixes(namespacePrefix.getName());
            AnnotationBuilder annotationBuilder = annotationUtil.annotateOriginGetBuilder(namespacePrefix, holder);

            if (annotationBuilder != null) {
                if (!knownPrefixes.isEmpty()) {
                    knownPrefixes.stream().map(OMTPrefix::getNamespaceIri)
                            .map(PsiElement::getText)
                            .distinct()
                            .forEach(iri -> annotationBuilder.withFix(
                                    registerPrefixIntention.getRegisterPrefixIntention(namespacePrefix, iri)
                                    )
                            );
                }
                annotationBuilder.create();
            }
        }
    }

    public void resetPrefixBlock(PsiElement element) {
        resetPrefixBlock(element, "", "");
    }

    public void addPrefixToBlock(PsiElement element, String addNamespacePrefix, String addNamespaceIri) {
        resetPrefixBlock(element, addNamespacePrefix, addNamespaceIri);
    }

    public void addPrefixToBlock(PsiElement element, OMTPrefix prefix) {
        resetPrefixBlock(element, prefix.getNamespacePrefix().getName().trim(), prefix.getNamespaceIri().getText().trim());
    }

    private void resetPrefixBlock(PsiElement element, @NotNull String addNamespacePrefix, @NotNull String addNamespaceIri) {
        OMTPrefixBlock prefixBlock = getPrefixBlock(element);
        StringBuilder prefixBlockBuilder = new StringBuilder();
        prefixBlockBuilder.append("prefixes:\n");
        int indents = 2;
        if (prefixBlock != null) {
            // check the max length of the prefix label used
            // adjust indentation based on that length to align the prefix IRIs
            int maxPrefixLength = Math.max(addNamespacePrefix.length(), prefixBlock.getPrefixList().stream().map(
                    prefix -> prefix.getNamespacePrefix().getText().length()
            ).max(Integer::compareTo).orElse(0));
            indents = (int) Math.ceil((double) (maxPrefixLength + 1) / OMTElementFactory.getIndentSpace(1).length());

            int finalIndents = indents;
            prefixBlock.getPrefixList()
                    .forEach(prefix -> {
                        if (prefix.getLeading() != null) {
                            prefixBlockBuilder.append(prefix.getLeading().getText().trim());
                        }
                        prefixBlockBuilder
                                .append(OMTElementFactory.getIndentSpace(1))
                                .append(prefix.getNamespacePrefix().getName());
                        prefixBlockBuilder
                                .append(OMTElementFactory.getIndentSpace(finalIndents, prefix.getNamespacePrefix().getText().length()))
                                .append(prefix.getNamespaceIri().getText().trim());
                        if (prefix.getTrailing() != null) {
                            prefixBlockBuilder.append(prefix.getTrailing().getText().trim());
                        }
                        prefixBlockBuilder.append("\n");
                    });
        }
        if (!addNamespacePrefix.isEmpty() && !addNamespaceIri.isEmpty()) {
            prefixBlockBuilder
                    .append(OMTElementFactory.getIndentSpace(1))
                    .append(addNamespacePrefix);
            prefixBlockBuilder
                    .append(OMTElementFactory.getIndentSpace(indents, addNamespacePrefix.length()))
                    .append(addNamespaceIri.trim());
            prefixBlockBuilder.append("\n");
        }
        prefixBlockBuilder.append("\n");
        ((OMTFile) element.getContainingFile()).setRootBlock((OMTPrefixBlock) OMTElementFactory.fromString(prefixBlockBuilder.toString(), OMTPrefixBlock.class, element.getProject()));
    }

    public void annotateCurieElement(OMTCurieElement curieElement, AnnotationHolder annotationHolder) {
        RDFModelUtil rdfModelUtil = new RDFModelUtil(projectUtil.getOntologyModel());
        Resource resource = curieElement.getAsResource();
        if (rdfModelUtil.isClassResource(resource)) {
            annotationHolder.newAnnotation(HighlightSeverity.INFORMATION, rdfModelUtil.describeResource(resource)).create();
        }
    }
}
