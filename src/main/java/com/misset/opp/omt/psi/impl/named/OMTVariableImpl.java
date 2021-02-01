package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.named.OMTVariableNamedElement;
import com.misset.opp.omt.psi.references.VariableReference;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.getModelUtil;
import static com.misset.opp.omt.psi.util.UtilManager.getVariableUtil;

public abstract class OMTVariableImpl extends NameIdentifierOwnerImpl<OMTVariable> implements OMTVariableNamedElement {

    public OMTVariableImpl(@NotNull ASTNode node) {
        super(node, OMTVariable.class);
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
    public OMTScalarValue getDefaultValue() {
        return getModelUtil().getEntryBlock(getPsi()).getBlockEntryList()
                .stream()
                .filter(omtBlockEntry -> omtBlockEntry.getName().equals("value"))
                .map(omtBlockEntry -> (OMTGenericBlock) omtBlockEntry)
                .findFirst()
                .filter(omtGenericBlock -> omtGenericBlock.getScalar() != null)
                .map(omtGenericBlock -> omtGenericBlock.getScalar().getScalarValue())
                .orElse(null);
    }

    @Override
    public List<OMTVariableAssignment> getAssignments() {
        return getVariableUtil().getAssignments(getPsi());
    }

    public boolean isIgnoredVariable() {
        return getPsi().getIgnoredVariable() != null;
    }

    @Override
    public boolean isReadOnly() {
        final OMTVariable variable = isDeclaredVariable() ? getPsi() :
                (getPsi().getReference() != null ? (OMTVariable) getPsi().getReference().resolve() : null);
        if (variable == null || !variable.isDestructedNotation()) {
            return false;
        }

        return getModelUtil().getEntryBlock(variable).getBlockEntryList()
                .stream()
                .filter(omtBlockEntry -> omtBlockEntry.getName().equals("readonly"))
                .map(omtBlockEntry -> (OMTGenericBlock) omtBlockEntry)
                .findFirst()
                .map(omtGenericBlock -> omtGenericBlock.getScalar() != null &&
                        omtGenericBlock.getScalar().getScalarValue().textMatches("true"))
                .orElse(false);
    }

    @Override
    public boolean isDestructedNotation() {
        return getModelUtil().getEntryBlockLabel(getPsi()).equals("name");
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
