package com.misset.opp.omt;

//import com.intellij.lang.annotation.Annotation;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.util.*;
import org.jetbrains.annotations.NotNull;

//import java.util.HashMap;
//import java.util.List;
//import java.util.Optional;
//
//import static com.misset.opp.omt.psi.intentions.prefix.registerPrefixIntention.getRegisterPrefixIntention;
//import static com.misset.opp.omt.psi.intentions.prefix.removePrefixIntention.getRemovePrefixIntention;


public class OMTAnnotator implements Annotator {

    final ModelUtil modelUtil = ModelUtil.SINGLETON;

    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof OMTVariable) {
            VariableUtil.annotateVariable((OMTVariable) element, holder);
        }
        if (element instanceof OMTNamespacePrefix) {
            CurieUtil.annotateNamespacePrefix((OMTNamespacePrefix) element, holder);
        }
        if (element instanceof OMTImport) {
            ImportUtil.annotateImport((OMTImport) element, holder);
        }
        if (element instanceof OMTCommandCall) {
            MemberUtil.annotateCall((OMTCommandCall) element, holder);
        }
        if (element instanceof OMTOperatorCall) {
            MemberUtil.annotateCall((OMTOperatorCall) element, holder);
        }
        if (element instanceof OMTModelItemBlock) {
            modelUtil.annotateModelItem((OMTModelItemBlock) element, holder);
        }
        if (element instanceof OMTReturnStatement) {
            ScriptUtil.annotateFinalStatement(element, holder);
        }
    }

}

