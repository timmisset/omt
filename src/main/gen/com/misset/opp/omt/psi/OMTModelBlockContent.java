// This is a generated file. Not intended for manual editing.
package com.misset.opp.omt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface OMTModelBlockContent extends PsiElement {

  @NotNull
  List<OMTListItem> getListItemList();

  @NotNull
  List<OMTPrefix> getPrefixList();

  @NotNull
  List<OMTProperty> getPropertyList();

  @NotNull
  List<OMTQuery> getQueryList();

}
