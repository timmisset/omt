package com.misset.opp.omt.psi.references;

import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class OMTReferenceProvider extends PsiReferenceContributor {
    private static final PatternCondition<PsiElement> pattern = new PatternCondition<>("ForOmtComponentDecorator") {
        @Override
        public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
            final ES6Decorator decorator = PsiTreeUtil.getParentOfType(element, ES6Decorator.class);
            return
                    element instanceof JSLiteralExpression &&
                            decorator != null &&
                            decorator.getDecoratorName().startsWith("ForOmt");
        }
    };

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement().with(pattern),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
                                                                 @NotNull ProcessingContext context) {
                        JSLiteralExpression literalExpression = (JSLiteralExpression) element;
                        final ES6Decorator decorator = PsiTreeUtil.getParentOfType(element, ES6Decorator.class);
                        final OMTFile importedOMTFile = getImportedOMTFile(decorator);
                        if (importedOMTFile == null) return PsiReference.EMPTY_ARRAY;

                        return PsiTreeUtil.findChildrenOfType(importedOMTFile, OMTModelItemLabel.class)
                                .stream()
                                .filter(modelItemLabel -> modelItemLabel.getName().equals(literalExpression.getStringValue()))
                                .findFirst()
                                .map(modelItemLabel -> new PsiReference[]{
                                        new ForeignReference(element, TextRange.from(1, element.getTextLength() - 2), modelItemLabel)
                                }).orElse(PsiReference.EMPTY_ARRAY);
                    }
                });
    }

    private OMTFile getImportedOMTFile(ES6Decorator decorator) {
        final Optional<JSReferenceExpression> omt = PsiTreeUtil.findChildrenOfType(decorator, JSReferenceExpression.class).stream().filter(
                jsReferenceExpression -> jsReferenceExpression.getText().equals("omt")
        ).findFirst();
        if (omt.isEmpty()) {
            return null;
        }
        final PsiReference reference = omt.get().getReference();
        if (reference == null) {
            return null;
        }
        final ES6ImportedBinding importedBinding = (ES6ImportedBinding) reference.resolve();
        if (importedBinding == null) {
            return null;
        }

        return importedBinding.findReferencedElements().stream()
                .filter(element -> element instanceof OMTFile)
                .map(element -> (OMTFile) element)
                .findFirst().orElse(null);
    }
}
