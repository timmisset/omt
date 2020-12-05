package com.misset.opp.omt.psi.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.ExampleFiles;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTParameterWithType;
import com.misset.opp.omt.psi.OMTQuery;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

class PsiImplUtilTest extends OMTTestSuite {

    private final ExampleFiles exampleFiles = new ExampleFiles(this, myFixture);

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setName("OMTPsiImplUtilTest");
        super.setUp();

        setBuiltinOperators();
        setOntologyModel();

    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    // parse first argument and expect the resourceList
    static Stream<Arguments> basicScenarios() {
        return Stream.of(
                Arguments.of("/ont:ClassA", new String[]{"http://ontologie#ClassA"}),
                Arguments.of("'test'", new String[]{"http://www.w3.org/2001/XMLSchema#string"}),
                Arguments.of("10", new String[]{"http://www.w3.org/2001/XMLSchema#int"}),
                Arguments.of("10.0", new String[]{"http://www.w3.org/2001/XMLSchema#double"}),
                Arguments.of("false", new String[]{"http://www.w3.org/2001/XMLSchema#boolean"}),
                Arguments.of("true", new String[]{"http://www.w3.org/2001/XMLSchema#boolean"}),
                Arguments.of("null", new String[0]),
                Arguments.of("10 | 'test'", new String[]{"http://www.w3.org/2001/XMLSchema#int", "http://www.w3.org/2001/XMLSchema#string"}),
                Arguments.of("true | false", new String[]{"http://www.w3.org/2001/XMLSchema#boolean"}),
                Arguments.of("/ont:ClassA / ont:classProperty", new String[]{"http://ontologie#ClassC"}),
                Arguments.of("$variable / ont:classProperty", new String[]{"http://ontologie#ClassC"}),
                Arguments.of("'test' / ^ont:stringProperty", new String[]{"http://ontologie#ClassA", "http://ontologie#ClassB", "http://ontologie#ClassC"}),
                Arguments.of("'test' / (^ont:stringProperty)", new String[]{"http://ontologie#ClassA", "http://ontologie#ClassB", "http://ontologie#ClassC"}),
                Arguments.of("true / ^ont:booleanProperty", new String[]{"http://ontologie#ClassA"}),
                Arguments.of("$variable / ^ont:booleanProperty", new String[]{"http://ontologie#ClassA"}),
                Arguments.of("/ont:ClassA / ^rdf:type", new String[]{"http://ontologie#ClassA"}),
                Arguments.of("$mijnParameter / CEIL", new String[]{"http://www.w3.org/2001/XMLSchema#decimal"}), // return decimal type from builtInOperator
                Arguments.of("/ont:ClassA / ^rdf:type / FIRST", new String[]{"http://ontologie#ClassA"}) // no returnType, return input
        );
    }

