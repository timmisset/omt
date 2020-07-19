package com.misset.opp.omt.psi.intentions.prefix;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import com.misset.opp.omt.psi.OMTCurie;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.OMTPrefixBlock;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.omt.psi.util.CurieUtil.addPrefix;
import static com.misset.opp.omt.psi.util.CurieUtil.getPrefixBlock;

public class removePrefixIntention {
    /**
     * In case the prefix is defined more than once or not used
     * @param prefix
     * @return
     */
    public static IntentionAction getRemovePrefixIntention(OMTPrefix prefix) {
        return new IntentionAction() {
            @Override
            public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getText() {
                return "Remove prefix declaration";
            }

            @Override
            public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getFamilyName() {
                return "Prefixes";
            }

            @Override
            public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
                return true;
            }

            @Override
            public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
                OMTPrefixBlock prefixBlock = getPrefixBlock(prefix);

                PsiElement sibling = prefix.getPrevSibling();
                while(sibling != null && !sibling.getText().equals("\n")) {
                    sibling = sibling.getPrevSibling();
                }
                if(sibling != null) {
                    prefixBlock.deleteChildRange(sibling, prefix);
                } else {
                    prefix.delete();
                }
                CodeStyleManager.getInstance(project).reformat(prefixBlock);
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }
}
