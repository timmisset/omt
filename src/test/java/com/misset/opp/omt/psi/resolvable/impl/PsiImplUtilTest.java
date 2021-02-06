package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTDefineQueryStatement;
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

class ResolvableTest extends OMTTestSuite {

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setName("ResolvableTest");
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
                Arguments.of("/ont:ClassA / ^rdf:type / ont:classProperty", new String[]{"http://ontologie#ClassC"}),
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
        return parseQueryFromContent(String.format("prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "    rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => %s;\n", query));
    }

    private OMTQuery parseQueryFromContent(String content) {
        myFixture.configureByText("test.omt", content);
        return getElement(OMTQuery.class);
    }

    private OMTQuery parseQueryFromContent(String content, Predicate<OMTQuery> condition) {
        myFixture.configureByText("test.omt", content);
        return getElement(OMTQuery.class, condition);
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
        return ReadAction.compute(query::resolveToResource);
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
        ReadAction.run(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassA"));
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
        ReadAction.run(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassB"));
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
        ReadAction.run(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassB"));
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
        ReadAction.run(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassA", "http://ontologie#ClassB"));
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
        ReadAction.run(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassA", "http://ontologie#ClassC"));
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
        ReadAction.run(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassB"));
    }

    @Test
    void queryPathResolveToResourceResolvesRightSideFilter() {
        String content = "prefixes:\n" +
                "    rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => 'test' / ^ont:stringProperty [/ont:ClassB == rdf:type];\n";
        OMTQuery query = parseQueryFromContent(content);
        ReadAction.run(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassB"));
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
        ReadAction.run(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassA", "http://ontologie#ClassC"));
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
        ReadAction.run(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassA"));

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
        ReadAction.run(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassA"));
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
        ReadAction.run(() -> validateResources(query.resolveToResource(), "http://ontologie#ClassB"));
    }

    @Test
    void queryPathResolveToResource_ResolvesFromQuery() {
        String content = "queries: |\n" +
                "    DEFINE QUERY myQuery($mijnParameter) => $mijnParameter / CEIL;\n" +
                "    DEFINE QUERY myQuery2() => myQuery;";

        OMTQuery query = parseQueryFromContent(content, omtQuery -> PsiTreeUtil.getParentOfType(omtQuery, OMTDefineQueryStatement.class).getDefineName().getName().equals("myQuery2"));
        ReadAction.run(() -> validateResources(query.resolveToResource(), "http://www.w3.org/2001/XMLSchema#decimal"));
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

        myFixture.configureByText("test.omt", content);
        OMTParameterWithType parameterWithType = getElement(OMTParameterWithType.class);
        ReadAction.run(() -> validateResources(parameterWithType.getType(), "http://ontologie#ClassA"));
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
        myFixture.configureByText("test.omt", content);
        OMTParameterWithType parameterWithType = getElement(OMTParameterWithType.class);
        ReadAction.run(() -> validateResources(parameterWithType.getType(), "http://www.w3.org/2001/XMLSchema#string"));
    }

}
