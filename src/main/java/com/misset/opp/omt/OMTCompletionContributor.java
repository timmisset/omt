package com.misset.opp.omt;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.external.util.builtIn.BuiltInType;
import com.misset.opp.omt.external.util.builtIn.BuiltInUtil;
import com.misset.opp.omt.external.util.rdf.RDFModelUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import com.misset.opp.omt.psi.support.OMTExportMember;
import com.misset.opp.omt.psi.util.*;
import com.misset.opp.omt.settings.OMTSettingsState;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OMTCompletionContributor extends CompletionContributor {

    private static String DUMMY_SCALAR = "DUMMYSCALARVALUE";
    private static String DUMMY_PROPERTY = "DUMMYPROPERTYVALUE:";
    private static String DUMMY_IMPORT = "- DUMMYIMPORT";
    private static String DUMMY_ENTRY = String.format("%s %s", DUMMY_PROPERTY, DUMMY_SCALAR);
    private static String SEMICOLON = ";";

    private static String ATTRIBUTES = "attributes";
    private boolean dummyPlaceHolderSet = false;

    List<LookupElement> resolvedElements;
    List<String> resolvedSuggestions;
    private boolean isResolved;

    final ModelUtil modelUtil = ModelUtil.SINGLETON;

    private VariableUtil variableUtil = VariableUtil.SINGLETON;
    private BuiltInUtil builtInUtil = BuiltInUtil.SINGLETON;
    private ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    private TokenUtil tokenUtil = TokenUtil.SINGLETON;
    private MemberUtil memberUtil = MemberUtil.SINGLETON;
    private CurieUtil curieUtil = CurieUtil.SINGLETON;
    private ImportUtil importUtil = ImportUtil.SINGLETON;
    private RDFModelUtil rdfModelUtil;

    private PsiElement originalElement;

    public OMTCompletionContributor() {
        /**
         * Generic completion that resolves the suggestion based on the cursor position
         */
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), getCompletionProvider());
    }

    private RDFModelUtil getRDFModel() {
        if (rdfModelUtil == null || !rdfModelUtil.isLoaded()) {
            rdfModelUtil = new RDFModelUtil(projectUtil.getOntologyModel());
        }
        return rdfModelUtil;
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
                originalElement = parameters.getOriginalPosition();

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
                    if (tokenUtil.isMemberImport(element)) {
                        setResolvedElementsForImport(
                                PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTImport),
                                false, parameters.getOriginalPosition());
                        result.addAllElements(resolvedElements);
                        return;
                    }
                    if (element != null && element.getParent() instanceof OMTParameterWithType) {
                        setResolvedElementsForClasses(element);
                        result.addAllElements(resolvedElements);
                        return;
                    }
                    setResolvedElementsForOperator(element);
                    resolvedElements.forEach(result::addElement);
                }
                if (tokenUtil.isNamespaceMember(element)) {
                    if (element != null && tokenUtil.isParameterType(element.getParent())) {
                        setResolvedElementsForClasses(element);
                        result.addAllElements(resolvedElements);
                        return;
                    }
                }
            }
        };
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
        dummyPlaceHolderSet = true;
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
        isResolved = false;
        resolvedElements = new ArrayList<>();
        resolvedSuggestions = new ArrayList<>();
        dummyPlaceHolderSet = true;

        PsiElement elementAtCaret = context.getFile().findElementAt(context.getCaret().getOffset());
        if (elementAtCaret == null) {
            // no element at caret, use the DUMMY_ENTRY_VALUE
            setDummyPlaceHolder(DUMMY_ENTRY, context);
            return;
        }
        trySuggestionsForCurrentElementAt(elementAtCaret.getParent(), elementAtCaret, context);
    }

    private void trySuggestionsForCurrentElementAt(PsiElement element, PsiElement elementAtCaret, CompletionInitializationContext context) {
        if (element instanceof OMTBlockEntry ||
                element instanceof OMTBlock ||
                element instanceof OMTModelBlock ||
                element instanceof OMTModelItemBlock) {
            setDummyPlaceHolder(DUMMY_ENTRY, context);
            return;
        }
        // find the PsiElement containing the caret:

        if (element instanceof OMTFile || element instanceof PsiErrorElement) {
            List<String> expectedTypesAtDummyBlock = getExpectedTypeAtOMTFile(elementAtCaret, false);
            setDummyContextFromExpectedList(expectedTypesAtDummyBlock, context, hasValuePosition(elementAtCaret, context));
            return;
        }

        if (element instanceof OMTCommandsBlock) {
            isResolved = true;
            resolvedElements.add(LookupElementBuilder.create("DEFINE COMMAND"));
            resolvedElements.add(LookupElementBuilder.create("DEFINE COMMAND myCommand() => { @LOG('hello world'); }"));
            return;
        }
        if (element instanceof OMTQueriesBlock) {
            isResolved = true;
            resolvedElements.add(LookupElementBuilder.create("DEFINE QUERY"));
            resolvedElements.add(LookupElementBuilder.create("DEFINE QUERY myQuery() => 'hello world';"));
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
                if (!(child instanceof OMTStart) && !(child instanceof OMTEnd) && !(tokenUtil.isWhiteSpace(child))) {
                    trySuggestionsForCurrentElementAt(child, elementAtCaret, context);
                }
            });
        }
        if (element instanceof OMTImportBlock) {
            // get the import itself:
            setResolvedElementsForImport(elementAtCaret, true, elementAtCaret);
            isResolved = true;
        }
    }

    private void setResolvedElementsFor(PsiElement elementAt, Class<? extends OMTDefinedStatement> definedType, BuiltInType builtInType) {
        // check if the path can be resolved to a model based suggestion
        OMTQueryStep queryStep = (OMTQueryStep) PsiTreeUtil.findFirstParent(elementAt, parent -> parent instanceof OMTQueryStep);
        if (queryStep != null) {
            List<Resource> previousStep = PsiImplUtil.getPreviousStep(queryStep);
            getRDFModel().listPredicatesForSubjectClass(previousStep).forEach((resource, relation) -> setCurieSuggestion(elementAt, resource, relation, false, 9));
            getRDFModel().listPredicatesForObjectClass(previousStep).forEach((resource, relation) -> setCurieSuggestion(elementAt, resource, relation, true, 8));
        }

        // check if there are local variables available, they also have the highest suggestion priority
        variableUtil.getLocalVariables(elementAt).forEach(
                (variableName, provider) -> addPriorityElement(variableName, 7)
        );

        // then the declared variables available at the point of completion
        variableUtil.getDeclaredVariables(elementAt).forEach(
                omtVariable -> addPriorityElement(omtVariable.getName(), 6)
        );

        // then the commands provided by this file
        // first the ones within the same modelItem:
        modelUtil.getModelItemBlock(elementAt).ifPresent(omtModelItemBlock ->
                PsiTreeUtil.findChildrenOfType(omtModelItemBlock, definedType)
                        .forEach(omtDefinedStatement -> {
                                    if (!PsiTreeUtil.isContextAncestor(omtDefinedStatement, elementAt, true)) {
                                        addPriorityElement(
                                                memberUtil.parseDefinedToCallable(omtDefinedStatement.getDefineName()).asSuggestion(), 5);
                                    }
                                }
                        ));

        if (definedType == OMTDefineCommandStatement.class) {
            modelUtil.getLocalCommands(elementAt).forEach(
                    command -> addPriorityElement(String.format("@%s()", command), 3)
            );
        }
        builtInUtil.getBuiltInSuggestions(builtInType)
                .forEach(suggestion -> addPriorityElement(suggestion, 2, suggestion, (context, item) -> {
                        },
                        "", builtInType.name()));
        projectUtil.getExportedMembers(builtInType == BuiltInType.Command).forEach(
                omtExportMember -> setImportMemberSuggestion(omtExportMember, 1)
        );
        isResolved = true;
    }

    private void setImportMemberSuggestion(OMTExportMember exportMember, int priority) {
        if (exportMember.getResolvingElement() == null || exportMember.getResolvingElement().getContainingFile() == null) {
            return;
        }
        final PsiFile containingFile = exportMember.getResolvingElement().getContainingFile();

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
        String title = exportMember.asSuggestion();
        addPriorityElement(title, priority, title,
                (context, item) -> {
                    OMTFile omtFile = (OMTFile) context.getFile();
                    if (!omtFile.hasImportFor(exportMember)) {
                        List<String> importPaths = importUtil.getImportPaths(exportMember, omtFile);
                        if (!importPaths.isEmpty()) {
                            String importPath = importPaths.get(0);
                            importUtil.addImportMemberToBlock(context.getFile(), importPath, exportMember.getName());
                        }
                    }
                }, path, exportMember.getCallableType());
    }

    private void setResolvedElementsForImport(PsiElement elementAtCaret, boolean includeLeadingBullet, PsiElement originalElement) {
        PsiElement sibling = elementAtCaret;
        while (sibling != null && !(sibling instanceof OMTImport)) {
            sibling = sibling.getPrevSibling();
        }
        if (sibling != null) {
            OMTImport omtImport = (OMTImport) sibling;
            VirtualFile importedFile = importUtil.getImportedFile(omtImport, originalElement.getContainingFile().getVirtualFile());
            List<String> existingImports =
                    omtImport.getMemberList() == null ? new ArrayList<>() :
                            omtImport.getMemberList().getMemberListItemList()
                                    .stream()
                                    .map(OMTMemberListItem::getMember)
                                    .map(OMTMember::getName)
                                    .collect(Collectors.toList());
            OMTFile omtFile = importedFile == null ? null : (OMTFile) PsiManager.getInstance(elementAtCaret.getProject()).findFile(importedFile);
            if (omtFile != null) {
                omtFile.getExportedMembers().values().stream()
                        .filter(exportMember -> !existingImports.contains(exportMember.getName()))
                        .forEach(exportMember -> resolvedElements.add(LookupElementBuilder.create(
                                String.format("%s%s", includeLeadingBullet ? " - " : "", exportMember.getName())
                        ).withPresentableText(exportMember.getName())));
            }
            isResolved = true;
        }
    }

    private void setResolvedElementsForClasses(PsiElement element) {
        getRDFModel().getAllClasses().stream().filter(resource -> resource.getURI() != null).forEach(
                resource -> setCurieSuggestion(element, resource, null, false, 1)
        );
    }

    private void setCurieSuggestion(PsiElement elementAt, Resource resource, Resource relation, boolean reverse, int priority) {
        OMTFile omtFile = (OMTFile) elementAt.getContainingFile();
        String curieElement = omtFile.resourceToCurie(resource);
        String title = curieElement;
        AtomicBoolean registerPrefix = new AtomicBoolean(false);
        if (curieElement.equals(resource.toString())) {
            curieElement = getPrefixSuggestion(resource);
            title = resource.toString();
            registerPrefix.set(true);
        }
        if (reverse) {
            title = "^" + title;
            curieElement = "^" + curieElement;
        }
        addPriorityElement(curieElement, priority, title, (context, item) ->
                // if the iri is not registered in the page, do it
                ApplicationManager.getApplication().runWriteAction(() -> {
                    if (originalElement.getPrevSibling() instanceof OMTNamespacePrefix) {
                        // remove the existing prefix
                        int startOffset = originalElement.getPrevSibling().getTextOffset();
                        int endOffset = startOffset + originalElement.getPrevSibling().getTextLength();
                        context.getDocument().replaceString(startOffset, endOffset, "");
                        context.commitDocument();
                    }
                    if (registerPrefix.get()) {
                        curieUtil.addPrefixToBlock(context.getFile(),
                                item.getLookupString().split(":")[0].substring(reverse ? 1 : 0),
                                resource.getNameSpace());
                    }
                }), null, relation != null ? omtFile.resourceToCurie(relation) : null);
    }

    private String getPrefixSuggestion(Resource resource) {
        if (resource.getNameSpace() == null) {
            return resource.getURI();
        }
        List<OMTPrefix> knownPrefixes = projectUtil.getKnownPrefixes(resource.getNameSpace());
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

    private void addPriorityElement(String text, int priority, String title) {
        addPriorityElement(text, priority, title, (context, item) -> {
        }, null, null);
    }

    private void addPriorityElement(String text, int priority, InsertHandler insertHandler) {
        addPriorityElement(text, priority, text, insertHandler, null, null);
    }

    private void addPriorityElement(String text, int priority, String title, InsertHandler insertHandler,
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

    private void setDummyContextFromExpectedList(List<String> expectedTypes, @NotNull CompletionInitializationContext context, boolean hasValuePosition) {
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
            return;
        }
    }

    private boolean hasValuePosition(@NotNull PsiElement element, @NotNull CompletionInitializationContext context) {
        Document document = context.getEditor().getDocument();
        int line = document.getLineNumber(element.getTextOffset());
        String textAtLine = document.getText(TextRange.create(document.getLineStartOffset(line), document.getLineEndOffset(line))).trim();
        return textAtLine.contains(":");

    }

    private List<String> getExpectedTypeAtOMTFile(PsiElement element, boolean reverse) {
        PsiElement sibling = reverse ? element.getPrevSibling() : element.getNextSibling();
        while (!(sibling instanceof PsiErrorElement) && sibling != null && sibling.getNextSibling() != null) {
            sibling = reverse ? sibling.getPrevSibling() : sibling.getNextSibling();
        }

        if (reverse) {
            return sibling instanceof PsiErrorElement ? getExpectedTypesFromError((PsiErrorElement) sibling) : new ArrayList<>();
        } else {
            return sibling instanceof PsiErrorElement ? getExpectedTypesFromError((PsiErrorElement) sibling) : getExpectedTypeAtOMTFile(element, true);
        }
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
