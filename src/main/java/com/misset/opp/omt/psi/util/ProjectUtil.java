package com.misset.opp.omt.psi.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.misset.opp.omt.external.util.builtIn.BuiltInType;
import com.misset.opp.omt.external.util.builtIn.BuiltInUtil;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.support.OMTExportMember;

import java.io.IOException;
import java.util.*;

import static util.Helper.getResources;

public class ProjectUtil {


    private static final HashMap<String, ArrayList<OMTPrefix>> knownPrefixes = new HashMap<>();
    private static final HashMap<String, ArrayList<OMTExportMember>> exportingMembers = new HashMap<>();
    private static final JsonObject parsedModel = new JsonObject();

    /**
     * Tries to load all built-in commands and operators that can be retrieved from the BuiltInUtil
     */
    public static void loadBuiltInMembers(Project project) {
        BuiltInUtil.reset();
        WindowManager.getInstance().getStatusBar(project).setInfo("Loading BuiltIn Members of OMT");
        Collection<VirtualFile> builtInCommandsCollection = FilenameIndex.getVirtualFilesByName(project, "builtinCommands.ts", GlobalSearchScope.allScope(project));
        if (builtInCommandsCollection.size() == 1) {
            WindowManager.getInstance().getStatusBar(project).setInfo("Discover builtinCommands.ts file, loading data");
            VirtualFile virtualFile = builtInCommandsCollection.iterator().next();
            Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
            if (document != null) {
                BuiltInUtil.reloadBuiltInFromDocument(document, BuiltInType.Command);
            }
        }

        Collection<VirtualFile> builtInOperatorsCollection = FilenameIndex.getVirtualFilesByName(project, "builtinOperators.ts", GlobalSearchScope.allScope(project));
        if (builtInOperatorsCollection.size() == 1) {
            WindowManager.getInstance().getStatusBar(project).setInfo("Discover builtinOperators.ts file, loading data");
            VirtualFile virtualFile = builtInOperatorsCollection.iterator().next();
            Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
            if (document != null) {
                BuiltInUtil.reloadBuiltInFromDocument(document, BuiltInType.Operator);
            }
        }

    }
    public static void registerPrefixes(OMTFile file) {
        file.getPrefixes().forEach((namespacePrefix, omtPrefix) -> {
            ArrayList<OMTPrefix> prefixes = knownPrefixes.getOrDefault(namespacePrefix, new ArrayList<>());
            prefixes.add(omtPrefix);
            knownPrefixes.put(namespacePrefix, prefixes);
        });
    }

    public static void registerExports(OMTFile file) {
        file.getExportedMembers().forEach(
                (key, omtExportMember) -> {
                    ArrayList<OMTExportMember> members = exportingMembers.getOrDefault(key, new ArrayList<>());
                    members.add(omtExportMember);
                    exportingMembers.put(key, members);
                }
        );
    }

    public static void analyzeFile(OMTFile file) {
        try {
            System.out.print("Analyzing file: " + file.getVirtualFile().getPath());
            registerExports(file);
            registerPrefixes(file);
            System.out.println(" succes");
        } catch (Exception e) {
            System.out.println("Error when analyzing file: " + e.getMessage());
        }

    }

    /**
     * Load the model (attributes) from the json files
     */
    public static void loadModelAttributes() throws IOException {

        java.util.List<String> allModelFiles = Arrays.asList(
                "action.json", "activity.json", "binding.json", "component.json", "graphSelection.json", "model.json",
                "onChange.json", "ontology.json", "param.json", "payload.json", "procedure.json",
                "queryWatcher.json", "service.json", "standaloneQuery.json", "variable.json"
        );
        List<String> files = getResources(allModelFiles, "model");

        for (String content : files) {
            JsonElement jsonElement = new JsonParser().parse(content);
            if (jsonElement.isJsonArray()) {
                ((JsonArray) jsonElement).forEach(ProjectUtil::addToJsonModel);
            } else {
                addToJsonModel(jsonElement);
            }
        }
    }

    private static void addToJsonModel(JsonElement jsonElement) {
        JsonObject asObject = (JsonObject) jsonElement;
        if (asObject.has("name")) {
            parsedModel.add(asObject.get("name").getAsString(), asObject);
        } else if (asObject.has("key")) {
            parsedModel.add(asObject.get("key").getAsString(), asObject);
        }
    }

    public static JsonObject getParsedModel() {
        return parsedModel;
    }
}
