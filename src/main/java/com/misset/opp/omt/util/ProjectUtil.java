package com.misset.opp.omt.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.support.BuiltInType;
import com.misset.opp.omt.psi.support.OMTCallable;
import com.misset.opp.omt.psi.support.OMTExportMember;
import com.misset.opp.omt.settings.OMTSettingsState;
import org.apache.jena.rdf.model.Model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.misset.opp.util.Helper.getResources;
import static com.misset.opp.util.UtilManager.getBuiltinUtil;
import static com.misset.opp.util.UtilManager.getTTLUtil;

public class ProjectUtil {
    public static final String BUILTIN_COMMANDS = "builtinCommands.ts";
    public static final String BUILTIN_OPERATORS = "builtinOperators.ts";
    public static final String BUILTIN_HTTP_COMMANDS = "http-commands.ts";
    public static final String BUILTIN_JSON_PARSE_COMMAND = "json-parse-command.ts";
    private static final String REASONS = "reasons";
    private static final String REASON = "reason";
    private static final String DESCRIPTION = "description";
    private final HashMap<String, ArrayList<OMTPrefix>> knownPrefixes = new HashMap<>();
    private final HashMap<String, ArrayList<OMTExportMember>> exportingMembers = new HashMap<>();
    private final JsonObject parsedModel = new JsonObject();
    private final HashMap<String, HashMap<String, String>> entriesOptions = new HashMap<>();

    private Model model;
    private WindowManager windowManager;
    private FileDocumentManager fileDocumentManager;
    private RDFModelUtil rdfModelUtil;

    public ProjectUtil() {
        if (ApplicationManager.getApplication() != null) {
            windowManager = WindowManager.getInstance();
            fileDocumentManager = FileDocumentManager.getInstance();
        }
    }

    public void setStatusbarMessage(Project project, String message) {
        final StatusBar statusBar = getStatusBar(project);
        if (statusBar == null) {
            return;
        }
        statusBar.setInfo(String.format("OMT PLUGIN: %s", message));
    }

    public RDFModelUtil getRDFModelUtil() {
        if (rdfModelUtil == null || !rdfModelUtil.isLoaded()) {
            rdfModelUtil = new RDFModelUtil(getOntologyModel());
        }
        return rdfModelUtil;
    }

