package com.misset.opp.omt.psi.util;

import com.google.gson.*;
import com.intellij.json.psi.JsonLiteral;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTDefineQueryStatement;
import com.misset.opp.omt.psi.OMTOperator;
import com.misset.opp.omt.psi.OMTOperatorCall;
import com.misset.opp.omt.psi.OMTParameter;
import org.apache.maven.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;

import java.io.File;
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

    private static List<OMTOperator> builtInOperators;
    public static void loadBuiltInOperators(Project project) {
        String builtInOperatorsPath = String.format("%s/core/operators/builtInOperators.ts", project.getBasePath());
        VirtualFile fileByIoFile = LocalFileSystem.getInstance().findFileByIoFile(new File(builtInOperatorsPath));
        Document document = FileDocumentManager.getInstance().getDocument(fileByIoFile);

        document.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                reloadBuiltInOperators(event.getDocument());
            }
        });
        reloadBuiltInOperators(document);
    }
    private static void reloadBuiltInOperators(Document document) {
        builtInOperators = new ArrayList<>();
        String text = document.getText();

        JsonParser parser = new JsonParser();
        JsonObject operators = (JsonObject) parser.parse(text);

        operators.keySet().forEach(operatorName -> {
            JsonObject operator = (JsonObject) operators.get(operatorName);
            JsonElement params = operator.get("params");
            List<OMTParameter> literals = new ArrayList<>();
            if(params != null) {
                JsonArray parameters = (JsonArray) params;
                parameters.forEach(jsonElement -> literals.add(new OMTParameter((JsonPrimitive) jsonElement)));
            }
            builtInOperators.add(new OMTOperator(operatorName, literals));
        });
    }
    public static List<OMTOperator> getBuiltInOperators(Project project) {
        if(builtInOperators == null || builtInOperators.isEmpty()) {
            loadBuiltInOperators(project);
        }
        return builtInOperators;
    }

    public static List<OMTOperator> getAllAvailableOperators(PsiElement element) {
        List<OMTOperator> operators = new ArrayList<>();

        // Operators can be build in operators, imported, defined queries in the document or standalone queries
        // TODO: Get builtin operators
        operators.addAll(getBuiltInOperators(element.getProject()));

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
