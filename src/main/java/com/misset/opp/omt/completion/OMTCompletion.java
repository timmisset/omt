package com.misset.opp.omt.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTImportBlock;
import com.misset.opp.omt.psi.OMTMemberListItem;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.support.OMTCallable;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import com.misset.opp.omt.psi.support.OMTExportMember;
import com.misset.opp.omt.psi.util.MemberUtil;
import com.misset.opp.omt.settings.OMTSettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.misset.opp.util.UtilManager.getBuiltinUtil;
import static com.misset.opp.util.UtilManager.getImportUtil;
import static com.misset.opp.util.UtilManager.getMemberUtil;
import static com.misset.opp.util.UtilManager.getModelUtil;
import static com.misset.opp.util.UtilManager.getProjectUtil;
import static com.misset.opp.util.UtilManager.getVariableUtil;

public abstract class OMTCompletion {

    protected static final ElementPattern<OMTMemberListItem> IMPORT_MEMBER_PATTERN =
            PlatformPatterns.psiElement(OMTMemberListItem.class).inside(
                    OMTImportBlock.class
            );
    /**
     * The higher the priority number, the higher it gets listed
     */
    protected static final int MODEL_ITEM_TYPE_PRIORITY = 13;
    protected static final int ATTRIBUTES_PRIORITY = 12;              // model entry attributes
    protected static final int EQUATION_PRIORITY = 11;                // the other side of the equation, shows a limited set of options based on the resolved type of the other side
    protected static final int PREDICATE_FORWARD_PRIORITY = 10;
    protected static final int PREDICATE_REVERSE_PRIORITY = 9;
    protected static final int CLASSES_PRIORITY = 8;                  // list with available classes
    protected static final int LOCAL_VARIABLE_PRIORITY = 7;
    protected static final int DECLARED_VARIABLE_PRIORITY = 6;
    protected static final int GLOBAL_VARIABLE_PRIORITY = 5;
    protected static final int DEFINED_STATEMENT_PRIORITY = 4;
    protected static final int LOCAL_COMMAND_PRIORITY = 3;
    protected static final int BUILTIN_MEMBER_PRIORITY = 2;
    protected static final int IMPORTABLE_MEMBER_PRIORITY = 1;
    private static final InsertHandler<LookupElement> NO_INSERT_HANDLER = (context, item) -> {
    };
    protected final String ATTRIBUTES = "attributes";
    private final List<LookupElement> resolvedElements = new ArrayList<>();
    private final List<String> resolvedSuggestions = new ArrayList<>();

    public CompletionProvider<CompletionParameters> getCompletionProvider() {
        return null;
    }

    protected void complete(@NotNull CompletionResultSet result) {
        result.addAllElements(resolvedElements);
        resolvedElements.clear();
        resolvedSuggestions.clear();
    }

    protected void addPriorityElement(String text, int priority) {
        addPriorityElement(text, priority, text, NO_INSERT_HANDLER, null, null);
    }

    protected void addPriorityElement(String text, int priority, List<String> withLookupStrings) {
        addPriorityElement(text, priority, text, NO_INSERT_HANDLER, null, null, withLookupStrings);
    }

    protected void addPriorityElement(String text, int priority, List<String> withLookupStrings, String tailText) {
        addPriorityElement(text, priority, text, NO_INSERT_HANDLER, tailText, null, withLookupStrings);
    }

    protected void addPriorityElement(String text, int priority, List<String> withLookupStrings, String tailText, String typeText) {
        addPriorityElement(text, priority, text, NO_INSERT_HANDLER, tailText, typeText, withLookupStrings);
    }

    protected void addPriorityElement(String text,
                                      int priority,
                                      String title,
                                      InsertHandler<LookupElement> insertHandler,
                                      String tailText,
                                      String typeText) {
        addPriorityElement(text, priority, title, insertHandler, tailText, typeText, new ArrayList<>());
    }

