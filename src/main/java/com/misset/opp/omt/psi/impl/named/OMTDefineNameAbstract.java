// This is a generated file. Not intended for manual editing.
package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.OMTDefineName;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.references.DefinedReference;
import com.misset.opp.omt.psi.util.PsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OMTDefineNameAbstract extends MemberNamedElementAbstract<OMTDefineName> implements OMTDefineName {

  public OMTDefineNameAbstract(@NotNull ASTNode node) {
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
    return new DefinedReference(getPsi(), getTextRangeInParent());
  }

  @NotNull
  @Override
  public NamedMemberType getType() {
    return NamedMemberType.DefineName;
  }
}
