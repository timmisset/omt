package com.misset.opp.omt;

//import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
//import com.misset.opp.omt.domain.OMTBuiltIn;
//import com.misset.opp.omt.domain.OMTCurie;
//import com.misset.opp.omt.domain.OMTModelItem;
//import com.misset.opp.omt.domain.OMTOperator;
//import com.misset.opp.omt.psi.*;
//import com.misset.opp.omt.psi.exceptions.NumberOfInputParametersMismatchException;
//import com.misset.opp.omt.domain.util.*;
import com.misset.opp.omt.psi.OMTCurieElement;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.util.CurieUtil;
import com.misset.opp.omt.psi.util.VariableUtil;
import org.jetbrains.annotations.NotNull;

//import java.util.HashMap;
//import java.util.List;
//import java.util.Optional;
//
//import static com.misset.opp.omt.psi.intentions.prefix.registerPrefixIntention.getRegisterPrefixIntention;
//import static com.misset.opp.omt.psi.intentions.prefix.removePrefixIntention.getRemovePrefixIntention;


public class OMTAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        if(element instanceof OMTVariable) {
            VariableUtil.annotateVariable((OMTVariable)element, holder);
        }
        if(element instanceof OMTNamespacePrefix) {
            CurieUtil.annotateNamespacePrefix((OMTNamespacePrefix) element, holder);
        }
    }

}

