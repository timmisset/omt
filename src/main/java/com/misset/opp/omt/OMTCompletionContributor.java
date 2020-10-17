package com.misset.opp.omt;

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
import com.misset.opp.omt.psi.util.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OMTCompletionContributor extends CompletionContributor {

    private static String DUMMY_SCALAR = "DUMMYSCALARVALUE";
    private static String DUMMY_PROPERTY = "DUMMYPROPERTYVALUE:";
    private static String DUMMY_ENTRY = String.format("%s %s", DUMMY_PROPERTY, DUMMY_SCALAR);
    private static String SEMICOLON = ";";

    private static String usedContextPlaceHolder = "";

    private static String ATTRIBUTES = "attributes";

    private PsiElement elementAtCaret;
    List<LookupElement> resolvedElements;
    List<String> resolvedSuggestions;
    private boolean alreadyResolved;

    final ModelUtil modelUtil = ModelUtil.SINGLETON;

    private VariableUtil variableUtil = VariableUtil.SINGLETON;
    private BuiltInUtil builtInUtil = BuiltInUtil.SINGLETON;
    private ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    private TokenUtil tokenUtil = TokenUtil.SINGLETON;
    private MemberUtil memberUtil = MemberUtil.SINGLETON;

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
                            result.addAllElements(resolvedElements);
                            return;
                        }
                        if (tokenUtil.isOperator(element)) {
                            setResolvedElementsForOperator(element);
                            result.addAllElements(resolvedElements);
                        }
                    }
                });
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
        resolvedSuggestions = new ArrayList<>();

        PsiElement elementAtCaret = context.getFile().findElementAt(context.getCaret().getOffset());
        if (elementAtCaret == null) {
            // no element at caret, use the DUMMY_ENTRY_VALUE
            setDummyPlaceHolder(DUMMY_ENTRY, context);
            return;
        }
        PsiElement elementAt = elementAtCaret.getParent();

        if (elementAt instanceof OMTBlockEntry) {
            setDummyPlaceHolder(DUMMY_ENTRY, context);
            return;
        }
        if (elementAt instanceof OMTBlock) {
            setDummyPlaceHolder(DUMMY_ENTRY, context);
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
        if (elementAt instanceof OMTCommandBlock ||
                elementAt instanceof OMTScript ||
                elementAt instanceof OMTScriptLine ||
                elementAt.getText().trim().equals("|")) {
            setResolvedElementsForCommand(elementAt);
            alreadyResolved = true;
            return;
        }
        setDummyPlaceHolder("", context);
        this.elementAtCaret = elementAt;
    }


    // all suggestions for a command statement
    // prioritize by likelihood of usage
    private void setResolvedElementsForCommand(PsiElement elementAt) {
        // first check if there are local variables available, they also have the highest suggestion priority
        includeLocalVariables(elementAt, 7);

        // then the declared variables available at the point of completion
        includeDeclaredVariables(elementAt, 6);

        // then the commands provided by this file
        // first the ones within the same modelItem:
        modelUtil.getModelItemBlock(elementAt).ifPresent(omtModelItemBlock ->
                PsiTreeUtil.findChildrenOfType(omtModelItemBlock, OMTDefineCommandStatement.class)
                        .forEach(omtDefineCommandStatement -> {
                                    if (!PsiTreeUtil.isContextAncestor(omtModelItemBlock, elementAt, true)) {
                                        addPriorityElement(
                                                memberUtil.parseDefinedToCallable(omtDefineCommandStatement.getDefineName()).asSuggestion(), 5);
                                    }
                                }
                        ));

        // then the ones exported by the containing file:
        ((OMTFile) elementAt.getContainingFile()).getExportedMembers().forEach(
                (name, omtExportMember) -> {
                    if (omtExportMember.isCommand() && !PsiTreeUtil.isContextAncestor(omtExportMember.getElement(), elementAt, true)) {
                        addPriorityElement(omtExportMember.asSuggestion(), 4);
                    }
                }
        );

        // next are the locally available commands, such as COMMIT, DONE etc.
        includeLocalCommands(elementAt, 3);

        // then the built-in commands:
        builtInUtil.getBuiltInCommandsAsSuggestions().forEach(suggestion ->
                addPriorityElement(suggestion, 2)
        );

        // and finally all the available commands in the project
        projectUtil.getExportingCommandsAsSuggestions().forEach(
                suggestion -> {
                    if (!resolvedSuggestions.contains(suggestion)) {
                        addPriorityElement(suggestion, 1);
                    }
                }
        );
        alreadyResolved = true;
    }

    // all suggestions for an operator statement
    // prioritize by likelihood of usage
    private void setResolvedElementsForOperator(PsiElement elementAt) {
        // first check if there are local variables available, they also have the highest suggestion priority
        includeLocalVariables(elementAt, 7);

        // then the declared variables available at the point of completion
        includeDeclaredVariables(elementAt, 6);

        // then the commands provided by this file
        // first the ones within the same modelItem:
        modelUtil.getModelItemBlock(elementAt).ifPresent(omtModelItemBlock ->
                PsiTreeUtil.findChildrenOfType(omtModelItemBlock, OMTDefineQueryStatement.class)
                        .forEach(omtDefineQueryStatement -> {
                                    if (!PsiTreeUtil.isContextAncestor(omtModelItemBlock, elementAt, true)) {
                                        addPriorityElement(
                                                memberUtil.parseDefinedToCallable(omtDefineQueryStatement.getDefineName()).asSuggestion(), 5);
                                    }
                                }
                        ));

        // then the ones exported by the containing file:
        ((OMTFile) elementAt.getContainingFile()).getExportedMembers().forEach(
                (name, omtExportMember) -> {
                    if (omtExportMember.isOperator() && !PsiTreeUtil.isContextAncestor(omtExportMember.getElement(), elementAt, true)) {
                        addPriorityElement(omtExportMember.asSuggestion(), 4);
                    }
                }
        );

        // then the built-in operators:
        builtInUtil.getBuiltInOperatorsAsSuggestions().forEach(suggestion ->
                addPriorityElement(suggestion, 2)
        );

        // and finally all the available commands in the project
        projectUtil.getExportingOperatorsAsSuggestions().forEach(
                suggestion -> {
                    if (!resolvedSuggestions.contains(suggestion)) {
                        addPriorityElement(suggestion, 1);
                    }
                }
        );
        alreadyResolved = true;
    }

    private void addPriorityElement(String text, int priority) {
        resolvedElements.add(PrioritizedLookupElement.withPriority(
                LookupElementBuilder.create(text), priority
        ));
        resolvedSuggestions.add(text);
    }

    private void includeLocalCommands(PsiElement elementAt, int priority) {
        modelUtil.getLocalCommands(elementAt).forEach(
                command -> addPriorityElement(String.format("@%s()", command), priority)
        );
    }

    private void includeDeclaredVariables(PsiElement elementAt, int priority) {
        variableUtil.getDeclaredVariables(elementAt).forEach(
                omtVariable -> addPriorityElement(omtVariable.getName(), priority)
        );
    }

    private void includeLocalVariables(PsiElement elementAt, int priority) {
        variableUtil.getLocalVariables(elementAt).forEach(
                (variableName, provider) -> addPriorityElement(variableName, priority)
        );
    }

    private void setDummyContextFromExpectedList(List<String> expectedTypes, @NotNull CompletionInitializationContext context) {
        if (expectedTypes.contains("block entry") || expectedTypes.contains("block")) {
            setDummyPlaceHolder(DUMMY_ENTRY, context);
            return;
        }
        if (expectedTypes.contains("query step")) {
            setDummyPlaceHolder(DUMMY_SCALAR, context);
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

}
