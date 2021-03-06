package com.misset.opp.omt.psi.resolvable.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.OMTScalarValue;
import com.misset.opp.omt.psi.OMTScriptContent;
import com.misset.opp.omt.psi.OMTScriptLine;
import com.misset.opp.omt.psi.OMTStringEntry;
import com.misset.opp.omt.psi.OMTVariableAssignment;
import com.misset.opp.omt.psi.resolvable.OMTScalarValueResolvable;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.misset.opp.util.UtilManager.getRDFModelUtil;

public abstract class OMTScalarValueResovableImpl extends ASTWrapperPsiElement implements OMTScalarValueResolvable, OMTScalarValue {
    public OMTScalarValueResovableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public OMTQuery getQuery() {
        final OMTScriptContent singleContentItem = getSingleContentItem();
        return singleContentItem != null ?
                singleContentItem.getQuery() :
                null;
    }

    @Override
    public OMTVariableAssignment getVariableAssignment() {
        final OMTScriptContent singleContentItem = getSingleContentItem();
        return singleContentItem != null ?
                singleContentItem.getVariableAssignment() :
                null;
    }

    @Override
    public OMTStringEntry getStringEntry() {
        final OMTScriptContent singleContentItem = getSingleContentItem();
        return singleContentItem != null ?
                singleContentItem.getStringEntry() :
                null;
    }

    private OMTScriptLine getSingleLineItem() {
        return getScript() != null &&
                getScript().getScriptLineList().size() == 1 ?
                getScript().getScriptLineList().get(0) :
                null;
    }

    private OMTScriptContent getSingleContentItem() {
        final OMTScriptLine singleLineItem = getSingleLineItem();
        return singleLineItem != null && singleLineItem.getScriptContent() != null ?
                singleLineItem.getScriptContent() :
                null;
    }

    @Override
    public List<Resource> resolveToResource() {
        if (getQuery() != null) {
            return getQuery().resolveToResource();
        } else if (getStringEntry() != null) {
            return Collections.singletonList(getRDFModelUtil().getStringType());
        }
        return Collections.emptyList();
    }
}
