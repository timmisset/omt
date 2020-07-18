package com.misset.opp.omt.psi.util;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.impl.OMTCuriePrefixImpl;
import com.misset.opp.omt.psi.impl.OMTPrefixBlockImpl;
import com.misset.opp.omt.psi.impl.OMTPrefixImpl;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class CurieUtil {

    // A curie and curie_constant (= /ont:someOntologyClass) are both used in the query parts
    // to discriminate between / curie and /curie the latter must be parsed specifically
    public static OMTCurie toOMTCurie(OMTCurieElement element) {
        return new OMTCurie(element, false);
    }
    public static OMTCurie toOMTCurie(OMTCurieConstantElement element) {
        return new OMTCurie(element, true);
    }

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
        OMTPrefix prefix = OMTElementFactory.createPrefix(leftHand, rightHand, block.getProject());
        List<OMTPrefix> prefixList = block.getPrefixList();
        PsiElement newLine = OMTElementFactory.createNewLine(block.getProject());
        PsiElement indent = OMTElementFactory.createIdent(block.getProject());
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
        return prefix;
    }

    /**
     * In case the curie is using a prefix which is not defined, provide a way to fix it
     * @param curie
     * @return
     */
    public static IntentionAction getRegisterPrefixIntention(OMTCurie curie) {
        return new IntentionAction() {
            @Override
            public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getText() {
                return "Register prefix";
            }

            @Override
            public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getFamilyName() {
                return "Prefixes";
            }

            @Override
            public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
                return true;
            }

            @Override
            public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
                addPrefix(curie.getCuriePrefix(), "", getPrefixBlock(curie.getElement(), true));
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }
}
