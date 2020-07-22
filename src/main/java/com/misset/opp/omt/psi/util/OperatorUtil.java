package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTDefineQueryStatement;
import com.misset.opp.omt.psi.OMTOperator;
import com.misset.opp.omt.psi.OMTOperatorCall;
import org.apache.maven.model.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OperatorUtil {

    public static Optional<OMTOperator> getOperator(OMTOperatorCall operatorCall) {
        // returns the operator with the same name
        return getAllAvailableOperators(operatorCall).stream()
                .filter(omtOperator -> omtOperator.getName().equals(getName(operatorCall)))
                .findFirst();
    }

    public static String getName(OMTOperatorCall operatorCall) {
        return operatorCall.getFirstChild().getText();
    }

    public static List<OMTOperator> getAllAvailableOperators(PsiElement element) {
        List<OMTOperator> operators = new ArrayList<>();

        // Operators can be build in operators, imported, defined queries in the document or standalone queries
        // TODO: Get buildin operators

        // TODO: Get imported operators

        // Defined in the document
        // from the root queries: block
        ModelUtil.getAllDefinedQueries(element.getContainingFile()).forEach(
                defineQueryStatement -> operators.add(new OMTOperator(defineQueryStatement))
        );
        // or as part of the containing modelItem block
        ModelUtil.getModelItemBlock(element).ifPresent(omtBlock ->
                ModelUtil.getAllDefinedQueries(omtBlock).forEach(defineQueryStatement ->
                        operators.add(new OMTOperator(defineQueryStatement)))
        );
        return operators;
    }
}
