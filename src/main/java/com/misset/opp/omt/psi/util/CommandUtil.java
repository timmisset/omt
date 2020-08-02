package com.misset.opp.omt.psi.util;

import com.misset.opp.omt.psi.OMTCommand;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.OMTParameter;

import java.util.List;

public class CommandUtil {
    public static OMTCommand modelItemToCommand(OMTModelItemBlock modelItemBlock) {

        OMTModelItemLabel modelItemLabel = modelItemBlock.getModelItemLabel();
        String modelItemType = modelItemLabel.getModelItemTypeCast().getText().substring(1); // drop the !
        String name = modelItemLabel.getPropertyName().getText();
        name = name.substring(0, name.length() - 1);

        // get the params:
        List<OMTParameter> parameters = ModelUtil.getModelItemParameters(modelItemBlock);
        return new OMTCommand(name, parameters, modelItemType);
    }
}
