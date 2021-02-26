// This is a generated file. Not intended for manual editing.
package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.named.NamedMemberType;
import org.jetbrains.annotations.NotNull;

public abstract class OMTModelItemLabelImpl extends MemberNamedElementImpl<OMTModelItemLabel> implements OMTModelItemLabel {

  public OMTModelItemLabelImpl(@NotNull ASTNode node) {
    super(node, OMTModelItemLabel.class);
  }

  @Override
  @NotNull
  public String getName() {
    return getPropertyLabel().getName();
  }

  @Override
  public PsiElement setName(@NotNull String newName) {
      ((OMTPropertyLabelImpl) getPropertyLabel()).setName(newName);
      return this;
  }

  @Override
  @NotNull
  public PsiElement getNameIdentifier() {
    return getPropertyLabel();
  }

  @NotNull
  @Override
  public NamedMemberType getType() {
    return NamedMemberType.ModelItem;
  }
}
