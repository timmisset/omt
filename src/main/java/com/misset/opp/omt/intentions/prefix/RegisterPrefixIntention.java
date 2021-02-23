package com.misset.opp.omt.intentions.prefix;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.util.UtilManager.getCurieUtil;

public class RegisterPrefixIntention {

    public static IntentionAction getRegisterPrefixIntention(OMTNamespacePrefix namespacePrefix, String iri) {
        return new IntentionAction() {
            @Override
            @NotNull
            public String getText() {
                return String.format("Register as %s", iri);
            }

            @Override
            @NotNull
            public String getFamilyName() {
                return "Prefixes";
            }

            @Override
            public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
                return true;
            }

            @Override
            public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
                getCurieUtil().addPrefixToBlock(
                        namespacePrefix, namespacePrefix.getName().trim(), iri
                );
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }
}
