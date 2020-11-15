package com.misset.opp.omt.external.util.rdf;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.shared.PropertyNotFoundException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
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
    Model model;
    public final Supplier<Resource> OWL_CLASS = () -> this.model.createResource("http://www.w3.org/2002/07/owl#Class");
    public final Supplier<Resource> NODE_SHAPE = () -> this.model.createResource("http://www.w3.org/ns/shacl#NodeShape");

    private static HashMap<Resource, List<Resource>> predicateObjects = new HashMap<>();
    private static HashMap<Resource, List<Resource>> predicateSubjects = new HashMap<>();

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
     *
     * @param predicate
     * @return
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
     *
     * @param resource
     * @return
     */
    public HashMap<Statement, Resource> getShaclProperties(Resource resource) {
        HashMap<Statement, Resource> statementMap = new HashMap<>();
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


    /**
     * Returns the parent classes of this resource (if any)
     *
     * @param resource
     * @return
     */
    public List<Resource> getClassLineage(Resource resource) {
        List<Resource> lineage = new ArrayList<>();
        lineage.add(resource);
        if (resource.getProperty(RDFS_SUBCLASS) != null && resource.getProperty(RDFS_SUBCLASS).getObject() != null) {
            final Resource subClass = resource.getProperty(RDFS_SUBCLASS).getObject().asResource();
            if (!subClass.toString().equals(resource.toString())) {
                lineage.addAll(getClassLineage(subClass));
            }
        }
        return lineage;
    }

    public boolean isClassOrType(Resource resource) {
        final List<Statement> statementList = resource.listProperties(RDF_TYPE).toList();
        for (Statement statement : statementList) {
            if (statement.getObject().asResource().toString().equals(OWL_CLASS.get().toString())) {
                return true;
            }
        }
        return isPrimitiveType(resource);
    }

    public List<Resource> getAllClasses() {
        return model.listSubjectsWithProperty(RDF_TYPE).toList();
    }

    public List<Resource> allSuperClasses(List<Resource> resources) {
        return getDistinctResources(resources.stream().map(
                this::getClassLineage
        ).flatMap(Collection::stream).collect(Collectors.toList()));
    }

    public List<Resource> allSubClasses(List<Resource> resources) {
        return getDistinctResources(resources.stream().map(
                this::getClassDescendants
        ).flatMap(Collection::stream).collect(Collectors.toList()));
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
        if (classResource.equals(OWL_CLASS.get()) || classResource.equals(NODE_SHAPE.get())) {
            return implementation;
        }
        return classResource;
    }

    public List<Resource> getClasses(List<Resource> implementantions) {
        return implementantions.stream().map(this::getClass).collect(Collectors.toList());
    }


    public boolean isClassResource(Resource resource) {
        StmtIterator stmtIterator = resource.listProperties(RDF_TYPE);
        while (stmtIterator.hasNext()) {
            if (stmtIterator.nextStatement().getObject().asResource().equals(OWL_CLASS.get())) {
                return true;
            }
        }
        return false;
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
     *
     * @param predicate
     * @param targetClass
     * @return
     */
    public List<Resource> listSubjectsWithPredicateObjectClass(Resource predicate, Resource targetClass) {

        ResIterator shaclsPointingToTargetClass = targetClass.getNameSpace().equals(XSD) ?
                model.listSubjectsWithProperty(SHACL_DATATYPE, targetClass) :
                model.listSubjectsWithProperty(SHACL_CLASS, targetClass);
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
        return resource.equals(RDF_TYPE);
    }

    public Resource getPrimitiveTypeAsResource(String name) {
        return model.createResource(String.format("%s%s", XSD, name));
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
        return getIntOrDefault(subject, predicate, SHACL_MINCOUNT, 0);
    }

    public int getMaxCount(Resource subject, Resource predicate) {
        return getIntOrDefault(subject, predicate, SHACL_MAXCOUNT, 0);
    }


    private int getIntOrDefault(Resource subject, Resource predicate, Property property, int defaultValue) {
        Optional<Statement> optionalPredicate = getSubjectPredicate(subject, predicate);
        return optionalPredicate.map(statement -> getPropertyValueOrDefault(statement, property,
                subject.getModel().createTypedLiteral(defaultValue)).getInt()).orElse(defaultValue);
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
     *
     * @param resource
     * @return
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
        HashMap<Statement, Resource> shaclProperties = getShaclProperties(resource);
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

    public Resource getResource(String iri) {
        return model.getResource(iri);
    }
}
