package com.misset.opp.omt.completion.command;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.completion.OMTCompletionContributor;
import com.misset.opp.omt.completion.RDFCompletion;
import com.misset.opp.omt.psi.OMTSignatureArgument;
import com.misset.opp.omt.psi.impl.OMTSignatureImpl;
import com.misset.opp.omt.psi.support.OMTCall;
import com.misset.opp.omt.psi.support.OMTCallable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class SignatureArgumentCompletion extends RDFCompletion {

    // VariableAssignment
    // commands: |
    //   DEFINE COMMAND command => {
    //      VAR $variable = @Method(<caret>);
    //      VAR $variable2 = @Method('value', <caret>);
    //   }
    // A signature has a type check to determine the acceptable inputs
    // we can use this to make more specific suggestions
    public static void register(OMTCompletionContributor completionContributor) {
        final ElementPattern<PsiElement> pattern = PlatformPatterns.psiElement()
//                .with(new PatternCondition<PsiElement>("test") {
//                    @Override
//                    public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
//                        return false;
//                    }
//                })
                .atStartOf(PlatformPatterns.psiElement(OMTSignatureArgument.class));
        completionContributor.extend(CompletionType.BASIC, pattern,
                new SignatureArgumentCompletion().getCompletionProvider());
    }

    public CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                PsiElement element = parameters.getPosition();

                // retrieve the signature argument and determine it's position in the signature
                OMTSignatureArgument signatureArgument = PsiTreeUtil.getParentOfType(element, OMTSignatureArgument.class);
                if (signatureArgument == null || signatureArgument.getParent() == null) {
                    return;
                }
                OMTSignatureImpl signature = (OMTSignatureImpl) signatureArgument.getParent();

                // then retrieve this call to resolve it to the callable method
                OMTCall call = PsiTreeUtil.getParentOfType(signatureArgument, OMTCall.class);
                if (call == null) {
                    return;
                }
                OMTCallable callable = call.getCallable();
                if (callable == null) {
                    return;
                }

                // the index is required to determine the type of parameter declared in the method
                // since ... rest/varArgs are accepted we cannot use the index of the argument itself
                int index = signature.getSignatureArgumentList().indexOf(signatureArgument);

                // Using the acceptsArgument to filter for applicable types
                Predicate<OMTCallable> acceptsInput = callableMember ->
                        !callableMember.getName().equals(callable.getName()) &&
                                callable.acceptsArgument(index, callableMember.getReturnType());

                // all accessible commands
                setResolvedElementsForDefinedCommands(element, acceptsInput);
                // all builtin commands
                setResolvedElementsForBuiltinCommands(acceptsInput);
                // all accessible commands
                setResolvedElementsForDefinedQueries(element, acceptsInput);
                // all builtin commands
                setResolvedElementsForBuiltinOperators(acceptsInput);
                // all accessible variables
                setResolvedElementsForVariables(element);

                complete(result);
            }
        };
    }

}
