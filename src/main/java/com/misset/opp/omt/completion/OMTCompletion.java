package com.misset.opp.omt.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import com.misset.opp.omt.psi.util.MemberUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.*;

public abstract class OMTCompletion {

    protected final String ATTRIBUTES = "attributes";

    /**
     * The higher the priority number, the higher it gets listed
     */
    protected final int MODEL_ITEM_TYPE_PRIORITY = 13;
    protected final int ATTRIBUTES_PRIORITY = 12;              // model entry attributes
    protected final int EQUATION_PRIORITY = 11;                // the other side of the equation, shows a limited set of options based on the resolved type of the other side
    protected final int CLASSES_PRIORITY = 10;                  // list with available classes
    protected final int PREDICATE_FORWARD_PRIORITY = 9;
    protected final int PREDICATE_REVERSE_PRIORITY = 8;
    protected final int LOCAL_VARIABLE_PRIORITY = 7;
    protected final int DECLARED_VARIABLE_PRIORITY = 6;
    protected final int GLOBAL_VARIABLE_PRIORITY = 5;
    protected final int DEFINED_STATEMENT_PRIORITY = 4;
    protected final int LOCAL_COMMAND_PRIORITY = 3;
    protected final int BUILTIN_MEMBER_PRIORITY = 2;
    protected final int IMPORTABLE_MEMBER_PRIORITY = 1;

    private List<LookupElement> resolvedElements = new ArrayList<>();
    private List<String> resolvedSuggestions = new ArrayList<>();

    public CompletionProvider<CompletionParameters> getCompletionProvider() {
        return null;
    }

    protected void complete(@NotNull CompletionResultSet result) {
        result.addAllElements(resolvedElements);
        resolvedElements.clear();
        resolvedSuggestions.clear();
    }

    protected void addPriorityElement(String text, int priority) {
        addPriorityElement(text, priority, text, (context, item) -> {
        }, null, null);
    }

    protected void addPriorityElement(String text, int priority, String title, InsertHandler<LookupElement> insertHandler,
                                      String tailText, String typeText) {
        if (resolvedSuggestions.contains(title)) {
            return;
        }
        LookupElementBuilder lookupElementBuilder = LookupElementBuilder
                .create(text)
                .withPresentableText(title)
                .withInsertHandler(insertHandler);
        if (text.contains(":")) {
            lookupElementBuilder = lookupElementBuilder.withLookupStrings(
                    Arrays.asList(text.split(":")));
        }
        if (tailText != null) {
            lookupElementBuilder = lookupElementBuilder.withTailText(tailText);
        }
        if (typeText != null) {
            lookupElementBuilder = lookupElementBuilder.withTypeText(typeText, true);
        }
        resolvedElements.add(PrioritizedLookupElement.withPriority(
                lookupElementBuilder, priority
        ));
        resolvedSuggestions.add(title);
    }

    protected void setResolvedElementsForLocalVariables(PsiElement element) {
        getVariableUtil().getLocalVariables(element).forEach(
                (variableName, provider) -> addPriorityElement(variableName, LOCAL_VARIABLE_PRIORITY)
        );
    }

    protected void setResolvedElementsForDeclaredVariables(PsiElement element) {
        getVariableUtil().getDeclaredVariables(element).forEach(
                omtVariable -> addPriorityElement(omtVariable.getName(), DECLARED_VARIABLE_PRIORITY)
        );
    }

    protected void setResolvedElementsForGlobalVariables(PsiElement element) {
        getVariableUtil().getGlobalVariables().forEach(
                omtVariable -> addPriorityElement(omtVariable, DECLARED_VARIABLE_PRIORITY)
        );
    }

    protected void setResolvedElementsForVariables(PsiElement element) {
        setResolvedElementsForLocalVariables(element);
        setResolvedElementsForDeclaredVariables(element);
        setResolvedElementsForGlobalVariables(element);
    }

    protected void setResolvedElementsForDefinedQueries(PsiElement element) {
        final MemberUtil memberUtil = getMemberUtil();
        memberUtil.getAccessibleDefinedStatements(element)
                .stream()
                .filter(OMTDefinedStatement::isQuery)
                .map(
                        definedStatement -> memberUtil.parseDefinedToCallable(definedStatement.getDefineName()).getAsSuggestion()
                ).forEach(
                suggestion -> addPriorityElement(suggestion, DEFINED_STATEMENT_PRIORITY)
        );
    }

    protected void setResolvedElementsForBuiltinOperators() {
        getBuiltinUtil().getBuiltInOperatorsAsSuggestions()
                .forEach(suggestion -> addPriorityElement(suggestion, BUILTIN_MEMBER_PRIORITY));
    }
}
