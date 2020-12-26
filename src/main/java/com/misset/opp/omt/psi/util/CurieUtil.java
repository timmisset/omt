package com.misset.opp.omt.psi.util;

import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.misset.opp.omt.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

import static com.misset.opp.omt.psi.util.UtilManager.getModelUtil;

public class CurieUtil {

    public Optional<OMTPrefix> getDefinedByPrefix(OMTNamespacePrefix namespacePrefix) {
        if (namespacePrefix.getParent() instanceof OMTPrefix) {
            return Optional.empty();
        }
        return getDefinedByPrefix(getPrefixModelBlock(namespacePrefix), namespacePrefix)
                .or(() -> getDefinedByPrefix(getPrefixRootBlock(namespacePrefix), namespacePrefix));
    }

    private Optional<OMTPrefix> getDefinedByPrefix(OMTPrefixBlock prefixBlock, OMTNamespacePrefix namespacePrefix) {
        if (prefixBlock == null) {
            return Optional.empty();
        }
        return prefixBlock.getPrefixList().stream()
                .filter(prefix ->
                        prefix.getNamespacePrefix() != namespacePrefix &&
                                prefix.getNamespacePrefix().getName().equals(namespacePrefix.getName()))
                .findFirst();
    }

    public OMTPrefixBlock getPrefixRootBlock(PsiElement element) {
        Optional<OMTPrefixBlock> prefixes = ((OMTFile) element.getContainingFile()).getSpecificBlock(OMTFile.PREFIXES, OMTPrefixBlock.class);
        return prefixes.orElse(null);
    }

    public OMTPrefixBlock getPrefixModelBlock(PsiElement element) {
        return (OMTPrefixBlock) getModelUtil().getModelItemBlockEntry(element, "prefixes").orElse(null);
    }

    public void addPrefixToBlock(PsiElement element, String addNamespacePrefix, String addNamespaceIri) {
        resetPrefixBlock(element, addNamespacePrefix, addNamespaceIri);
    }

    public void addPrefixToBlock(PsiElement element, OMTPrefix prefix) {
        resetPrefixBlock(element, prefix.getNamespacePrefix().getName().trim(), prefix.getNamespaceIri().getText().trim());
    }

    private void resetPrefixBlock(PsiElement element, @NotNull String addNamespacePrefix, @NotNull String addNamespaceIri) {
        OMTPrefixBlock prefixBlock = getPrefixRootBlock(element);
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
}
