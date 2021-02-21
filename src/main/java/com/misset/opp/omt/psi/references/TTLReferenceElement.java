package com.misset.opp.omt.psi.references;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.ttl.psi.TTLObject;
import com.misset.opp.ttl.psi.TTLStatement;
import com.misset.opp.ttl.psi.TTLSubject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TTLReferenceElement extends FakePsiElement {

    final PsiElement psiElement;

    public TTLReferenceElement(PsiElement psiElement) {
        this.psiElement = psiElement;
    }

    public static String getSubjectClassIri(TTLObject turtleObject, boolean rawValue) {
        final TTLStatement normalResource = PsiTreeUtil.getParentOfType(turtleObject, TTLStatement.class);
        final TTLSubject subjectClass = PsiTreeUtil.findChildOfType(normalResource, TTLSubject.class);
        if (subjectClass == null || subjectClass.getIri() == null) {
            return "";
        }
        return rawValue ?
                subjectClass.getIri().getText() :
                subjectClass.getIri().getResourceAsString();
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
                return psiElement instanceof TTLObject ?
                        getSubjectClassIri((TTLObject) psiElement, true) + " / " + psiElement.getText() :
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
