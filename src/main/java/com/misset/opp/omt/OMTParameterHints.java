package com.misset.opp.omt;

import com.intellij.codeInsight.hints.InlayInfo;
import com.intellij.codeInsight.hints.InlayParameterHintsProvider;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTQueryStep;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OMTParameterHints implements InlayParameterHintsProvider {

    @NotNull
    @Override
    public List<InlayInfo> getParameterHints(@NotNull PsiElement element) {
        // turn off for now, very intrusive feature
//        if(element instanceof OMTQueryStep) {
//                return Collections.singletonList(
//                        new InlayInfo(
//                                getResourceHints((OMTQueryStep) element),
//                                element.getTextOffset())
//            );
//        }
        return new ArrayList<>();
    }

    private String getResourceHints(OMTQueryStep queryStep) {
        return String.join(", ",
                queryStep.resolveToResource().stream().map(Resource::getLocalName)
                        .collect(Collectors.toList()));
    }

    @NotNull
    @Override
    public Set<String> getDefaultBlackList() {
        return Collections.emptySet();
    }
}
