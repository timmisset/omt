package com.misset.opp.omt.formatter;

import com.intellij.application.options.CodeStyle;
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTJdComment;
import com.misset.opp.omt.psi.OMTSequenceItem;
import com.misset.opp.omt.psi.OMTTypes;
import com.misset.opp.omt.settings.OMTCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.omt.util.UtilManager.getModelUtil;

public class OMTEnterTypedHandler extends EnterHandlerDelegateAdapter {

    private PsiDocumentManager documentManager = null;

    private PsiElement elementAtCaretOnEnter;
    private IElementType nodeTypeAtCaretOnEnter;

    @Override
    public Result postProcessEnter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull DataContext dataContext) {

        if (elementAtCaretOnEnter == null) {
            return Result.Continue;
        }
        editor.getCaretModel().getOffset();
        documentManager = PsiDocumentManager.getInstance(file.getProject());
        int caretOffset = editor.getCaretModel().getOffset();

        // get the usuable element at the caret
        PsiElement elementAt = getElementAtCaret(file, caretOffset);

        if (isInJavaDocs(elementAt)) {
            insert(editor, " * ", caretOffset);
            if (addJavaDocsClosure()) {
                insert(editor, "\n */", caretOffset + 3, false);
            }
            return Result.Stop;
        }
        if (setBullet()) {
            insert(editor, "-" + getAfterSequenceBulletSpacing(file), caretOffset);
            return Result.Stop;
        }

        return Result.Continue;
    }

    private boolean addJavaDocsClosure() {
        return PsiTreeUtil.getParentOfType(elementAtCaretOnEnter, OMTJdComment.class) == null;
    }

    private boolean isInJavaDocs(PsiElement elementAt) {
        return TokenSet.create(OMTTypes.JAVADOCS_START, OMTTypes.JAVADOCS_CONTENT, OMTTypes.JD_COMMENT, OMTTypes.JD_CONTENT)
                .contains(nodeTypeAtCaretOnEnter) ||
                (elementAt != null && PsiTreeUtil.getParentOfType(elementAt, OMTJdComment.class) != null);
    }

    private boolean setBullet() {
        return getModelUtil().isImportNode(elementAtCaretOnEnter.getNode()) ||
                getModelUtil().isSequenceNode(elementAtCaretOnEnter.getNode()) || isSequenceItem();
    }

    private boolean isAtBullet() {
        return elementAtCaretOnEnter != null &&
                elementAtCaretOnEnter.getNode().getElementType() == OMTTypes.SEQUENCE_BULLET;
    }

    private boolean isSequenceItem() {
        final OMTSequenceItem parentOfType = PsiTreeUtil.getParentOfType(elementAtCaretOnEnter, OMTSequenceItem.class);
        return
                // the entry of a sequence item
                parentOfType != null &&
                        // but only as shortcut or single value
                        PsiTreeUtil.findChildOfType(parentOfType, OMTBlock.class) == null;
    }

    private String getAfterSequenceBulletSpacing(PsiFile file) {
        final OMTCodeStyleSettings codeStyleSettings = CodeStyle.getCustomSettings(file, OMTCodeStyleSettings.class);
        if (codeStyleSettings.INDENT_AFTER_SEQUENCE_VALUE) {
            final CommonCodeStyleSettings languageSettings = CodeStyle.getLanguageSettings(file);
            if (languageSettings.getIndentOptions() == null) {
                return " ";
            }
            final int indent_size = languageSettings.getIndentOptions().INDENT_SIZE;
            return StringUtil.repeat(" ", indent_size - 1);
        }
        return " ";

    }

    private PsiElement getElementAtCaret(PsiFile file, int caretOffset) {
        PsiElement elementAt = file.findElementAt(caretOffset);
        while (caretOffset > 0 && elementAt == null) {
            caretOffset -= 1;
            elementAt = file.findElementAt(caretOffset);
        }
        if (elementAt != null && OMTTokenSets.WHITESPACE.contains(elementAt.getNode().getElementType())) {
            elementAt = PsiTreeUtil.prevLeaf(elementAt);
        }
        return elementAt;
    }

    @Override
    public Result preprocessEnter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull Ref<Integer> caretOffset, @NotNull Ref<Integer> caretAdvance, @NotNull DataContext dataContext, EditorActionHandler originalHandler) {
        elementAtCaretOnEnter = getElementAtCaret(file, caretOffset.get());
        nodeTypeAtCaretOnEnter = elementAtCaretOnEnter != null ? elementAtCaretOnEnter.getNode().getElementType() : null;

        if (isAtBullet()) {
            editor.getDocument().replaceString(
                    elementAtCaretOnEnter.getTextOffset(),
                    caretOffset.get(),
                    StringUtil.repeat(" ", caretOffset.get() - elementAtCaretOnEnter.getTextOffset())
            );
            return Result.Stop;
        }
        return Result.Continue;
    }

    private void insert(Editor editor, String token, int caretOffset) {
        insert(editor, token, caretOffset, true);
    }

    private void insert(Editor editor, String text, int caretOffset, boolean moveToEndOfInsertedText) {
        final Document document = editor.getDocument();
        documentManager.doPostponedOperationsAndUnblockDocument(document);
        document.insertString(caretOffset, text);
        documentManager.commitDocument(document);
        if (moveToEndOfInsertedText) {
            editor.getCaretModel().moveToOffset(caretOffset + text.length());
        }
    }
}
