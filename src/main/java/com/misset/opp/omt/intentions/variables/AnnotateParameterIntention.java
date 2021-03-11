package com.misset.opp.omt.intentions.variables;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTDefineParam;
import com.misset.opp.omt.psi.OMTElementFactory;
import com.misset.opp.omt.psi.OMTJdComment;
import com.misset.opp.omt.psi.OMTLeading;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import org.jetbrains.annotations.NotNull;

public class AnnotateParameterIntention {

    public IntentionAction getAnnotateParameterIntention(OMTDefineParam defineParam, String variableName, String type) {
        return new IntentionAction() {
            @Override
            @NotNull
            public String getText() {
                return String.format("Add annotation as %s", type);
            }

            @Override
            @NotNull
            public String getFamilyName() {
                return "Parameters";
            }

            @Override
            public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
                return true;
            }

            @Override
            public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
                OMTDefinedStatement statement = (OMTDefinedStatement) defineParam.getParent();

                final int leadingSpaceLength = getLeadingSpaceLength(statement, editor.getDocument());
                final String whiteSpace = OMTElementFactory.getWhiteSpace(leadingSpaceLength, " ");
                final OMTLeading leading = statement.getLeading();
                if (leading == null || PsiTreeUtil.findChildOfType(leading, OMTJdComment.class) == null) {
                    // no javadocs present:
                    String comment = String.format("/**%n" +
                            "%s* @param %s (%s)%n" +
                            "%s*/", whiteSpace, variableName, type, whiteSpace);
                    insertAnnotation(comment, statement, null, whiteSpace, editor.getDocument(), project);
                } else {
                    // add to existing block:
                    final OMTJdComment jdComment = PsiTreeUtil.findChildOfType(leading, OMTJdComment.class);
                    if (jdComment != null) {
                        String comment = jdComment.getText().replace("*/",
                                String.format("* @param %s (prefix:Type)%n" +
                                        "%s*/", variableName, whiteSpace));
                        insertAnnotation(comment, statement, jdComment, whiteSpace, editor.getDocument(), project);
                    }
                }
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }

    private int getLeadingSpaceLength(OMTDefinedStatement definedStatement, Document document) {
        final int lineNumber = document.getLineNumber(definedStatement.getTextOffset());
        final int lineStartOffset = document.getLineStartOffset(lineNumber);
        return definedStatement.getTextOffset() - lineStartOffset;
    }

    private void insertAnnotation(String comment, OMTDefinedStatement statement, OMTJdComment existingComment, String whiteSpace, Document document, Project project) {
        String template = String.format("queries:|%n" +
                "   %s%n" +
                "   DEFINE QUERY test() => '';%n" +
                "%n", comment);
        template = template.replace("\r\n", "\n");

        final OMTJdComment commentBlock = (OMTJdComment) OMTElementFactory.fromString(template, OMTJdComment.class, statement.getProject());
        if (existingComment == null) {
            statement.addBefore(commentBlock, statement.getFirstChild());
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);
            document.insertString(statement.getDefineLabel().getTextOffset(), "\n" + whiteSpace); // final padding with spaces
            PsiDocumentManager.getInstance(project).commitDocument(document);
        } else {
            existingComment.replace(commentBlock);
        }

    }

}
