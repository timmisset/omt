package com.misset.opp.omt;

import com.intellij.codeInsight.completion.*;
        import com.intellij.codeInsight.lookup.LookupElementBuilder;
        import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.psi.OMTTypes;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.util.CurieUtil;
import com.misset.opp.omt.psi.util.OperatorUtil;
import com.misset.opp.omt.psi.util.QueryUtil;
import com.misset.opp.omt.psi.util.VariableUtil;
import org.jetbrains.annotations.NotNull;

public class OMTCompletionContributor extends CompletionContributor {

    public OMTCompletionContributor() {
        // variables
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(OMTTypes.VARIABLE_NAME),
                new CompletionProvider<CompletionParameters>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {
                        // do not autocomplete variable declare statements
                        if(!VariableUtil.isVariableDeclare(
                                (OMTVariable)parameters.getPosition().getParent()
                        )) {
                            VariableUtil.getDeclaredVariables(parameters.getPosition())
                                    .forEach(variable -> resultSet.addElement(LookupElementBuilder.create(variable.getText())));
                        }

                    }
                }
        );
        // operator
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(OMTTypes.OPERATOR),
                new CompletionProvider<CompletionParameters>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {
                        if(QueryUtil.isPartOfQueryStep(parameters.getPosition())) {
                            addQueryStepCompletion(parameters, resultSet);
                        }
                    }
                }
        );
        // whitespace
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(TokenType.WHITE_SPACE),
                new CompletionProvider<CompletionParameters>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {
                        if(QueryUtil.isPartOfQueryStep(parameters.getPosition())) {
                            addQueryStepCompletion(parameters, resultSet);
                        }
                    }
                }
        );
    }
    private void addQueryStepCompletion(@NotNull CompletionParameters parameters,
                                        @NotNull CompletionResultSet resultSet) {
        PsiElement currentElement = parameters.getPosition();
        // all suggested items are:
        // prefixes
        CurieUtil.getAllPrefixes(currentElement)
                .forEach(prefix -> addToResultSet(prefix.getCuriePrefix().getText(), resultSet));
        // operators
        OperatorUtil.getAllAvailableOperators(currentElement)
                .forEach(omtOperator -> addToResultSet(omtOperator.getName(), resultSet));
    }
    private void addToResultSet(String suggestion, CompletionResultSet resultSet) {
        resultSet.addElement(LookupElementBuilder.create(suggestion));
    }
}
