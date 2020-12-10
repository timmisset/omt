package com.misset.opp.omt.completion;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.completion.model.CommandBlockCompletion;
import com.misset.opp.omt.completion.model.ModelCompletion;
import com.misset.opp.omt.completion.model.ModelItemCompletion;
import com.misset.opp.omt.completion.model.QueryBlockCompletion;
import com.misset.opp.omt.completion.query.QueryEquationStatementCompletion;
import com.misset.opp.omt.completion.query.QueryFilterStepCompletion;
import com.misset.opp.omt.completion.query.QueryFirstStepCompletion;
import com.misset.opp.omt.completion.query.QueryNextStepCompletion;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.support.BuiltInType;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import com.misset.opp.omt.psi.support.OMTExportMember;
import com.misset.opp.omt.settings.OMTSettingsState;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.misset.opp.omt.psi.util.UtilManager.*;

public class OMTCompletionContributor extends CompletionContributor {

    private static final String DUMMY_SCALAR = "DUMMYSCALARVALUE";
    private static final String DUMMY_PROPERTY = "DUMMYPROPERTYVALUE:";
    private static final String DUMMY_IMPORT = "- DUMMYIMPORT";
    private static final String DUMMY_QUERYSTEP = "DUMMY / ";
    private static final String DUMMY_MODEL_ITEM_TYPE = "!DUMMY";
    private static final String DUMMY_ENTRY = String.format("%s %s", DUMMY_PROPERTY, DUMMY_SCALAR);
    private static final String SEMICOLON = ";";
    private static final String ATTRIBUTES = "attributes";


    /**
     * The higher the priority number, the higher it gets listed
     */
    private static final int ATTRIBUTES_PRIORITY = 11;              // model entry attributes
    private static final int EQUATION_PRIORITY = 10;                // the other side of the equation, shows a limited set of options based on the resolved type of the other side
    private static final int CLASSES_PRIORITY = 9;                  // list with available classes
    private static final int PREDICATE_FORWARD_PRIORITY = 8;
    private static final int PREDICATE_REVERSE_PRIORITY = 7;
    private static final int LOCAL_VARIABLE_PRIORITY = 6;
    private static final int DECLARED_VARIABLE_PRIORITY = 5;
    private static final int DEFINED_STATEMENT_PRIORITY = 4;
    private static final int LOCAL_COMMAND_PRIORITY = 3;
    private static final int BUILTIN_MEMBER_PRIORITY = 2;
    private static final int IMPORTABLE_MEMBER_PRIORITY = 1;


    private boolean dummyPlaceHolderSet = false;

    List<LookupElement> resolvedElements;
    List<String> resolvedSuggestions;
    private boolean isResolved;

    private String dummyPlaceHolder = null;

    /**
     * Generic completion that resolves the suggestion based on the cursor position
     */
    public OMTCompletionContributor() {
        //extend(CompletionType.BASIC, PlatformPatterns.psiElement(), getCompletionProvider());
        ModelItemCompletion.register(this);
        ModelCompletion.register(this);
        QueryBlockCompletion.register(this);
        CommandBlockCompletion.register(this);
        QueryFirstStepCompletion.register(this);
        QueryNextStepCompletion.register(this);
        QueryFilterStepCompletion.register(this);
        QueryEquationStatementCompletion.register(this);
    }


    private CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                if (isResolved) {
                    result.addAllElements(resolvedElements);
                    return;
                }

                PsiElement element = parameters.getPosition();

