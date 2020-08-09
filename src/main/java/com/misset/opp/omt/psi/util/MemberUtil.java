package com.misset.opp.omt.psi.util;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.file.PsiJavaDirectoryImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberUtil {

    /**
     * Returns the PsiElement which contains the declaration for this OperatorCall
     * This can be a DefineQueryStatement somewhere upstream or an import statement
     * The declaration of the operator must precede it's call to it, not only upstream but also within the same declaration block
     * @param operatorCall
     * @return
     */
    public static Optional<PsiElement> getDeclaringMember(OMTOperatorCall operatorCall) {
        OMTDefineQueryStatement definedQueryStatement = getDefinedQueryStatement(operatorCall);
        if(definedQueryStatement != null) {
            // check if the member is declared before it's used, which is a requirement for an OperatorCall
            if(!isCallBeforeDefine(operatorCall, definedQueryStatement)) {
                return Optional.of(definedQueryStatement);
            }
        }

        // not found as a member of a defined block, check for imports:
        OMTFile containingFile = (OMTFile) operatorCall.getContainingFile();
        return containingFile.getImportedMembers().stream()
                .filter(member -> member.textMatches(operatorCall.getFirstChild().getText()))
                .map(member -> (PsiElement)member)
                .findFirst();
    }

    /**
     * Checks if the call is made before the operator is defined
     * @param call - operatorCall or commandCall
     * @param define - definedQueryStatement or definedCommandStatement
     * @return
     */
    private static boolean isCallBeforeDefine(PsiElement call, PsiElement define) {
        PsiElement callContainingElement = getCallContainingElement(call);

        if(callContainingElement.getParent() == define.getParent()) {
            // call is part of the same block as the parent:
            return callContainingElement.getStartOffsetInParent() <= define.getStartOffsetInParent();
        } else {
            // different containers. For example:
            //
            // title: getTitle()                            <<-- Block entry (Scalar)
            // queries: |
            //      DEFINE QUERY getTitle() => 'title';     <<-- DefineQueryStatement

            // in this case, the parent define statement, must have a lower or equal depth to the
            // the block entry that's using it.
            return PsiTreeUtil.getDepth(call, call.getContainingFile()) < PsiTreeUtil.getDepth(define.getParent(), define.getContainingFile());
        }




    }

    private static PsiElement getCallContainingElement(PsiElement call) {
        // the call depth depends on the origin:
        PsiElement containingElement = PsiTreeUtil.getTopmostParentOfType(call, OMTDefineQueryStatement.class);
        if(containingElement == null) { containingElement = PsiTreeUtil.getTopmostParentOfType(call, OMTDefineCommandStatement.class); }
        if(containingElement == null) { containingElement = PsiTreeUtil.getTopmostParentOfType(call, OMTScriptLine.class); }
        if(containingElement == null) { containingElement = PsiTreeUtil.getTopmostParentOfType(call, OMTBlockEntry.class); }

        return containingElement;
    }

    public static Optional<OMTQueriesBlock> getRootQueriesBlock(PsiElement element) {
        OMTBlock mainBlock = (OMTBlock) element.getContainingFile().getFirstChild();
        return mainBlock.getSpecificBlockList().stream()
                .filter(specificBlock -> specificBlock.getQueriesBlock() != null)
                .map(OMTSpecificBlock::getQueriesBlock)
                .findFirst();
    }

    /**
     * Returns the defined query statement corresponding to the operator call name
     * It doesn't validate that the defined statement precedes the call
     * @param operatorCall
     * @return
     */
    public static OMTDefineQueryStatement getDefinedQueryStatement(OMTOperatorCall operatorCall) {

        PsiElement element = operatorCall.getParent();
        List<OMTQueriesBlock> processedBlocks = new ArrayList<>();
        while(element != null) {
            if(element instanceof OMTQueriesBlock) {
                Optional<OMTDefineQueryStatement> queryDefined = ((OMTQueriesBlock) element).getDefineQueryStatementList().stream()
                        .filter(defineQueryStatement -> defineQueryStatement.getDefineName().textMatches(operatorCall.getFirstChild().getText()))
                        .findFirst();
                if(queryDefined.isPresent()) { return queryDefined.get(); }
                processedBlocks.add((OMTQueriesBlock) element);
            }
            element = element.getParent();
            if(element instanceof PsiJavaDirectoryImpl) {
                return null;
            }
            if(element != null) {
                OMTQueriesBlock childOfType = PsiTreeUtil.findChildOfType(element, OMTQueriesBlock.class);
                if(childOfType != null && !processedBlocks.contains(childOfType)) { element = childOfType; }
            }

        }
        return null;
    }

}
