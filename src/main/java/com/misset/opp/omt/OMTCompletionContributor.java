package com.misset.opp.omt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.external.util.builtIn.BuiltInUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.util.ModelUtil;
import com.misset.opp.omt.psi.util.VariableUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OMTCompletionContributor extends CompletionContributor {

    private static String DUMMY_SCALAR_VALUE = "DUMMYSCALARVALUE";
    private static String DUMMY_PROPERTY_VALUE = "DUMMYPROPERTYVALUE:";
    private static String DUMMY_STATEMENT = "@DUMMYSTATEMENT();";

    final ModelUtil modelUtil = ModelUtil.SINGLETON;

    private static final VariableUtil variableUtil = VariableUtil.SINGLETON;

    public OMTCompletionContributor() {
        /**
         * Generic completion that resolves the suggestion based on the cursor position
         */
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                        PsiElement atPosition = parameters.getPosition();
                        result.getPrefixMatcher();
                        if (isPropertyLabelSuggestion(atPosition) || isSequenceItem(atPosition)) {
                            addCompletionsForModelTreeAttributes(atPosition, result);
                        }
                        addCompletionsForScript(atPosition, result);
                    }
                });
    }

    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        PsiElement elementAt = context.getFile().findElementAt(context.getCaret().getOffset()).getParent();
        // find the PsiElement containing the carrot:

        if (elementAt instanceof OMTBlock) {
            context.setDummyIdentifier(DUMMY_PROPERTY_VALUE + " " + DUMMY_SCALAR_VALUE);
            return;
        }
        if (elementAt instanceof OMTBlockEntry) {
            context.setDummyIdentifier(DUMMY_PROPERTY_VALUE + " " + DUMMY_SCALAR_VALUE);
            return;
        }
        if (elementAt instanceof OMTCommandBlock) {
            context.setDummyIdentifier(DUMMY_STATEMENT);
            return;
        }
        if (elementAt instanceof OMTFile) {
            context.setDummyIdentifier(DUMMY_PROPERTY_VALUE + " " + DUMMY_SCALAR_VALUE);
            return;
        }

        context.setDummyIdentifier(DUMMY_SCALAR_VALUE);
    }

    private void addCompletionsForScript(PsiElement elementAtPosition, CompletionResultSet resultSet) {
        addCompletionsForVariables(elementAtPosition, resultSet);

        if (isInQueryPath(elementAtPosition)) {
            BuiltInUtil.getBuiltInOperatorsAsSuggestions().forEach(suggestion -> resultSet.addElement(LookupElementBuilder.create(suggestion)));
        } else if (isInCommandBlock(elementAtPosition)) {
            List<String> localCommands = modelUtil.getLocalCommands(elementAtPosition);
            localCommands.forEach(localCommand -> resultSet.addElement(LookupElementBuilder.create("@" + localCommand + "()")));

            BuiltInUtil.getBuiltInCommandsAsSuggestions().forEach(suggestion -> resultSet.addElement(LookupElementBuilder.create(suggestion)));
        }

    }

    private void addCompletionsForModelTreeAttributes(PsiElement elementAtPosition, CompletionResultSet resultSet) {
        OMTBlockEntry blockEntry = (OMTBlockEntry) PsiTreeUtil.findFirstParent(elementAtPosition, parent -> parent instanceof OMTBlockEntry);
        boolean isDummyProperty =
                blockEntry != null && blockEntry.getPropertyLabel() != null &&
                        blockEntry.getPropertyLabel().getText().equals(DUMMY_PROPERTY_VALUE);

        JsonObject json = modelUtil.getJson(isDummyProperty ? blockEntry.getParent() : elementAtPosition);

        if (json != null) {
            if (json.has("attributes")) {
                JsonObject attributes = json.getAsJsonObject("attributes");
                attributes.keySet().forEach(attribute ->
                        resultSet.addElement(LookupElementBuilder.create(attribute + ":")));
            }
            if (json.has("variables")) {
                JsonArray variables = json.getAsJsonArray("variables");
                variables.forEach(variable ->
                        resultSet.addElement(LookupElementBuilder.create(variable.getAsString())));
            }
        }
    }

    private void addCompletionsForVariables(PsiElement elementAtPosition, CompletionResultSet resultSet) {
        List<OMTVariable> declaredVariables = variableUtil.getDeclaredVariables(elementAtPosition);
        declaredVariables.forEach(variable -> resultSet.addElement(LookupElementBuilder.create(variable.getText())));
    }

    private boolean isPropertyLabelSuggestion(PsiElement element) {
        return element.getParent() instanceof OMTBlockEntry ||
                element.getParent() instanceof OMTPropertyLabel ||
                element.getParent() instanceof OMTBlock;
    }

    private boolean isSequenceItem(PsiElement element) {
        return PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTSequenceItem) != null;
    }

    private boolean isInCommandBlock(PsiElement element) {
        return PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTScript || parent instanceof OMTCommandBlock) != null;
    }

    private boolean isInQueryPath(PsiElement element) {
        return PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTQueryPath) != null;
    }
}
