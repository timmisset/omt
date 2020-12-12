//package com.misset.opp.omt;
//
//import com.intellij.openapi.vfs.VirtualFile;
//import com.misset.opp.omt.psi.ExampleFiles;
//import org.junit.Ignore;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static com.misset.opp.omt.psi.util.UtilManager.getBuiltinUtil;
//
//@Ignore("Uitgeschakeld, wordt vervangen door alle testen vanuit de afzonderlijke completion providerss")
//class OMTCompletionContributorTest extends OMTTestSuite {
//
//    ExampleFiles exampleFiles;
//
//    @BeforeEach
//    @Override
//    public void setUp() throws Exception {
//        super.setName("OMTCompletionContributorTest");
//        super.setUp();
//        setBuiltinAndModel();
//        exampleFiles = new ExampleFiles(this, myFixture);
//
//    }
//
//    @AfterEach
//    @Override
//    public void tearDown() throws Exception {
//        super.tearDown();
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForActivity() {
//        List<String> suggestions = getSuggestions("model:\n" +
//                "    MijnActiviteit: !Activity\n" +
//                "        <caret>\n");
//        assertContainsElements(suggestions, "title:", "graphs:", "actions:", "onCancel:", "onCommit:");
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForPartiallyTypedEntry() {
//        List<String> suggestions = getSuggestions("model:\n" +
//                "    MijnActiviteit: !Activity\n" +
//                "        ti<caret>\n");
//        assertEquals(2, suggestions.size());
//        assertContainsElements(suggestions, "title:", "actions:");
//    }
//
//    @Test
//    void completionProviders_addsSuggestionsWithLeadingAttributes() {
//        List<String> suggestions = getSuggestions("model:\n" +
//                "    test: !Activity\n" +
//                "        <caret>\n" +
//                "        params:\n" +
//                "            -   $variable");
//        assertContainsElements(suggestions, "title:", "graphs:", "actions:", "onCancel:", "onCommit:");
//    }
//
//    @Test
//    void completionProvider_skipsAttributesAlreadyPartOfTheActivity() {
//        List<String> suggestions = getSuggestions("model:\n" +
//                "    MijnActiviteit: !Activity\n" +
//                "        title: Mijn Activiteit\n" +
//                "        <caret>\n");
//        assertDoesntContain(suggestions, "title:");
//        assertContainsElements(suggestions, "graphs:", "actions:", "onCancel:", "onCommit:");
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForQueries() {
//        List<String> suggestions = getSuggestions("queries: |\n" +
//                "    <caret>\n");
//        assertContainsElements(suggestions, "DEFINE QUERY", "DEFINE QUERY myQuery() => 'hello world';");
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForCommands() {
//        List<String> suggestions = getSuggestions("commands: |\n" +
//                "    <caret>\n");
//        assertContainsElements(suggestions, "DEFINE COMMAND", "DEFINE COMMAND myCommand() => { @LOG('hello world'); }");
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForModelItemType() {
//        List<String> suggestions = getSuggestions("model:\n" +
//                "    MijnActiviteit: <caret>");
//        assertContainsElements(suggestions, "!Activity", "!Procedure", "!Ontology", "!Procedure", "!StandaloneQuery");
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForModelItemTypePartial() {
//        List<String> suggestions = getSuggestions("model:\n" +
//                "    MijnActiviteit: !<caret>");
//        assertContainsElements(suggestions, "Activity", "Procedure", "Ontology", "Procedure", "StandaloneQuery");
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForCommandInCommandBlock() {
//        List<String> suggestions = getSuggestions("commands: |\n" +
//                "    DEFINE COMMAND myRootCommand() => { <caret> }");
//        assertContainsElements(suggestions, getBuiltinUtil().getBuiltInCommandsAsSuggestions());
//        assertDoesntContain(suggestions, getBuiltinUtil().getBuiltInOperatorsAsSuggestions());
//        assertNotEmpty(suggestions);
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForCommandAtScriptStart() {
//        List<String> suggestions = getSuggestions("model:\n" +
//                "    MijnActiviteit: !Activity\n" +
//                "        onStart: |\n" +
//                "            @<caret>\n");
//        assertContainsElements(suggestions, getBuiltinUtil().getBuiltInCommandsAsSuggestions());
//        assertDoesntContain(suggestions, getBuiltinUtil().getBuiltInOperatorsAsSuggestions());
//        assertNotEmpty(suggestions);
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForCommandAtScript() {
//        List<String> suggestions = getSuggestions("model:\n" +
//                "    MijnActiviteit: !Activity\n" +
//                "        onStart: |\n" +
//                "            @myFirstCommand();\n" +
//                "            @<caret>\n");
//        assertContainsElements(suggestions, getBuiltinUtil().getBuiltInCommandsAsSuggestions());
//        assertDoesntContain(suggestions, getBuiltinUtil().getBuiltInOperatorsAsSuggestions());
//        assertNotEmpty(suggestions);
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForBuiltInCommandAtScript() {
//        List<String> suggestions = getSuggestions("model:\n" +
//                "    MijnActiviteit: !Activity\n" +
//                "        onStart: |\n" +
//                "            @FOR<caret>\n");
//        assertContainsElements(suggestions, "@FOREACH($param0, $param1)", "@FORKJOIN($param0)");
//        assertNotEmpty(suggestions);
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForBuiltInCommandAtScriptAtAt() {
//        List<String> suggestions = getSuggestions("model:\n" +
//                "    MijnActiviteit: !Activity\n" +
//                "        onStart: |\n" +
//                "            @<caret>\n");
//        assertContainsElements(suggestions, "@FOREACH($param0, $param1)", "@FORKJOIN($param0)");
//        assertNotEmpty(suggestions);
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForBuiltInCommandAtScriptAtAtWithSemicolon() {
//        List<String> suggestions = getSuggestions("model:\n" +
//                "    MijnActiviteit: !Activity\n" +
//                "        onStart: |\n" +
//                "            @<caret>;\n");
//        assertContainsElements(suggestions, "@FOREACH($param0, $param1)", "@FORKJOIN($param0)");
//        assertNotEmpty(suggestions);
//    }
//
//
//    @Test
//    void completionProvider_addsSuggestionsForLocalCommandAtScript() {
//        List<String> suggestions = getSuggestions("model:\n" +
//                "    MijnActiviteit: !Activity\n" +
//                "        commands: |\n" +
//                "            DEFINE COMMAND myFirstCommand => { @LOG('Hello world'); }\n" +
//                "            \n" +
//                "        onStart: |\n" +
//                "            @my<caret>\n" +
//                "\n");
//        // should only show the myFirstCommand statement as none of the the commands start with @my
//        assertEquals(1, suggestions.size());
//        assertContainsElements(suggestions, "@myFirstCommand()");
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForLocalVariables() {
//        List<String> suggestions = getSuggestions("model:\n" +
//                "    MijnActiviteit: !Activity\n" +
//                "        commands: |\n" +
//                "            DEFINE COMMAND myFirstCommand => { @FOREACH('', { <caret> }); }\n" +
//                "\n");
//        // should only show the myFirstCommand statement as none of the the commands start with @my
//        assertContainsElements(suggestions, "$array", "$index", "$value");
//    }
//
//    @Test
//    void completionProvider_addsSuggestionForVariableInPayload() {
//        List<String> suggestions = getSuggestions("model:\n" +
//                "    MijnActiviteit: !Activity\n" +
//                "        variables:\n" +
//                "            - $mijnVariabel\n" +
//                "\n" +
//                "        payload:\n" +
//                "            item: <caret>\n" +
//                "\n");
//
//        assertContainsElements(suggestions, "$mijnVariabel");
//
//    }
//
//    @Test
//    void completionProvider_addsSuggestionForParameterWithTypeWithoutKnownPrefix() {
//        List<String> suggestions = getSuggestions("model:\n" +
//                "    MijnActiviteit: !Activity\n" +
//                "        params:\n" +
//                "            - $mijnParam (<caret>)\n" +
//                "\n");
//
//        assertContainsElements(suggestions, "http://ontologie#ClassA", "http://ontologie#ClassB", "http://ontologie#ClassC");
//    }
//
//
//    @Test
//    void completionProvider_addsSuggestionForParameterWithTypeWithKnownPrefix() {
//        List<String> suggestions = getSuggestions("" +
//                "prefixes:\n" +
//                "    ont:     <http://ontologie#>\n" +
//                "model:\n" +
//                "    MijnActiviteit: !Activity\n" +
//                "        params:\n" +
//                "            - $mijnParam (<caret>)\n" +
//                "\n");
//
//        assertContainsElements(suggestions, "ont:ClassA", "ont:ClassB", "ont:ClassC");
//    }
//
//    @Test
//    void completionProvider_addsSuggestionForParameterWithTypeWithKnownPrefixWithExistingType() {
//        List<String> suggestions = getSuggestions("" +
//                "prefixes:\n" +
//                "    ont:     <http://ontologie#>\n" +
//                "model:\n" +
//                "    MijnActiviteit: !Activity\n" +
//                "        params:\n" +
//                "            - $mijnParam (<caret>ont:ClassA)\n" +
//                "\n");
//
//        assertContainsElements(suggestions, "ont:ClassA", "ont:ClassB", "ont:ClassC");
//    }
//
//    @Test
//    void completionProvider_addsSuggestionForParameterWithTypeWithKnownPrefixPartial() {
//        List<String> suggestions = getSuggestions("" +
//                "prefixes:\n" +
//                "    ont:     <http://ontologie#>\n" +
//                "model:\n" +
//                "    MijnActiviteit: !Activity\n" +
//                "        params:\n" +
//                "            - $mijnParam (Cla<caret>)\n" +
//                "\n");
//
//        assertContainsElements(suggestions, "ont:ClassA", "ont:ClassB", "ont:ClassC");
//    }
//
//    @Test
//    void completionProvider_addsSuggestionForParameterWithTypeWithKnownPrefixAfterPrefix() {
//        List<String> suggestions = getSuggestions("" +
//                "prefixes:\n" +
//                "    ont:     <http://ontologie#>\n" +
//                "model:\n" +
//                "    MijnActiviteit: !Activity\n" +
//                "        params:\n" +
//                "            - $mijnParam (ont:<caret>)\n" +
//                "\n");
//
//        assertContainsElements(suggestions, "ont:ClassA", "ont:ClassB", "ont:ClassC");
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForQueryStep() {
//        List<String> suggestions = getSuggestions("model:\n" +
//                "    MijnActiviteit: !Activity\n" +
//                "        title: Mijn Activiteit\n" +
//                "\n" +
//                "        queries: |\n" +
//                "            DEFINE QUERY myFirstQuery() => 'Hello world';\n" +
//                "            DEFINE QUERY mySecondQuery($param1) => <caret>\n");
//
//        assertContainsElements(suggestions, "$param1", "myFirstQuery");
//        assertContainsElements(suggestions, getBuiltinUtil().getBuiltInOperatorsAsSuggestions());
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForQueryPathFromModelReverse() {
//        List<String> suggestions = getSuggestions("" +
//                "prefixes:\n" +
//                "    ont:     <http://ontologie#>\n" +
//                "queries: |\n" +
//                "    DEFINE QUERY myFirstQuery() => 'Hello world' / <caret>;\n");
//        assertContainsElements(suggestions, "^ont:stringProperty");
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForQueryPathFromModelForward() {
//        List<String> suggestions = getSuggestions("" +
//                "prefixes:\n" +
//                "    ont:     <http://ontologie#>\n" +
//                "queries: |\n" +
//                "    DEFINE QUERY myFirstQuery() => 'Hello world' / ^ont:stringProperty / <caret>;\n");
//        assertContainsElements(suggestions, "ont:stringProperty", "^ont:classProperty");
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForQueryPathFromModelForwardForClass() {
//        List<String> suggestions = getSuggestions("" +
//                "prefixes:\n" +
//                "    ont:     <http://ontologie#>\n" +
//                "queries: |\n" +
//                "    DEFINE QUERY myFirstQuery() => /ont:ClassA / <caret>;\n");
//        assertContainsElements(suggestions, "ont:stringProperty", "ont:classProperty", "ont:booleanProperty");
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForQueryPathFromModelForwardForClassWhenPathIsNotClosed() {
//        List<String> suggestions = getSuggestions("" +
//                "prefixes:\n" +
//                "    ont:     <http://ontologie#>\n" +
//                "queries: |\n" +
//                "    DEFINE QUERY myFirstQuery() => /ont:ClassA / <caret>\n");
//        assertContainsElements(suggestions, "ont:stringProperty", "ont:classProperty", "ont:booleanProperty");
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForQueryPathFromModelForwardForClassWhenPathIsNotClosedPartial() {
//        List<String> suggestions = getSuggestions("" +
//                "prefixes:\n" +
//                "    ont:     <http://ontologie#>\n" +
//                "queries: |\n" +
//                "    DEFINE QUERY myFirstQuery() => /ont:ClassA / ont:<caret>\n");
//        assertContainsElements(suggestions, "ont:stringProperty", "ont:classProperty", "ont:booleanProperty");
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForQueryPathFromModelForwardInFilter() {
//        List<String> suggestions = getSuggestions("" +
//                "prefixes:\n" +
//                "    ont:     <http://ontologie#>\n" +
//                "queries: |\n" +
//                "    DEFINE QUERY myFirstQuery() => /ont:ClassA [<caret>];\n");
//        assertContainsElements(suggestions, "ont:stringProperty", "ont:classProperty", "ont:booleanProperty");
//    }
//
//    @Test
//    void completionProvider_addsSuggestionsForQueryPathInExistingPath() {
//        List<String> suggestions = getSuggestions("" +
//                "prefixes:\n" +
//                "    ont:     <http://ontologie#>\n" +
//                "    rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                "queries: |\n" +
//                "    DEFINE QUERY myFirstQuery() => /ont:ClassA / ^rdf:type / <caret>ont:classProperty / ont:stringProperty;\n");
//        assertContainsElements(suggestions, "ont:classProperty");
//    }
//
//    @Test
//    void completionProviders_addsSuggestionsForImport() {
//        String exportData = "queries: |\n" +
//                "   DEFINE QUERY myQuery() => 'hello';\n" +
//                "";
//
//        copyFileToProject(exportData, "frontend/libs", "exportFile.omt");
//
//        List<String> suggestions = getSuggestions("" +
//                "import:\n" +
//                "    '@client/exportFile.omt':\n" +
//                "    <caret>\n" +
//                "\n");
//        assertContainsElements(suggestions, "myQuery");
//    }
//
//    @Test
//    void completionProviders_addsSuggestionsForImport2() {
//        String exportData = "queries: |\n" +
//                "   DEFINE QUERY myQuery() => 'hello';\n" +
//                "";
//
//        copyFileToProject(exportData, "frontend/libs", "exportFile.omt");
//
//        List<String> suggestions = getSuggestions("" +
//                "import:\n" +
//                "    '@client/exportFile.omt':\n" +
//                "       - <caret>\n");
//        assertContainsElements(suggestions, "myQuery");
//    }
//
//    @Test
//    void completionProviders_addsSuggestionForDeclaredVariable() {
//        String content = "commands: |\n" +
//                "   DEFINE COMMAND command() => {\n" +
//                "       VAR $myVar = 'test';\n" +
//                "       <caret>\n" +
//                "   }\n";
//        List<String> suggestions = getSuggestions(content);
//        assertContainsElements(suggestions, "$myVar");
//    }
//
//    private List<String> getSuggestions(String content) {
//        myFixture.configureFromExistingVirtualFile(copyFileToProject(content, "tmp", "test.omt"));
//        myFixture.completeBasic();
//        return myFixture.getLookupElementStrings();
//    }
//
//    private VirtualFile copyFileToProject(String content, String folder, String fileName) {
//        String target = String.format("%s/%s", folder, fileName);
//        return myFixture.addFileToProject(target, content).getVirtualFile();
//    }
//
//}
