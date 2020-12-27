// This is a generated file. Not intended for manual editing.
package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.OMTDefineName;
import com.misset.opp.omt.psi.OMTElementFactory;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.references.DefinedReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OMTDefineNameImpl extends MemberNamedElementImpl<OMTDefineName> implements OMTDefineName {

    public OMTDefineNameImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    @NotNull
    public String getName() {
        return getText();
    }

    @Override
    public PsiElement setName(@NotNull String newName) {
        PsiElement replacement = OMTElementFactory.createOperator(getProject(), newName);
        if (replacement != null) {
            replace(replacement);
        }
        return replacement;
    }

  @Override
  @NotNull
  public PsiElement getNameIdentifier() {
      return this;
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
