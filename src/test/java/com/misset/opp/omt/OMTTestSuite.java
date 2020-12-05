package com.misset.opp.omt;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.psi.util.*;
import com.misset.opp.omt.util.BuiltInUtil;
import com.misset.opp.omt.util.ProjectUtil;
import com.misset.opp.omt.util.RDFModelUtil;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;

import static com.misset.opp.omt.psi.util.UtilManager.*;

public class OMTTestSuite extends LightJavaCodeInsightFixtureTestCase {

    private MockedStatic<UtilManager> utilManager;

    @Override
    protected void tearDown() throws Exception {
        if (utilManager != null && !utilManager.isClosed()) {
            utilManager.close();
        }
        if (myFixture != null) {
            super.tearDown();
        }
    }


    protected void setOntologyModel() {
        myFixture.copyFileToProject(new File("src/test/resources/examples/model.ttl").getAbsolutePath(), "test/resources/examples/root.ttl");
        ApplicationManager.getApplication().runReadAction(() -> getProjectUtil().loadOntologyModel(getProject()));
    }

    protected void setBuiltinOperators() {
        myFixture.copyFileToProject(new File("src/test/resources/builtinOperators.ts").getAbsolutePath(), "builtinOperators.ts");
    }

    protected void setBuiltinCommands() {
        myFixture.copyFileToProject(new File("src/test/resources/builtinCommands.ts").getAbsolutePath(), "builtinCommands.ts");
    }

    protected void setBuiltin() {
        setBuiltinCommands();
        setBuiltinOperators();
    }

    protected void setBuiltinAndModel() {
        setBuiltin();
        setOntologyModel();
    }

    private void startUtilMock() {
        if (utilManager == null || utilManager.isClosed()) {
            // get the original utils first to be returned as valid utils when they are not mocked
            // since the entire UtilManager is statically mocked we need to manually return the non-mocked utils

            ProjectUtil projectUtil = getProjectUtil();
            QueryUtil queryUtil = getQueryUtil();
            MemberUtil memberUtil = getMemberUtil();
            AnnotationUtil annotationUtil = getAnnotationUtil();
            ImportUtil importUtil = getImportUtil();
            ScriptUtil scriptUtil = getScriptUtil();
            ModelUtil modelUtil = getModelUtil();
            BuiltInUtil builtInUtil = getBuiltinUtil();
            TokenUtil tokenUtil = getTokenUtil();
            TokenFinderUtil tokenFinderUtil = getTokenFinderUtil();
            VariableUtil variableUtil = getVariableUtil();
            CurieUtil curieUtil = getCurieUtil();

            utilManager = Mockito.mockStatic(UtilManager.class);
            utilManager.when(UtilManager::getProjectUtil).thenReturn(projectUtil);
            utilManager.when(UtilManager::getQueryUtil).thenReturn(queryUtil);
            utilManager.when(UtilManager::getMemberUtil).thenReturn(memberUtil);
            utilManager.when(UtilManager::getAnnotationUtil).thenReturn(annotationUtil);
            utilManager.when(UtilManager::getImportUtil).thenReturn(importUtil);
            utilManager.when(UtilManager::getScriptUtil).thenReturn(scriptUtil);
            utilManager.when(UtilManager::getModelUtil).thenReturn(modelUtil);
            utilManager.when(UtilManager::getBuiltinUtil).thenReturn(builtInUtil);
            utilManager.when(UtilManager::getTokenUtil).thenReturn(tokenUtil);
            utilManager.when(UtilManager::getTokenFinderUtil).thenReturn(tokenFinderUtil);
            utilManager.when(UtilManager::getVariableUtil).thenReturn(variableUtil);
            utilManager.when(UtilManager::getCurieUtil).thenReturn(curieUtil);
        }
    }

    protected void setUtilMock(ProjectUtil projectUtil) {
        validateMock(projectUtil);
        utilManager.when(UtilManager::getProjectUtil).thenReturn(projectUtil);
    }

    protected void setUtilMock(ImportUtil importUtil) {
        validateMock(importUtil);
        utilManager.when(UtilManager::getImportUtil).thenReturn(importUtil);
    }

    protected void setUtilMock(MemberUtil memberUtil) {
        validateMock(memberUtil);
        utilManager.when(UtilManager::getMemberUtil).thenReturn(memberUtil);
    }

    protected void setUtilMock(CurieUtil curieUtil) {
        validateMock(curieUtil);
        utilManager.when(UtilManager::getCurieUtil).thenReturn(curieUtil);
    }

    protected void setUtilMock(RDFModelUtil rdfModelUtil) {
        validateMock(rdfModelUtil);
        utilManager.when(UtilManager::getRDFModelUtil).thenReturn(rdfModelUtil);
    }

    protected void setUtilMock(BuiltInUtil builtInUtil) {
        validateMock(builtInUtil);
        utilManager.when(UtilManager::getBuiltinUtil).thenReturn(builtInUtil);
    }

    protected void setUtilMock(ModelUtil modelUtil) {
        validateMock(modelUtil);
        utilManager.when(UtilManager::getModelUtil).thenReturn(modelUtil);
    }

    protected void setUtilMock(ScriptUtil scriptUtil) {
        validateMock(scriptUtil);
        utilManager.when(UtilManager::getScriptUtil).thenReturn(scriptUtil);
    }

    protected void setUtilMock(AnnotationUtil annotationUtil) {
        validateMock(annotationUtil);
        utilManager.when(UtilManager::getAnnotationUtil).thenReturn(annotationUtil);
    }

    private void validateMock(Object mockInstance) {
        assertNotNull("Mock has not been created yet", mockInstance);
        startUtilMock();
    }
}
