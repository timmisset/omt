package com.misset.opp.omt.psi.impl;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.support.BuiltInType;
import com.misset.opp.omt.psi.support.OMTCallable;
import com.misset.opp.omt.psi.support.OMTParameter;

import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.getBuiltinUtil;

public class BuiltInMember extends OMTCallableImpl implements OMTCallable {

    public BuiltInMember(String name, List<OMTParameter> params, BuiltInType type, List<String> localVariables) {
        super(type.name(), name, params, getBuiltinUtil().isCommand(type), localVariables);
    }

    public BuiltInMember(String name, List<OMTParameter> params, BuiltInType type, List<String> localVariables, List<String> flags, String dataType) {
        super(type.name(), name, params, getBuiltinUtil().isCommand(type), localVariables, flags, dataType);
    }

    @Override
    public PsiElement getElement() {
        return null;
    }
}
