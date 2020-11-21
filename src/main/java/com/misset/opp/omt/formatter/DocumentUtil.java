package com.misset.opp.omt.formatter;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleManager;

public class DocumentUtil {

    public static void insertText(Project project,
                                  Editor editor,
                                  String text,
                                  int offset,
                                  int navigateToPosAfterInsertedText,
                                  PsiElement reformat) {
        Document document = editor.getDocument();
        final PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        documentManager.doPostponedOperationsAndUnblockDocument(document);
        String insertedString = String.format("%s%s",
                text,
                navigateToPosAfterInsertedText > 0 ?
                        new String(new char[navigateToPosAfterInsertedText]).replace("\0", " ") :
                        "");
        document.insertString(offset, insertedString);
        editor.getCaretModel().moveToOffset(offset + insertedString.length());
        documentManager.commitDocument(document);
        if (reformat != null) {
            CodeStyleManager.getInstance(project).reformat(reformat);
        }
    }

}
