package com.misset.opp.omt;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.util.TokenUtil;
import org.jetbrains.annotations.NotNull;

public class OMTEnterTypedHandler extends EnterHandlerDelegateAdapter {

    private TokenUtil tokenUtil = TokenUtil.SINGLETON;

    @Override
    public Result postProcessEnter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull DataContext dataContext) {
        if (!(file instanceof OMTFile)) {
            return Result.Continue;
        }
        int caretOffset = editor.getCaretModel().getOffset();
        PsiElement elementAt = file.findElementAt(caretOffset);
        while (elementAt == null && caretOffset > 0) {
            caretOffset -= 1;
            elementAt = file.findElementAt(caretOffset);
        }
        caretOffset = editor.getCaretModel().getOffset();

        if (PsiTreeUtil.findFirstParent(elementAt, parent -> parent instanceof OMTJdComment) != null) {
            insert(editor, "*", 1, caretOffset);
            return Result.Continue;
        }

        PsiElement sibling = elementAt;
        while (sibling != null && tokenUtil.isWhiteSpace(sibling)) {
            sibling = sibling.getPrevSibling();
        }
        if (sibling instanceof OMTImport || sibling instanceof OMTSequence) {
            insert(editor, "-", getSequenceBulletTrailingSpace(sibling), caretOffset);
            return Result.Stop;
        }

        if (sibling != null && tokenUtil.isOperator(sibling)) {
            final PsiElement firstNonWhiteTokenSibling = getFirstNonWhiteTokenSibling(sibling.getPrevSibling());
            if (firstNonWhiteTokenSibling != null && tokenUtil.isSequenceBullet(firstNonWhiteTokenSibling)) {
                insert(editor, "-", getSequenceBulletTrailingSpace(firstNonWhiteTokenSibling.getParent()), caretOffset);
                return Result.Stop;
            }
        }
        return Result.Continue;
    }


    private void insert(Editor editor, String token, int indentationAfter, int caretOffset) {
        String insertedString = String.format("%s%s",
                token,
                new String(new char[indentationAfter]).replace("\0", " "));
        editor.getDocument().insertString(caretOffset, insertedString);
        editor.getCaretModel().moveToOffset(caretOffset + insertedString.length());
    }

    private PsiElement getFirstNonWhiteTokenSibling(PsiElement element) {
        PsiElement sibling = element;
        while (sibling != null && tokenUtil.isWhiteSpace(sibling)) {
            sibling = sibling.getPrevSibling();
        }
        return sibling;
    }

    private int getSequenceBulletTrailingSpace(PsiElement container) {
        final OMTMemberListItem sequenceItem = PsiTreeUtil.findChildOfType(container, OMTMemberListItem.class);
        if (sequenceItem == null) {
            return 3;
        } // default indentation of 4 - 1 for the leading "-"
        final PsiElement whiteSpace = sequenceItem.getFirstChild() != null ? sequenceItem.getFirstChild().getNextSibling() : null;
        if (whiteSpace != null && tokenUtil.isWhiteSpace(whiteSpace)) {
            return whiteSpace.getTextLength();
        }

        return 3;
    }

}
