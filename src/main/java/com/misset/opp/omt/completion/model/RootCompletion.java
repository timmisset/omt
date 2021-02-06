package com.misset.opp.omt.completion.model;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.completion.OMTCompletion;
import com.misset.opp.omt.completion.OMTCompletionContributor;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTTypes;
import com.misset.opp.omt.psi.support.OMTBlockEntrySup;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.misset.opp.omt.util.UtilManager.getModelUtil;
import static com.misset.opp.omt.util.UtilManager.getProjectUtil;

public class RootCompletion extends OMTCompletion {
    public static void register(OMTCompletionContributor completionContributor) {
        // Completion for document root elements (column position == 0)
        final PsiElementPattern.Capture<PsiElement> propertyPattern =
                PlatformPatterns.psiElement(OMTTypes.PROPERTY)
                        .with(new PatternCondition<>("StartOfLine") {
                                  @Override
                                  public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
                                      final PsiElement psiElement = PsiTreeUtil.prevLeaf(element);
                                      return psiElement == null || psiElement.getText().endsWith("\n");
                                  }
                              }
                        );
        completionContributor.extend(CompletionType.BASIC, propertyPattern, new RootCompletion().getCompletionProvider());
    }

    public CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                final OMTFile omtFile = (OMTFile) parameters.getOriginalFile();
                final OMTBlock root = omtFile.getRoot();
                final List<String> entries = root != null ?
                        root.getBlockEntryList().stream().map(OMTBlockEntrySup::getName).collect(Collectors.toList()) :
                        Collections.emptyList();
                getModelUtil().getRootEntries()
                        .stream()
                        .filter(label -> !entries.contains(label))
                        .forEach(label -> {
                            if (omtFile.isModuleFile() && label.equals("model")) return;
                            if (entries.contains("model") && label.equals("moduleName")) return;
                            addPriorityElement(String.format("%s:", label), ATTRIBUTES_PRIORITY);
                        });
                complete(result);
            }
        };
    }

    private void setReasons() {
        HashMap<String, String> reasons = getProjectUtil().getReasons();
        reasons.forEach((key, value) -> addPriorityElement(
                key, ATTRIBUTES_PRIORITY, Collections.emptyList(), "", value));
    }

}
