package com.misset.opp.omt.psi.intentions.query;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.psi.OMTElementFactory;
import com.misset.opp.omt.psi.OMTQueryFilter;
import com.misset.opp.omt.psi.OMTQueryStep;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.Collectors;

public class MergeFiltersIntention {
    public static IntentionAction getMergeFilterIntention(OMTQueryStep step) {
        return new IntentionAction() {
            @NotNull
            @Override
            public @IntentionName String getText() {
                return "Merge";
            }

            @NotNull
            @Override
            public @IntentionFamilyName String getFamilyName() {
                return "Query";
            }

            @Override
            public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
                return true;
            }

            @Override
            public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
                final String joinedFilter = step.getQueryFilterList().stream()
                        .map(OMTQueryFilter::getQuery)
                        .filter(Objects::nonNull)
                        .map(PsiElement::getText)
                        .collect(Collectors.joining(" AND "));
                final OMTQueryFilter filter = (OMTQueryFilter) OMTElementFactory.fromString(String.format("queries: |\n" +
                        "   DEFINE QUERY query => 'a'[%s];", joinedFilter), OMTQueryFilter.class, project);

                step.getQueryFilterList().get(0).replace(filter);
                while (step.getQueryFilterList().size() > 1) {
                    step.getQueryFilterList().get(1).delete();
                }
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }
}
