package util;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.shared.PropertyNotFoundException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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
    /**
     * RDF TYPE alias 'a' in usage => some:thing a ClassName == some:thing rdf:type ClassName
     */
    public static final Property RDF_TYPE = new PropertyImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    private final String rootFolder;
    Model model = ModelFactory.createDefaultModel();
    public final Supplier<Resource> OWL_CLASS = () -> this.model.createResource("http://www.w3.org/2002/07/owl#Class");

    public RDFModelUtil(String rootFolder) {
        this.rootFolder = rootFolder;
        this.model = null;
    }

    public RDFModelUtil(Model model) {
        this.model = model;
        this.rootFolder = "";
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
        return model;
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
            lineage.addAll(getClassLineage(resource.getProperty(RDFS_SUBCLASS).getObject().asResource()));
        }
        return lineage;
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

    private Optional<Statement> getPredicate(Resource subject, Resource predicate) {
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

    private Resource getPropertyFromSubjectPredicate(Resource subject, Resource predicate, Property property) {
        Optional<Statement> optionalPredicate = getPredicate(subject, predicate);
        if (optionalPredicate.isPresent()) {
            Statement propertyStatement = optionalPredicate.get().getProperty(property);
            if (propertyStatement.getObject() != null) {
                return propertyStatement.getObject().asResource();
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
        Optional<Statement> optionalPredicate = getPredicate(subject, predicate);
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
            List<Resource> classLineage = getClassLineage(resource);
            return String.join("\n", classLineage.stream().map(Resource::toString).collect(Collectors.toList()));
        }
        return "Describing " + resource.toString();
    }
}