                if (getTokenUtil().isCommand(element)) {
                    setResolvedElementsForCommand(element);
                    result.addAllElements(resolvedElements);
                    return;
                }
                if (getTokenUtil().isOperator(element)) {
                    if (getTokenUtil().isMemberImport(element)) {
                        setResolvedElementsForImport(
                                PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTImport),
                                parameters.getOriginalPosition());
                        result.addAllElements(resolvedElements);
                        return;
                    }
                    if (element != null && getTokenUtil().isParameterType(element.getParent())) {
                        setResolvedElementsForClasses(element);
                        result.addAllElements(resolvedElements);
                        return;
                    }
                    setResolvedElementsForOperator(element);
                    resolvedElements.forEach(result::addElement);
                }
                if (getTokenUtil().isNamespaceMember(element)) {
                    assert element != null;
                    if (getTokenUtil().isParameterType(element.getParent())) {
                        setResolvedElementsForClasses(element);
                        result.addAllElements(resolvedElements);
                    } else if (element.getParent() instanceof OMTCurieElement &&
                            element.getParent().getParent() instanceof OMTQueryStep) {
                        setQueryStepSuggestions((OMTQueryStep) element.getParent().getParent(), element);
                        result.addAllElements(resolvedElements);
                    }
                }
            }
        };
    }

    // add completion in the OMT model based on the Json attributes
    private void setAttributeSuggestions(PsiElement element, boolean atParent) {
        JsonObject json = atParent ? getModelUtil().getJsonAtDepth(element, getModelUtil().getModelDepth(element) - 1) :
                getModelUtil().getJson(element);
        List<String> existingSiblingEntryLabels = getExistingSiblingEntryLabels(element);
        if (json != null && json.has(ATTRIBUTES)) {

            json.getAsJsonObject(ATTRIBUTES).keySet()
                    .stream()
                    .filter(key -> !existingSiblingEntryLabels.contains(key))
                    .forEach(key -> addPriorityElement(String.format("%s:", key).trim(), ATTRIBUTES_PRIORITY)
                    );
        }
    }

    // used to fetch the entries already available for the item that is targeted by the completion
    private List<String> getExistingSiblingEntryLabels(PsiElement element) {
        OMTBlock container = (OMTBlock) PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTBlock);
        if (container == null) {
            return Collections.emptyList();
        }
        List<String> entryList = new ArrayList<>();
        container.getBlockEntryList().forEach(blockEntry -> entryList.add(getModelUtil().getEntryBlockLabel(blockEntry)));
        return entryList;
    }

    private void setDummyPlaceHolder(String placeHolder, @NotNull CompletionInitializationContext context) {
        if (!dummyPlaceHolderSet) {
            dummyPlaceHolderSet = true;
            dummyPlaceHolder = placeHolder;
            context.setDummyIdentifier(placeHolder);
        }
    }

    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        final String identifier = new PlaceholderProvider(context).getIdentifier();
        if (identifier != null) {
            context.setDummyIdentifier(identifier);
        }
    }

    private void trySuggestionsForCurrentElementAt(PsiElement element, PsiElement elementAtCaret, CompletionInitializationContext context) {
        if (element instanceof OMTBlock ||
                element instanceof OMTModelBlock ||
                element instanceof OMTModelItemBlock) {
            setDummyPlaceHolder(DUMMY_ENTRY, context);
            return;
        }

        // error in parsing, try to resolve it
        if ((element instanceof OMTFile || element instanceof PsiErrorElement)) {
            PsiErrorElement errorElement = getErrorElement(element);
            if (errorElement == null) {
                errorElement = getErrorElement(elementAtCaret);
            }
            List<String> expectedTypesAtDummyBlock = getExpectedTypesFromError(errorElement);
            if (errorElement != null && !expectedTypesAtDummyBlock.isEmpty()) {
                setDummyContextFromExpectedList(expectedTypesAtDummyBlock, context, hasValuePosition(elementAtCaret, context), errorElement);
                return;
            }
        }

        if (element instanceof OMTSequence &&
                getModelUtil().getJson(element) != null &&
                getModelUtil().getJson(element).has(ATTRIBUTES)) {
            // A sequence item that can destructed into separate entries
            setAttributeSuggestions(element, false);
            isResolved = true;
            return;
        }

        if (element instanceof OMTCommandBlock ||
                element instanceof OMTScript ||
                element instanceof OMTScriptLine ||
                element.getText().trim().equals("|")) {
            setResolvedElementsForCommand(element);
            isResolved = true;
            return;
        }
        if (element instanceof OMTScalarValue) {
            // scalar value as a block is not enough for useful completion:
            // check to see if there are children that can be used
            Arrays.stream(element.getChildren()).forEach(child -> {
                if (!(getTokenUtil().isWhiteSpace(child))) {
                    trySuggestionsForCurrentElementAt(child, elementAtCaret, context);
                }
            });
        }
        if (element instanceof OMTImportBlock) {
            // get the import itself:
            setResolvedElementsForImport(elementAtCaret, elementAtCaret);
            isResolved = true;
        }
        if (element.getParent() instanceof OMTQueryPath) {
            isResolved = setQueryPathSuggestions((OMTQueryPath) element.getParent());
        }
        if (PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTQueryStep) != null) {
            setDummyPlaceHolder(DUMMY_QUERYSTEP, context);
        }
        if (element instanceof OMTDefineQueryStatement) {
            setDummyPlaceHolder(DUMMY_QUERYSTEP, context);
        }
        setDummyPlaceHolder(DUMMY_SCALAR, context);
    }

    private boolean setQueryPathSuggestions(@NotNull OMTQueryPath omtQueryPath) {
        if (omtQueryPath.getParent() instanceof OMTEquationStatement) {
            return setEquationStepSuggestions((OMTEquationStatement) omtQueryPath.getParent(), omtQueryPath);
        }
        return false;
    }

    private boolean setEquationStepSuggestions(@NotNull OMTEquationStatement equationStep, @NotNull OMTQuery query) {
        final List<Resource> resources = equationStep.getOpposite(query).resolveToResource();
        if (!resources.isEmpty()) {
            getRDFModelUtil().getComparableOptions(resources).forEach(
                    resource -> setCurieSuggestion(query, resource, false, EQUATION_PRIORITY)
            );
            return true;
        }
        return false;
    }

    private void setQueryStepSuggestions(@NotNull OMTQueryStep queryStep, @NotNull PsiElement elementAt) {
        List<Resource> previousStep = getQueryUtil().getPreviousStep(queryStep);
        if (previousStep.isEmpty()) {
            // start of the query:
            if (queryStep.getParent() instanceof OMTQueryPath) {
                setQueryPathSuggestions((OMTQueryPath) queryStep.getParent());
            }
        } else {
            // part of a query flow
            getRDFModelUtil().listPredicatesForSubjectClass(previousStep).forEach((resource, relation) -> setCurieSuggestion(elementAt, resource, false,
                    PREDICATE_FORWARD_PRIORITY));
            getRDFModelUtil().listPredicatesForObjectClass(previousStep).forEach((resource, relation) -> setCurieSuggestion(elementAt, resource, true,
                    PREDICATE_REVERSE_PRIORITY));
        }

    }

    private void setResolvedElementsFor(@NotNull PsiElement elementAt, Class<? extends OMTDefinedStatement> definedType, BuiltInType builtInType) {
        // check if the path can be resolved to a model based suggestion
        OMTQueryStep queryStep = (OMTQueryStep) PsiTreeUtil.findFirstParent(elementAt, parent -> parent instanceof OMTQueryStep);
        if (queryStep != null) {
            setQueryStepSuggestions(queryStep, elementAt);
        }

        // check if there are local variables available, they also have the highest suggestion priority
        getVariableUtil().getLocalVariables(elementAt).forEach(
                (variableName, provider) -> addPriorityElement(variableName, LOCAL_VARIABLE_PRIORITY)
        );

        // then the declared variables available at the point of completion
        getVariableUtil().getDeclaredVariables(elementAt).forEach(
                omtVariable -> addPriorityElement(omtVariable.getName(), DECLARED_VARIABLE_PRIORITY)
        );

        // then the commands provided by this file
        // first the ones within the same modelItem:
        getModelUtil().getModelItemBlock(elementAt).ifPresent(omtModelItemBlock ->
                PsiTreeUtil.findChildrenOfType(omtModelItemBlock, definedType)
                        .forEach(omtDefinedStatement -> {
                                    if (!PsiTreeUtil.isContextAncestor(omtDefinedStatement, elementAt, true)) {
                                        addPriorityElement(
                                                getMemberUtil().parseDefinedToCallable(omtDefinedStatement.getDefineName()).getAsSuggestion(), DEFINED_STATEMENT_PRIORITY);
                                    }
                                }
                        ));

        if (definedType == OMTDefineCommandStatement.class) {
            getModelUtil().getLocalCommands(elementAt).forEach(
                    command -> addPriorityElement(String.format("@%s()", command), LOCAL_COMMAND_PRIORITY)
            );
        }
        getBuiltinUtil().getBuiltInSuggestions(builtInType)
                .forEach(suggestion -> addPriorityElement(suggestion, BUILTIN_MEMBER_PRIORITY, suggestion, (context, item) -> {
                        },
                        "", builtInType.name()));
        getProjectUtil().getExportedMembers(builtInType == BuiltInType.Command).forEach(
                this::setImportMemberSuggestion
        );
        isResolved = true;
    }

    private void setImportMemberSuggestion(OMTExportMember exportMember) {
        if (exportMember.getResolvingElement() == null || exportMember.getResolvingElement().getContainingFile() == null) {
            return;
        }
        final PsiFile containingFile = exportMember.getResolvingElement().getContainingFile();
        if (containingFile == null || containingFile.getVirtualFile() == null) {
            return;
        }
        if (!containingFile.isValid()) {
            return;
        }

        // filter out suggestions from mocha subfolders
        OMTSettingsState settings = OMTSettingsState.getInstance();
        if (!settings.includeMochaFolderImportSuggestions &&
                containingFile.getVirtualFile() != null &&
                containingFile.getVirtualFile().getPath().contains("/mocha/")
        ) {
            return;
        }

        String path = String.format("%s/%s", containingFile.getContainingDirectory() != null ?
                containingFile.getContainingDirectory().getName() :
                "<root>", containingFile.getName());
        String title = exportMember.getAsSuggestion();
        addPriorityElement(title, OMTCompletionContributor.IMPORTABLE_MEMBER_PRIORITY, title,
                (context, item) -> {
                    OMTFile omtFile = (OMTFile) context.getFile();
                    if (!omtFile.hasImportFor(exportMember)) {
                        List<String> importPaths = getImportUtil().getImportPaths(exportMember, omtFile);
                        if (!importPaths.isEmpty()) {
                            String importPath = importPaths.get(0);
                            getImportUtil().addImportMemberToBlock(context.getFile(), importPath, exportMember.getName());
                        }
                    }
                }, path, exportMember.getCallableType());
    }

    private void setResolvedElementsForImport(PsiElement elementAtCaret, PsiElement originalElement) {
        final Optional<OMTImport> importOptional = getTokenFinderUtil().findImport(elementAtCaret);
        importOptional.ifPresent(
                omtImport -> {
                    VirtualFile importedFile = getImportUtil().getImportedFile(omtImport, originalElement.getContainingFile().getVirtualFile());
                    OMTFile omtFile = importedFile == null ? null : (OMTFile) PsiManager.getInstance(elementAtCaret.getProject()).findFile(importedFile);
                    if (omtFile == null) {
                        return;
                    } // cannot find imported file

                    final List<String> existingImports = getImportUtil().getImportedMemberNames(omtImport);
                    omtFile.getExportedMembers().values().stream()
                            .filter(exportMember -> !existingImports.contains(exportMember.getName()))
                            .forEach(this::setResolvedElementsForImport);
                    isResolved = true;
                }
        );
    }

    private void setResolvedElementsForImport(OMTExportMember exportMember) {
        resolvedElements.add(LookupElementBuilder.create(exportMember.getName())
                .withPresentableText(exportMember.getName()));
    }

    private void setResolvedElementsForClasses(PsiElement element) {
        getProjectUtil().getRDFModelUtil().getAllClasses().stream().filter(resource -> resource.getURI() != null).forEach(
                resource -> setCurieSuggestion(element, resource, false, CLASSES_PRIORITY)
        );
        Arrays.asList("string", "integer", "boolean", "date").forEach(
                type -> addPriorityElement("string", 1)
        );
    }

    private void setCurieSuggestion(PsiElement elementAt, Resource resource, boolean reverse, int priority) {
        OMTFile omtFile = (OMTFile) elementAt.getContainingFile();
        String curieElement = omtFile.resourceToCurie(resource);
        String title = curieElement;
        AtomicBoolean registerPrefix = new AtomicBoolean(false);
        if (curieElement.equals(resource.toString())) {
            curieElement = getPrefixSuggestion(resource);
            title = resource.toString();
            registerPrefix.set(true);
        }
        List<Resource> resolvesTo;
        if (reverse) {
            title = "^" + title;
            curieElement = "^" + curieElement;
            resolvesTo = getRDFModelUtil().getPredicateSubjects(resource, false);
        } else {
            resolvesTo = getRDFModelUtil().getPredicateObjects(resource, false);
        }


        addPriorityElement(curieElement, priority, title, (context, item) ->
                        // if the iri is not registered in the page, do it
                        ApplicationManager.getApplication().runWriteAction(() -> {
                            if (registerPrefix.get()) {
                                getCurieUtil().addPrefixToBlock(context.getFile(),
                                        item.getLookupString().split(":")[0].substring(reverse ? 1 : 0),
                                        resource.getNameSpace());
                            }
                        }),
                null,
                !resolvesTo.isEmpty() && !getRDFModelUtil().isTypePredicate(resource) ? omtFile.resourceToCurie(resolvesTo.get(0)) : null);
    }

    private String getPrefixSuggestion(Resource resource) {
        if (resource.getNameSpace() == null) {
            return resource.getURI();
        }
        List<OMTPrefix> knownPrefixes = getProjectUtil().getKnownPrefixes(resource.getNameSpace());
        if (!knownPrefixes.isEmpty()) {
            return knownPrefixes.get(0).getNamespacePrefix().getName() + ":" + resource.getLocalName();
        }
        return resource.getURI();
    }

    // all suggestions for a command statement
    // prioritize by likelihood of usage
    private void setResolvedElementsForCommand(PsiElement elementAt) {
        setResolvedElementsFor(elementAt, OMTDefineCommandStatement.class, BuiltInType.Command);
    }

    // all suggestions for an operator statement
    // prioritize by likelihood of usage
    private void setResolvedElementsForOperator(PsiElement elementAt) {
        setResolvedElementsFor(elementAt, OMTDefineQueryStatement.class, BuiltInType.Operator);
    }

    private void addPriorityElement(String text, int priority) {
        addPriorityElement(text, priority, text, (context, item) -> {
        }, null, null);
    }

    private void addPriorityElement(String text, int priority, String title, InsertHandler<LookupElement> insertHandler,
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

    private void setDummyContextFromExpectedList(List<String> expectedTypes, @NotNull CompletionInitializationContext context, boolean hasValuePosition, @NotNull PsiErrorElement errorElement) {
        if (!hasValuePosition && (expectedTypes.contains("block entry") || expectedTypes.contains("block"))) {
            setDummyPlaceHolder(DUMMY_ENTRY, context);
            return;
        }
        if (expectedTypes.contains("scalar") || expectedTypes.contains("parameter type")) {
            setDummyPlaceHolder(DUMMY_SCALAR, context);
            return;
        }

        // indicates that the current line isn't properly closed with a semicolon.
        if (expectedTypes.contains("SEMICOLON") || expectedTypes.contains("query") || expectedTypes.contains("query step")) {
            setDummyPlaceHolder(String.format("%s%s", DUMMY_SCALAR, SEMICOLON), context);
            return;
        }
        if (expectedTypes.contains("import $")) {
            setDummyPlaceHolder(DUMMY_IMPORT, context);
        }
        if (expectedTypes.contains("MODEL_ITEM_TYPE")) {
            setDummyPlaceHolder(errorElement.getText().startsWith("!") ? DUMMY_SCALAR : DUMMY_MODEL_ITEM_TYPE, context);
        }
    }

    private boolean hasValuePosition(@NotNull PsiElement element, @NotNull CompletionInitializationContext context) {
        Document document = context.getEditor().getDocument();
        int line = document.getLineNumber(element.getTextOffset());
        String textAtLine = document.getText(TextRange.create(document.getLineStartOffset(line), document.getLineEndOffset(line))).trim();
        return textAtLine.contains(":");

    }

    private PsiErrorElement getErrorElement(PsiElement element) {
        if (element instanceof PsiErrorElement) {
            return (PsiErrorElement) element;
        }
        return getErrorElement(element, false);
    }

    private PsiErrorElement getErrorElement(PsiElement element, boolean reverse) {
        PsiElement sibling = reverse ? element.getPrevSibling() : element.getNextSibling();
        while (!(sibling instanceof PsiErrorElement) && sibling != null && sibling.getNextSibling() != null) {
            sibling = reverse ? sibling.getPrevSibling() : sibling.getNextSibling();
        }
        if (sibling == null && !reverse) {
            return getErrorElement(element, true);
        }
        return sibling instanceof PsiErrorElement ? (PsiErrorElement) sibling : null;
    }

    private List<String> getExpectedTypesFromError(PsiErrorElement errorElement) {
        List<String> expectedTypes = new ArrayList<>();
        if (errorElement == null) {
            return expectedTypes;
        }

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
