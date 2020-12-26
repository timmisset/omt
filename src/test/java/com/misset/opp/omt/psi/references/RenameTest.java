package com.misset.opp.omt.psi.references;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.OMTTestSuite;

public class RenameTest extends OMTTestSuite {

    private Class<? extends PsiElement> elementAtCaretClass;

    /**
     * @param elementAtCaretClass Will get the parent (or self) of class for the element at caret position
     * @throws Exception
     */
    protected void setUp(Class<? extends PsiElement> elementAtCaretClass) throws Exception {
        super.setUp();
        this.elementAtCaretClass = elementAtCaretClass;
    }

    protected void renameElement(String content, String newName) {
        getElementAtCaret(content, element ->
                        WriteCommandAction.runWriteCommandAction(getProject(), () -> myFixture.renameElementAtCaret(newName))
                , elementAtCaretClass, false);
    }

}
