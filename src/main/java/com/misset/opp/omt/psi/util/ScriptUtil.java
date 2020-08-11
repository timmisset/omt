package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTScript;
import com.misset.opp.omt.psi.OMTScriptLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScriptUtil {

    public static boolean isPartOfScript(PsiElement element) {
        return getScript(element).isPresent();
    }
    public static Optional<OMTScript> getScript(PsiElement element) {
        return Optional.ofNullable(PsiTreeUtil.getTopmostParentOfType(element, OMTScript.class));
    }
    public static Optional<OMTScriptLine> getScriptLine(PsiElement element) {
        return Optional.ofNullable(PsiTreeUtil.getParentOfType(element, OMTScriptLine.class));
    }
    public static List<OMTScriptLine> getScriptLinesAtSameDepth(PsiElement elementA, PsiElement elementB) {
        Optional<OMTScriptLine> optA = getScriptLine(elementA);
        Optional<OMTScriptLine> optB = getScriptLine(elementB);
        List<OMTScriptLine> lines = new ArrayList<>();
        // both should resolve to the same top most parent of Script
        if(optA.isPresent() && optB.isPresent()) {
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
    public static boolean isBefore(PsiElement isElement, PsiElement beforeElement) {
        Optional<OMTScriptLine> isElementScriptline = getScriptLine(isElement);
        Optional<OMTScriptLine> beforeElementScriptline = getScriptLine(beforeElement);
        List<OMTScriptLine> scriptLinesAtSameDepth = getScriptLinesAtSameDepth(isElement, beforeElement);
        if(scriptLinesAtSameDepth.isEmpty()) {
            throw new Error("Cannot resolve scriptlines for elements");
        }
        return scriptLinesAtSameDepth.get(0).getStartOffsetInParent() <
                scriptLinesAtSameDepth.get(1).getStartOffsetInParent();

    }
}
