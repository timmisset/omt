package com.misset.opp.omt.external.util.builtIn;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.impl.OMTCallableImpl;
import com.misset.opp.omt.psi.support.OMTCallable;
import com.misset.opp.omt.psi.support.OMTParameter;

import java.util.List;

public class BuiltInMember extends OMTCallableImpl implements OMTCallable {

    public BuiltInMember(String name, List<OMTParameter> params, BuiltInType type) {
        super(type.name(), name, params, type == BuiltInType.Command);
    }

    public BuiltInMember(String name, List<OMTParameter> params, BuiltInType type, List<String> localVariables) {
        super(type.name(), name, params, type == BuiltInType.Command, localVariables);
    }

    @Override
    public PsiElement getElement() {
        return null;
    }

}
