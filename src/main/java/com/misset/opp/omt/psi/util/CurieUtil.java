package com.misset.opp.omt.psi.util;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public class CurieUtil {

    public static CurieUtil SINGLETON = new CurieUtil();

    private static final AnnotationUtil annotationUtil = AnnotationUtil.SINGLETON;

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
        return getPrefixBlock(element, false);
    }

    public OMTPrefixBlock getPrefixBlock(PsiElement element, boolean createIfNeeded) {
        final PsiFile file = element.getContainingFile();
        @NotNull Collection<OMTSpecificBlock> children = PsiTreeUtil.findChildrenOfType(file, OMTSpecificBlock.class);
        for (OMTSpecificBlock child : children) {
            if (child != null && child.getPrefixBlock() != null) {
                return child.getPrefixBlock();
            }
        }
        // create if required:
        if (createIfNeeded) {
            OMTPrefixBlock prefixBlock = OMTElementFactory.createPrefixBlock(element.getProject());
            PsiElement firstChild = file.getFirstChild();
            prefixBlock = (OMTPrefixBlock) file.addBefore(prefixBlock, firstChild);
            file.addAfter(OMTElementFactory.createNewLine(element.getProject()), prefixBlock);
            return prefixBlock;
        }
        return  null;
    }

    public void annotateNamespacePrefix(@NotNull OMTNamespacePrefix namespacePrefix, @NotNull AnnotationHolder holder) {
        if (namespacePrefix.getParent() instanceof OMTPrefix) {
            annotationUtil.annotateUsage(namespacePrefix, OMTNamespacePrefix.class, holder);
        } else {
            annotationUtil.annotateOrigin(namespacePrefix, holder);
        }
    }
}
