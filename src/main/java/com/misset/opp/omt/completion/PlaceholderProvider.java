package com.misset.opp.omt.completion;

import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.TokenType;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTQueryPath;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The PlaceholderProvider determines a valid placeholder based on the current caret position and possible
 * error state the grammar parser might be in.
 * IntelliJ inserts a placeholder on the place of caret when completion is triggered to have an anchor
 * that can be used for completion. By default, the IntelliJRulezzzzz placeholder is used. However, in our
 * grammar parser that token is not always valid. This provider will replace the default token with
 * specific placeholders: PROVIDE_...
 * <p>
 * Try not to add any more complexity to this class, it's usually possible to change the flexibility of the
 * grammar parser instead.
 */
public class PlaceholderProvider {

    private static final String EXPECTED_MODEL_ITEM_TYPE = "MODEL_ITEM_TYPE";
    private static final String EXPECTED_BLOCK = "block";
    private static final String EXPECTED_MODEL_ITEM_BLOCK = "model item block";
    private static final String EXPECTED_QUERY = "query";
    private static final String EXPECTED_QUERY_STEP = "query step";

    private static final String PROVIDE_MODEL_ITEM_TYPE = "!MODEL_ITEM_TYPE"; // must be prefixed with an !
    private static final String PROVIDE_MODEL_ENTRY = "MODEL: ENTRY";
    private static final String PROVIDE_QUERY = "Operator();";
    private static final String PROVIDE_QUERY_STEP_WITH_SEPARATOR = "QueryStep /";

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
            return PROVIDE_MODEL_ENTRY;
        }
        // check if the file has an error in the grammar parser before completion
        if (hasErrorState() && setFromErrorState()) {
            // try to resolve it, only if the error element is close to the caret so it can be safely assumed
            // the placeholder will provide the missing element in the grammar
            return placeholder;
        }
        if (provideModelEntry()) {
            // Model or Block entry at the start of the line (including indentation), set an entry placeholder
            return PROVIDE_MODEL_ENTRY;
        }
        if (provideQueryStepWithSeperator()) {
            return PROVIDE_QUERY_STEP_WITH_SEPARATOR;
        }
        return context.getDummyIdentifier();
    }

    private boolean provideModelEntry() {
        final PsiElement elementParent = contextElement.getParent();
        return startOfLine() && (
                // start of new line
                (contextElement.getText().endsWith("\n") && elementParent instanceof OMTFile) ||
                        // entry block
                        elementParent instanceof OMTBlockEntry ||
                        elementParent instanceof OMTBlock
        );
    }

    private boolean provideQueryStepWithSeperator() {
        return PlatformPatterns.psiElement()
                .inside(OMTQueryPath.class)
                .afterLeaf("/")
                .accepts(contextElement);
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

    private void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
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
            setPlaceholder(PROVIDE_MODEL_ITEM_TYPE);
            return true;
        }
        if (expectedTypesFromError.contains(EXPECTED_BLOCK) ||
                expectedTypesFromError.contains(EXPECTED_MODEL_ITEM_BLOCK)) {
            setPlaceholder(PROVIDE_MODEL_ENTRY);
            return true;
        }
        if (expectedTypesFromError.contains(EXPECTED_QUERY) ||
                expectedTypesFromError.contains(EXPECTED_QUERY_STEP)) {
            setPlaceholder(PROVIDE_QUERY);
            return true;
        }
        return false;
    }

    private boolean startOfLine() {
        if (context.getCaret().getLogicalPosition().column == 0) return true;
        PsiElement el = contextElement.getPrevSibling();
        while (el != null && !el.getText().startsWith("\n")) {
            if (el.getNode().getElementType() != TokenType.WHITE_SPACE) {
                return false;
            }
            el = el.getPrevSibling();
        }
        return true;
    }

    private void setElementAtCaret() {
        final int startOffset = context.getStartOffset();
        elementAtCaret = context.getFile().findElementAt(startOffset);
    }

    private void setContextElement() {
        contextElement = elementAtCaret;
        int offset = context.getStartOffset();
        while (contextElement == null && offset > 0) {
            offset--;
            contextElement = context.getFile().findElementAt(offset);
        }
    }

}
