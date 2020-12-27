package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.OMTElementFactory;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.OMTVariableAssignment;
import com.misset.opp.omt.psi.OMTVariableValue;
import com.misset.opp.omt.psi.named.OMTVariableNamedElement;
import com.misset.opp.omt.psi.references.VariableReference;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.getVariableUtil;

public abstract class OMTVariableImpl extends NameIdentifierOwnerImpl<OMTVariable> implements OMTVariableNamedElement {

    public OMTVariableImpl(@NotNull ASTNode node) {
        super(node);
    }

    @NotNull
    @Override
    public String getName() {
        return getText();
    }

    @Override
    public OMTVariable setName(@NotNull String newName) {
        if (newName.startsWith("$")) {
            OMTVariable replacement = OMTElementFactory.createVariable(getPsi().getProject(), newName);
            getPsi().replace(replacement);
            return replacement;
        }
        return getPsi();
    }

    @NotNull
    @Override
    public PsiElement getNameIdentifier() {
        return this;
    }

    public boolean isDeclaredVariable() {
        return getVariableUtil().isDeclaredVariable(getPsi());
    }

    public boolean isGlobalVariable() {
        return getPsi().getGlobalVariable() != null;
    }

    @Override
    public List<Resource> getType() {
        return getVariableUtil().getType(getPsi());
    }

    @Override
    public OMTVariableValue getValue() {
        return getVariableUtil().getValue(getPsi());
    }

    @Override
    public List<OMTVariableAssignment> getAssignments() {
        return getVariableUtil().getAssignments(getPsi());
    }

    public boolean isIgnoredVariable() {
        return getPsi().getIgnoredVariable() != null;
    }

    @Nullable
    @Override
    public PsiReference getReference() {
        return toReference((OMTVariable) getNode().getPsi());
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        return new PsiReference[]{toReference((OMTVariable) getNode().getPsi())};
    }

    private PsiReference toReference(OMTVariable variable) {
        TextRange property = new TextRange(0, variable.getText().length());
        return new VariableReference(variable, property);
    }

}
