package com.misset.opp.omt.external.util.rdf;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.intellij.testFramework.UsefulTestCase.assertContainsElements;
import static com.intellij.testFramework.UsefulTestCase.assertDoesntContain;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RDFModelUtilTest {

    RDFModelUtil modelUtil;
    Model model;

    @BeforeEach
    void setUp() {
        modelUtil = new RDFModelUtil("src/test/resources/examples");
        model = modelUtil.readModel();
    }

    @Test
    void hasResourcesOfClass() {
        Resource resource = resourceFor("ClassA");
        assertTrue(modelUtil.hasPredicate(resource, resourceFor("booleanProperty")));
        assertTrue(modelUtil.hasPredicate(resource, resourceFor("classProperty")));
    }

    @Test
    void hasResourcesOfSuperClass() {
        Resource resource = resourceFor("ClassA");
        assertTrue(modelUtil.hasPredicate(resource, resourceFor("stringProperty")));
    }

    @Test
    void getClassBySubjectPredicate() {
        Resource subject = resourceFor("ClassA");
        Resource predicate = resourceFor("classProperty");
        Resource expectedResource = resourceFor("ClassC");
        assertEquals(expectedResource, modelUtil.getClassBySubjectPredicate(subject, predicate));
    }

    @Test
    void listSubjectsWithPredicateObjectClass_ReturnsClassPointingToClass() {
        Resource predicate = resourceFor("classProperty");
        Resource object = resourceFor("ClassC");
        List<Resource> resources = modelUtil.listSubjectsWithPredicateObjectClass(predicate, object);
        assertContainsElements(resources, resourceFor("ClassA"));
        assertEquals(1, resources.size());
    }

    @Test
    void listSubjectsWithPredicateObjectClass_ReturnsClassPointingToDataType() {
        Resource predicate = resourceFor("stringProperty");
        Resource object = modelUtil.getPrimitiveTypeAsResource("string");
        List<Resource> resources = modelUtil.listSubjectsWithPredicateObjectClass(predicate, object);
        assertContainsElements(resources, resourceFor("ClassA"), resourceFor("ClassB"), resourceFor("ClassC"));
        assertEquals(3, resources.size());
    }

    @Test
    void listSubjectsWithPredicateObjectClasses_ReturnsClassPointingToClass() {
        Resource predicate = resourceFor("classProperty");
        Resource object = resourceFor("ClassC");
        List<Resource> resources = modelUtil.listSubjectsWithPredicateObjectClass(predicate, Collections.singletonList(object));
        assertContainsElements(resources, resourceFor("ClassA"));
        assertEquals(1, resources.size());
    }

    @Test
    void listSubjectsWithPredicateObjectClass_DoesntReturnClassWhenUsingDifferentPredicate() {
        Resource predicate = resourceFor("otherProperty");
        Resource object = resourceFor("ClassC");
        List<Resource> resources = modelUtil.listSubjectsWithPredicateObjectClass(predicate, object);
        assertDoesntContain(resources, resourceFor("ClassA"));
        assertEquals(0, resources.size());
    }

    @Test
    void listObjectsWithSubjectPredicate() {
        Resource predicate = resourceFor("classProperty");
        Resource subject = resourceFor("ClassA");
        List<Resource> resources = modelUtil.listObjectsWithSubjectPredicate(Collections.singletonList(subject), predicate);
        assertContainsElements(resources, resourceFor("ClassC"));
        assertEquals(1, resources.size());
    }

    @Test
    void getDataTypeBySubjectPredicate() {
        Resource subject = resourceFor("ClassA");
        Resource predicate = resourceFor("stringProperty");
        Resource expectedResource = resourceFor("http://www.w3.org/2001/XMLSchema#", "string");
        assertEquals(expectedResource, modelUtil.getDataTypeSubjectPredicate(subject, predicate));
    }

    @Test
    void getPredicateMinCount() {
        // when none, return default 0
        assertEquals(0, modelUtil.getMinCount(resourceFor("ClassA"), resourceFor("classProperty")));

        assertEquals(1, modelUtil.getMinCount(resourceFor("ClassA"), resourceFor("booleanProperty")));
    }

    @Test
    void getPredicateMaxCount() {
        assertEquals(1, modelUtil.getMaxCount(resourceFor("ClassA"), resourceFor("classProperty")));
        // when none, return default 0
        assertEquals(0, modelUtil.getMaxCount(resourceFor("ClassA"), resourceFor("booleanProperty")));
    }

    private Resource resourceFor(String value) {
        return resourceFor("http://ontologie#", value);
    }

    private Resource resourceFor(String prefix, String value) {
        return model.getResource(String.format("%s%s", prefix, value));
    }
}