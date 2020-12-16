package com.misset.opp.omt.util;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.shared.PropertyNotFoundException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RDFModelUtil {

    public static final Property SHACL_PROPERTY = new PropertyImpl("http://www.w3.org/ns/shacl#property");
    public static final Property SHACL_PATH = new PropertyImpl("http://www.w3.org/ns/shacl#path");
    public static final Property SHACL_CLASS = new PropertyImpl("http://www.w3.org/ns/shacl#class");
    public static final Property SHACL_DATATYPE = new PropertyImpl("http://www.w3.org/ns/shacl#datatype");
    public static final Property SHACL_MAXCOUNT = new PropertyImpl("http://www.w3.org/ns/shacl#maxCount");
    public static final Property SHACL_MINCOUNT = new PropertyImpl("http://www.w3.org/ns/shacl#minCount");
    public static final Property RDFS_SUBCLASS = new PropertyImpl("http://www.w3.org/2000/01/rdf-schema#subClassOf");

    public static final String XSD = "http://www.w3.org/2001/XMLSchema#";
    /**
     * RDF TYPE alias 'a' in usage => some:thing a ClassName == some:thing rdf:type ClassName
     */
    public static final Property RDF_TYPE = new PropertyImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    private final String rootFolder;

    public static final Function<Model, Resource> OWL_CLASS = (Model model) -> model.createResource("http://www.w3.org/2002/07/owl#Class");
    public static final Function<Model, Resource> NODE_SHAPE = (Model model) -> model.createResource("http://www.w3.org/ns/shacl#NodeShape");

    private static final HashMap<Resource, List<Resource>> predicateObjects = new HashMap<>();
    private static final HashMap<Resource, List<Resource>> predicateSubjects = new HashMap<>();

    private Resource owlClass;
    // The ontology is completely refreshed when a change is made to the ttl files in the project
    // while this doesn't happen the same ontology queries will always return the same result
    final Map<String, Object> cache = new HashMap<>();
    private Resource nodeShape;
    private Model model;

    private Resource getOwlClass() {
        if (owlClass == null) {
            owlClass = OWL_CLASS.apply(model);
        }
        return owlClass;
    }
    public RDFModelUtil(String rootFolder) {
        this.rootFolder = rootFolder;
        this.model = null;
    }

    public RDFModelUtil(Model model) {
        this.model = model;
        this.rootFolder = "";
    }

    public boolean isLoaded() {
        return model != null;
    }

    public Model readModel() {
        if (model != null) {
            return model;
        }
        model = ModelFactory.createDefaultModel();
        List<File> modelFiles = getModelFiles(rootFolder);
        for (File file : modelFiles) {
            try {
                model.read(new FileInputStream(file), null, "TTL");
            } catch (FileNotFoundException fileNotFoundException) {
                System.out.println(fileNotFoundException.getMessage());
            }
        }
        setIndexes();
        return model;
    }

    private void setIndexes() {
        setPredicateIndexes();
    }

    private void setPredicateIndexes() {
        model.listSubjectsWithProperty(SHACL_PATH).toList()
                .forEach(shaclInstance -> {
                    final ResIterator iterator = model.listSubjectsWithProperty(SHACL_PROPERTY, shaclInstance);
                    if (iterator.hasNext()) {
                        final Resource subject = iterator.next();
                        final Resource predicate = shaclInstance.getPropertyResourceValue(SHACL_PATH);
                        final Resource object = shaclInstance.hasProperty(SHACL_DATATYPE) ? shaclInstance.getPropertyResourceValue(SHACL_DATATYPE) : shaclInstance.getPropertyResourceValue(SHACL_CLASS);
                        addToMap(predicateSubjects, predicate, subject);
                        addToMap(predicateObjects, predicate, object);
                    }
                });
    }

    private void addToMap(Map<Resource, List<Resource>> map, Resource key, Resource value) {
        List<Resource> values = map.getOrDefault(key, new ArrayList<>());
        values.add(value);
        map.put(key, values);
    }

    public List<Resource> getPredicateObjects(Resource predicate) {
        return getPredicateObjects(predicate, true);
    }

    /**
     * Returns the predicate types and all their subclasses
     */
    public List<Resource> getPredicateObjects(Resource predicate, boolean includeSubclasses) {
        final List<Resource> objects = new ArrayList<>(predicateObjects.getOrDefault(predicate, new ArrayList<>()));
        if (includeSubclasses) {
            objects.addAll(allSubClasses(objects));
        }
        return getDistinctResources(objects);
    }

    public List<Resource> getPredicateSubjects(Resource predicate) {
        return getPredicateSubjects(predicate, true);
    }

    public List<Resource> getPredicateSubjects(Resource predicate, boolean includeSubclasses) {
        final List<Resource> subjects = new ArrayList<>(predicateSubjects.getOrDefault(predicate, new ArrayList<>()));
        if (includeSubclasses) {
            subjects.addAll(allSubClasses(subjects));
        }
        return getDistinctResources(subjects);
    }


    /**
     * Will return the sh:properties of this resource or its parent classes
     */
    public Map<Statement, Resource> getShaclProperties(Resource resource) {
        HashMap<Statement, Resource> statementMap = new HashMap<>();
        if (resource == null) {
            return statementMap;
        }

        resource.listProperties(SHACL_PROPERTY).forEachRemaining(statement -> statementMap.put(statement, resource));

        if (resource.getProperty(RDFS_SUBCLASS) != null && resource.getProperty(RDFS_SUBCLASS).getObject() != null) {
            Resource superClassResource = resource.getProperty(RDFS_SUBCLASS).getObject().asResource();
            if (statementMap.values().stream().anyMatch(resource1 -> resource1.equals(superClassResource))) {
                return statementMap; // infinite recursion
            }
            statementMap.putAll(getShaclProperties(superClassResource));
        }
        return statementMap;
    }

    private Resource getNodeShape() {
        if (nodeShape == null) {
            nodeShape = NODE_SHAPE.apply(model);
        }
        return nodeShape;
    }

    /**
     * Returns the parent classes of this resource (if any)
     */
    public List<Resource> getClassLineage(Resource resource) {
        List<Resource> lineage = new ArrayList<>();
        lineage.add(resource);
        resource.listProperties(RDFS_SUBCLASS).toList()
                .stream().filter(statement -> statement.getObject() != null && statement.getObject().asResource() != resource)
                .forEach(
                        statement -> lineage.addAll(getClassLineage(statement.getObject().asResource()))
                );
        return lineage;
    }

    public List<Resource> getAllClasses() {
        return model.listSubjectsWithProperty(RDF_TYPE).toList();
    }

    public List<Resource> allSuperClasses(List<Resource> resources) {
        return getDistinctResources(resources.stream().map(
                this::getClassLineage
        ).flatMap(Collection::stream).distinct().collect(Collectors.toList()));
    }

    public List<Resource> allSubClasses(List<Resource> resources) {
        return getDistinctResources(resources.stream().map(
                this::getClassDescendants
        ).flatMap(Collection::stream).distinct().collect(Collectors.toList()));
    }

    public boolean isNumeric(Resource resource) {
        if (resource == null || resource.getNameSpace() == null || resource.getLocalName() == null) {
            return false;
        }
        List<String> numericTypes = Arrays.asList("integer", "int", "double", "decimal");
        return resource.getNameSpace().equals(XSD) &&
                numericTypes.contains(resource.getLocalName());
    }

    public boolean isDate(Resource resource) {
        if (resource == null || resource.getNameSpace() == null || resource.getLocalName() == null) {
            return false;
        }
        List<String> dateTypes = Arrays.asList("date", "dateTime");
        return resource.getNameSpace().equals(XSD) &&
                dateTypes.contains(resource.getLocalName());
    }

    public boolean areComparable(Resource resource1, Resource resource2) {
        return (resource1 != null && resource2 != null) &&
                (resource1.equals(resource2) ||
                        getClass(resource1).equals(getClass(resource2)) ||
                        (isNumeric(resource1) && isNumeric(resource2)) ||
                        (isDate(resource1) && isDate(resource2)));
    }

    public List<Resource> getComparableOptions(List<Resource> resources) {
        return getAllClasses().stream().filter(
                classAsResource ->
                        !resources.contains(classAsResource) &&
                                resources.stream().anyMatch(
                                        resource -> areComparable(classAsResource, resource)
                                )
        )
                .filter(resource -> resource.getLocalName() != null)
                .sorted(Comparator.comparing(Resource::getLocalName))
                .collect(Collectors.toList());
    }

    public List<Resource> getClassDescendants(Resource resource) {
        return getClassDescendants(resource, false);
    }

    public List<Resource> getClassDescendants(Resource resource, boolean includeSelf) {
        List<Resource> descendants = new ArrayList<>();
        if (includeSelf) {
            descendants.add(resource);
        }
        final ResIterator resIterator = model.listSubjectsWithProperty(RDFS_SUBCLASS, resource);
        resIterator.forEachRemaining(
                descendant -> {
                    descendants.add(descendant);
                    descendants.addAll(getClassDescendants(descendant));
                }
        );
        return descendants;
    }

    public Resource getClass(Resource implementation) {
        final Resource classResource = implementation.getPropertyResourceValue(RDF_TYPE);
        if (classResource == null) {
            return implementation;
        }
        if (classResource.equals(getOwlClass()) || classResource.equals(getNodeShape())) {
            return implementation;
        }
        return classResource;
    }

    public boolean isClassOrType(Resource resource) {
        if (resource == null) {
            return false;
        }
        final List<Statement> statementList = resource.listProperties(RDF_TYPE).toList();
        for (Statement statement : statementList) {
            if (statement.getObject().asResource().equals(getOwlClass())) {
                return true;
            }
        }
        return isPrimitiveType(resource);
    }

    public List<Resource> getClasses(List<Resource> implementantions) {
        return getOrCache(
                () -> implementantions.stream().map(this::getClass).collect(Collectors.toList())
                , implementantions.toArray());
    }

    public boolean hasPredicate(Resource subject, Resource predicate) {
        return getShaclProperties(subject)
                .keySet()
                .stream()
                .anyMatch(statement -> statement.getProperty(SHACL_PATH).getObject().asResource().equals(predicate));
    }

    private Optional<Statement> getSubjectPredicate(Resource subject, Resource predicate) {
        return getShaclProperties(subject)
                .keySet()
                .stream()
                .filter(statement -> statement.getProperty(SHACL_PATH).getObject().asResource().equals(predicate))
                .findFirst();
    }

    public Resource getClassBySubjectPredicate(Resource subject, Resource predicate) {
        return getPropertyFromSubjectPredicate(subject, predicate, SHACL_CLASS);
    }

    public Resource getDataTypeSubjectPredicate(Resource subject, Resource predicate) {
        return getPropertyFromSubjectPredicate(subject, predicate, SHACL_DATATYPE);
    }

    /**
     * Returns the classes that point to the given class using the specified predicate, traversing the shacl structure
     */
    public List<Resource> listSubjectsWithPredicateObjectClass(Resource predicate, Resource object) {
        if (isTypePredicate(predicate)) {
            return Collections.singletonList(object);
        }
        ResIterator shaclsPointingToTargetClass = object.getNameSpace().equals(XSD) ?
                model.listSubjectsWithProperty(SHACL_DATATYPE, object) :
                model.listSubjectsWithProperty(SHACL_CLASS, object);
        List<Resource> resources = new ArrayList<>();
        while (shaclsPointingToTargetClass.hasNext()) {
            Resource shacl = shaclsPointingToTargetClass.next();
            if (shacl.hasProperty(SHACL_PATH, predicate)) {
                ResIterator classPointingToShacl = model.listSubjectsWithProperty(SHACL_PROPERTY, shacl);
                final Resource resource = classPointingToShacl.nextResource();
                resources.add(resource);
                resources.addAll(getClassDescendants(resource));
            }
        }
        return getDistinctResources(resources);
    }

    public List<Resource> listSubjectsWithPredicateObjectClass(Resource predicate, List<Resource> targetClass) {
        List<Resource> resources = new ArrayList<>();
        targetClass.forEach(resource -> resources.addAll(listSubjectsWithPredicateObjectClass(predicate, resource)));
        return resources;
    }

    public Map<Resource, Resource> listPredicatesForObjectClass(List<Resource> objectClasses) {
        Map<Resource, Resource> resources = new HashMap<>();
        superClassesSortedByLevel(objectClasses).forEach(object -> {
            ResIterator shaclsPointingToTargetClass = object.getNameSpace().equals(XSD) ?
                    model.listSubjectsWithProperty(SHACL_DATATYPE, object) :
                    model.listSubjectsWithProperty(SHACL_CLASS, object);

            while (shaclsPointingToTargetClass.hasNext()) {
                Resource shacl = shaclsPointingToTargetClass.next();
                resources.put(shacl.getProperty(SHACL_PATH).getObject().asResource(), object);
            }
        });
        if (!resources.isEmpty()) {
            resources.put(RDF_TYPE.asResource(), null);
        }
        return resources;
    }

    public Map<Resource, Resource> listPredicatesForSubjectClass(List<Resource> subjectClasses) {
        Map<Resource, Resource> resources = new HashMap<>();
        superClassesSortedByLevel(subjectClasses).forEach(subject ->
                getShaclProperties(subject).keySet().forEach(statement ->
                        resources.put(statement.getProperty(SHACL_PATH).getObject().asResource(), subject)));
        if (!resources.isEmpty()) {
            resources.put(RDF_TYPE.asResource(), null);
        }
        return resources;
    }

    private List<Resource> superClassesSortedByLevel(List<Resource> classes) {
        return allSuperClasses(classes).stream().sorted((o1, o2) -> getClassLineage(o2).size() - getClassLineage(o1).size()).collect(Collectors.toList());
    }

    public List<Resource> getDistinctResources(List<Resource> resources) {
        return resources.stream().distinct().collect(Collectors.toList());
    }

    public List<Resource> listObjectsWithSubjectPredicate(List<Resource> subjects, Resource predicate) {
        List<Resource> resources = new ArrayList<>();
        resources.addAll(subjects.stream().map(subject -> getClassBySubjectPredicate(subject, predicate))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        resources.addAll(subjects.stream().map(subject -> getDataTypeSubjectPredicate(subject, predicate))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        return getDistinctResources(resources);
    }

    public boolean isTypePredicate(Resource resource) {
        return resource != null && resource.equals(RDF_TYPE);
    }

    public Resource getPrimitiveTypeAsResource(String name) {
        return model.createResource(String.format("%s%s", XSD, name));
    }

    public Resource getBooleanType() {
        return getPrimitiveTypeAsResource("boolean");
    }

    public Resource getStringType() {
        return getPrimitiveTypeAsResource("string");
    }

    public List<Resource> getPrimitiveTypeAsResourceList(String name) {
        return Collections.singletonList(getPrimitiveTypeAsResource(name));
    }

    public boolean isPrimitiveType(Resource resource) {
        return resource.toString().startsWith(XSD) && !resource.toString().endsWith("#any");
    }

    public Resource createResource(String iri) {
        return model.createResource(iri);
    }

    private Resource getPropertyFromSubjectPredicate(Resource subject, Resource predicate, Property property) {
        if (isTypePredicate(predicate)) {
            return subject;
        }
        Optional<Statement> optionalPredicate = getSubjectPredicate(subject, predicate);
        if (optionalPredicate.isPresent()) {
            Statement statement = optionalPredicate.get();
            try {
                Statement propertyStatement = statement.getProperty(property);
                if (propertyStatement.getObject() != null) {
                    return propertyStatement.getObject().asResource();
                }
            } catch (Exception ignored) {

            }
        }
        return null;
    }

    public int getMinCount(Resource subject, Resource predicate) {
        return getInt(subject, predicate, SHACL_MINCOUNT);
    }

    public int getMaxCount(Resource subject, Resource predicate) {
        return getInt(subject, predicate, SHACL_MAXCOUNT);
    }


    private int getInt(Resource subject, Resource predicate, Property property) {
        Optional<Statement> optionalPredicate = getSubjectPredicate(subject, predicate);
        return optionalPredicate.map(statement -> getPropertyValueOrDefault(statement, property,
                subject.getModel().createTypedLiteral(0)).getInt()).orElse(0);
    }

    private Literal getPropertyValueOrDefault(Statement statement, Property property, Literal defaultValue) {
        return hasProperty(statement, property) ?
                statement.getProperty(property).getObject().asLiteral() :
                defaultValue;
    }

    private boolean hasProperty(Statement statement, Property property) {
        try {
            statement.getProperty(property);
            return true;
        } catch (PropertyNotFoundException e) {
            return false;
        }
    }

    private List<File> getModelFiles(String modelRoot) {
        return (List<File>) FileUtils.listFiles(new File(modelRoot), new String[]{"ttl"}, true);
    }

    public String describeResource(Resource resource) {
        if (isClassResource(resource)) {
            return describeClass(resource);
        }
        return resource.toString();
    }

    public boolean isClassResource(Resource resource) {
        StmtIterator stmtIterator = resource.listProperties(RDF_TYPE);
        while (stmtIterator.hasNext()) {
            if (stmtIterator.nextStatement().getObject().asResource().equals(getOwlClass())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a copy of the input and all subclasses
     */
    public List<Resource> appendAllSubclasses(List<Resource> resources) {
        List<Resource> allTypes = new ArrayList<>(resources);
        allTypes.addAll(allSubClasses(resources));
        return getDistinctResources(allTypes);
    }

    public List<Resource> appendAllSubclasses(Resource resource) {
        return appendAllSubclasses(Collections.singletonList(resource));
    }

    /**
     * Returns a copy of the input, all subclasses and implementations of those classes
     */
    public List<Resource> appendAllSubclassesAndImplementations(List<Resource> resources) {
        List<Resource> allTypes = new ArrayList<>(resources);
        allTypes.addAll(allSubClasses(allTypes));
        return getDistinctResources(getClasses(allTypes));
    }

    /**
     * Validate if the 2 collections with types are compatible
     * For acceptableTypes, run it through the appendSubclasses first, to make sure it contains all acceptable subtypes also
     * For argumentTypes, run it through the appendAllSubclassesAndImplementations first
     */
    public boolean validateType(
            List<Resource> acceptableTypes,
            List<Resource> argumentTypes) {
        if (acceptableTypes.isEmpty() ||
                argumentTypes.isEmpty() ||
                acceptableTypes.stream().noneMatch(this::isClassOrType) ||
                argumentTypes.stream().noneMatch(this::isClassOrType) ||
                argumentTypes.contains(this.getAnyType())) {
            // when the argument type cannot be resolved to specific class or type it's resolved to any
            // which means we cannot validate the call
            return true;
        }
        for (Resource argumentType : argumentTypes) {
            if (acceptableTypes.stream().anyMatch(resource ->
                    areComparable(resource, argumentType)
            )) {
                return true; // acceptable type found
            }
        }
        return false;
    }

    public Resource getResource(String iri) {
        return model.getResource(iri);
    }

    public List<Resource> getAnyTypeAsList() {
        return Collections.singletonList(getAnyType());
    }

    public Resource getAnyType() {
        return getPrimitiveTypeAsResource("any");
    }

    public void setModel(Model model) {
        this.model = model;
    }

    /**
     * <p>Class:&nbsp;<strong>myClass</strong></p>
     * <p>Parent lineage:</p>
     * <ul>
     * <li>First parent</li>
     * <li>Grandparent</li>
     * </ul>
     * <p>Predicates:</p>
     * <ul>
     * <li>myPredicate (typeOfPredicate)</li>
     * <li>mySecondPredicate (typeOfPredicate)</li>
     * </ul>
     * <p>Referred to by:</p>
     * <ul>
     * <li>referedByClass (viaPredicate)</li>
     * </ul>
     */
    private String describeClass(Resource resource) {
        StringBuilder description = new StringBuilder();

        description.append(String.format("<p>Class:&nbsp;<strong>%s</strong></p>", resource.getLocalName()));
        List<Resource> classLineage = getClassLineage(resource);
        if (classLineage.size() > 1) {
            List<Resource> superClasses = classLineage.subList(1, classLineage.size() - 1);
            description.append("<p>Parent lineage:</p>");
            description.append("<ul>");
            superClasses.forEach(superClass ->
                    description.append(String.format("<li>%s:%s (%s)</li>", superClass.getModel().getNsURIPrefix(superClass.getNameSpace()), superClass.getLocalName(), superClass.toString()))
            );
            description.append("</ul>");
        }
        Map<Statement, Resource> shaclProperties = getShaclProperties(resource);
        if (!shaclProperties.isEmpty()) {
            description.append("<p>Predicates:</p>");
            description.append("<ul>");
            shaclProperties.forEach((statement, fromResource) -> {
                Resource predicate = statement.getProperty(SHACL_PATH).getObject().asResource();
                description.append(String.format("<li>%s:%s (%s)</li>",
                        statement.getModel().getNsURIPrefix(predicate.getNameSpace()), predicate.getLocalName(), fromResource.getLocalName()));
            });
            description.append("</ul>");
        }
        return description.toString();
    }

    private <T> T getOrCache(Supplier<Object> method, Object... keyIds) {
        if (1 == 1) {
            return (T) method.get();
        }
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        String keyId = methodName + "." + Arrays.stream(keyIds).map(Object::toString).collect(Collectors.joining("."));

        if (cache.containsKey(keyId)) {
            return (T) cache.get(keyId);
        }
        final Object oneTimeResult = method.get();
        cache.put(keyId, oneTimeResult);
        return (T) oneTimeResult;
    }
}
