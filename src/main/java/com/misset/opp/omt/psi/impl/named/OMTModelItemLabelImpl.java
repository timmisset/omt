// This is a generated file. Not intended for manual editing.
package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.OMTElementFactory;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.references.ModelItemReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OMTModelItemLabelImpl extends MemberNamedElementImpl<OMTModelItemLabel> implements OMTModelItemLabel {

  public OMTModelItemLabelImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  @NotNull
  public String getName() {
    return getPropertyLabel().getName();
  }

  @Override
  public PsiElement setName(@NotNull String newName) {
    return replace(OMTElementFactory.createModelItemLabelPropertyLabel(getProject(), newName, getModelItemTypeElement().getText()));
  }

  @Override
  @NotNull
  public PsiElement getNameIdentifier() {
    return getPropertyLabel();
  }

  @Nullable
  @Override
  public PsiReference getReference() {
    return new ModelItemReference(getPsi(), getNameIdentifier().getTextRangeInParent());
  }

  @NotNull
  @Override
  public NamedMemberType getType() {
    return NamedMemberType.ModelItem;
  }
}
