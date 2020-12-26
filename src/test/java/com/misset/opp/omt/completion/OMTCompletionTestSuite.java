package com.misset.opp.omt.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.OMTTestSuite;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.util.UtilManager.getBuiltinUtil;

public class OMTCompletionTestSuite extends OMTTestSuite {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setBuiltinAndModel();
    }

    protected void assertCompletionSameContents(String content, List<String> expected) {
        assertSameElements(getCompletionLookupElements(content), expected);
    }

    protected void assertCompletionAutocompleted(String content, String expectedContentAfterCompletion) {
        getCompletionLookupElements(content);
        assertEquals(expectedContentAfterCompletion, myFixture.getEditor().getDocument().getText());
    }

    protected void assertCompletionContains(String content, String... expected) {
        assertContainsElements(getCompletionLookupElements(content), expected);
    }

    protected void assertCompletionNotContains(String content, String... expected) {
        assertDoesntContain(getCompletionLookupElements(content), expected);
    }

    protected List<String> getCompletionLookupElements(String content) {
        // we need to create real files for the import completions to work, they require relative lookups to determine
        // the imported file
        final PsiFile psiFile = myFixture.addFileToProject(String.format("tmp/%s", getFileName()), content);
        // then set the fixture on the added file
        myFixture.configureFromExistingVirtualFile(psiFile.getVirtualFile());
        // finally run the completions
        final LookupElement[] lookupElements = myFixture.completeBasic();
        return parseSuggestions(lookupElements);
    }

    protected List<String> parseSuggestions(LookupElement[] lookupElements) {
        if (lookupElements == null || lookupElements.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.stream(lookupElements).filter(Objects::nonNull).map(LookupElement::getLookupString).collect(Collectors.toList());
    }

    protected void assertCompletionContainsGlobalVariables(String content) {
        assertCompletionContainsGlobalVariables(getCompletionLookupElements(content));
    }

    protected void assertCompletionContainsGlobalVariables(List<String> completionResult) {
        assertContainsElements(completionResult, "$username", "$medewerkerGraph", "$offline");
    }

    protected void assertCompletionNOTContainsGlobalVariables(List<String> completionResult) {
        assertDoesntContain(completionResult, "$username", "$medewerkerGraph", "$offline");
    }

    protected void assertCompletionNOTContainsBuiltinOperators(List<String> completionResult) {
        final List<String> builtInOperatorsAsSuggestions = getBuiltinUtil().getBuiltInOperatorsAsSuggestions();
        assertNotEmpty(builtInOperatorsAsSuggestions);
        assertDoesntContain(completionResult, builtInOperatorsAsSuggestions);
    }

    protected void assertCompletionNOTContainsBuiltinCommands(List<String> completionResult) {
        final List<String> builtInCommandsAsSuggestions = getBuiltinUtil().getBuiltInCommandsAsSuggestions();
        assertNotEmpty(builtInCommandsAsSuggestions);
        assertDoesntContain(completionResult, builtInCommandsAsSuggestions);
    }

    protected void assertCompletionContainsBuiltinOperators(List<String> completionResult) {
        final List<String> builtInOperatorsAsSuggestions = getBuiltinUtil().getBuiltInOperatorsAsSuggestions();
        assertNotEmpty(builtInOperatorsAsSuggestions);
        assertContainsElements(completionResult, builtInOperatorsAsSuggestions);
    }

    protected void assertCompletionContainsBuiltinCommands(List<String> completionResult) {
        final List<String> builtInCommandsAsSuggestions = getBuiltinUtil().getBuiltInCommandsAsSuggestions();
        assertNotEmpty(builtInCommandsAsSuggestions);
        assertContainsElements(completionResult, builtInCommandsAsSuggestions);
    }

    protected void assertCompletionContainsClasses(List<String> completionResult, boolean startPath) {
        String prefix = startPath ? "/" : "";
        Arrays.stream(new String[]{"ClassA", "ClassB", "ClassC"})
                .forEach(
                        className -> assertTrue(completionResult
                                .stream()
                                .anyMatch(s ->
                                        s.equals(String.format("%shttp://ontologie#%s", prefix, className)) ||
                                                s.equals(String.format("%sont:%s", prefix, className))))
                );
    }

}
