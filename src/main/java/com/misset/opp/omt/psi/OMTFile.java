package com.misset.opp.omt.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.OMTFileType;
import com.misset.opp.omt.OMTLanguage;
import com.misset.opp.omt.psi.impl.OMTExportMemberImpl;
import com.misset.opp.omt.psi.support.ExportMemberType;
import com.misset.opp.omt.psi.support.OMTExportMember;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.misset.opp.util.UtilManager.getImportUtil;
import static com.misset.opp.util.UtilManager.getModelUtil;
import static com.misset.opp.util.UtilManager.getProjectUtil;

public class OMTFile extends PsiFileBase {
    public static final String IMPORT = "import";
    public static final String QUERIES = "queries";
    public static final String COMMANDS = "commands";
    public static final String MODEL = "model";
    public static final String PREFIXES = "prefixes";
    public static final String EXPORT = "export";
    public static final String MODULE = "module";
    private String currentContent = "";
    private HashMap<String, OMTExportMember> exportMembers = new HashMap<>();

    public OMTFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, OMTLanguage.INSTANCE);
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        String activeContent = getText();
        if (!currentContent.equals(activeContent)) {
            updateMembers();
            currentContent = activeContent;
        }
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return OMTFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "OMT File";
    }

    public OMTBlock getRoot() {
        return PsiTreeUtil.getChildOfType(this, OMTBlock.class);
    }

    public String resourcesAsTypes(List<Resource> resources) {
        return resources.stream()
                .map(this::resourceToCurie)
                .collect(Collectors.joining(", "));
    }

    public Optional<OMTBlockEntry> getRootBlock(String name) {
        OMTBlock rootBlock = PsiTreeUtil.getChildOfType(this, OMTBlock.class);
        if (rootBlock == null) {
            return Optional.empty();
        }
        List<OMTBlockEntry> blockEntryList = rootBlock.getBlockEntryList();
        return blockEntryList.stream()
                .filter(blockEntry -> getModelUtil().getEntryBlockLabel(blockEntry).startsWith(name))
                .filter(Objects::nonNull)
                .findFirst();
    }

    public boolean isPartOfRootBlock(PsiElement element) {
        OMTBlockEntry blockEntry = PsiTreeUtil.getParentOfType(element, OMTBlockEntry.class);
        OMTBlock rootBlock = PsiTreeUtil.getChildOfType(this, OMTBlock.class);
        if (rootBlock == null) {
            return false;
        }
        return rootBlock.getBlockEntryList().contains(blockEntry);
    }

    public <T> Optional<T> getSpecificBlock(String name, Class<T> specificBlockClass) {
        Optional<OMTBlockEntry> blockEntry = getRootBlock(name);
        if (blockEntry.isPresent() && specificBlockClass.isAssignableFrom(blockEntry.get().getClass())) {
            return Optional.of(specificBlockClass.cast(blockEntry.get()));
        }
        return Optional.empty();
    }

    public List<OMTMember> getImportedMembers() {
        List<OMTMember> importedList = new ArrayList<>();
        Optional<OMTImportBlock> optionalOMTImportBlock = getSpecificBlock(IMPORT, OMTImportBlock.class);
        optionalOMTImportBlock.ifPresent(omtImportBlock ->
                omtImportBlock.getImportList()
                        .stream()
                        .map(OMTImport::getMemberList)
                        .filter(Objects::nonNull)
                        .forEach(omtMemberList -> importedList.addAll(
                                omtMemberList.getMemberListItemList()
                                        .stream()
                                        .map(OMTMemberListItem::getMember)
                                        .collect(Collectors.toList())
                        ))
        );
        return importedList;
    }

    public boolean isModuleFile() {
        return getRootBlock(MODULE).isPresent();
    }

    public Map<OMTImport, VirtualFile> getImportedFiles() {
        Optional<OMTImportBlock> importBlock = getSpecificBlock(IMPORT, OMTImportBlock.class);
        if (importBlock.isEmpty()) {
            return new HashMap<>();
        }

        HashMap<OMTImport, VirtualFile> importHashmap = new HashMap<>();
        importBlock.get().getImportList()
                .stream().filter(omtImport -> omtImport.getContainingFile().getVirtualFile() != null)
                .forEach(omtImport -> importHashmap.put(omtImport, getImportUtil().getImportedFile(omtImport)));
        return importHashmap;
    }

    public Map<String, OMTExportMember> getImportedMembersAsExportedMembers() {
        HashMap<String, OMTExportMember> importedMembers = new HashMap<>();
        getImportedFiles().forEach(
                (omtImport, virtualFile) -> {
                    if (virtualFile != null && omtImport.getMemberList() != null) {
                        omtImport.getMemberList().getMemberListItemList().forEach(
                                omtMemberListItem -> {
                                    final OMTFile omtFile = (OMTFile) PsiManager.getInstance(getProject()).findFile(virtualFile);
                                    if (omtFile != null) {
                                        omtFile.getExportedMember(omtMemberListItem.getName())
                                                .ifPresent(exportMember -> importedMembers.put(exportMember.getName(), exportMember));
                                    }
                                }
                        );
                    }
                }
        );
        return importedMembers;
    }

    /**
     * Root queries and commands are available as well as OMT model items: Activity, Procedure and StandAloneQuery
     * They are available as OMTExportMembers which can resolve to the PsiElement that defines them but have additional
     * resolve functions such as the type, number of expected parameters etc
     */
    public HashMap<String, OMTExportMember> getExportedMembers() {
        initExportMembers();
        return exportMembers;
    }

    public Optional<OMTExportMember> getExportedMember(String name) {
        name = name.trim();
        initExportMembers();
        return exportMembers.containsKey(name) ? Optional.of(exportMembers.get(name)) : Optional.empty();
    }

    public HashMap<String, OMTPrefix> getPrefixes() {
        HashMap<String, OMTPrefix> prefixHashMap = new HashMap<>();
        Optional<OMTPrefixBlock> prefixes = getSpecificBlock(PREFIXES, OMTPrefixBlock.class);
        prefixes.ifPresent(omtPrefixBlock -> omtPrefixBlock.getPrefixList().forEach(omtPrefix ->
        {
            prefixHashMap.put(omtPrefix.getNamespacePrefix().getName(), omtPrefix);
            prefixHashMap.put(omtPrefix.getNamespaceIri().getName(), omtPrefix);
        }));
        return prefixHashMap;
    }

    public String getPrefixIri(String prefix) {
        OMTPrefix omtPrefix = getPrefixes().get(prefix);
        if (omtPrefix == null) {
            return "";
        }
        String iri = omtPrefix.getNamespaceIri().getText();
        iri = iri.startsWith("<") ? iri.substring(1) : iri;
        iri = iri.endsWith(">") ? iri.substring(0, iri.length() - 1) : iri;
        return iri;
    }

    public String resourceToCurie(Resource resource) {
        if (resource == null) {
            return "";
        }
        OMTPrefix prefix = getPrefixes().get(resource.getNameSpace());
        if (prefix == null) {
            return resource.toString();
        }

        return String.format("%s:%s", prefix.getNamespacePrefix().getName(), resource.getLocalName());
    }

    public String curieToIri(String curie) {
        final String[] curieContent = curie.split(":");
        return String.format("%s%s", getPrefixIri(curieContent[0]), curieContent[1]);
    }

    public void updateMembers() {
        updateExportMembers();
        getProjectUtil().resetExportedMembers(this);
    }

    public Map<String, OMTModelItemBlock> getDeclaredOntologies() {
        Optional<OMTModelBlock> model = getSpecificBlock(MODEL, OMTModelBlock.class);
        HashMap<String, OMTModelItemBlock> ontologies = new HashMap<>();

        model.ifPresent(omtModelBlock -> omtModelBlock.getModelItemBlockList()
                .forEach(omtModelItemBlock -> {
                    if (omtModelItemBlock.getType().equalsIgnoreCase("ontology")) {
                        String name = omtModelItemBlock.getName();
                        name = name != null && name.endsWith(":") ? name.substring(0, name.length() - 1) : name;
                        ontologies.put(name, omtModelItemBlock);
                    }
                })
        );
        return ontologies;
    }

    private void initExportMembers() {
        if (exportMembers.isEmpty()) {
            updateExportMembers();
        }
    }

    public void setRootBlock(OMTPrefixBlock prefixBlock) {
        if (prefixBlock == null) {
            return;
        }
        setRootBlock(prefixBlock, PREFIXES);
    }

    public void setRootBlock(OMTImportBlock importBlock) {
        if (importBlock == null) {
            return;
        }
        setRootBlock(importBlock, IMPORT);
    }

    private void setRootBlock(OMTBlockEntry blockEntry, String rootLabel) {
        Optional<OMTBlockEntry> rootBlock = getRootBlock(rootLabel);
        if (rootBlock.isPresent()) {
            rootBlock.get().replace(blockEntry);
        } else {
            // always add the imports to the top of the page
            // add the parent (block entry) to the root block
            getRoot().addBefore(blockEntry, getBeforeAnchor(rootLabel));
        }
    }

    /**
     * Checks if this file already has an import for this exporting member
     */
    public boolean hasImportFor(OMTExportMember exportMember) {
        Optional<OMTBlockEntry> imports = getRootBlock(IMPORT);
        if (imports.isEmpty()) {
            return false;
        }
        Collection<OMTMember> importingMembers = PsiTreeUtil.findChildrenOfType(imports.get(), OMTMember.class);
        for (OMTMember member : importingMembers) {
            PsiElement resolved = member.getReference() != null ? member.getReference().resolve() : null;
            if (resolved != null && resolved.equals(exportMember.getResolvingElement())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasImport(String importPath) {
        return PsiTreeUtil.findChildrenOfType(this, OMTImport.class).stream().anyMatch(
                omtImport -> omtImport.getImportSource().getImportLocation().textMatches(importPath)
        );
    }

    public void quickFormat() {
        removeDuplicateEmptyLines();
    }

    private void removeDuplicateEmptyLines() {
        String replace = getRoot().getText().replaceAll("(?:\\h*\\n){2,}", "\n\n");
        if (!replace.equals(getRoot().getText())) {
            PsiElement psiElement = OMTElementFactory.fromString(replace, OMTBlock.class, this.getProject());
            getRoot().replace(((OMTFile) psiElement.getContainingFile()).getRoot());
        }
    }

    private PsiElement getBeforeAnchor(String rootLabel) {
        switch (rootLabel) {
            case PREFIXES:
                // prefixes is either after import or at the very top
                return getRootBlock(IMPORT).orElse(this.getRoot().getBlockEntryList().get(0));

            // import or undefined can be added at the top
            default:
            case IMPORT:
                return this.getRoot().getFirstChild();
        }
    }

    private void updateExportMembers() {
        HashMap<String, OMTExportMember> exported = new HashMap<>();
        // commands
        Optional<OMTCommandsBlock> commands = getSpecificBlock(COMMANDS, OMTCommandsBlock.class);
        commands.ifPresent(omtCommandsBlock -> omtCommandsBlock.getDefineCommandStatementList().stream()
                .map(omtDefineCommandStatement -> new OMTExportMemberImpl(omtDefineCommandStatement, ExportMemberType.Command))
                .forEach(omtExportMember -> exported.put(omtExportMember.getName(), omtExportMember))
        );

        // queries
        Optional<OMTQueriesBlock> queries = getSpecificBlock(QUERIES, OMTQueriesBlock.class);
        queries.ifPresent(omtQueriesBlock -> omtQueriesBlock.getDefineQueryStatementList().stream()
                .map(omtDefineQueryStatement -> new OMTExportMemberImpl(omtDefineQueryStatement, ExportMemberType.Query))
                .forEach(omtExportMember -> exported.put(omtExportMember.getName(), omtExportMember))
        );

        // modelItems
        Optional<OMTModelBlock> model = getSpecificBlock(MODEL, OMTModelBlock.class);
        model.ifPresent(omtModelBlock -> omtModelBlock.getModelItemBlockList().stream()
                .map(omtModelItemBlock -> {
                    String modelItemType = omtModelItemBlock.getType();
                    switch (modelItemType.toUpperCase()) {
                        case "ACTIVITY":
                            return new OMTExportMemberImpl(omtModelItemBlock, ExportMemberType.Activity);
                        case "PROCEDURE":
                            return new OMTExportMemberImpl(omtModelItemBlock, ExportMemberType.Procedure);
                        case "STANDALONEQUERY":
                            return new OMTExportMemberImpl(omtModelItemBlock, ExportMemberType.StandaloneQuery);
                        case "COMPONENT":
                            return new OMTExportMemberImpl(omtModelItemBlock, ExportMemberType.Component);
                        default:
                            return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(omtExportMember -> exported.put(omtExportMember.getName(), omtExportMember))
        );

        if (isModuleFile()) {
            // modules must specify exporting members explicitely via the export: block
            Optional<OMTExportBlock> optionalOMTExportBlock = getSpecificBlock(EXPORT, OMTExportBlock.class);
            optionalOMTExportBlock.ifPresent(
                    omtExportBlock ->
                    {
                        final Map<String, OMTExportMember> importedMembersAsExportedMembers = getImportedMembersAsExportedMembers();
                        if (omtExportBlock.getMemberList() != null) {
                            omtExportBlock.getMemberList().getMemberListItemList()
                                    .stream().map(OMTMemberListItem::getMember)
                                    .filter(member -> member != null && importedMembersAsExportedMembers.containsKey(member.getName()))
                                    .forEach(
                                            member -> exported.put(member.getName(), importedMembersAsExportedMembers.get(member.getName()))
                                    );
                        }
                    }
            );
        } else {
            // other omt files export anything that they import
            exported.putAll(getImportedMembersAsExportedMembers());
        }

        exportMembers = exported;
    }
}
