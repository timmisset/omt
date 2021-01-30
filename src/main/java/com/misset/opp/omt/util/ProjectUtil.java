package com.misset.opp.omt.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.named.OMTCurie;
import com.misset.opp.omt.psi.references.TTLReferenceElement;
import com.misset.opp.omt.psi.support.BuiltInType;
import com.misset.opp.omt.psi.support.OMTCallable;
import com.misset.opp.omt.psi.support.OMTExportMember;
import com.misset.opp.omt.settings.OMTSettingsState;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import tech.lnkd.editor.intellij.turtle.TurtleFile;
import tech.lnkd.editor.intellij.turtle.psi.TurtleObject;
import tech.lnkd.editor.intellij.turtle.psi.TurtleSubject;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.references.TTLReferenceElement.getSubjectClassIri;
import static com.misset.opp.omt.psi.util.UtilManager.getBuiltinUtil;
import static util.Helper.getResources;

public class ProjectUtil {
    private final HashMap<String, ArrayList<OMTPrefix>> knownPrefixes = new HashMap<>();
    private final HashMap<String, ArrayList<OMTExportMember>> exportingMembers = new HashMap<>();
    private final HashMap<String, ArrayList<FakePsiElement>> ttlSubjectReferences = new HashMap<>();
    private final HashMap<String, HashMap<String, ArrayList<FakePsiElement>>> ttlPredicateReferences = new HashMap<>();
    private final JsonObject parsedModel = new JsonObject();

    public static final String BUILTIN_COMMANDS = "builtinCommands.ts";
    public static final String BUILTIN_OPERATORS = "builtinOperators.ts";
    public static final String BUILTIN_HTTP_COMMANDS = "http-commands.ts";
    public static final String BUILTIN_JSON_PARSE_COMMAND = "json-parse-command.ts";

    private Model model;
    private WindowManager windowManager;
    private FileDocumentManager fileDocumentManager;

    public ProjectUtil() {
        if (ApplicationManager.getApplication() != null) {
            windowManager = WindowManager.getInstance();
            fileDocumentManager = FileDocumentManager.getInstance();
        }
    }

    private RDFModelUtil rdfModelUtil;

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
        model = new RDFModelUtil(rootFolderPath).readModel();
        updateModelUtil();
        if (resetOntologyPsiReferences) {
            ApplicationManager.getApplication().invokeLater(() -> resetOntologyPsiReferences(project));
        }
        setStatusbarMessage(project, "Finished loading ontology");
    }

    /**
     * Make references to the TTL ontology files in order to resolve them from OMTCurieElements
     * Since the TTL files are structured by the LNKD.tech plugin this needs to be installed and will be used
     */
    private void resetOntologyPsiReferences(Project project) {
        ttlSubjectReferences.clear();
        ttlPredicateReferences.clear();
        final Collection<VirtualFile> ttlFiles = FilenameIndex.getAllFilesByExt(project, "ttl");
        ttlFiles.stream()
                .filter(virtualFile -> !virtualFile.getPath().contains("target"))
                .map(
                        virtualFile -> {
                            PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
                            return file instanceof TurtleFile ? (TurtleFile) file : null;
                        })
                .filter(Objects::nonNull)
                .forEach(turtleFile -> {
                    PsiTreeUtil.findChildrenOfType(turtleFile, TurtleSubject.class).stream()
                            .filter(turtleSubject ->
                                    turtleSubject.getRef() != null &&
                                            turtleSubject.getRef().iri() != null)
                            .forEach(turtleSubject -> {
                                String iri = turtleSubject.getRef().iri().toString();
                                ArrayList<FakePsiElement> fakePsiElements = ttlSubjectReferences.getOrDefault(iri, new ArrayList<>());
                                fakePsiElements.add(new TTLReferenceElement(turtleSubject));
                                ttlSubjectReferences.put(iri, fakePsiElements);
                            });

                    PsiTreeUtil.findChildrenOfType(turtleFile, TurtleObject.class).stream()
                            .filter(turtleObject ->
                                    turtleObject.getRef() != null &&
                                            Objects.requireNonNull(turtleObject.getRef().decompiledValue()).iri() != null)
                            .forEach(turtleObject -> {
                                final String iri = Objects.requireNonNull(turtleObject.getRef().decompiledValue()).iri();
                                final String subjectClassIri = getSubjectClassIri(turtleObject, false);
                                final HashMap<String, ArrayList<FakePsiElement>> predicateReferencesBySubject = ttlPredicateReferences.getOrDefault(iri, new HashMap<>());
                                ArrayList<FakePsiElement> fakePsiElements = predicateReferencesBySubject.getOrDefault(subjectClassIri, new ArrayList<>());
                                fakePsiElements.add(new TTLReferenceElement(turtleObject));
                                predicateReferencesBySubject.put(subjectClassIri, fakePsiElements);
                                ttlPredicateReferences.put(iri, predicateReferencesBySubject);
                            });
                });
    }

    public List<FakePsiElement> getTTLReference(OMTCurie curie, List<Resource> subjectFilter) {
        if (getOntologyModel() == null) {
            return new ArrayList<>();
        } // not loaded
        String iri = curie.getAsResource().getURI();
        return ttlSubjectReferences.getOrDefault(iri,
                ttlPredicateReferences.getOrDefault(iri, new HashMap<>())
                        .keySet()
                        .stream()
                        .filter(subjectIri -> subjectFilter == null || subjectFilter.isEmpty() ||
                                subjectFilter.contains(getOntologyModel().createResource(subjectIri)))
                        .map(subjectIri -> ttlPredicateReferences.get(iri).get(subjectIri))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toCollection(ArrayList::new)));
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
                "rules.json", "queryWatcher.json", "service.json", "standaloneQuery.json", "variable.json"
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
