package com.misset.opp.omt.psi.annotations;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.intentions.prefix.RegisterPrefixIntention;

import static com.misset.opp.omt.psi.util.UtilManager.getProjectUtil;

public class CurieAnnotator extends AbstractAnnotator {

    public CurieAnnotator(AnnotationHolder holder) {
        super(holder);
    }

    public void annotate(PsiElement element) {
        if (element instanceof OMTNamespacePrefix) {
            annotate((OMTNamespacePrefix) element);
        } else if (element instanceof OMTCurieElement) {
            annotate((OMTCurieElement) element);
        }
    }

    private void annotate(OMTNamespacePrefix namespacePrefix) {
        if (namespacePrefix.getParent() instanceof OMTPrefix) {
            annotateUsage(namespacePrefix);
        } else {
            validateReference(namespacePrefix, "Unknown prefix",
                    annotationBuilder -> getProjectUtil().getKnownPrefixes(namespacePrefix.getName())
                            .stream()
                            .map(OMTPrefix::getNamespaceIri)
                            .map(PsiElement::getText)
                            .distinct()
                            .map(iri -> RegisterPrefixIntention.getRegisterPrefixIntention(namespacePrefix, iri))
                            .forEach(annotationBuilder::withFix)
            );
        }
    }

    private void annotate(OMTCurieElement curieElement) {
        annotate(curieElement.getAsResource());
    }

}
