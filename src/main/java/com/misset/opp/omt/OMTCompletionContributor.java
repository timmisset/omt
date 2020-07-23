package com.misset.opp.omt;

import com.intellij.codeInsight.completion.*;
        import com.intellij.codeInsight.lookup.LookupElementBuilder;
        import com.intellij.patterns.PlatformPatterns;
        import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.psi.OMTTypes;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.util.VariableUtil;
import org.jetbrains.annotations.NotNull;

public class OMTCompletionContributor extends CompletionContributor {

    public OMTCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(OMTTypes.VARIABLE_NAME),
                new CompletionProvider<CompletionParameters>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {
                        if(!VariableUtil.isVariableDeclare(
                                (OMTVariable)parameters.getPosition().getParent()
                        )) {
                            VariableUtil.getDeclaredVariables(parameters.getPosition())
                                    .forEach(variable -> resultSet.addElement(LookupElementBuilder.create(variable.getText())));
                        }

                    }
                }
        );
    }
}
