//package com.misset.opp.omt.psi.intentions.prefix;
//
//import com.intellij.codeInsight.intention.IntentionAction;
//import com.intellij.openapi.editor.Editor;
//import com.intellij.openapi.project.Project;
//import com.intellij.psi.PsiFile;
//import com.misset.opp.omt.domain.OMTCurie;
//import org.jetbrains.annotations.Nls;
//import org.jetbrains.annotations.NotNull;
//
//import static com.misset.opp.omt.domain.util.CurieUtil.addPrefix;
//import static com.misset.opp.omt.domain.util.CurieUtil.getPrefixBlock;
//
//public class registerPrefixIntention {
//    /**
//     * In case the curie is using a prefix which is not defined, provide a way to fix it
//     * @param curie
//     * @return
//     */
//    public static IntentionAction getRegisterPrefixIntention(OMTCurie curie) {
//        return new IntentionAction() {
//            @Override
//            public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getText() {
//                return "Register prefix";
//            }
//
//            @Override
//            public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getFamilyName() {
//                return "Prefixes";
//            }
//
//            @Override
//            public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
//                return true;
//            }
//
//            @Override
//            public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
//                addPrefix(curie.getCuriePrefix(), "", getPrefixBlock(curie.getElement(), true));
//            }
//
//            @Override
//            public boolean startInWriteAction() {
//                return true;
//            }
//        };
//    }
//}
