package com.misset.opp.omt.psi.util;//package com.misset.opp.omt.domain.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTModelItemBlock;

import java.util.Optional;

//
//import com.intellij.openapi.project.Project;
//import com.intellij.psi.PsiElement;
//import com.intellij.psi.util.PsiTreeUtil;
//import com.misset.opp.omt.domain.OMTModelItem;
//import com.misset.opp.omt.domain.OMTModelItemAttribute;
//import com.misset.opp.omt.psi.*;
//import com.misset.opp.omt.domain.OMTParameter;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
public class ModelUtil {

    /**
     * Returns the modelItem block that this element belongs to
     * A modelItem is an element declared directly under model:
     * @param element
     * @return
     */
    public static Optional<OMTModelItemBlock> getModelItemBlock(PsiElement element) {
        OMTModelItemBlock block = PsiTreeUtil.getTopmostParentOfType(element, OMTModelItemBlock.class);
        return block != null ? Optional.of(block) : Optional.empty();
    }

    public static Optional<OMTBlockEntry> getModelItemBlockEntry(PsiElement element, String propertyLabel) {
        OMTModelItemBlock modelItemBlock = element instanceof OMTModelItemBlock ? (OMTModelItemBlock) element : getModelItemBlock(element).orElse(null);
        if(modelItemBlock == null) { return Optional.empty(); }

        final String finalPropertyLabel = propertyLabel.endsWith(":") ? propertyLabel : propertyLabel + ":";
        return modelItemBlock.getBlock().getBlockEntryList().stream()
                .filter(omtBlockEntry ->
                        omtBlockEntry.getPropertyLabel().getText().equals(finalPropertyLabel))
                .findFirst();
    }
}
