package com.misset.opp.omt.psi.annotations;

import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.misset.opp.omt.psi.OMTDefineParam;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.intentions.variables.AnnotateParameterIntention;
import org.apache.jena.rdf.model.Resource;

import java.util.List;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.util.UtilManager.getVariableUtil;

public class DefinedParameterAnnotator extends AbstractAnnotator {

    public void annotateDefineParameter(OMTDefineParam defineParam, AnnotationHolder holder) {
        defineParam.getVariableList().forEach(variable -> {
            final List<Resource> type = variable.getType();
            if (type.isEmpty()) {

                AnnotationBuilder annotateParameterWithType = holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "Annotate parameter with type")
                        .tooltip(String.format("Annotate parameter %s with a type, this help to resolve the query path%n%n" +
                                "/**" +
                                "%n* @param %s (pol:Classname)%n" +
                                "*/", variable.getName(), variable.getName()))
                        .range(variable);

                final AnnotateParameterIntention annotateParameterIntention = new AnnotateParameterIntention();
                for (String suggestion : getTypeSuggestions(defineParam, variable)) {
                    annotateParameterWithType = annotateParameterWithType.withFix(
                            annotateParameterIntention.getAnnotateParameterIntention(
                                    defineParam, variable.getName(), suggestion
                            )
                    );
                }
                annotateParameterWithType.create();
            }
        });
    }

    private List<String> getTypeSuggestions(OMTDefineParam defineParam, OMTVariable variable) {
        final OMTFile containingFile = (OMTFile) variable.getContainingFile();
        final List<String> suggestions = getVariableUtil()
                .getTypeSuggestions(defineParam, variable)
                .stream()
                .map(containingFile::resourceToCurie)
                .collect(Collectors.toList());

        if (suggestions.isEmpty()) {
            suggestions.add("prefix:Class");
        }
        return suggestions;
    }
}
