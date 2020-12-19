package com.misset.opp.omt.psi.util;

import com.intellij.application.options.CodeStyle;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.intentions.prefix.RegisterPrefixIntention;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static com.misset.opp.omt.psi.util.UtilManager.getRDFModelUtil;

public class CurieUtil {

    private final RegisterPrefixIntention registerPrefixIntention = new RegisterPrefixIntention();

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
        Optional<OMTPrefixBlock> prefixes = ((OMTFile) element.getContainingFile()).getSpecificBlock(OMTFile.PREFIXES, OMTPrefixBlock.class);
        return prefixes.orElse(null);
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
        final int indent_size = Objects.requireNonNull(CodeStyle.getLanguageSettings(element.getContainingFile()).getIndentOptions()).INDENT_SIZE;
        String indent = StringUtil.repeat(" ", indent_size);

        String template = String.format("prefixes:\n%s%s: %s\n\n", indent, addNamespacePrefix, addNamespaceIri);
        if (prefixBlock == null) {
            prefixBlock = (OMTPrefixBlock) OMTElementFactory.fromString(template, OMTPrefixBlock.class, project);
            CodeStyleManager.getInstance(project).reformat(prefixBlock);
            prefixBlock = ((OMTFile) element.getContainingFile()).setRootBlock(prefixBlock);
        } else {
            OMTPrefix prefix = (OMTPrefix) OMTElementFactory.fromString(template, OMTPrefix.class, project);
            prefixBlock.addBefore(prefix, prefixBlock.getDedentToken());
            CodeStyleManager.getInstance(project).reformat(prefixBlock);
        }
        prefixBlock.replace(OMTElementFactory.removeBlankLinesInside(prefixBlock, OMTPrefixBlock.class, "\n"));
    }

    public void annotateCurieElement(OMTCurieElement curieElement, AnnotationHolder annotationHolder) {
        annotateAsResource(curieElement.getAsResource(), annotationHolder);
    }

    private void annotateAsResource(Resource resource, AnnotationHolder annotationHolder) {
        annotationHolder.newAnnotation(HighlightSeverity.INFORMATION, resource.toString())
                .tooltip(getRDFModelUtil().describeResource(resource))
                .create();
    }
}
