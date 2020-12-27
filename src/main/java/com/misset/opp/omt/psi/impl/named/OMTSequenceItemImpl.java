package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTGenericBlock;
import com.misset.opp.omt.psi.OMTScalarValue;
import com.misset.opp.omt.psi.OMTSequenceItem;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class OMTSequenceItemImpl extends OMTNamedElementImpl {
    public OMTSequenceItemImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        final OMTSequenceItem sequenceItem = getNode().getPsi(OMTSequenceItem.class);
        if (sequenceItem.getScalarValue() != null) {
            final OMTScalarValue scalarValue = sequenceItem.getScalarValue();
            if (scalarValue.getParameterWithType() != null) {
                return scalarValue.getParameterWithType().getVariable().getName();
            }
            if (scalarValue.getVariableAssignment() != null) {
                return scalarValue.getVariableAssignment().getVariableList().get(0).getName();
            }
            if (scalarValue.getQuery() != null) {
                return scalarValue.getQuery().getText();
            }
            if (scalarValue.getIndentedBlock() != null) {
                // a name can only be returned for a scalar value when the block contains the name: property
                return scalarValue.getIndentedBlock().getBlockEntryList()
                        .stream().filter(
                                blockEntry ->
                                        blockEntry instanceof OMTGenericBlock &&
                                                blockEntry.getName().equals("name"))
                        .map(blockEntry -> ((OMTGenericBlock) blockEntry).getScalar())
                        .filter(Objects::nonNull)
                        .map(omtScalar -> omtScalar.getText().trim())
                        .findFirst()
                        .orElse(null);
            }
        }
        return null;
    }
}
