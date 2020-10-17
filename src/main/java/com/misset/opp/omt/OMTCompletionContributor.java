package com.misset.opp.omt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.external.util.builtIn.BuiltInUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.util.ModelUtil;
import com.misset.opp.omt.psi.util.ProjectUtil;
import com.misset.opp.omt.psi.util.TokenUtil;
import com.misset.opp.omt.psi.util.VariableUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OMTCompletionContributor extends CompletionContributor {

    private static String DUMMY_SCALAR_VALUE = "DUMMYSCALARVALUE";
    private static String DUMMY_PROPERTY_VALUE = "DUMMYPROPERTYVALUE:";
    private static String DUMMY_ENTRY_VALUE = String.format("%s %s", DUMMY_PROPERTY_VALUE, DUMMY_SCALAR_VALUE);
    private static String DUMMY_STATEMENT = "@DUMMYSTATEMENT();";
    private static String SEMICOLON = ";";

    private static String usedContextPlaceHolder = "";

    private static String ATTRIBUTES = "attributes";

    private PsiElement elementAtCaret;
    List<LookupElement> resolvedElements;
    private boolean alreadyResolved;

    final ModelUtil modelUtil = ModelUtil.SINGLETON;

    private VariableUtil variableUtil = VariableUtil.SINGLETON;
    private BuiltInUtil builtInUtil = BuiltInUtil.SINGLETON;
    private ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    private TokenUtil tokenUtil = TokenUtil.SINGLETON;

    public OMTCompletionContributor() {
        /**
         * Generic completion that resolves the suggestion based on the cursor position
         */
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                        if (alreadyResolved) {
                            result.addAllElements(resolvedElements);
                            return;
                        }

                        PsiElement element = parameters.getPosition();
                        while (tokenUtil.isWhiteSpace(element) && element != null) {
                            element = element.getParent();
                        }
                        // !ModelItemType
                        if (tokenUtil.isModelItemType(element)) {
                            modelUtil.getModelRootItems().forEach(label -> result.addElement(LookupElementBuilder.create(label)));
                        }
                        // entry: for a model item
                        if (tokenUtil.isProperty(element)) {
                            addJsonAttributes(element, result);
                        }
                        if (tokenUtil.isCommand(element)) {
                            setResolvedElementsForCommand(element);
                            addResolvedElementsToCompletionWithPrefix(result, element.getText());
                            return;
                        }

//                        if (isPropertyLabelSuggestion(atPosition) || isSequenceItem(atPosition)) {
//                            addCompletionsForModelTreeAttributes(atPosition, result);
//                        }
//                        addCompletionsForScript(atPosition, result);
                    }
                });
    }

    private void addResolvedElementsToCompletionWithPrefix(CompletionResultSet resultSet, String prefixIfEmpty) {
        resultSet.withPrefixMatcher(prefixIfEmpty).addAllElements(resolvedElements);
    }

    // add completion in the OMT model based on the Json attributes
    private void addJsonAttributes(PsiElement element, @NotNull CompletionResultSet result) {
        JsonObject json = modelUtil.getJsonAtDepth(element, modelUtil.getModelDepth(element) - 1);
        List<String> existingSiblingEntryLabels = getExistingSiblingEntryLabels(element);
        if (json != null && json.has(ATTRIBUTES)) {
            json.getAsJsonObject(ATTRIBUTES).keySet()
                    .stream()
                    .filter(key -> !existingSiblingEntryLabels.contains(key))
                    .forEach(key -> result.addElement(LookupElementBuilder.create(
                            String.format("%s:", key).trim()
                            ))
                    );
        }
    }

    // used to fetch the entries already available for the item that is targeted by the completion
    private List<String> getExistingSiblingEntryLabels(PsiElement element) {
        List<String> entryList = new ArrayList<>();
        PsiElement container = PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTBlock);
        if (container == null) {
            return entryList;
        }

        List<OMTBlockEntry> childrenOfTypeAsList = PsiTreeUtil.getChildrenOfTypeAsList(container, OMTBlockEntry.class);
        childrenOfTypeAsList.forEach(blockEntry -> entryList.add(modelUtil.getEntryBlockLabel(blockEntry)));
        return entryList;
    }

    private void setDummyPlaceHolder(String placeHolder, @NotNull CompletionInitializationContext context) {
        usedContextPlaceHolder = placeHolder;
        context.setDummyIdentifier(placeHolder);
    }

    /**
     * This method is used to check if the current caret position can be used to determine the type of completion
     * that can be expected. Since IntelliJ will actually parse the document with a placeholder and resolve that
     * placeholder to a PsiElement, the correct template must be used or the Lexer or Grammar will throw
     *
     * @param context
     */
    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        alreadyResolved = false;
        resolvedElements = new ArrayList<>();

        PsiElement elementAtCaret = context.getFile().findElementAt(context.getCaret().getOffset());
        if (elementAtCaret == null) {
            // no element at caret, use the DUMMY_ENTRY_VALUE
            setDummyPlaceHolder(DUMMY_ENTRY_VALUE, context);
            return;
        }
        PsiElement elementAt = elementAtCaret.getParent();

        if (elementAt instanceof OMTBlockEntry) {
            setDummyPlaceHolder(DUMMY_ENTRY_VALUE, context);
            return;
        }
        if (elementAt instanceof OMTBlock) {
            setDummyPlaceHolder(DUMMY_ENTRY_VALUE, context);
            return;
        }
        // find the PsiElement containing the caret:

        if (elementAt instanceof GeneratedParserUtilBase.DummyBlock) {
            // whatever is being type right now is not parsable. Most likely an incomplete entry (without the colon)
            // try to resolve it so we can use a valid placeholder to allow the parsing to proceed correctly
            List<String> expectedTypesAtDummyBlock = getExpectedTypesAtDummyBlock((GeneratedParserUtilBase.DummyBlock) elementAt);
            setDummyContextFromExpectedList(expectedTypesAtDummyBlock, context);
            return;
        }
        if (elementAt instanceof OMTFile) {
            List<String> expectedTypesAtDummyBlock = getExpectedTypeAtOMTFile(elementAtCaret);
            setDummyContextFromExpectedList(expectedTypesAtDummyBlock, context);
            return;
        }

        if (elementAt instanceof OMTCommandsBlock) {
            alreadyResolved = true;
            resolvedElements.add(LookupElementBuilder.create("DEFINE COMMAND"));
            resolvedElements.add(LookupElementBuilder.create("DEFINE COMMAND myCommand() => { @LOG('hello world'); }"));
            return;
        }
        if (elementAt instanceof OMTQueriesBlock) {
            alreadyResolved = true;
            resolvedElements.add(LookupElementBuilder.create("DEFINE QUERY"));
            resolvedElements.add(LookupElementBuilder.create("DEFINE QUERY myQuery() => 'hello world';"));
            return;
        }
        if (elementAt instanceof OMTCommandBlock) {
            setResolvedElementsForCommand(elementAt);
            alreadyResolved = true;
            return;
        }
        setDummyPlaceHolder("", context);
        this.elementAtCaret = elementAt;
    }


    private void setResolvedElementsForCommand(PsiElement elementAt) {
        resolvedElements.addAll(
                modelUtil.getLocalCommands(elementAt).stream()
                        .map(command -> LookupElementBuilder.create(String.format("@%s()", command)))
                        .collect(Collectors.toList()));

        resolvedElements.addAll(
                builtInUtil.getBuiltInCommandsAsSuggestions().stream()
                        .map(LookupElementBuilder::create)
                        .collect(Collectors.toList()));

        resolvedElements.addAll(
                projectUtil.getExportingCommandsAsSuggestions().stream()
                        .map(LookupElementBuilder::create)
                        .collect(Collectors.toList()));
        alreadyResolved = true;
    }

    private void setDummyContextFromExpectedList(List<String> expectedTypes, @NotNull CompletionInitializationContext context) {
        if (expectedTypes.contains("block entry")) {
            setDummyPlaceHolder(DUMMY_ENTRY_VALUE, context);
            return;
        }
        if (expectedTypes.contains("SEMICOLON")) {
            // incomplete, as a placeholder, use the current text with a semicolon closing
            setDummyPlaceHolder(String.format("%s%s", getCurrentText(context), SEMICOLON), context);
            // make sure the offset is including the full text to be replace:
            return;
        }
    }

    private String getCurrentText(@NotNull CompletionInitializationContext context) {
        PsiElement elementAt = context.getFile().findElementAt(context.getCaret().getOffset() - 1);
        return elementAt != null ? elementAt.getText() : "";
    }

    private List<String> getExpectedTypesAtDummyBlock(GeneratedParserUtilBase.DummyBlock dummyBlock) {

        PsiElement previous = dummyBlock.getPrevSibling();
        while (!(previous instanceof PsiErrorElement) && previous != null && previous.getPrevSibling() != null) {
            previous = previous.getPrevSibling();
        }
        return previous instanceof PsiErrorElement ? getExpectedTypesFromError((PsiErrorElement) previous) : new ArrayList<>();
    }

    private List<String> getExpectedTypeAtOMTFile(PsiElement element) {
        PsiElement next = element.getNextSibling();
        while (!(next instanceof PsiErrorElement) && next != null && next.getNextSibling() != null) {
            next = next.getNextSibling();
        }

        return next instanceof PsiErrorElement ? getExpectedTypesFromError((PsiErrorElement) next) : new ArrayList<>();
    }

    private List<String> getExpectedTypesFromError(PsiErrorElement errorElement) {
        List<String> expectedTypes = new ArrayList<>();
        String errorDescription = errorElement.getErrorDescription();
        Pattern pattern = Pattern.compile("\\<(.*?)\\>|OMTTokenType.([^ ]*)");
        Matcher matcher = pattern.matcher(errorDescription);
        while (matcher.find()) {
            // every match is grouped as either 1 (<...>) or 2 OMTTokenType.
            expectedTypes.add(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
        }
        return expectedTypes;
    }

    private void addCompletionsForScript(PsiElement elementAtPosition, CompletionResultSet resultSet) {
        addCompletionsForVariables(elementAtPosition, resultSet);

        if (isInQueryPath(elementAtPosition)) {
            List<String> suggestions = builtInUtil.getBuiltInOperatorsAsSuggestions();
            suggestions.addAll(projectUtil.getExportingOperatorsAsSuggestions());

            suggestions.forEach(
                    suggestion -> resultSet.addElement(LookupElementBuilder.create(suggestion))
            );
        } else if (isInCommandBlock(elementAtPosition)) {
            List<String> suggestions = modelUtil.getLocalCommands(elementAtPosition).stream().map(command -> String.format("@%s()", command)).collect(Collectors.toList());
            suggestions.addAll(builtInUtil.getBuiltInCommandsAsSuggestions());
            suggestions.addAll(projectUtil.getExportingCommandsAsSuggestions());

            suggestions.forEach(
                    suggestion -> resultSet.addElement(LookupElementBuilder.create(suggestion))
            );
        } else {
            includeCaretPositionCompletions(resultSet);
        }

    }

    private void includeCaretPositionCompletions(CompletionResultSet resultSet) {
        if (elementAtCaret instanceof OMTQueriesBlock) {
            resultSet.addElement(LookupElementBuilder.create("DEFINE QUERY"));
            resultSet.addElement(LookupElementBuilder.create("DEFINE QUERY myQuery() => 'hello world';"));
        }
        if (elementAtCaret instanceof OMTCommandsBlock) {
            resultSet.addElement(LookupElementBuilder.create("DEFINE COMMAND"));
            resultSet.addElement(LookupElementBuilder.create("DEFINE COMMAND myCommand() => { @LOG('hello world'); }"));
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
        return
                elementAtCaret instanceof OMTScript ||
                        element instanceof OMTCommandBlock ||
                        PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTScript || parent instanceof OMTCommandBlock) != null;
    }

    private boolean isInQueryPath(PsiElement element) {
        return PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTQueryPath) != null;
    }

}
