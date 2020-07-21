package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTPropertyLabel;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ModelUtil {

    public static Optional<OMTBlock> getModelItemBlock(PsiElement element) {
        OMTPropertyLabel propertyLabel = PsiTreeUtil.getChildOfType(element, OMTPropertyLabel.class);
        if(propertyLabel != null) {
            if(propertyLabel.getLastChild().getText().startsWith("!")) { return Optional.of((OMTBlock)element); } // found the modelitem block
        }
        PsiElement parent = element.getParent();
        return parent != null ? getModelItemBlock(parent) : Optional.empty();
    }

}
