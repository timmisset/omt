package com.misset.opp.omt.psi.intentions.prefix;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.util.CurieUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class RegisterPrefixIntention {
    public static final RegisterPrefixIntention SINGLETON = new RegisterPrefixIntention();
    private CurieUtil curieUtil = CurieUtil.SINGLETON;

    public IntentionAction getRegisterPrefixIntention(OMTNamespacePrefix namespacePrefix, String iri) {
        return new IntentionAction() {
            @Override
            public @Nls(capitalization = Nls.Capitalization.Sentence)
            @NotNull
            String getText() {
                return String.format("Register as %s", iri);
            }

            @Override
            public @NotNull
            @Nls(capitalization = Nls.Capitalization.Sentence)
            String getFamilyName() {
                return "Prefixes";
            }

            @Override
            public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
                return true;
            }

            @Override
            public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
                curieUtil.addPrefixToBlock(
                        namespacePrefix, namespacePrefix.getName(), iri
                );
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }
}
