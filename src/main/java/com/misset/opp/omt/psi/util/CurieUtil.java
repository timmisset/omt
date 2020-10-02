package com.misset.opp.omt.psi.util;

import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.intentions.prefix.RegisterPrefixIntention;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class CurieUtil {

    public static CurieUtil SINGLETON = new CurieUtil();

    private static final AnnotationUtil annotationUtil = AnnotationUtil.SINGLETON;
    private static final RegisterPrefixIntention registerPrefixIntention = RegisterPrefixIntention.SINGLETON;
    private ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    private ModelUtil modelUtil = ModelUtil.SINGLETON;

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
            List<OMTPrefix> knownPrefixes = projectUtil.getKnownPrefixes(namespacePrefix.getName());
            AnnotationBuilder annotationBuilder = annotationUtil.annotateOriginGetBuilder(namespacePrefix, holder);

            if (annotationBuilder != null) {
                if (!knownPrefixes.isEmpty()) {
                    knownPrefixes.stream().map(OMTPrefix::getNamespaceIri)
                            .map(omtNamespaceIri -> omtNamespaceIri.getText())
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
        resetPrefixBlock(element, null, null);
    }

    public void addPrefixToBlock(PsiElement element, String addNamespacePrefix, String addNamespaceIri) {
        resetPrefixBlock(element, addNamespacePrefix, addNamespaceIri);
    }

    public void addPrefixToBlock(PsiElement element, OMTPrefix prefix) {
        resetPrefixBlock(element, prefix.getNamespacePrefix().getName().trim(), prefix.getNamespaceIri().getText().trim());
    }

    private void resetPrefixBlock(PsiElement element, String addNamespacePrefix, String addNamespaceIri) {
        OMTPrefixBlock prefixBlock = getPrefixBlock(element);
        StringBuilder prefixBlockBuilder = new StringBuilder();
        prefixBlockBuilder.append("prefixes:\n");
        if (prefixBlock != null) {
            prefixBlock.getPrefixList()
                    .forEach(prefix -> {
                        if (prefix.getLeading() != null) {
                            prefixBlockBuilder.append(prefix.getLeading().getText().trim());
                        }
                        prefixBlockBuilder
                                .append(OMTElementFactory.getIndentSpace(1))
                                .append(prefix.getNamespacePrefix().getName());
                        prefixBlockBuilder
                                .append(OMTElementFactory.getIndentSpace(2, prefix.getNamespacePrefix().getName().length()))
                                .append(prefix.getNamespaceIri().getText().trim());
                        if (prefix.getTrailing() != null) {
                            prefixBlockBuilder.append(prefix.getTrailing().getText().trim());
                        }
                        prefixBlockBuilder.append("\n");
                    });
        }
        if (addNamespacePrefix != null && addNamespaceIri != null) {
            prefixBlockBuilder
                    .append(OMTElementFactory.getIndentSpace(1))
                    .append(addNamespacePrefix);
            prefixBlockBuilder
                    .append(OMTElementFactory.getIndentSpace(2, addNamespacePrefix.length()))
                    .append(addNamespaceIri.trim());
            prefixBlockBuilder.append("\n");
        }
        prefixBlockBuilder.append("\n");
        PsiElement psiElement = OMTElementFactory.fromString(prefixBlockBuilder.toString(), OMTPrefixBlock.class, element.getProject());
        if (psiElement instanceof OMTPrefixBlock) {
            if (prefixBlock != null) {
                prefixBlock.replace(psiElement);
            } else {
                OMTFile containingFile = (OMTFile) element.getContainingFile();
                Optional<OMTBlockEntry> importBlock = containingFile.getRootBlock("import");
                if (importBlock.isPresent()) {
                    containingFile.addAfter(psiElement, importBlock.get());
                } else {
                    containingFile.addBefore(psiElement, containingFile.getFirstChild());
                }
            }
        }
    }
}
