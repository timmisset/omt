package com.misset.opp.omt.references;

import com.intellij.codeInsight.lookup.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.misset.opp.omt.psi.OMTOperator;
import com.misset.opp.omt.psi.util.OperatorUtil;
import org.jetbrains.annotations.*;

import java.util.*;

public class OperatorReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private final String key;

    public OperatorReference(@NotNull PsiElement element, TextRange textRange) {
        super(element, textRange);
        key = element.getText().substring(textRange.getStartOffset(), textRange.getEndOffset());
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        Project project = myElement.getProject();
//        final List<OMTOperator> omtOperators = OperatorUtil.getOperator();
        List<ResolveResult> results = new ArrayList<>();
//        for (SimpleProperty property : properties) {
//            results.add(new PsiElementResolveResult(property));
//        }
        return results.toArray(new ResolveResult[results.size()]);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        Project project = myElement.getProject();
//        List<SimpleProperty> properties = SimpleUtil.findProperties(project);
        List<LookupElement> variants = new ArrayList<>();
//        for (final SimpleProperty property : properties) {
//            if (property.getKey() != null && property.getKey().length() > 0) {
//                variants.add(LookupElementBuilder
//                        .create(property).withIcon(SimpleIcons.FILE)
//                        .withTypeText(property.getContainingFile().getName())
//                );
//            }
//        }
        variants.add(LookupElementBuilder
                .create("test")
        );
        return variants.toArray();
    }
}
