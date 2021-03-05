package com.misset.opp.omt.psi.util;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTCommandBlock;
import com.misset.opp.omt.psi.OMTScript;
import com.misset.opp.omt.psi.OMTScriptLine;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ScriptUtil {

    public Optional<OMTScript> getScript(PsiElement element) {
        return Optional.ofNullable(PsiTreeUtil.getTopmostParentOfType(element, OMTScript.class));
    }

    private Optional<OMTScriptLine> getScriptLine(PsiElement element) {
        return Optional.ofNullable(PsiTreeUtil.getParentOfType(element, OMTScriptLine.class));
    }

    /**
     * Returns all items in the script which are accessible to the element, meaning they are not contained
     * within other { } blocks and are listed above the provided element
     */
    public <T> List<T> getAccessibleElements(PsiElement element, Class<T> type) {
        List<T> items = new ArrayList<>();
        OMTScriptLine currentScriptLine = PsiTreeUtil.getParentOfType(element, OMTScriptLine.class);
        if (currentScriptLine == null && element instanceof OMTCommandBlock) {
            final List<OMTScriptLine> scriptLines = PsiTreeUtil.getChildrenOfTypeAsList(element, OMTScriptLine.class);
            if (scriptLines.isEmpty()) {
                return items;
            }
            currentScriptLine = scriptLines.get(scriptLines.size() - 1);
        }
        if (currentScriptLine == null || type == null) {
            return items;
        }
        items.addAll(getChildrenOfTypeNotEnclosed(currentScriptLine, type));
        while (currentScriptLine != null) {
            getPrecedingScriptLines(currentScriptLine).forEach(scriptLine -> items.addAll(getChildrenOfTypeNotEnclosed(scriptLine, type)));
            currentScriptLine = PsiTreeUtil.getParentOfType(currentScriptLine.getParent(), OMTScriptLine.class);
        }
        return items;
    }

    public <T extends PsiElement> List<T> getRelatableElements(PsiElement element, Class<T> type, Predicate<T> condition) {
        List<T> items = new ArrayList<>();
        if (!PsiElement.class.isAssignableFrom(type)) {
            return items;
        }
        OMTScriptLine currentScriptLine = PsiTreeUtil.getParentOfType(element, OMTScriptLine.class);
        while (currentScriptLine != null) {
            getPrecedingScriptLines(currentScriptLine)
                    .forEach(scriptLine -> PsiTreeUtil.findChildrenOfType(scriptLine, type)
                            .stream()
                            .map(type::cast)
                            .filter(condition)
                            .forEach(items::add));
            currentScriptLine = PsiTreeUtil.getParentOfType(currentScriptLine.getParent(), OMTScriptLine.class);
        }
        return items;
    }

    private List<OMTScriptLine> getPrecedingScriptLines(OMTScriptLine scriptLine) {
        return PsiTreeUtil.getChildrenOfTypeAsList(scriptLine.getParent(), OMTScriptLine.class)
                .stream().filter(sibling -> sibling.getStartOffsetInParent() < scriptLine.getStartOffsetInParent())
                .collect(Collectors.toList());
    }

    private <T> List<T> getChildrenOfTypeNotEnclosed(PsiElement element, Class<T> type) {
        List<T> allChildren = new ArrayList<>();
        if (type.isAssignableFrom(element.getClass())) {
            allChildren.add(type.cast(element));
        }
        @NotNull PsiElement[] children = element.getChildren();
        if (children.length > 0) {
            for (PsiElement child : children) {
                if (!(child instanceof OMTCommandBlock) && element != child) {
                    allChildren.addAll(getChildrenOfTypeNotEnclosed(child, type));
                }
            }
        }
        return allChildren;
    }

    public void annotateFinalStatement(PsiElement returnStatement, AnnotationHolder holder) {
        OMTScriptLine scriptLine = PsiTreeUtil.getParentOfType(returnStatement, OMTScriptLine.class);
        if (scriptLine != null) {
            OMTScriptLine nextLine = PsiTreeUtil.getNextSiblingOfType(scriptLine, OMTScriptLine.class);
            while (nextLine != null) {
                holder.newAnnotation(HighlightSeverity.ERROR, "Unreachable code").range(nextLine).create();
                nextLine = PsiTreeUtil.getNextSiblingOfType(nextLine, OMTScriptLine.class);
            }
        }
    }

}
