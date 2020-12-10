package com.misset.opp.omt.completion;

import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTBlockEntry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderProvider {

    private static final String EXPECTED_MODEL_ITEM_TYPE = "MODEL_ITEM_TYPE";
    private static final String EXPECTED_BLOCK = "block";
    private static final String EXPECTED_MODEL_ITEM_BLOCK = "model item block";
    private static final String EXPECTED_QUERY = "query";
    private static final String EXPECTED_QUERY_STEP = "query step";

    private static final String PROVIDE_MODEL_ITEM_TYPE = "!MODEL_ITEM_TYPE"; // must be prefixed with an !
    private static final String PROVIDE_MODEL_ENTRY = "MODEL: ENTRY";
    private static final String PROVIDE_QUERY = "Operator();";


    private final CompletionInitializationContext context;
    private PsiElement elementAtCaret;
    private PsiElement contextElement;
    private String placeholder;

    public PlaceholderProvider(@NotNull CompletionInitializationContext context) {
        this.context = context;
        setElementAtCaret();
        setContextElement();
    }

    public String getIdentifier() {
        if (contextElement == null) {
            return context.getDummyIdentifier();
        }
        // check if the file has an error in the grammar parser before completion
        if (hasErrorState()) {
            // try to resolve it, only if the error element is close to the caret so it can be safely assumed
            // the placeholder will provide the missing element in the grammar
            if (setFromErrorState()) {
                return placeholder;
            }
        }
        if (provideModelEntry()) {
            // Model or Block entry at the start of the line, set an entry placeholder
            return PROVIDE_MODEL_ENTRY;
        }

        return context.getDummyIdentifier();
    }

    private boolean provideModelEntry() {
        return (startOfLine() && contextElement.getParent() instanceof OMTBlockEntry) ||
                (startOfLine() && contextElement.getParent() instanceof OMTBlock);
    }

    private boolean hasErrorState() {
        return getErrorElement() != null;
    }

    public static List<String> getExpectedTypesFromError(PsiErrorElement errorElement) {
        List<String> expectedTypes = new ArrayList<>();
        if (errorElement == null) {
            return expectedTypes;
        }

        String errorDescription = errorElement.getErrorDescription();
        Pattern pattern = Pattern.compile("\\<(.*?)\\>|OMTTokenType.([^ ]*)");
        Matcher matcher = pattern.matcher(errorDescription);
        while (matcher.find()) {
            // every match is grouped as either 1 (<...>) or 2 OMTTokenType.
            expectedTypes.add(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
        }
        return expectedTypes;
    }

    private boolean setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        return true;
    }

    private PsiErrorElement getErrorElement() {
        return Arrays.stream(context.getFile().getChildren()).filter(
                element -> element instanceof PsiErrorElement &&
                        element.getTextOffset() <= context.getStartOffset() + 5
        ).map(element -> (PsiErrorElement) element).findFirst().orElse(null);
    }

    private boolean setFromErrorState() {
        final PsiErrorElement errorElement = getErrorElement();
        if (errorElement == null) {
            return false;
        }
        final List<String> expectedTypesFromError = getExpectedTypesFromError(errorElement);
        if (expectedTypesFromError.contains(EXPECTED_MODEL_ITEM_TYPE)) {
            return setPlaceholder(PROVIDE_MODEL_ITEM_TYPE);
        }
        if (expectedTypesFromError.contains(EXPECTED_BLOCK) ||
                expectedTypesFromError.contains(EXPECTED_MODEL_ITEM_BLOCK)) {
            return setPlaceholder(PROVIDE_MODEL_ENTRY);
        }
        if (expectedTypesFromError.contains(EXPECTED_QUERY) ||
                expectedTypesFromError.contains(EXPECTED_QUERY_STEP)) {
            return setPlaceholder(PROVIDE_QUERY);
        }
        return false;
    }

    private boolean startOfLine() {
        return contextElement != null &&
                contextElement.getNode() != null &&
                contextElement.getNode().getTreePrev() != null &&
                contextElement.getNode().getTreePrev().getText().equals("\n");
    }

    private void setElementAtCaret() {
        final int startOffset = context.getStartOffset();
        elementAtCaret = context.getFile().findElementAt(startOffset);
    }

    private void setContextElement() {
        contextElement = elementAtCaret;
        int offset = context.getStartOffset();
        while (contextElement == null) {
            offset--;
            contextElement = context.getFile().findElementAt(offset);
        }
    }

}
