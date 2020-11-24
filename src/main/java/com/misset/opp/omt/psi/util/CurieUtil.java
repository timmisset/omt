package com.misset.opp.omt.psi.util;

import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.external.util.rdf.RDFModelUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.intentions.prefix.RegisterPrefixIntention;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

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

    public void addPrefixToBlock(PsiElement element, String addNamespacePrefix, String addNamespaceIri) {
        resetPrefixBlock(element, addNamespacePrefix, addNamespaceIri);
    }

    public void addPrefixToBlock(PsiElement element, OMTPrefix prefix) {
        resetPrefixBlock(element, prefix.getNamespacePrefix().getName().trim(), prefix.getNamespaceIri().getText().trim());
    }

    private void resetPrefixBlock(PsiElement element, @NotNull String addNamespacePrefix, @NotNull String addNamespaceIri) {
        OMTPrefixBlock prefixBlock = getPrefixBlock(element);
        Project project = element.getProject();
        // do not use %n instead of \n, this is not accepted by IntelliJ
        if (!addNamespaceIri.startsWith("<")) {
            addNamespaceIri = "<" + addNamespaceIri + ">";
        }
        String template = String.format("prefixes: \n %s: %s\n\n", addNamespacePrefix, addNamespaceIri);
        if (prefixBlock == null) {
            prefixBlock = (OMTPrefixBlock) OMTElementFactory.fromString(template, OMTPrefixBlock.class, project);
            ((OMTFile) element.getContainingFile()).setRootBlock(prefixBlock);
        } else {
            OMTPrefix prefix = (OMTPrefix) OMTElementFactory.fromString(template, OMTPrefix.class, project);
            final List<OMTPrefix> prefixList = prefixBlock.getPrefixList();
            final OMTPrefix lastPrefix = prefixList.get(prefixList.size() - 1);
            final PsiElement insertedPrefix = prefixBlock.addAfter(prefix, lastPrefix);
            prefixBlock.addBefore(OMTElementFactory.createNewLine(project), insertedPrefix);
        }
        CodeStyleManager.getInstance(project).reformat(prefixBlock);
    }

    public void annotateCurieElement(OMTCurieElement curieElement, AnnotationHolder annotationHolder) {
        annotateAsResource(curieElement.getAsResource(), annotationHolder);
    }

    public void annotateParameterType(OMTParameterType parameterType, AnnotationHolder annotationHolder) {
        annotateAsResource(parameterType.getAsResource(), annotationHolder);
    }

    private void annotateAsResource(Resource resource, AnnotationHolder annotationHolder) {
        RDFModelUtil rdfModelUtil = new RDFModelUtil(projectUtil.getOntologyModel());
        annotationHolder.newAnnotation(HighlightSeverity.INFORMATION, resource.toString())
                .tooltip(rdfModelUtil.describeResource(resource))
                .create();
    }
}
