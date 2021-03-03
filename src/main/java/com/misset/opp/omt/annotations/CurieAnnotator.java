package com.misset.opp.omt.annotations;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.intentions.prefix.RegisterPrefixIntention;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.OMTNamespaceIri;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTPrefix;

import static com.misset.opp.util.UtilManager.getProjectUtil;

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
        if (!(namespacePrefix.getParent() instanceof OMTPrefix)) {
            validateReference(namespacePrefix, "Unknown prefix",
                    annotationBuilder -> getProjectUtil().getKnownPrefixes(namespacePrefix.getName())
                            .stream()
                            .map(OMTPrefix::getNamespaceIri)
                            .map(OMTNamespaceIri::getName)
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
