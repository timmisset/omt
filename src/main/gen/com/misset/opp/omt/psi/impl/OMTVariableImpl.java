// This is a generated file. Not intended for manual editing.
package com.misset.opp.omt.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.misset.opp.omt.psi.OMTTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.misset.opp.omt.psi.*;

public class OMTVariableImpl extends ASTWrapperPsiElement implements OMTVariable {

  public OMTVariableImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull OMTVisitor visitor) {
    visitor.visitVariable(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof OMTVisitor) accept((OMTVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public OMTVariableType getVariableType() {
    return findChildByClass(OMTVariableType.class);
  }

  @Override
  @Nullable
  public OMTVariableValue getVariableValue() {
    return findChildByClass(OMTVariableValue.class);
  }

}
