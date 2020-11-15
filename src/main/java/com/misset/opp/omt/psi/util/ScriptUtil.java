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

    public static final ScriptUtil SINGLETON = new ScriptUtil();

    public Optional<OMTScript> getScript(PsiElement element) {
        return Optional.ofNullable(PsiTreeUtil.getTopmostParentOfType(element, OMTScript.class));
    }

    private Optional<OMTScriptLine> getScriptLine(PsiElement element) {
        return Optional.ofNullable(PsiTreeUtil.getParentOfType(element, OMTScriptLine.class));
    }

    /**
     * Returns all items in the script which are accessible to the element, meaning they are not contained
     * within other { } blocks and are listed above the provided element
     *
     * @param element
     * @return
     */
    public <T> List<T> getAccessibleElements(PsiElement element, Class<T> type) {
        List<T> items = new ArrayList<>();
        OMTScriptLine currentScriptLine = (OMTScriptLine) PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTScriptLine);
        while (currentScriptLine != null) {
            getPrecedingScriptLines(currentScriptLine).forEach(scriptLine -> items.addAll(getChildrenOfTypeNotEnclosed(scriptLine, type)));
            currentScriptLine = (OMTScriptLine) PsiTreeUtil.findFirstParent(currentScriptLine.getParent(), parent -> parent instanceof OMTScriptLine);
        }
        return items;
    }

    public <T> List<T> getRelatableElements(PsiElement element, Class<T> type, Predicate<T> condition) {
        List<T> items = new ArrayList<>();
        if (!PsiElement.class.isAssignableFrom(type)) {
            return items;
        }
        OMTScriptLine currentScriptLine = (OMTScriptLine) PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTScriptLine);
        while (currentScriptLine != null) {
            getPrecedingScriptLines(currentScriptLine)
                    .forEach(scriptLine -> PsiTreeUtil.findChildrenOfType(scriptLine, (Class<? extends PsiElement>) type)
                            .stream()
                            .map(type::cast)
                            .filter(condition)
                            .forEach(items::add));
            currentScriptLine = (OMTScriptLine) PsiTreeUtil.findFirstParent(currentScriptLine.getParent(), parent -> parent instanceof OMTScriptLine);
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
                if (!(child instanceof OMTCommandBlock)) {
                    allChildren.addAll(getChildrenOfTypeNotEnclosed(child, type));
                }
            }
        }
        return allChildren;
    }

    private List<OMTScriptLine> getScriptLinesAtSameDepth(PsiElement elementA, PsiElement elementB) {
        Optional<OMTScriptLine> optA = getScriptLine(elementA);
        Optional<OMTScriptLine> optB = getScriptLine(elementB);
        List<OMTScriptLine> lines = new ArrayList<>();
        // both should resolve to the same top most parent of Script
        if (optA.isPresent() && optB.isPresent()) {
            OMTScriptLine scriptLineA = optA.get();
            OMTScriptLine scriptLineB = optB.get();

            Optional<OMTScript> scriptA = getScript(scriptLineA);
            Optional<OMTScript> scriptB = getScript(scriptLineB);
            if(scriptA.isPresent() && scriptB.isPresent() && scriptA.get().isEquivalentTo(scriptB.get())) {
                OMTScript script = scriptA.get();
                int depthA = PsiTreeUtil.getDepth(scriptLineA, script);
                int depthB = PsiTreeUtil.getDepth(scriptLineB, script);
                while(depthA > depthB) {
                    Optional<OMTScriptLine> scriptLine = getScriptLine(scriptLineA);
                    if(scriptLine.isPresent()) { scriptLineA = scriptLine.get(); depthA = PsiTreeUtil.getDepth(scriptLineA, script); }
                    else { return lines; }
                }
                while(depthB > depthA) {
                    Optional<OMTScriptLine> scriptLine = getScriptLine(scriptLineB);
                    if(scriptLine.isPresent()) { scriptLineB = scriptLine.get(); depthB = PsiTreeUtil.getDepth(scriptLineB, script); }
                    else { return lines; }
                }
                lines.add(scriptLineA);
                lines.add(scriptLineB);
            }
        }
        return lines;
    }

    public boolean isBefore(PsiElement isElement, PsiElement beforeElement) {
        List<OMTScriptLine> scriptLinesAtSameDepth = getScriptLinesAtSameDepth(isElement, beforeElement);
        if (scriptLinesAtSameDepth.isEmpty()) {
            throw new Error("Cannot resolve scriptlines for elements");
        }
        return scriptLinesAtSameDepth.get(0).getStartOffsetInParent() <
                scriptLinesAtSameDepth.get(1).getStartOffsetInParent();

    }

    public void annotateFinalStatement(PsiElement returnStatement, AnnotationHolder holder) {
        OMTScriptLine scriptLine = (OMTScriptLine) PsiTreeUtil.findFirstParent(returnStatement, parent -> parent instanceof OMTScriptLine);
        if (scriptLine != null) {
            OMTScriptLine nextLine = PsiTreeUtil.getNextSiblingOfType(scriptLine, OMTScriptLine.class);
            while (nextLine != null) {
                holder.newAnnotation(HighlightSeverity.ERROR, "Unreachable code").range(nextLine).create();
                nextLine = PsiTreeUtil.getNextSiblingOfType(nextLine, OMTScriptLine.class);
            }
        }
    }

}
