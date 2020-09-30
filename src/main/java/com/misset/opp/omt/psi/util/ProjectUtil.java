package com.misset.opp.omt.psi.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.misset.opp.omt.external.util.builtIn.BuiltInType;
import com.misset.opp.omt.external.util.builtIn.BuiltInUtil;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.support.OMTExportMember;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static util.Helper.getResources;

public class ProjectUtil {


    private static final HashMap<String, ArrayList<OMTPrefix>> knownPrefixes = new HashMap<>();
    private static final HashMap<String, ArrayList<OMTExportMember>> exportingMembers = new HashMap<>();
    private static final JsonObject parsedModel = new JsonObject();

    public static final ProjectUtil SINGLETON = new ProjectUtil();

    private WindowManager windowManager = WindowManager.getInstance();
    private FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
    private BuiltInUtil builtInUtil = BuiltInUtil.SINGLETON;

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public StatusBar getStatusBar(Project project) {
        return windowManager.getStatusBar(project);
    }
    public FileDocumentManager getFileDocumentManager() {
        return fileDocumentManager;
    }


    public List<VirtualFile> getVirtualFilesByName(Project project, String filename) {
        return
                new ArrayList<>(FilenameIndex.getVirtualFilesByName(project, filename, GlobalSearchScope.projectScope(project)));
    }

    /**
     * Tries to load all built-in commands and operators that can be retrieved from the BuiltInUtil
     */
    public void loadBuiltInMembers(Project project) {
        builtInUtil.reset();
        WindowManager.getInstance().getStatusBar(project).setInfo("Loading BuiltIn Members of OMT");

        loadBuiltInMembers(project, "builtinCommands.ts", BuiltInType.Command);
        loadBuiltInMembers(project, "builtinOperators.ts", BuiltInType.Operator);
        loadBuiltInMembers(project, "http-commands.ts", BuiltInType.HttpCommands);
        loadBuiltInMembers(project, "json-parse-command.ts", BuiltInType.ParseJsonCommand);
    }
    private void loadBuiltInMembers(Project project, String filename, BuiltInType type) {
        List<VirtualFile> virtualFiles = getVirtualFilesByName(project, filename);
        if (virtualFiles.size() == 1) {
            windowManager.getStatusBar(project).setInfo(
                    String.format("Discovered %s file, loading data", filename));
            VirtualFile virtualFile = virtualFiles.get(0);
            Document document = fileDocumentManager.getDocument(virtualFile);
            if (document != null) {
                builtInUtil.reloadBuiltInFromDocument(document, type, project);
                getStatusBar(project).setInfo(
                        String.format("Finished loading %s", filename)
                );
            } else {
                getStatusBar(project).setInfo(
                        String.format("Error loading %s", filename)
                );
            }

        } else {
            getStatusBar(project).setInfo(
                    String.format("Number of virtual files found for %s in project != 1, number found is = %s",
                            filename,
                            virtualFiles.size())
            );
        }
    }

    private void registerPrefixes(OMTFile file) {
        file.getPrefixes().forEach((namespacePrefix, omtPrefix) -> {
            namespacePrefix = namespacePrefix.endsWith(":") ? namespacePrefix.substring(0, namespacePrefix.length() - 1) : namespacePrefix;
            ArrayList<OMTPrefix> prefixes = knownPrefixes.getOrDefault(namespacePrefix, new ArrayList<>());
            prefixes.add(omtPrefix);

            knownPrefixes.put(namespacePrefix, prefixes);
        });
    }

    public List<OMTPrefix> getKnownPrefixes(String prefix) {
        return knownPrefixes.get(prefix);
    }

    private void registerExports(OMTFile file) {
        file.getExportedMembers().forEach(
                (key, omtExportMember) -> {
                    ArrayList<OMTExportMember> members = exportingMembers.getOrDefault(key, new ArrayList<>());
                    members.add(omtExportMember);
                    exportingMembers.put(key, members);
                }
        );
    }

    public List<OMTExportMember> getExportMember(String name) {
        return exportingMembers.get(name);
    }

    public void analyzeFile(OMTFile file) {
        registerExports(file);
        registerPrefixes(file);
    }

    /**
     * Load the model (attributes) from the json files
     */
    private void loadModelAttributes() {

        java.util.List<String> allModelFiles = Arrays.asList(
                "action.json", "activity.json", "binding.json", "component.json", "graphSelection.json", "model.json",
                "onChange.json", "ontology.json", "param.json", "payload.json", "procedure.json",
                "queryWatcher.json", "service.json", "standaloneQuery.json", "variable.json"
        );
        List<String> files = getResources(allModelFiles, "model");

        for (String content : files) {

            JsonElement jsonElement = JsonParser.parseString(content);
            if (jsonElement.isJsonArray()) {
                ((JsonArray) jsonElement).forEach(this::addToJsonModel);
            } else {
                addToJsonModel(jsonElement);
            }
        }
    }

    private void addToJsonModel(JsonElement jsonElement) {
        JsonObject asObject = (JsonObject) jsonElement;
        if (asObject.has("name")) {
            parsedModel.add(asObject.get("name").getAsString(), asObject);
        } else if (asObject.has("key")) {
            parsedModel.add(asObject.get("key").getAsString(), asObject);
        }
    }

    public JsonObject getParsedModel() {
        if (parsedModel.size() == 0) {
            loadModelAttributes();
        }
        return parsedModel;
    }
}
