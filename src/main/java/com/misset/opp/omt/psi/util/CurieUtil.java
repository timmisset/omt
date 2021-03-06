package com.misset.opp.omt.psi.util;

import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.misset.opp.omt.psi.OMTElementFactory;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.OMTPrefixBlock;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

import static com.misset.opp.util.UtilManager.getModelUtil;

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
                .filter(prefix -> prefix.getNamespacePrefix().getName().equals(namespacePrefix.getName()))
                .findFirst();
    }

    private OMTPrefixBlock getPrefixRootBlock(PsiElement element) {
        Optional<OMTPrefixBlock> prefixes = ((OMTFile) element.getContainingFile()).getSpecificBlock(OMTFile.PREFIXES, OMTPrefixBlock.class);
        return prefixes.orElse(null);
    }

    private OMTPrefixBlock getPrefixModelBlock(PsiElement element) {
        return (OMTPrefixBlock) getModelUtil().getModelItemBlockEntry(element, "prefixes").orElse(null);
    }

    public void addPrefixToBlock(PsiElement element, @NotNull String addNamespacePrefix, @NotNull String addNamespaceIri) {
        OMTPrefixBlock prefixBlock = getPrefixRootBlock(element);
        Project project = element.getProject();
        final int indent_size = Objects.requireNonNull(CodeStyle.getLanguageSettings(element.getContainingFile()).getIndentOptions()).INDENT_SIZE;
        String indent = StringUtil.repeat(" ", indent_size);

        String template = String.format("prefixes:\n%s%s: <%s>\n\n", indent, addNamespacePrefix, addNamespaceIri);
        if (prefixBlock == null) {
            prefixBlock = (OMTPrefixBlock) OMTElementFactory.fromString(template, OMTPrefixBlock.class, project);
            CodeStyleManager.getInstance(project).reformat(prefixBlock);
            ((OMTFile) element.getContainingFile()).setRootBlock(prefixBlock);
        } else {
            OMTPrefix prefix = (OMTPrefix) OMTElementFactory.fromString(template, OMTPrefix.class, project);
            final OMTPrefix anchor = prefixBlock.getPrefixList().get(prefixBlock.getPrefixList().size() - 1);
            prefixBlock.addAfter(prefix, anchor);
            CodeStyleManager.getInstance(project).reformat(prefixBlock);
        }
    }
}
