package com.misset.opp.omt.psi.util;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class CurieUtil {

    public static Optional<OMTPrefix> getDefinedByPrefix(OMTParameterType parameterType) {
        // Otherwise the main prefixes block:
        return getDefinedByPrefix(getPrefixBlock(parameterType), parameterType);
    }
    public static Optional<OMTPrefix> getDefinedByPrefix(OMTCurieElement curieElement) {
        // First the script block:
        Optional<OMTPrefix> definedByScript = getDefinedByPrefix(PsiTreeUtil.getParentOfType(curieElement, OMTScript.class), curieElement);
        if(definedByScript.isPresent()) { return definedByScript; }

        // Otherwise the main prefixes block:
        return getDefinedByPrefix(getPrefixBlock(curieElement), curieElement);
    }
    public static  Optional<OMTPrefix> getDefinedByPrefix(PsiElement containingElement, OMTCurieElement curieElement) {
        if(containingElement == null) { return Optional.empty(); }
        Collection<OMTPrefix> prefixes = PsiTreeUtil.findChildrenOfType(containingElement, OMTPrefix.class);
        return prefixes.stream()
                .filter(curieElement::isDefinedByPrefix)
                .findFirst();
    }
    public static  Optional<OMTPrefix> getDefinedByPrefix(PsiElement containingElement, OMTParameterType parameterType) {
        if(containingElement == null) { return Optional.empty(); }
        Collection<OMTPrefix> prefixes = PsiTreeUtil.findChildrenOfType(containingElement, OMTPrefix.class);
        return prefixes.stream()
                .filter(parameterType::isDefinedByPrefix)
                .findFirst();
    }

    public static  OMTPrefixBlock getPrefixBlock(PsiElement element) {
        return getPrefixBlock(element, false);
    }
    public static  OMTPrefixBlock getPrefixBlock(PsiElement element, boolean createIfNeeded) {
        final PsiFile file = element.getContainingFile();
        @NotNull Collection<OMTSpecificBlock> children = PsiTreeUtil.findChildrenOfType(file, OMTSpecificBlock.class);
        for(OMTSpecificBlock child : children) {
            if(child != null && child.getPrefixBlock() != null) { return child.getPrefixBlock(); }
        }
        // create if required:
        if(createIfNeeded) {
            OMTPrefixBlock prefixBlock = OMTElementFactory.createPrefixBlock(element.getProject());
            PsiElement firstChild = file.getFirstChild();
            prefixBlock = (OMTPrefixBlock)file.addBefore(prefixBlock, firstChild);
            file.addAfter(OMTElementFactory.createNewLine(element.getProject()), prefixBlock);
            return prefixBlock;
        }
        return  null;
    }
    public static OMTPrefix addPrefix(String leftHand, String rightHand, OMTPrefixBlock block) {
        Project project = block.getProject();
        OMTPrefix prefix = OMTElementFactory.createPrefix(leftHand, rightHand, project);
        List<OMTPrefix> prefixList = block.getPrefixList();
        PsiElement newLine = OMTElementFactory.createNewLine(project);
        PsiElement indent = OMTElementFactory.createIdent(project);
        if(prefixList.isEmpty()) {
            // first entry:
            prefix = (OMTPrefix)block.add(prefix);
            block.addAfter(newLine, prefix);
            block.addBefore(indent, prefix);
        } else {
            // add at the bottom of list:
            OMTPrefix lastPrefix =  prefixList.get(prefixList.size() - 1);
            prefix = (OMTPrefix)block.addAfter(prefix, lastPrefix);
            block.addBefore(indent, prefix);
            block.addAfter(newLine, lastPrefix);
        }
        CodeStyleManager.getInstance(project).reformat(block);
        return prefix;
    }
    public static Collection<OMTPrefix> getAllPrefixes(PsiElement element) {
        return PsiTreeUtil.findChildrenOfType(element.getContainingFile(), OMTPrefix.class);
    }

    public static void annotateNamespacePrefix(@NotNull OMTNamespacePrefix namespacePrefix, @NotNull AnnotationHolder holder) {

        if(namespacePrefix.getParent() instanceof OMTPrefix) {
            AnnotationUtil.annotateUsage(namespacePrefix, OMTNamespacePrefix.class, holder);
        } else {
            AnnotationUtil.annotateOrigin(namespacePrefix, holder);
        }

    }
}
