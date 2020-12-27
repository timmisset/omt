// This is a generated file. Not intended for manual editing.
package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.references.ModelItemReference;
import com.misset.opp.omt.psi.util.PsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OMTModelItemAbstract extends MemberNamedElementAbstract<OMTModelItemLabel> implements OMTModelItemLabel {

  public OMTModelItemAbstract(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  @NotNull
  public String getName() {
    return PsiImplUtil.getName(this);
  }

  @Override
  public PsiElement setName(String newName) {
    return PsiImplUtil.setName(this, newName);
  }

  @Override
  @NotNull
  public PsiElement getNameIdentifier() {
    return PsiImplUtil.getNameIdentifier(this);
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
