package com.misset.opp.omt.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.misset.opp.omt.completion.command.ScriptContentCompletion;
import com.misset.opp.omt.completion.command.SignatureArgumentCompletion;
import com.misset.opp.omt.completion.command.VariableAssignmentCompletion;
import com.misset.opp.omt.completion.model.*;
import com.misset.opp.omt.completion.query.QueryEquationStatementCompletion;
import com.misset.opp.omt.completion.query.QueryFilterStepCompletion;
import com.misset.opp.omt.completion.query.QueryFirstStepCompletion;
import com.misset.opp.omt.completion.query.QueryNextStepCompletion;
import org.jetbrains.annotations.NotNull;

/**
 * The completion contributor is registered into IntelliJ and is started whenever the users triggers auto-completion
 * A completion consists of a pattern that is recognized and a CompletionProvider which is then returned for that specific
 * pattern.
 * Although it is possible to have distinct providers (one for variables, one for queries etc) that respond to different patterns,
 * it was more concise to have providers organised by the pattern they react to and have them provide multiple completions.
 * For example, at the start of a new query the QueryFirstStepCompletion
 * is triggered by the pattern that recognizes the first query step position and then adds operators, queries, variables etc.
 * <p>
 * For testing:
 * When adding a new pattern, register it here, then set a break-point at the first pattern operator (for example in the inside())
 * to see what the parsed element is and why it might not be picked up.
 */
public class OMTCompletionContributor extends CompletionContributor {
    /**
     * Generic completion that resolves the suggestion based on the cursor position
     */
    public OMTCompletionContributor() {
        ModelItemCompletion.register(this);
        ModelCompletion.register(this);
        ScalarValueCompletion.register(this);
        RootCompletion.register(this);
        QueryBlockCompletion.register(this);
        CommandBlockCompletion.register(this);
        QueryFirstStepCompletion.register(this);
        QueryNextStepCompletion.register(this);
        QueryFilterStepCompletion.register(this);
        QueryEquationStatementCompletion.register(this);
        ParameterTypeCompletion.register(this);
        ImportCompletion.register(this);
        ScriptContentCompletion.register(this);
        VariableAssignmentCompletion.register(this);
        SignatureArgumentCompletion.register(this);
    }

    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        final String identifier = new PlaceholderProvider(context).getIdentifier();
        if (identifier != null) {
            context.setDummyIdentifier(identifier);
        }
    }

}
