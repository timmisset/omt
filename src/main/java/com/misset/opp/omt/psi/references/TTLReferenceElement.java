package com.misset.opp.omt.psi.references;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lnkd.editor.intellij.turtle.psi.TurtleNormalResource;
import tech.lnkd.editor.intellij.turtle.psi.TurtleObject;
import tech.lnkd.editor.intellij.turtle.psi.TurtleSubject;

import javax.swing.*;
import java.util.Objects;

public class TTLReferenceElement extends FakePsiElement {

    final PsiElement psiElement;

    public TTLReferenceElement(PsiElement psiElement) {
        this.psiElement = psiElement;
    }

    public static String getSubjectClassIri(TurtleObject turtleObject, boolean rawValue) {
        final TurtleNormalResource normalResource = PsiTreeUtil.getParentOfType(turtleObject, TurtleNormalResource.class);
        final TurtleSubject subjectClass = PsiTreeUtil.findChildOfType(normalResource, TurtleSubject.class);
        if (subjectClass == null || subjectClass.getRef() == null) {
            return "";
        }
        return rawValue ?
                subjectClass.getRef().rawValue() :
                Objects.requireNonNull(subjectClass.getRef().decompiledValue()).iri();
    }

    @NotNull
    @Override
    public PsiElement getNavigationElement() {
        return psiElement.getNavigationElement();
    }

    @Override
    public PsiElement getParent() {
        return psiElement.getParent();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return psiElement instanceof TurtleObject ?
                        getSubjectClassIri((TurtleObject) psiElement, true) + " / " + psiElement.getText() :
                        psiElement.getText();
            }

            @NotNull
            @Override
            public String getLocationString() {
                return psiElement.getContainingFile().getName();
            }

            @Nullable
            @Override
            public Icon getIcon(boolean b) {
                return psiElement.getIcon(0);
            }
        };
    }

}
