package com.misset.opp.omt.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.misset.opp.omt.OMTTestSuite;

import java.util.Arrays;
import java.util.List;
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

    protected void assertCompletionContains(String content, String... expected) {
        assertContainsElements(getCompletionLookupElements(content), expected);
    }

    protected void assertCompletionNotContains(String content, String... expected) {
        assertDoesntContain(getCompletionLookupElements(content), expected);
    }

    protected List<String> getCompletionLookupElements(String content) {
        myFixture.configureByText(getFileName(), content);
        final LookupElement[] lookupElements = myFixture.completeBasic();
        return parseSuggestions(lookupElements);
    }

    protected List<String> parseSuggestions(LookupElement[] lookupElements) {
        return Arrays.stream(lookupElements).map(LookupElement::getLookupString).collect(Collectors.toList());
    }

    protected void assertCompletionContainsGlobalVariables(String content) {
        assertCompletionContainsGlobalVariables(getCompletionLookupElements(content));
    }

    protected void assertCompletionContainsGlobalVariables(List<String> completionResult) {
        assertContainsElements(completionResult, "$username", "$medewerkerGraph", "$offline");
    }

    protected void assertCompletionDoesntGlobalVariables(List<String> completionResult) {
        assertDoesntContain(completionResult, "$username", "$medewerkerGraph", "$offline");
    }

    protected void assertCompletionDoesntContainBuiltinOperators(List<String> completionResult) {
        final List<String> builtInOperatorsAsSuggestions = getBuiltinUtil().getBuiltInOperatorsAsSuggestions();
        assertNotEmpty(builtInOperatorsAsSuggestions);
        assertDoesntContain(completionResult, builtInOperatorsAsSuggestions);
    }

    protected void assertCompletionContainsBuiltinOperators(List<String> completionResult) {
        final List<String> builtInOperatorsAsSuggestions = getBuiltinUtil().getBuiltInOperatorsAsSuggestions();
        assertNotEmpty(builtInOperatorsAsSuggestions);
        assertContainsElements(completionResult, builtInOperatorsAsSuggestions);
    }

    protected void assertCompletionContainsClasses(String content) {
        assertCompletionContainsClasses(getCompletionLookupElements(content));
    }

    protected void assertCompletionContainsClasses(List<String> completionResult) {
        assertContainsElements(completionResult, "http://ontologie#ClassA", "http://ontologie#ClassB", "http://ontologie#ClassC");
    }

}
