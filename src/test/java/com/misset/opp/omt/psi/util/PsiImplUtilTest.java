package com.misset.opp.omt.psi.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.external.util.rdf.RDFModelUtil;
import com.misset.opp.omt.psi.ExampleFiles;
import com.misset.opp.omt.psi.OMTDefineQueryStatement;
import com.misset.opp.omt.psi.OMTParameterWithType;
import com.misset.opp.omt.psi.OMTQueryPath;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

class PsiImplUtilTest extends LightJavaCodeInsightFixtureTestCase {

    private final ExampleFiles exampleFiles = new ExampleFiles(this);

    @BeforeEach
    void setUpSuite() throws Exception {
        super.setName("OMTPsiImplUtilTest");
        super.setUp();

        myFixture.copyFileToProject(new File("src/test/resources/builtinOperators.ts").getAbsolutePath(), "builtinOperators.ts");
        myFixture.copyFileToProject(new File("src/test/resources/examples/model.ttl").getAbsolutePath(), "test/resources/examples/root.ttl");

        ApplicationManager.getApplication().runReadAction(() -> ProjectUtil.SINGLETON.loadOntologyModel(getProject()));
    }

    @AfterEach
    void tearDownSuite() throws Exception {
        super.tearDown();
    }

    @Test
    void queryPathResolveToResource_ResolvesConstant() {
        String content = "prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => /ont:ClassA;\n";
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTQueryPath queryPath = exampleFiles.getPsiElementFromRootDocument(OMTQueryPath.class, rootBlock);
            List<Resource> resources = PsiImplUtil.resolveToResource(queryPath);
            assertEquals(1, resources.size());
            assertEquals("http://ontologie#ClassA", resources.get(0).toString());
        });
    }

    @Test
    void queryPathResolveToResource_ResolvesConstantValueToString() {
        String content = "prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => 'test';\n";
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTQueryPath queryPath = exampleFiles.getPsiElementFromRootDocument(OMTQueryPath.class, rootBlock);
            List<Resource> resources = PsiImplUtil.resolveToResource(queryPath);
            assertEquals(1, resources.size());
            assertEquals("http://www.w3.org/2001/XMLSchema#string", resources.get(0).toString());
        });
    }

    @Test
    void queryPathResolveToResource_ResolvesConstantValueToInteger() {
        String content = "prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => 10;\n";
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTQueryPath queryPath = exampleFiles.getPsiElementFromRootDocument(OMTQueryPath.class, rootBlock);
            List<Resource> resources = PsiImplUtil.resolveToResource(queryPath);
            assertEquals(1, resources.size());
            assertEquals("http://www.w3.org/2001/XMLSchema#int", resources.get(0).toString());
        });
    }

    @Test
    void queryPathResolveToResource_ResolvesConstantValueToDouble() {
        String content = "prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => 10.0;\n";
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTQueryPath queryPath = exampleFiles.getPsiElementFromRootDocument(OMTQueryPath.class, rootBlock);
            List<Resource> resources = PsiImplUtil.resolveToResource(queryPath);
            assertEquals(1, resources.size());
            assertEquals("http://www.w3.org/2001/XMLSchema#double", resources.get(0).toString());
        });
    }

    @Test
    void queryPathResolveToResource_ResolvesConstantValueToBooleanTrue() {
        String content = "prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => true;\n";
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTQueryPath queryPath = exampleFiles.getPsiElementFromRootDocument(OMTQueryPath.class, rootBlock);
            List<Resource> resources = PsiImplUtil.resolveToResource(queryPath);
            assertEquals(1, resources.size());
            assertEquals("http://www.w3.org/2001/XMLSchema#boolean", resources.get(0).toString());
        });
    }

    @Test
    void queryPathResolveToResource_ResolvesConstantValueToBooleanFalse() {
        String content = "prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => false;\n";
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTQueryPath queryPath = exampleFiles.getPsiElementFromRootDocument(OMTQueryPath.class, rootBlock);
            List<Resource> resources = PsiImplUtil.resolveToResource(queryPath);
            assertEquals(1, resources.size());
            assertEquals("http://www.w3.org/2001/XMLSchema#boolean", resources.get(0).toString());
        });
    }

    @Test
    void queryPathResolveToResource_ResolvesPredicateObject() {
        String content = "prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => /ont:ClassA / ont:classProperty;\n";
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTQueryPath queryPath = exampleFiles.getPsiElementFromRootDocument(OMTQueryPath.class, rootBlock);
            List<Resource> resources = PsiImplUtil.resolveToResource(queryPath);
            assertEquals(1, resources.size());
            assertEquals("http://ontologie#ClassC", resources.get(0).toString());
        });
    }

    @Test
    void queryPathResolveToResource_ResolvesConstantStringReversePathToClass() {
        String content = "prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => 'test' / ^ont:stringProperty;\n";
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTQueryPath queryPath = exampleFiles.getPsiElementFromRootDocument(OMTQueryPath.class, rootBlock);
            List<Resource> resources = PsiImplUtil.resolveToResource(queryPath);
            assertEquals(3, resources.size());
            assertContainsElements(resources.stream().map(Resource::getLocalName).collect(Collectors.toList())
                    , "ClassA", "ClassB", "ClassC");
        });
    }

    @Test
    void queryPathResolveToResource_ResolvesConstantBooleanReversePathToClass() {
        String content = "prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => true / ^ont:booleanProperty;\n";
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTQueryPath queryPath = exampleFiles.getPsiElementFromRootDocument(OMTQueryPath.class, rootBlock);
            List<Resource> resources = PsiImplUtil.resolveToResource(queryPath);
            assertEquals(1, resources.size());
            assertContainsElements(resources.stream().map(Resource::getLocalName).collect(Collectors.toList())
                    , "ClassA");
        });
    }

    @Test
    void queryPathResolveToResource_ResolvesType() {
        String content = "prefixes:\n" +
                "    rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => /ont:ClassA / rdf:type;\n";
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTQueryPath queryPath = exampleFiles.getPsiElementFromRootDocument(OMTQueryPath.class, rootBlock);
            List<Resource> resources = PsiImplUtil.resolveToResource(queryPath);
            assertEquals(1, resources.size());
            assertContainsElements(resources.stream().map(Resource::getLocalName).collect(Collectors.toList())
                    , "ClassA");
        });
    }

    @Test
    void queryPathResolveToResource_ResolvesFilterWithTypeAllFilteredOut() {
        String content = "prefixes:\n" +
                "    rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => 'test' / ^ont:stringProperty [rdf:type == /ont:ClassA];\n";
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTQueryPath queryPath = exampleFiles.getPsiElementFromRootDocument(OMTQueryPath.class, rootBlock);
            List<Resource> resources = PsiImplUtil.resolveToResource(queryPath);
            assertEquals(1, resources.size());
            assertContainsElements(resources.stream().map(Resource::getLocalName).collect(Collectors.toList())
                    , "ClassA");
        });
    }

    @Test
    void queryPathResolveToResource_ResolvesFilterWithType() {
        String content = "prefixes:\n" +
                "    rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => 'test' / ^ont:stringProperty [rdf:type == /ont:ClassB];\n";
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTQueryPath queryPath = exampleFiles.getPsiElementFromRootDocument(OMTQueryPath.class, rootBlock);
            List<Resource> resources = PsiImplUtil.resolveToResource(queryPath);
            assertEquals(1, resources.size());
            assertContainsElements(resources.stream().map(Resource::getLocalName).collect(Collectors.toList())
                    , "ClassB");

        });
    }

    @Test
    void queryPathResolveToResource_ResolvesSubQuery() {
        String content = "prefixes:\n" +
                "    rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY test() => 'test' / (^ont:stringProperty);\n";
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTQueryPath queryPath = exampleFiles.getPsiElementFromRootDocument(OMTQueryPath.class, rootBlock);
            List<Resource> resources = PsiImplUtil.resolveToResource(queryPath);
            assertEquals(3, resources.size());
            assertContainsElements(resources.stream().map(Resource::getLocalName).collect(Collectors.toList())
                    , "ClassA", "ClassB", "ClassC");

        });
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
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTQueryPath queryPath = exampleFiles.getPsiElementFromRootDocument(OMTQueryPath.class, rootBlock);
            List<Resource> resources = PsiImplUtil.resolveToResource(queryPath);
            assertEquals(1, resources.size());
            assertContainsElements(resources.stream().map(Resource::getLocalName).collect(Collectors.toList())
                    , "ClassA");
        });
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
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTQueryPath queryPath = exampleFiles.getPsiElementFromRootDocument(OMTQueryPath.class, rootBlock);
            List<Resource> resources = PsiImplUtil.resolveToResource(queryPath);
            assertEquals(1, resources.size());
            assertContainsElements(resources.stream().map(Resource::getLocalName).collect(Collectors.toList())
                    , "ClassA");
        });
    }

    @Test
    void queryPathResolveToResource_ResolvesFromBuiltInOperator() {
        String content = "queries: |\n" +
                "    DEFINE QUERY myQuery($mijnParameter) => $mijnParameter / CEIL;";
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTQueryPath queryPath = exampleFiles.getPsiElementFromRootDocument(OMTQueryPath.class, rootBlock);
            List<Resource> resources = PsiImplUtil.resolveToResource(queryPath);
            assertEquals(1, resources.size());
            assertContainsElements(resources.stream().map(Resource::getLocalName).collect(Collectors.toList())
                    , "decimal");
        });
    }

    @Test
    void queryPathResolveToResource_ResolvesFromBuiltInOperatorReturnsCurrent() {
        String content = "prefixes:\n" +
                "    ont:     <http://ontologie#>\n" +
                "\n" +
                "queries: |\n" +
                "    /**\n" +
                "    * @param $mijnParameter (ont:ClassA)\n" +
                "    */\n" +
                "    DEFINE QUERY myQuery($mijnParameter) => $mijnParameter / FIRST;";
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTQueryPath queryPath = exampleFiles.getPsiElementFromRootDocument(OMTQueryPath.class, rootBlock);
            List<Resource> resources = PsiImplUtil.resolveToResource(queryPath);
            assertEquals(1, resources.size());
            assertContainsElements(resources.stream().map(Resource::getLocalName).collect(Collectors.toList())
                    , "ClassA");
        });
    }

    @Test
    void queryPathResolveToResource_ResolvesFromQuery() {
        String content = "queries: |\n" +
                "    DEFINE QUERY myQuery($mijnParameter) => $mijnParameter / CEIL;\n" +
                "    DEFINE QUERY myQuery2() => myQuery;";
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTDefineQueryStatement queryStatement = exampleFiles.getPsiElementFromRootDocument(OMTDefineQueryStatement.class,
                    rootBlock, statement -> statement.getDefineName().getName().equals("myQuery2"));
            List<Resource> resources = PsiImplUtil.resolveToResource(queryStatement.getQueryPath());
            assertEquals(1, resources.size());
            assertContainsElements(resources.stream().map(Resource::getLocalName).collect(Collectors.toList())
                    , "decimal");
        });
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
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTParameterWithType parameterWithType = exampleFiles.getPsiElementFromRootDocument(OMTParameterWithType.class, rootBlock);

            List<Resource> type = parameterWithType.getType();
            assertEquals(1, type.size());
            assertEquals("ClassA", type.get(0).getLocalName());
        });
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
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiElement rootBlock = exampleFiles.fromContent(content);
            OMTParameterWithType parameterWithType = exampleFiles.getPsiElementFromRootDocument(OMTParameterWithType.class, rootBlock);

            List<Resource> type = parameterWithType.getType();
            assertEquals(1, type.size());
            assertEquals(RDFModelUtil.XSD, type.get(0).getNameSpace());
            assertEquals("string", type.get(0).getLocalName());
        });
    }
}