    private void updateModelUtil() {
        if (rdfModelUtil != null) {
            rdfModelUtil.setModel(model);
        }
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public StatusBar getStatusBar(Project project) {
        return windowManager != null ? windowManager.getStatusBar(project) : null;
    }

    public FileDocumentManager getFileDocumentManager() {
        return fileDocumentManager;
    }

    public PsiFile[] getFilesByName(Project project, String name) {
        return FilenameIndex.getFilesByName(project, name, GlobalSearchScope.everythingScope(project));
    }

    public Model getOntologyModel() {
        return model;
    }

    public List<VirtualFile> getVirtualFilesByName(Project project, String filename) {
        return
                new ArrayList<>(FilenameIndex.getVirtualFilesByName(project, filename, GlobalSearchScope.projectScope(project)));
    }

    public Document getDocument(VirtualFile virtualFile) {
        return getFileDocumentManager().getDocument(virtualFile);
    }

    /**
     * Tries to load all built-in commands and operators that can be retrieved from the BuiltInUtil
     */
    public void loadBuiltInMembers(Project project) {
        getBuiltinUtil().reset();
        setStatusbarMessage(project, "Loading BuiltIn Members of OMT");

        OMTSettingsState settings = OMTSettingsState.getInstance();
        VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();

        loadBuiltInMembersViaSettingsOrFromFilename(project, BUILTIN_COMMANDS,
                BuiltInType.Command, settings.builtInCommandsPath, virtualFileManager, s -> settings.builtInCommandsPath = s);
        loadBuiltInMembersViaSettingsOrFromFilename(project, BUILTIN_OPERATORS,
                BuiltInType.Operator, settings.builtInOperatorsPath, virtualFileManager, s -> settings.builtInOperatorsPath = s);
        loadBuiltInMembersViaSettingsOrFromFilename(project, BUILTIN_HTTP_COMMANDS,
                BuiltInType.HttpCommands, settings.builtInHttpCommandsPath, virtualFileManager, s -> settings.builtInHttpCommandsPath = s);
        loadBuiltInMembersViaSettingsOrFromFilename(project, BUILTIN_JSON_PARSE_COMMAND,
                BuiltInType.ParseJsonCommand, settings.builtInParseJsonPath, virtualFileManager, s -> settings.builtInParseJsonPath = s);
    }

    public void loadOntologyModel(Project project, boolean resetOntologyPsiReferences) {
        setStatusbarMessage(project, "Loading ontology");
        VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
        OMTSettingsState settings = OMTSettingsState.getInstance();
        VirtualFile virtualFile = getVirtualFileFromSettingOrVFS(virtualFileManager, project, "root.ttl", settings.ontologyModelRootPath);
        if (virtualFile == null) {
            return;
        }
        settings.ontologyModelRootPath = virtualFile.getPath();
        String rootFolderPath = new File(virtualFile.getPath()).getParent();
        if (rootFolderPath.startsWith(File.separator)) {
            rootFolderPath = rootFolderPath.substring(1);
        }
        model = new RDFModelUtil(rootFolderPath).readModel(project);
        updateModelUtil();
        if (resetOntologyPsiReferences) {
            ApplicationManager.getApplication().invokeLater(() -> getTTLUtil().resetOntologyPsiReferences(project));
        }
        setStatusbarMessage(project, "Finished loading ontology");
    }

    private void loadBuiltInMembersViaSettingsOrFromFilename(Project project,
                                                             String filename,
                                                             BuiltInType type,
                                                             String settingsPath,
                                                             VirtualFileManager virtualFileManager,
                                                             Consumer<String> pathToVirtualFile) {

        VirtualFile virtualFile = getVirtualFileFromSettingOrVFS(virtualFileManager, project, filename, settingsPath);
        if (virtualFile == null) {
            return;
        }
        if (loadBuiltInMembersFile(virtualFile, project, filename, type)) {
            pathToVirtualFile.accept(virtualFile.getPath());
        }
    }

    private VirtualFile getVirtualFileFromSettingOrVFS(VirtualFileManager virtualFileManager, Project project, String filename, String settingsPath) {
        VirtualFile virtualFile = null;
        if (settingsPath != null && new File(settingsPath).exists()) {
            virtualFile = virtualFileManager.findFileByNioPath(new File(settingsPath).toPath());
        } else {
            List<VirtualFile> virtualFiles = getVirtualFilesByName(project, filename);
            if (!virtualFiles.isEmpty()) {
                virtualFile = virtualFiles.get(0);
            }
        }
        return virtualFile;
    }

    private boolean loadBuiltInMembersFile(VirtualFile virtualFile, Project project, String filename, BuiltInType type) {
        Document document = fileDocumentManager.getDocument(virtualFile);
        if (document != null) {
            getBuiltinUtil().reloadBuiltInFromDocument(document, type, project, this);
            setStatusbarMessage(project,
                    String.format("Finished loading %s", filename)
            );
            return true;
        } else {
            setStatusbarMessage(project,
                    String.format("Error loading %s", filename)
            );
            return false;
        }
    }

    private void registerPrefixes(OMTFile file) {
        file.getPrefixes().forEach((namespacePrefix, omtPrefix) -> Arrays.asList(omtPrefix.getNamespacePrefix().getName(), omtPrefix.getNamespaceIri().getName())
                .forEach(key -> {
                    // register the prefix by namespace and prefix
                    ArrayList<OMTPrefix> prefixes = knownPrefixes.getOrDefault(key, new ArrayList<>());
                    prefixes.add(omtPrefix);
                    knownPrefixes.put(key, prefixes);
                }));
    }

    public List<OMTPrefix> getKnownPrefixes(String prefix) {
        String prefixName = prefix.trim();
        if (prefixName.endsWith(":")) {
            prefixName = prefixName.substring(0, prefixName.length() - 1);
        }
        return knownPrefixes.getOrDefault(prefixName, new ArrayList<>())
                .stream().filter(distinctByKey(OMTPrefix::getName)).collect(Collectors.toList());
    }

    private <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
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

    private void removeExports(OMTFile file) {
        exportingMembers.values().forEach(omtExportMembers -> {
            Optional<OMTExportMember> exportingMember = omtExportMembers.stream().filter(omtExportMember -> omtExportMember.getResolvingElement().getContainingFile() == file).findFirst();
            exportingMember.ifPresent(omtExportMembers::remove);
        });
    }

    public List<OMTExportMember> getExportMember(String name) {
        return exportingMembers.getOrDefault(name, new ArrayList<>());
    }

    public List<String> getExportedMembersAsSuggestions(boolean commands) {
        return getExportedMembers(commands).stream().map(OMTCallable::getAsSuggestion).distinct().collect(Collectors.toList());
    }

    public List<OMTExportMember> getExportedMembers(boolean commands) {
        List<OMTExportMember> exportedCommands = new ArrayList<>();
        exportingMembers.values().forEach(omtExportMembers ->
                omtExportMembers.stream()
                        .filter(omtExportMember -> (commands && omtExportMember.isCommand()) || (!commands && omtExportMember.isOperator()))
                        .forEach(exportedCommands::add)
        );
        return exportedCommands;
    }

    /**
     * Resets the index with exported members based on changes made in this file
     */
    public void resetExportedMembers(OMTFile file) {
        setStatusbarMessage(file.getProject(),
                String.format("Resetting exporting members for %s", file.getName())
        );
        removeExports(file);
        registerExports(file);
    }

    public void analyzeFile(OMTFile file) {
        registerExports(file);
        registerPrefixes(file);
        setStatusbarMessage(file.getProject(),
                String.format("Finished analyzing %s", file.getName())
        );
    }

    /**
     * Load the model (attributes) from the json files
     */
    private void loadModelAttributes() {

        java.util.List<String> allModelFiles = Arrays.asList(
                "action.json", "activity.json", "binding.json", "component.json", "declare.json", "graphSelection.json",
                "module.json", "onChange.json", "ontology.json", "param.json", "payload.json", "procedure.json",
                "root.json", "rules.json", "queryWatcher.json", "service.json", "standaloneQuery.json", "variable.json"
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

    public void loadReasons(Project project) {
        FileTypeIndex
                .getFiles(JsonFileType.INSTANCE, GlobalSearchScope.projectScope(project))
                .stream()
                .filter(virtualFile -> virtualFile.getPath().contains("/reasons/"))
                .map(virtualFile -> PsiManager.getInstance(project).findFile(virtualFile))
                .filter(Objects::nonNull)
                .map(PsiElement::getText)
                .map(JsonParser::parseString)
                .filter(jsonElement -> jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has(REASONS))
                .map(JsonElement::getAsJsonObject)
                .forEach(jsonObject -> {
                    final JsonArray reasons = jsonObject.getAsJsonArray(REASONS);
                    reasons.forEach(
                            jsonElement -> {
                                if (jsonElement.isJsonObject()) {
                                    final JsonObject elementAsJsonObject = jsonElement.getAsJsonObject();
                                    if (elementAsJsonObject.has(REASON) && elementAsJsonObject.has(DESCRIPTION)) {
                                        final HashMap<String, String> entryOptionsReasons = entriesOptions.getOrDefault(REASONS, new HashMap<>());
                                        entryOptionsReasons.put(
                                                elementAsJsonObject.get(REASON).getAsString(),
                                                elementAsJsonObject.get(DESCRIPTION).getAsString()
                                        );
                                        entriesOptions.put(REASONS, entryOptionsReasons);
                                    }
                                }
                            }
                    );
                });
    }

    public HashMap<String, String> getReasons() {
        return entriesOptions.getOrDefault(REASONS, new HashMap<>());
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
