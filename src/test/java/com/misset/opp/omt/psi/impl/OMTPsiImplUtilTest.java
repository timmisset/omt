package com.misset.opp.omt.psi.impl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.psi.ExampleFiles;
import com.misset.opp.omt.psi.OMTQueryPath;
import com.misset.opp.omt.psi.util.ProjectUtil;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

class OMTPsiImplUtilTest extends LightJavaCodeInsightFixtureTestCase {

    private final ExampleFiles exampleFiles = new ExampleFiles(this);

    @BeforeEach
    void setUpSuite() throws Exception {
        super.setName("OMTPsiImplUtilTest");
        super.setUp();

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
            List<Resource> resources = OMTPsiImplUtil.resolveToResource(queryPath);
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
            List<Resource> resources = OMTPsiImplUtil.resolveToResource(queryPath);
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
            List<Resource> resources = OMTPsiImplUtil.resolveToResource(queryPath);
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
            List<Resource> resources = OMTPsiImplUtil.resolveToResource(queryPath);
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
            List<Resource> resources = OMTPsiImplUtil.resolveToResource(queryPath);
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
            List<Resource> resources = OMTPsiImplUtil.resolveToResource(queryPath);
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
            List<Resource> resources = OMTPsiImplUtil.resolveToResource(queryPath);
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
            List<Resource> resources = OMTPsiImplUtil.resolveToResource(queryPath);
            assertEquals(2, resources.size());
            assertContainsElements(resources.stream().map(Resource::getLocalName).collect(Collectors.toList())
                    , "ClassB", "ClassC");
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
            List<Resource> resources = OMTPsiImplUtil.resolveToResource(queryPath);
            assertEquals(1, resources.size());
            assertContainsElements(resources.stream().map(Resource::getLocalName).collect(Collectors.toList())
                    , "ClassA");
        });
    }
}
