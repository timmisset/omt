package util;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
