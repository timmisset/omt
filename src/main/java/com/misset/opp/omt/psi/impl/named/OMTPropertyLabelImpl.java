package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.OMTElementFactory;
import com.misset.opp.omt.psi.OMTPropertyLabel;
import com.misset.opp.omt.psi.references.PropertyLabelReference;
import com.misset.opp.omt.psi.support.OMTLabelledElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OMTPropertyLabelImpl extends NameIdentifierOwnerImpl<OMTPropertyLabel> implements OMTPropertyLabel, OMTLabelledElement {

    public OMTPropertyLabelImpl(@NotNull ASTNode node) {
        super(node, OMTPropertyLabel.class);
    }

    @Nullable
    @Override
    public PsiReference getReference() {
        return new PropertyLabelReference(getPsi(), TextRange.create(0, getPsi().getText().length() - 1));
    }

    @Override
    public PsiElement getLabel() {
        return getPsi();
    }

    @Override
    public String getName() {
        String propertyLabelText = getLabel().getText();
        return propertyLabelText.endsWith(":") ?
                propertyLabelText.substring(0, propertyLabelText.length() - 1) :
                propertyLabelText;
    }

    @Override
    public PsiElement setName(@NotNull String name) {
        String content = String.format("model:\n" +
                "   %s: !ModelItemType", name);
        return replace(OMTElementFactory.fromString(content, OMTPropertyLabel.class, getProject()));
    }


}
