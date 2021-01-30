package com.misset.opp.omt.annotations;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.misset.opp.omt.psi.OMTFile;
import org.apache.jena.rdf.model.Resource;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.util.UtilManager.getRDFModelUtil;

public abstract class AbstractAnnotator {

    private final AnnotationHolder holder;

    protected AbstractAnnotator(AnnotationHolder holder) {
        this.holder = holder;
    }

    public abstract void annotate(PsiElement element);

    protected void validateReference(PsiElement element, String errorMessage) {
        validateReference(element, errorMessage, annotationBuilder -> {
        });
    }

    protected void validateReference(PsiElement element, String errorMessage, Consumer<AnnotationBuilder> builderConsumer) {
        if (element.getReference() != null &&
                (element.getReference().resolve() == null ||
                        element.getReference().resolve() == element)) {
            setError(errorMessage, builderConsumer);
        }
    }

    protected void setError(String message) {
        holder.newAnnotation(HighlightSeverity.ERROR, message)
                .create();
    }

    protected void setError(String message, Consumer<AnnotationBuilder> builder) {
        final AnnotationBuilder annotationBuilder = holder.newAnnotation(HighlightSeverity.ERROR, message);
        builder.accept(annotationBuilder);
        annotationBuilder.create();
    }

    protected void setInformation(String message) {
        holder.newAnnotation(HighlightSeverity.INFORMATION, message).create();
    }

    protected void setInformation(String message, Consumer<AnnotationBuilder> builder) {
        final AnnotationBuilder annotationBuilder = holder.newAnnotation(HighlightSeverity.INFORMATION, message);
        builder.accept(annotationBuilder);
        annotationBuilder.create();
    }

    protected void setWeakWarning(String message) {
        holder.newAnnotation(HighlightSeverity.WEAK_WARNING, message).create();
    }

    protected void setWeakWarning(String message, Consumer<AnnotationBuilder> builder) {
        final AnnotationBuilder annotationBuilder = holder.newAnnotation(HighlightSeverity.WEAK_WARNING, message);
        builder.accept(annotationBuilder);
        annotationBuilder.create();
    }

    protected void setWarning(String message, Consumer<AnnotationBuilder> builder) {
        final AnnotationBuilder annotationBuilder = holder.newAnnotation(HighlightSeverity.WARNING, message);
        builder.accept(annotationBuilder);
        annotationBuilder.create();
    }

    protected void annotateUsage(PsiElement element) {
        annotateUsage(element, builder -> {
        });
    }


    protected void annotateUsage(PsiElement element, Consumer<AnnotationBuilder> builder) {
        if (!ReferencesSearch.search(element)
                .anyMatch(psiReference ->
                        psiReference.getElement().getContainingFile() instanceof OMTFile &&
                                element != psiReference.getElement())) {
            String message = String.format("%s is never used", element.getText());
            final AnnotationBuilder annotationBuilder =
                    holder.newAnnotation(HighlightSeverity.WARNING, message)
                            .highlightType(ProblemHighlightType.LIKE_UNUSED_SYMBOL);
            builder.accept(annotationBuilder);
            annotationBuilder.create();
        }
    }

    protected void annotateBoolean(List<Resource> valueType, String source) {
        final Resource booleanType = getRDFModelUtil().getPrimitiveTypeAsResource("boolean");
        if (valueType == null || valueType.isEmpty() || booleanType == null) {
            return;
        }
        if (valueType.stream().noneMatch(
                booleanType::equals
        )) {
            String causedBy = source != null ? String.format(", caused by: %s", source, "") : "";
            final String message = String.format("Expected boolean, got %s%s",
                    valueType.stream().map(Resource::getLocalName).sorted().collect(Collectors.joining(", ")),
                    causedBy);
            setError(message);
        }
    }

    protected void annotate(Resource resource) {
        holder.newAnnotation(HighlightSeverity.INFORMATION, resource.toString())
                .tooltip(getRDFModelUtil().describeResource(resource))
                .create();
    }

    protected void highlight(String message, TextAttributesKey key) {
        holder.newAnnotation(HighlightSeverity.INFORMATION, message)
                .textAttributes(key)
                .create();
    }

}
