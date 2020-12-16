package com.misset.opp.omt.psi.annotations;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import org.apache.jena.rdf.model.Resource;

import java.util.List;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.util.UtilManager.getRDFModelUtil;

public abstract class AbstractAnnotator {

    protected void annotateBoolean(List<Resource> valueType, AnnotationHolder holder, PsiElement range) {
        final Resource booleanType = getRDFModelUtil().getPrimitiveTypeAsResource("boolean");
        if (valueType == null || valueType.isEmpty()) {
            return;
        }
        if (valueType.stream().noneMatch(
                booleanType::equals
        )) {
            holder.newAnnotation(HighlightSeverity.ERROR,
                    String.format("Expected boolean, got %s",
                            valueType.stream().map(Resource::getLocalName).sorted().collect(Collectors.joining(", ")))
            ).range(range).create();
        }
    }

}
