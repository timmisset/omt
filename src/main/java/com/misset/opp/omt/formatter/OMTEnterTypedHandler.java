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
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.OMTLanguage;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTJdComment;
import com.misset.opp.omt.psi.OMTMemberListItem;
import com.misset.opp.omt.psi.OMTTypes;
import com.misset.opp.omt.psi.support.OMTTokenSets;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.misset.opp.omt.psi.util.UtilManager.getTokenFinderUtil;
import static com.misset.opp.omt.psi.util.UtilManager.getTokenUtil;

public class OMTEnterTypedHandler extends EnterHandlerDelegateAdapter {

    private PsiDocumentManager documentManager = null;

    private PsiElement elementAtCaretOnEnter;
    private IElementType nodeTypeAtCaretOnEnter;

    @Override
    public Result postProcessEnter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull DataContext dataContext) {
        if (!(file instanceof OMTFile)) { // disable for now, doesn't seem to work with the formatted blocks
            return Result.Continue;
        }
        editor.getCaretModel().getOffset();
        documentManager = PsiDocumentManager.getInstance(file.getProject());
        int caretOffset = editor.getCaretModel().getOffset();

        indentationOnNewLineInRootLevelCorrection(file, editor, caretOffset);

        // get the usuable element at the caret
        PsiElement elementAt = getElementAtCaret(file, caretOffset);

        if (isInJavaDocs(elementAt)) {
            insert(editor, "* ", 0, caretOffset);
            return Result.Stop;
        }

        PsiElement sibling = elementAt;
        while (sibling != null && getTokenUtil().isWhiteSpace(sibling)) {
            sibling = sibling.getPrevSibling();
        }
        if (getTokenUtil().isSequenceItemContainer(elementAt) && !hasEmptySequenceItem(elementAt)) {
            insert(editor, "-", getSequenceBulletTrailingSpace(sibling), caretOffset);
            assureIndentation(elementAt, editor.getDocument(), caretOffset);
            return Result.Stop;
        }

        return Result.Continue;
    }

    /**
     * This method is required to correct for indentation on empty lines after a root level key:
     * import:<caret>
     * is anchoring to the PsiFile instead of the block which causes a space(0) indentation
     *
     * @param file
     * @param editor
     * @param caretOffset
     */
    private void indentationOnNewLineInRootLevelCorrection(PsiFile file, Editor editor, int caretOffset) {
        // check for indentation in new line
        int lineNumber = editor.getDocument().getLineNumber(caretOffset);
        int lineOffset = editor.getDocument().getLineStartOffset(lineNumber);
        int previousLineOffset = editor.getDocument().getLineStartOffset(lineNumber - 1);
        if (!isInJavaDocs(null) && lineOffset == caretOffset && lineOffset - previousLineOffset > 1) {
            // this is an issue with rootblock indentation, something wrong in the block formation probably
            // but I can't seem to find the cause. For now, add the indentation manually:
            int indentSize = Objects.requireNonNull(CodeStyle.getLanguageSettings(file, OMTLanguage.INSTANCE).getIndentOptions()).INDENT_SIZE;
            editor.getDocument().insertString(caretOffset, StringUtil.repeat(" ", indentSize));
            editor.getCaretModel().moveToOffset(caretOffset + indentSize);
        }
    }

    private boolean isInJavaDocs(PsiElement elementAt) {
        return TokenSet.create(OMTTypes.JAVADOCS_START, OMTTypes.JAVADOCS_CONTENT, OMTTypes.JD_COMMENT, OMTTypes.JD_CONTENT)
                .contains(nodeTypeAtCaretOnEnter) ||
                (elementAt != null && PsiTreeUtil.findFirstParent(elementAt, parent -> parent instanceof OMTJdComment) != null);
    }

    private PsiElement getElementAtCaret(PsiFile file, int caretOffset) {
        PsiElement elementAt = file.findElementAt(caretOffset);
        while (caretOffset > 0 && elementAt == null) {
            caretOffset -= 1;
            elementAt = file.findElementAt(caretOffset);
        }
        while (elementAt != null && OMTTokenSets.WHITESPACE.contains(elementAt.getNode().getElementType())) {
            elementAt = elementAt.getParent();
        }
        return elementAt;
    }


    @Override
    public Result preprocessEnter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull Ref<Integer> caretOffset, @NotNull Ref<Integer> caretAdvance, @NotNull DataContext dataContext, EditorActionHandler originalHandler) {
        // TODO: Check for sequence bullet indentation
        elementAtCaretOnEnter = getElementAtCaret(file, caretOffset.get());
        nodeTypeAtCaretOnEnter = elementAtCaretOnEnter != null ? elementAtCaretOnEnter.getNode().getElementType() : null;
        return Result.Continue;
    }

    private void insert(Editor editor, String token, int spacesAfterInsert, int caretOffset) {
        final Document document = editor.getDocument();
        documentManager.doPostponedOperationsAndUnblockDocument(document);
        String insertedString = String.format("%s%s",
                token,
                StringUtil.repeat(" ", spacesAfterInsert));

        document.insertString(caretOffset, insertedString);
        documentManager.commitDocument(document);
        editor.getCaretModel().moveToOffset(caretOffset + insertedString.length());
    }

    private int getSequenceBulletTrailingSpace(PsiElement container) {
        final OMTMemberListItem sequenceItem = PsiTreeUtil.findChildOfType(container, OMTMemberListItem.class);
        if (sequenceItem == null) {
            return 3;
        } // default indentation of 4 - 1 for the leading "-"
        final PsiElement whiteSpace = sequenceItem.getFirstChild() != null ? sequenceItem.getFirstChild().getNextSibling() : null;
        if (whiteSpace != null && getTokenUtil().isWhiteSpace(whiteSpace)) {
            return whiteSpace.getTextLength();
        }

        return 3;
    }

    private void assureIndentation(PsiElement anchor, Document document, int caretOffset) {
        final int containerOffset = getTokenFinderUtil().getLineOffset(anchor, document);
        final int lineOffset = getTokenFinderUtil().getLineOffset(caretOffset, document);
        if (lineOffset < containerOffset) {
            document.insertString(caretOffset, StringUtil.repeat(" ", containerOffset - lineOffset));
        }
    }

    private boolean hasEmptySequenceItem(PsiElement container) {
        final String text = container.getText().trim();
        return text.length() > 0 && text.endsWith("-");
    }
}
