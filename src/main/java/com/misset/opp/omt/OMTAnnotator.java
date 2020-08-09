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
//        if(element instanceof OMTCurieElement) {
//            annotateCurie(new OMTCurie((OMTCurieElement)element), holder);
//        }
//        else if(element instanceof OMTCurieConstantElement) {
//            annotateCurie(new OMTCurie((OMTCurieConstantElement)element), holder);
//        }
//        else if(element instanceof OMTPrefix) {
//            annotatePrefix((OMTPrefix)element, holder);
//        }
//        else if(element instanceof OMTVariable) {
//            annotateVariable((OMTVariable)element, holder);
//        }
//        else if(element instanceof OMTVariableValue) {
//            annotateVariableAssignment((OMTVariableValue)element, holder);
//        }
//        else if(element instanceof OMTOperatorCall) {
//            annotateOperatorCall((OMTOperatorCall)element, holder);
//        }
//        else if(element instanceof OMTModelItemLabel) {
//            annotateModelItemBlock((OMTModelItemLabel)element, holder);
//        }
//        else if(element instanceof OMTListItemImport) {
//            annotateImport((OMTListItemImport) element, holder);
//        }
//        else if(element instanceof OMTFile) {
//            ProjectUtil.resetImportedExportedMembers((OMTFile) element);
//        }
    }

//    private void annotateImport(@NotNull OMTListItemImport element, @NotNull AnnotationHolder holder) {
//        // discriminate between a module import and a file import
//        OMTImport omtImport = (OMTImport)element.getParent().getParent(); // get the block, listItem -> list -> block
//        OMTImportSource importSource = omtImport.getImportSource();
//
//        if(importSource.toString().startsWith("module:")) {
//            // TODO:
//            // process module imports
//        }
//        else {
//            OMTFile omtFile = ImportUtil.getOMTFile(omtImport);
//            if(omtFile == null) {
//                holder.createErrorAnnotation(omtImport.getImportSource(), String.format("Cannot find file at: %s", ImportUtil.resolvePathToSource(omtImport)));
//            }
//
//            HashMap<String, OMTBuiltIn> allExportingMembers = ImportUtil.getAllExportedMembers(omtFile);
//            String memberName = element.getListItemImportMember().getText();
//            if(!allExportingMembers.containsKey(memberName)) {
//                String message = String.format("%s has no exporting member '%s'", omtImport.getImportSource().getText(), memberName);
//                holder.createErrorAnnotation(element.getListItemImportMember(), message);
//            }
//        }
//    }
//
//    private void annotateModelItemBlock(@NotNull OMTModelItemLabel modelItemLabel, @NotNull AnnotationHolder holder) {
//        OMTModelItemBlock modelItemBlock = (OMTModelItemBlock) modelItemLabel.getParent();
//
//        // bepaal aan de hand van de ingeladen OMT model files of the attributes op het model item wel mogen bestaan:
//        OMTModelItem modelItem = ModelUtil.getModelItem(modelItemBlock, modelItemBlock.getProject());
//        if(!ModelUtil.annotateModelItems) { return; } // disabled due to some error with parsing the files.
//        if(modelItem == null) {
//            OMTModelItemTypeElement omtModelItemTypeElement = modelItemBlock.getModelItemLabel().getModelItemTypeElement();
//            String message = String.format("%s is not a known model item type", omtModelItemTypeElement.getText().substring(1));
//            holder.createErrorAnnotation(omtModelItemTypeElement, message);
//            return;
//        }
//
//        // annotate the attributen:
//        List<OMTBlock> blockContentList = modelItemBlock.getBlockList();
//        blockContentList.forEach(omtBlock -> {
//            String label = ModelUtil.getModelItemAttributeLabel(omtBlock);
//            if(!modelItem.hasAttribute(label)) {
//                String suggestion = ModelUtil.getModelItemAttributeSuggestion(modelItem, label);
//                String message = String.format("%s is not a known attribute for %s", label, modelItem.getType());
//                if(suggestion != null) {
//                    message = String.format("%s, did you mean '%s'", message, suggestion);
//                }
//                holder.createErrorAnnotation(omtBlockContent, message);
//            };
//        });
//    }
//
//    private void annotateOperatorCall(@NotNull OMTOperatorCall operatorCall, @NotNull AnnotationHolder holder) {
//        Optional<OMTOperator> optionalOperator = OperatorUtil.getOperator(operatorCall);
//        optionalOperator.ifPresent(OMTOperator -> {
//            // check the signature:
//            try {
//                OMTOperator.validateSignature(operatorCall);
//            } catch (NumberOfInputParametersMismatchException e) {
//                holder.createErrorAnnotation(operatorCall, e.getMessage());
//            }
//        });
//        if(!optionalOperator.isPresent()) {
//            holder.createErrorAnnotation(operatorCall.getFirstChild(), "Unknown operator");
//        }
//    }
//    private void annotateVariableAssignment(@NotNull OMTVariableValue variableAssignmentValue, @NotNull AnnotationHolder holder) {
//        // check if the assigned value is ever used:
//        if(!VariableUtil.isVariableAssignmentValueUsed(variableAssignmentValue)) {
//            holder.createErrorAnnotation(variableAssignmentValue, "Value is never used");
//        }
//    }
//
//    private void annotateVariable(@NotNull OMTVariable variable, @NotNull AnnotationHolder holder) {
//        // first, check if we are dealing with a variable declare:
//        if(VariableUtil.isVariableDeclare(variable)) {
//            List<OMTVariable> variableUsage = VariableUtil.getVariableUsage(variable);
//            if(variableUsage.isEmpty()) {
//                holder.createErrorAnnotation(variable, "Variable is declared but it's never used");
//            }
//        } else {
//            // variable is used, check if it has been declared
//            Optional<OMTVariable> declaredByVariable = VariableUtil.getDeclaredByVariable(variable);
//            if(!declaredByVariable.isPresent()) {
//                holder.createErrorAnnotation(variable, String.format("Variable %s is not defined", variable.getText()));
//            }
//        }
//    }
//
//    private void annotateCurie(@NotNull OMTCurie curie, @NotNull AnnotationHolder holder) {
//        Optional<OMTPrefix> definedByPrefix = CurieUtil.getDefinedByPrefix(curie);
//        if(!definedByPrefix.isPresent()) {
//            Annotation prefix_is_not_defined = holder.createErrorAnnotation(curie.getElement(), "Prefix is not defined");
//            prefix_is_not_defined.registerFix(getRegisterPrefixIntention(curie));
//        }
//    }
//    private void annotatePrefix(@NotNull OMTPrefix prefix, @NotNull AnnotationHolder holder) {
//        if(CurieUtil.isPrefixedDefinedMoreThanOnce(prefix)) {
//            Annotation prefix_is_defined_more_than_once = holder.createErrorAnnotation(prefix, "Prefix is defined more than once");
//            prefix_is_defined_more_than_once.registerFix(getRemovePrefixIntention(prefix));
//        }
//        if(!CurieUtil.isPrefixUsed(prefix)) {
//            Annotation prefix_is_not_used = holder.createWarningAnnotation(prefix, "Prefix is defined but it's value is never used");
//            prefix_is_not_used.registerFix(getRemovePrefixIntention(prefix));
//        }
//    }
}

