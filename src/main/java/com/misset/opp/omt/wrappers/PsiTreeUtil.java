package com.misset.opp.omt.wrappers;

import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class PsiTreeUtil {

    public static final PsiTreeUtil SINGLETON = new PsiTreeUtil();

    public @Nullable
    <T extends PsiElement> T getTopmostParentOfType(@Nullable PsiElement element, @NotNull Class<T> aClass) {
        return com.intellij.psi.util.PsiTreeUtil.getTopmostParentOfType(element, aClass);
    }

    public @NotNull
    <T extends PsiElement> List<T> collectParents(@NotNull PsiElement element,
                                                  @NotNull Class<? extends T> parent,
                                                  boolean includeMyself, @NotNull Predicate<? super PsiElement> stopCondition) {
        return com.intellij.psi.util.PsiTreeUtil.collectParents(element, parent, includeMyself, stopCondition);
    }

    public @NotNull
    <T extends PsiElement> List<T> getChildrenOfTypeAsList(@Nullable PsiElement element, @NotNull Class<? extends T> aClass) {
        return com.intellij.psi.util.PsiTreeUtil.getChildrenOfTypeAsList(element, aClass);
    }

    public @Nullable
    PsiElement findFirstParent(@Nullable PsiElement element, Condition<? super PsiElement> condition) {
        return com.intellij.psi.util.PsiTreeUtil.findFirstParent(element, condition);
    }
}