    protected void addPriorityElement(String text,
                                      int priority,
                                      String title,
                                      InsertHandler<LookupElement> insertHandler,
                                      String tailText,
                                      String typeText,
                                      List<String> withLookupStrings) {
        if (resolvedSuggestions.contains(title)) {
            return;
        }
        LookupElementBuilder lookupElementBuilder = LookupElementBuilder
                .create(text)
                .withPresentableText(title)
                .withInsertHandler(insertHandler)
                .withLookupStrings(withLookupStrings);
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

    protected void setResolvedElementsForGlobalVariables() {
        getVariableUtil().getGlobalVariables().forEach(
                omtVariable -> addPriorityElement(omtVariable, GLOBAL_VARIABLE_PRIORITY)
        );
    }

    protected void setResolvedElementsForVariables(PsiElement element) {
        setResolvedElementsForLocalVariables(element);
        setResolvedElementsForDeclaredVariables(element);
        setResolvedElementsForGlobalVariables();
    }

    protected void setResolvedElementsForDefinedQueries(PsiElement element, Predicate<OMTCallable> typeCheck) {
        setResolvedElementsForDefined(element, OMTDefinedStatement::isQuery, typeCheck);
    }

    protected void setResolvedElementsForDefinedQueries(PsiElement element) {
        setResolvedElementsForDefined(element, OMTDefinedStatement::isQuery, callable -> true);
    }

    protected void setResolvedElementsForDefinedCommands(PsiElement element, Predicate<OMTCallable> typeCheck) {
        setResolvedElementsForDefined(element, OMTDefinedStatement::isCommand, typeCheck);
    }

    protected void setResolvedElementsForDefinedCommands(PsiElement element) {
        setResolvedElementsForDefined(element, OMTDefinedStatement::isCommand, callable -> true);
    }

    private void setResolvedElementsForDefined(PsiElement element,
                                               Predicate<OMTDefinedStatement> condition,
                                               Predicate<OMTCallable> typeCheck) {
        final MemberUtil memberUtil = getMemberUtil();
        final List<OMTDefinedStatement> accessibleDefinedStatements = new ArrayList<>(memberUtil.getAccessibleDefinedStatements(element));
        accessibleDefinedStatements.addAll(memberUtil.getSameScriptDefinedStatement(element));
        accessibleDefinedStatements
                .stream()
                .filter(condition)
                .map(definedStatement -> memberUtil.parseDefinedToCallable(definedStatement.getDefineName()))
                .filter(typeCheck)
                .map(OMTCallable::getAsSuggestion)
                .forEach(
                        suggestion -> addPriorityElement(suggestion, DEFINED_STATEMENT_PRIORITY)
                );
    }

    protected void setResolvedElementsForBuiltinOperators() {
        setResolvedElementsForBuiltinOperators(callable -> true);
    }

    protected void setResolvedElementsForBuiltinCommands() {
        setResolvedElementsForBuiltinCommands(callable -> true);
    }

    protected void setResolvedElementsForBuiltinOperators(Predicate<OMTCallable> condition) {
        getBuiltinUtil().getBuiltInOperatorsAsSuggestions(condition)
                .forEach(suggestion -> addPriorityElement(suggestion, BUILTIN_MEMBER_PRIORITY));
    }

    protected void setResolvedElementsForBuiltinCommands(Predicate<OMTCallable> condition) {
        getBuiltinUtil().getBuiltInCommandsAsSuggestions(condition)
                .forEach(suggestion -> addPriorityElement(suggestion, BUILTIN_MEMBER_PRIORITY));
    }

    protected void setResolvedElementsForExportedCommands(PsiFile originalFile, PsiElement element) {
        getProjectUtil().getExportedMembers(true)
                .forEach(omtExportMember -> setResolvedElementsForImportableMembers(omtExportMember, originalFile, element));
    }

    protected void setResolvedElementsForExportedOperators(PsiFile originalFile, PsiElement element) {
        getProjectUtil().getExportedMembers(false)
                .forEach(omtExportMember -> setResolvedElementsForImportableMembers(omtExportMember, originalFile, element));
    }

    /**
     * Include all BuiltInCommands, ExportedCommands and DefinedCommands
     */
    protected void setResolvedElementsForCommands(PsiElement element, PsiFile originalFile) {
        setResolvedElementsForBuiltinCommands();
        setResolvedElementsForExportedCommands(originalFile, element);
        setResolvedElementsForDefinedCommands(element);
    }

    protected void setResolvedElementsForOperators(PsiElement element, PsiFile originalFile) {
        setResolvedElementsForBuiltinOperators();
        setResolvedElementsForExportedOperators(originalFile, element);
        setResolvedElementsForDefinedQueries(element);
    }

    private void setResolvedElementsForImportableMembers(OMTExportMember exportMember, PsiFile originalFile, PsiElement element) {
        final PsiElement resolvingElement = exportMember.getResolvingElement();
        final PsiFile containingFile = resolvingElement != null ? resolvingElement.getContainingFile() : null;
        if (!includeImport(containingFile)) return;
        if (exportMember.isComponent() && !((OMTFile) originalFile).isModuleFile()) return;

        assert containingFile != null;

        String title = exportMember.getAsSuggestion();
        final String callableType = exportMember.getCallableType();
        if (originalFile == containingFile) {
            final Optional<OMTModelItemBlock> modelItemBlock = getModelUtil().getModelItemBlock(element);
            if (modelItemBlock.isPresent() &&
                    exportMember.getResolvingElement() instanceof OMTModelItemLabel &&
                    modelItemBlock.get().getModelItemLabel().getName().equals(
                            ((OMTModelItemLabel) exportMember.getResolvingElement()).getName()
                    )) return; // do not show auto-complete that will result in self-referencing
            addPriorityElement(title, LOCAL_COMMAND_PRIORITY, Collections.emptyList(), "", callableType);
        } else {
            final String folder = containingFile.getContainingDirectory() != null ?
                    containingFile.getContainingDirectory().getName() :
                    "<root>";
            String path = String.format("%s/%s", folder, containingFile.getName());
            addPriorityElement(title, IMPORTABLE_MEMBER_PRIORITY, title,
                    (context, item) -> {
                        OMTFile omtFile = (OMTFile) context.getFile();
                        if (!omtFile.hasImportFor(exportMember)) {
                            List<String> importPaths = getImportUtil().getImportPaths(exportMember, omtFile);
                            if (!importPaths.isEmpty()) {
                                String importPath = importPaths.get(0);
                                getImportUtil().addImportMemberToBlock(context.getFile(), importPath, exportMember.getName());
                            }
                        }
                    }, path, callableType);
        }

    }

    private boolean includeImport(PsiFile containingFile) {
        // filter out suggestions from mocha subfolders
        OMTSettingsState settings = OMTSettingsState.getInstance();
        final boolean valid = containingFile != null && containingFile.isValid() && containingFile.getVirtualFile() != null;
        boolean hasMochaSubfolder = valid && containingFile.getVirtualFile().getPath().contains("/mocha/");
        return valid && (!hasMochaSubfolder || settings.includeMochaFolderImportSuggestions);
    }
}
