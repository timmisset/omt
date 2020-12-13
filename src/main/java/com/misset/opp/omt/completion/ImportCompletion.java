package com.misset.opp.omt.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTImport;
import com.misset.opp.omt.psi.OMTMember;
import com.misset.opp.omt.psi.OMTMemberList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.util.UtilManager.getImportUtil;

public class ImportCompletion extends RDFCompletion {

    public static void register(OMTCompletionContributor completionContributor) {

        completionContributor.extend(CompletionType.BASIC, PlatformPatterns.psiElement().inside(IMPORT_MEMBER_PATTERN),
                new ImportCompletion().getCompletionProvider());
    }

    public CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                // The first step of the query will suggest starting points of the query
                PsiElement element = parameters.getPosition();
                OMTMemberList memberList = (OMTMemberList) PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTMemberList);
                OMTImport omtImport = memberList != null ? (OMTImport) memberList.getParent() :
                        (OMTImport) PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTImport);

                final OMTFile file = getImportUtil().getFile(omtImport, parameters.getOriginalFile());
                if (file == null) {
                    return;
                }

                // filter out existing imports
                final List<String> existingImports = memberList == null ? new ArrayList<>() :
                        PsiTreeUtil.findChildrenOfType(memberList, OMTMember.class).stream()
                                .map(OMTMember::getName).collect(Collectors.toList());

                file.getExportedMembers().values().stream()
                        .filter(exportMember -> !existingImports.contains(exportMember.getName()))
                        .forEach(exportMember -> addPriorityElement(exportMember.getName(), IMPORTABLE_MEMBER_PRIORITY));

                complete(result);
            }
        };

    }
}
