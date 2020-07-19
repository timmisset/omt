package com.misset.opp.omt.psi.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class CurieUtil {

    public static Optional<OMTPrefix> getDefinedByPrefix(OMTCurie curie) {
        PsiElement element = curie.getElement();

        // This method will return (if any) the prefix definition used by the curie
        // A prefix definition can be part of the root prefixes: block, or by a PREFIX dat: <http://data/> definition
        // in the script that contains the curie

        // First the script block:
        Optional<OMTPrefix> definedByScript = getDefinedByPrefix(PsiTreeUtil.getParentOfType(element, OMTScript.class), curie);
        if(definedByScript.isPresent()) { return definedByScript; }

        // Otherwise the main prefixes block:
        return getDefinedByPrefix(getPrefixBlock(element), curie);
    }
    public static  Optional<OMTPrefix> getDefinedByPrefix(PsiElement containingElement, OMTCurie curie) {
        if(containingElement == null) { return Optional.empty(); }
        Collection<OMTPrefix> prefixes = PsiTreeUtil.findChildrenOfType(containingElement, OMTPrefix.class);
        return prefixes.stream()
                .filter(curie::isDefinedByPrefix)
                .findFirst();
    }

    public static  OMTPrefixBlock getPrefixBlock(PsiElement element) {
        return getPrefixBlock(element, false);
    }
    public static  OMTPrefixBlock getPrefixBlock(PsiElement element, boolean createIfNeeded) {
        final PsiFile file = element.getContainingFile();
        OMTPrefixBlock prefixBlock = PsiTreeUtil.getChildOfType(file, OMTPrefixBlock.class);
        if(prefixBlock == null && createIfNeeded) {
            prefixBlock = OMTElementFactory.createPrefixBlock(element.getProject());
            PsiElement firstChild = file.getFirstChild();
            prefixBlock = (OMTPrefixBlock)file.addBefore(prefixBlock, firstChild);
            file.addAfter(OMTElementFactory.createNewLine(element.getProject()), prefixBlock);
        }
        return prefixBlock;
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
    public static Collection<OMTCurie> getAllCuries(PsiElement element) {
        Collection<OMTCurieElement> curiePrefixes = PsiTreeUtil.findChildrenOfType(element.getContainingFile(), OMTCurieElement.class);
        Collection<OMTCurieConstantElement> curieConstants = PsiTreeUtil.findChildrenOfType(element.getContainingFile(), OMTCurieConstantElement.class);

        List<OMTCurie> curies = new ArrayList<>();
        curiePrefixes.forEach(omtCuriePrefix -> curies.add(new OMTCurie(omtCuriePrefix)));
        curieConstants.forEach(omtCurieConstant -> curies.add(new OMTCurie(omtCurieConstant)));
        return curies;
    }
    public static boolean isPrefixedDefinedMoreThanOnce(OMTPrefix prefix) {
        Collection<OMTPrefix> allPrefixes = getAllPrefixes(prefix);
        for(OMTPrefix _prefix : allPrefixes) {
            if(_prefix != prefix && _prefix.getText().equals(prefix.getText())) {
                return true;
            }
        }
        return false;
    }
    public static boolean isPrefixUsed(OMTPrefix prefix) {
        Collection<OMTCurie> allCuries = getAllCuries(prefix);
        for(OMTCurie curie : allCuries) {
            if(!curie.isPrefixDefinition() && curie.isDefinedByPrefix(prefix)) {
                return true;
            }
        }
        return false;
    }
}
