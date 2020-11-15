package com.misset.opp.omt.exceptions;

import com.misset.opp.omt.psi.support.OMTParameter;
import org.apache.jena.rdf.model.Resource;

import java.util.List;
import java.util.stream.Collectors;

public class IncorrectSignatureArgument extends Throwable {
    public IncorrectSignatureArgument(OMTParameter parameter, List<Resource> acceptableTypes, List<Resource> argumentTypes) {
        super(String.format("Incorrect type, %s expects type %s but value is of type: %s",
                parameter.getName(),
                acceptableTypes.stream().map(Resource::getLocalName).sorted().collect(Collectors.joining(", ")),
                argumentTypes.stream().map(Resource::getLocalName).sorted().collect(Collectors.joining(", "))
        ));
    }
}
