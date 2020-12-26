package com.misset.opp.omt.psi.util;

import com.misset.opp.omt.util.BuiltInUtil;
import com.misset.opp.omt.util.ProjectUtil;
import com.misset.opp.omt.util.RDFModelUtil;

/**
 * Util manager to create and distribute singletons of the util classes
 */
public class UtilManager {

    private static ProjectUtil projectUtil;
    private static QueryUtil queryUtil;
    private static MemberUtil memberUtil;
    private static ImportUtil importUtil;
    private static ScriptUtil scriptUtil;
    private static ModelUtil modelUtil;
    private static BuiltInUtil builtInUtil;
    private static TokenUtil tokenUtil;
    private static VariableUtil variableUtil;
    private static CurieUtil curieUtil;

    private UtilManager() {
    }

    public static ProjectUtil getProjectUtil() {
        if (projectUtil == null) {
            projectUtil = new ProjectUtil();
        }
        return projectUtil;
    }

    public static QueryUtil getQueryUtil() {
        if (queryUtil == null) {
            queryUtil = new QueryUtil();
        }
        return queryUtil;
    }
    public static MemberUtil getMemberUtil() {
        if (memberUtil == null) {
            memberUtil = new MemberUtil();
        }
        return memberUtil;
    }

    public static ImportUtil getImportUtil() {
        if (importUtil == null) {
            importUtil = new ImportUtil();
        }
        return importUtil;
    }

    public static ScriptUtil getScriptUtil() {
        if (scriptUtil == null) {
            scriptUtil = new ScriptUtil();
        }
        return scriptUtil;
    }

    public static ModelUtil getModelUtil() {
        if (modelUtil == null) {
            modelUtil = new ModelUtil();
        }
        return modelUtil;
    }

    public static BuiltInUtil getBuiltinUtil() {
        if (builtInUtil == null) {
            builtInUtil = new BuiltInUtil();
        }
        return builtInUtil;
    }

    public static RDFModelUtil getRDFModelUtil() {
        return getProjectUtil().getRDFModelUtil();
    }

    public static VariableUtil getVariableUtil() {
        if (variableUtil == null) {
            variableUtil = new VariableUtil();
        }
        return variableUtil;
    }

    public static TokenUtil getTokenUtil() {
        if (tokenUtil == null) {
            tokenUtil = new TokenUtil();
        }
        return tokenUtil;
    }

    public static CurieUtil getCurieUtil() {
        if (curieUtil == null) {
            curieUtil = new CurieUtil();
        }
        return curieUtil;
    }
}
