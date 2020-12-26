package com.misset.opp.omt.annotations;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.intentions.variables.AnnotateParameterIntention;
import com.misset.opp.omt.psi.*;
import org.apache.jena.rdf.model.Resource;

import java.util.List;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.util.UtilManager.getVariableUtil;

public class ParameterAnnotator extends AbstractAnnotator {

    public ParameterAnnotator(AnnotationHolder holder) {
        super(holder);
    }

    @Override
    public void annotate(PsiElement element) {
        if (element instanceof OMTParameterWithType) {
            annotate((OMTParameterWithType) element);
        } else if (element instanceof OMTDefineParam) {
            annotate((OMTDefineParam) element);
        } else if (element instanceof OMTParameterType) {
            annotate(((OMTParameterType) element).getAsResource());
        }
    }

    private void annotate(OMTParameterWithType parameterWithType) {
        if (parameterWithType.getParameterType() == null) {
            setError("No type specified");
        }
    }

    private void annotate(OMTDefineParam defineParam) {
        defineParam.getVariableList().forEach(variable -> {
            final List<Resource> type = variable.getType();
            if (type.isEmpty()) {
                final AnnotateParameterIntention annotateParameterIntention = new AnnotateParameterIntention();
                final String message = String.format("Annotate parameter %s with a type, this help to resolve the query path%n%n" +
                        "/**" +
                        "%n* @param %s (pol:Classname)%n" +
                        "*/", variable.getName(), variable.getName());

                setWeakWarning("Annotate parameter with type", annotationBuilder -> {
                    annotationBuilder.tooltip(message);
                    for (String suggestion : getTypeSuggestions(defineParam, variable)) {
                        annotationBuilder.withFix(
                                annotateParameterIntention.getAnnotateParameterIntention(defineParam, variable.getName(), suggestion)
                        );
                    }
                });

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
