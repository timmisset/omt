package com.misset.opp.omt;

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.external.util.builtIn.BuiltInUtil;
import com.misset.opp.omt.psi.ExampleFiles;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

class OMTCompletionContributorTest extends LightJavaCodeInsightFixtureTestCase {

    ExampleFiles exampleFiles;

    @BeforeEach
    void setUpSuite() throws Exception {
        super.setName("OMTCompletionContributorTest");
        super.setUp();
        myFixture.copyFileToProject(new File("src/test/resources/builtinCommands.ts").getAbsolutePath(), "builtinCommands.ts");
        myFixture.copyFileToProject(new File("src/test/resources/builtinOperators.ts").getAbsolutePath(), "builtinOperators.ts");
        exampleFiles = new ExampleFiles(this);
    }

    @AfterEach
    void tearDownSuite() throws Exception {
        super.tearDown();
    }

    @Test
    void completionProvider_addsSuggestionsForActivity() {
        List<String> suggestions = getSuggestions("model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        <caret>\n");
        assertContainsElements(suggestions, "title:", "graphs:", "actions:", "onCancel:", "onCommit:");
    }

    @Test
    void completionProvider_addsSuggestionsForPartiallyTypedEntry() {
        List<String> suggestions = getSuggestions("model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        ti<caret>\n");
        assertEquals(2, suggestions.size());
        assertContainsElements(suggestions, "title:", "actions:");
    }

    @Test
    void completionProvider_skipsAttributesAlreadyPartOfTheActivity() {
        List<String> suggestions = getSuggestions("model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        title: Mijn Activiteit\n" +
                "        <caret>\n");
        assertDoesntContain(suggestions, "title:");
        assertContainsElements(suggestions, "graphs:", "actions:", "onCancel:", "onCommit:");
    }

    @Test
    void completionProvider_addsSuggestionsForQueries() {
        List<String> suggestions = getSuggestions("queries: |\n" +
                "    <caret>\n");
        assertContainsElements(suggestions, "DEFINE QUERY", "DEFINE QUERY myQuery() => 'hello world';");
    }

    @Test
    void completionProvider_addsSuggestionsForCommands() {
        List<String> suggestions = getSuggestions("commands: |\n" +
                "    <caret>\n");
        assertContainsElements(suggestions, "DEFINE COMMAND", "DEFINE COMMAND myCommand() => { @LOG('hello world'); }");
    }

    @Test
    void completionProvider_addsSuggestionsForModelItemType() {
        List<String> suggestions = getSuggestions("model:\n" +
                "    MijnActiviteit: !<caret>");
        assertContainsElements(suggestions, "Activity", "Procedure", "Ontology", "Procedure", "StandaloneQuery");
    }

    @Test
    void completionProvider_addsSuggestionsForCommandInCommandBlock() {
        List<String> suggestions = getSuggestions("commands: |\n" +
                "    DEFINE COMMAND myRootCommand() => { <caret> }");
        assertContainsElements(suggestions, BuiltInUtil.SINGLETON.getBuiltInCommandsAsSuggestions());
        assertDoesntContain(suggestions, BuiltInUtil.SINGLETON.getBuiltInOperatorsAsSuggestions());
        assertNotEmpty(suggestions);
    }

    @Test
    void completionProvider_addsSuggestionsForCommandAtScriptStart() {
        List<String> suggestions = getSuggestions("model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        onStart: |\n" +
                "            <caret>\n");
        assertContainsElements(suggestions, BuiltInUtil.SINGLETON.getBuiltInCommandsAsSuggestions());
        assertDoesntContain(suggestions, BuiltInUtil.SINGLETON.getBuiltInOperatorsAsSuggestions());
        assertNotEmpty(suggestions);
    }


    @Test
    void completionProvider_addsSuggestionsForCommandAtScript() {
        List<String> suggestions = getSuggestions("model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        onStart: |\n" +
                "            @myFirstCommand();\n" +
                "            <caret>\n");
        assertContainsElements(suggestions, BuiltInUtil.SINGLETON.getBuiltInCommandsAsSuggestions());
        assertDoesntContain(suggestions, BuiltInUtil.SINGLETON.getBuiltInOperatorsAsSuggestions());
        assertNotEmpty(suggestions);
    }

    @Test
    void completionProvider_addsSuggestionsForBuiltInCommandAtScript() {
        List<String> suggestions = getSuggestions("model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        onStart: |\n" +
                "            @FOR<caret>\n");
        assertContainsElements(suggestions, "@FOREACH($param0, $param1)", "@FORKJOIN($param0)");
        assertNotEmpty(suggestions);
    }


    @Test
    void completionProvider_addsSuggestionsForLocalCommandAtScript() {
        List<String> suggestions = getSuggestions("model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        commands: |\n" +
                "            DEFINE COMMAND myFirstCommand => { @LOG('Hello world'); }\n" +
                "            \n" +
                "        onStart: |\n" +
                "            @my<caret>\n" +
                "\n");
        // should only show the myFirstCommand statement as none of the the commands start with @my
        assertEquals(1, suggestions.size());
        assertContainsElements(suggestions, "@myFirstCommand()");
    }

    @Test
    void completionProvider_addsSuggestionsForLocalVariables() {
        List<String> suggestions = getSuggestions("model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        commands: |\n" +
                "            DEFINE COMMAND myFirstCommand => { @FOREACH('', { <caret> }); }\n" +
                "\n");
        // should only show the myFirstCommand statement as none of the the commands start with @my
        assertContainsElements(suggestions, "$array", "$index", "$value");
    }

    @Test
    void completionProvider_addsSuggestionForVariableInPayload() {
        List<String> suggestions = getSuggestions("model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        variables:\n" +
                "            - $mijnVariabel\n" +
                "\n" +
                "        payload:\n" +
                "            item: <caret>\n" +
                "\n");

        assertContainsElements(suggestions, "$mijnVariabel");

    }

    @Test
    void completionProvider_addsSuggestionsForQueryStep() {
        List<String> suggestions = getSuggestions("model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        title: Mijn Activiteit\n" +
                "\n" +
                "        queries: |\n" +
                "            DEFINE QUERY myFirstQuery() => 'Hello world';\n" +
                "            DEFINE QUERY mySecondQuery($param1) => <caret>\n");

        assertContainsElements(suggestions, "$param1", "myFirstQuery");
        assertContainsElements(suggestions, BuiltInUtil.SINGLETON.getBuiltInOperatorsAsSuggestions());
    }

    private List<String> getSuggestions(String content) {
        myFixture.configureByText("test.omt", content);
        myFixture.completeBasic();
        return myFixture.getLookupElementStrings();
    }

    @Test
    void beforeCompletion() {
    }
}