    private OMTQuery parseQuery(String query) {
        String parsed = String.format("prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "    rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => %s;\n", query);
        final OMTFile omtFile = (OMTFile) myFixture.configureByText("test.omt", parsed);
        return exampleFiles.getPsiElementFromRootDocument(OMTQuery.class, omtFile);
    }

    private OMTQuery parseQueryFromContent(String content) {
        final OMTFile omtFile = (OMTFile) myFixture.configureByText("test.omt", content);
        return exampleFiles.getPsiElementFromRootDocument(OMTQuery.class, omtFile);
    }

    private <T> T parseQueryFromContent(String content, Predicate<T> condition) {
        final OMTFile omtFile = (OMTFile) myFixture.configureByText("test.omt", content);
        return exampleFiles.getPsiElementFromRootDocument(OMTQuery.class, omtFile, condition);
    }

    private void validateResources(List<Resource> resources, String... expected) {
        assertEquals(expected.length, resources.size());
        resources.forEach(
                resource -> assertTrue(Arrays.stream(expected).anyMatch(expectedValue ->
                        expectedValue.equals(resource.toString())
                ))
        );
    }

    private List<Resource> resolve(OMTQuery query) {
        return ApplicationManager.getApplication().runReadAction(
                (Computable<List<Resource>>) query::resolveToResource
        );
    }

    @ParameterizedTest
    @MethodSource("basicScenarios")
    void testResolveBasicQueriesToResourceTypes(String parse, String... expectedClasses) {
        final OMTQuery query = parseQuery(parse);
        final List<Resource> resources = resolve(query);
        validateResources(resources, expectedClasses);
    }

    @Test
    void queryPathResolveToResourceResolvesFilterWithTypeAllFilteredOut() {
        String content = "prefixes:\n" +
                "    rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => 'test' / ^ont:stringProperty [rdf:type == /ont:ClassA];\n";
        OMTQuery query = parseQueryFromContent(content);
        ApplicationManager.getApplication().runReadAction(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassA"));
    }

    @Test
    void queryPathResolveToResourceResolvesFilterWithType() {
        String content = "prefixes:\n" +
                "    rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => 'test' / ^ont:stringProperty [rdf:type == /ont:ClassB];\n";
        OMTQuery query = parseQueryFromContent(content);
        ApplicationManager.getApplication().runReadAction(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassB"));
    }

    @Test
    void queryPathResolveToResourceResolvesFilterWithTypeFromUnknown() {
        String content = "prefixes:\n" +
                "    rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => $unknown / ^ont:stringProperty [rdf:type == /ont:ClassB];\n";
        OMTQuery query = parseQueryFromContent(content);
        ApplicationManager.getApplication().runReadAction(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassB"));
    }

    @Test
    void queryPathResolveToResourceResolvesReverseRDFType() {
        String content = "prefixes:\n" +
                "    rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => /ont:ClassB / ^rdf:type;\n";
        OMTQuery query = parseQueryFromContent(content);
        ApplicationManager.getApplication().runReadAction(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassA", "http://ontologie#ClassB"));
    }

    @Test
    void queryPathResolveToResourceResolvesNOTFilterWithType() {
        String content = "prefixes:\n" +
                "    rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => 'test' / ^ont:stringProperty [NOT rdf:type == /ont:ClassB];\n";
        OMTQuery query = parseQueryFromContent(content);
        ApplicationManager.getApplication().runReadAction(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassA", "http://ontologie#ClassC"));
    }

    @Test
    void queryPathResolveToResourceResolvesANDFilterWithType() {
        String content = "prefixes:\n" +
                "    rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => 'test' / ^ont:stringProperty ['a' == 'b' AND rdf:type == /ont:ClassB];\n";
        OMTQuery query = parseQueryFromContent(content);
        ApplicationManager.getApplication().runReadAction(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassB"));
    }

    @Test
    void queryPathResolveToResourceResolvesANDNOTFilterWithType() {
        String content = "prefixes:\n" +
                "    rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => 'test' / ^ont:stringProperty ['a' == 'b' AND NOT rdf:type == /ont:ClassB];\n";
        OMTQuery query = parseQueryFromContent(content);
        ApplicationManager.getApplication().runReadAction(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassA", "http://ontologie#ClassC"));
    }

    @Test
    void queryPathResolveToResource_ResolvesParameterType() {
        String content = "prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "model:\n" +
                "   activity: !Activity\n" +
                "       params:\n" +
                "           -   $mijnParam (ont:ClassA)\n" +
                "       queries: |\n" +
                "           DEFINE QUERY test() => $mijnParam;\n";

        OMTQuery query = parseQueryFromContent(content);
        ApplicationManager.getApplication().runReadAction(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassA"));

    }

    @Test
    void queryPathResolveToResource_ResolvesFromAnnotation() {
        String content = "prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    /**\n" +
                "    * @param $mijnParameter (ont:ClassA)\n" +
                "    */\n" +
                "    DEFINE QUERY myQuery($mijnParameter) => $mijnParameter;";

        OMTQuery query = parseQueryFromContent(content);
        ApplicationManager.getApplication().runReadAction(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassA"));
    }

    @Test
    void queryPathResolveToResource_ResolvesFromAnnotationMultiple() {
        String content = "prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    /**\n" +
                "    * @param $mijnParameterA (ont:ClassA)\n" +
                "    * @param $mijnParameterB (ont:ClassB)\n" +
                "    */\n" +
                "    DEFINE QUERY myQuery($mijnParameterA, $mijnParameterB) => $mijnParameterB;";

        OMTQuery query = parseQueryFromContent(content);
        ApplicationManager.getApplication().runReadAction(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassB"));
    }

    @Test
    void queryPathResolveToResource_ResolvesFromQuery() {
        String content = "queries: |\n" +
                "    DEFINE QUERY myQuery($mijnParameter) => $mijnParameter / CEIL;\n" +
                "    DEFINE QUERY myQuery2() => myQuery;";

        OMTQuery query = parseQueryFromContent(content, omtQuery -> omtQuery.getDefinedName().equals("myQuery2"));
        ApplicationManager.getApplication().runReadAction(() -> validateResources(query.resolveToResource(), "http://www.w3.org/2001/XMLSchema#decimal"));
    }


    @Test
    void getType_ParameterWithTypeFromCurie() {
        String content = "prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "model:\n" +
                "   activity: !Activity\n" +
                "       params:\n" +
                "           -   $mijnParam (ont:ClassA)\n";

        final PsiFile psiFile = myFixture.configureByText("test.omt", content);
        OMTParameterWithType parameterWithType = exampleFiles.getPsiElementFromRootDocument(OMTParameterWithType.class, psiFile);
        ApplicationManager.getApplication().runReadAction(() -> validateResources(parameterWithType.getType(), "http://ontologie#ClassA"));
    }

    @Test
    void getType_ParameterWithTypeFromPrimitiveType() {
        String content = "prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "model:\n" +
                "   activity: !Activity\n" +
                "       params:\n" +
                "           -   $mijnParam (string)\n";
        final PsiFile psiFile = myFixture.configureByText("test.omt", content);
        OMTParameterWithType parameterWithType = exampleFiles.getPsiElementFromRootDocument(OMTParameterWithType.class, psiFile);
        ApplicationManager.getApplication().runReadAction(() -> validateResources(parameterWithType.getType(), "http://www.w3.org/2001/XMLSchema#string"));
    }

}
