package com.misset.opp.omt.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.OMTFileType;
import com.misset.opp.omt.OMTLanguage;
import com.misset.opp.omt.psi.impl.OMTExportMemberImpl;
import com.misset.opp.omt.psi.support.ExportMemberType;
import com.misset.opp.omt.psi.support.OMTExportMember;
import com.misset.opp.omt.psi.util.ImportUtil;
import com.misset.opp.omt.psi.util.ModelUtil;
import com.misset.opp.omt.psi.util.ProjectUtil;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class OMTFile extends PsiFileBase {
    public OMTFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, OMTLanguage.INSTANCE);
    }

    private static final ModelUtil modelUtil = ModelUtil.SINGLETON;
    private static final ImportUtil importUtil = ImportUtil.SINGLETON;

    private String currentContent = "";

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

    public Optional<OMTBlockEntry> getRootBlock(String name) {
        OMTBlock rootBlock = PsiTreeUtil.getChildOfType(this, OMTBlock.class);
        if (rootBlock == null) {
            return Optional.empty();
        }
        List<OMTBlockEntry> blockEntryList = rootBlock.getBlockEntryList();
        return blockEntryList.stream()
                .filter(blockEntry -> modelUtil.getEntryBlockLabel(blockEntry).startsWith(name))
                .findFirst();
    }

    public boolean isPartOfRootBlock(PsiElement element) {
        PsiElement blockEntry = PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTBlockEntry);
        OMTBlock rootBlock = PsiTreeUtil.getChildOfType(this, OMTBlock.class);
        return rootBlock.getBlockEntryList().contains(blockEntry);
    }


    public <T> Optional<T> getSpecificBlock(String name, Class<T> specificBlockClass) {
        Optional<OMTBlockEntry> blockEntry = getRootBlock(name);
        if (!blockEntry.isPresent()) {
            return Optional.empty();
        } else {
            PsiElement firstChild = blockEntry.get().getSpecificBlock().getFirstChild();
            if (specificBlockClass.isAssignableFrom(firstChild.getClass())) {
                return Optional.of(specificBlockClass.cast(firstChild));
            }
            return Optional.empty();
        }

    }

    private HashMap<String, OMTExportMember> exportMembers = new HashMap<>();

    public List<OMTMember> getImportedMembers() {
        List<OMTMember> importedList = new ArrayList<>();
        Optional<OMTImportBlock> optionalOMTImportBlock = getSpecificBlock("import", OMTImportBlock.class);
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
        return getRootBlock("module").isPresent();
    }

    public HashMap<OMTImport, VirtualFile> getImportedFiles() {
        Optional<OMTImportBlock> importBlock = getSpecificBlock("import", OMTImportBlock.class);
        if (!importBlock.isPresent()) {
            return new HashMap<>();
        }

        HashMap<OMTImport, VirtualFile> importHashmap = new HashMap<>();
        importBlock.get().getImportList()
                .forEach(omtImport -> importHashmap.put(omtImport, importUtil.getImportedFile(omtImport)));
        return importHashmap;
    }

    /**
     * Root queries and commands are available as well as OMT model items: Activity, Procedure and StandAloneQuery
     * They are available as OMTExportMembers which can resolve to the PsiElement that defines them but have additional
     * resolve functions such as the type, number of expected parameters etc
     *
     * @return
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
        Optional<OMTPrefixBlock> prefixes = getSpecificBlock("prefixes", OMTPrefixBlock.class);
        prefixes.ifPresent(omtPrefixBlock -> omtPrefixBlock.getPrefixList().forEach(omtPrefix ->
        {
            prefixHashMap.put(omtPrefix.getNamespacePrefix().getName(), omtPrefix);
            prefixHashMap.put(omtPrefix.getNamespaceIri().getNamespace(), omtPrefix);
        }));
        return prefixHashMap;
    }

    public String getPrefixIri(String prefix) {
        OMTPrefix omtPrefix = getPrefixes().get(prefix);
        if (omtPrefix == null) {
            return "";
        }
        String iri = omtPrefix.getNamespaceIri().getStart().getNextSibling().getText();
        iri = iri.startsWith("<") ? iri.substring(1) : iri;
        iri = iri.endsWith(">") ? iri.substring(0, iri.length() - 1) : iri;
        return iri;
    }

    public String resourceToCurie(Resource resource) {
        OMTPrefix prefix = getPrefixes().get(resource.getNameSpace());
        if (prefix == null) {
            return resource.toString();
        }

        return String.format("%s:%s", prefix.getNamespacePrefix().getName(), resource.getLocalName());
    }

    public void updateMembers() {
        updateExportMembers();
        ProjectUtil.SINGLETON.resetExportedMembers(this);
    }

    public HashMap<String, OMTModelItemBlock> getDeclaredOntologies() {
        Optional<OMTModelBlock> model = getSpecificBlock("model", OMTModelBlock.class);
        HashMap<String, OMTModelItemBlock> ontologies = new HashMap<>();
        model.ifPresent(omtModelBlock -> omtModelBlock.getModelItemBlockList()
                .forEach(omtModelItemBlock -> {
                    String modelItemType = omtModelItemBlock.getModelItemLabel().getModelItemTypeElement().getText();
                    if (modelItemType.equalsIgnoreCase("!ontology")) {
                        String name = omtModelItemBlock.getModelItemLabel().getName();
                        name = name.endsWith(":") ? name.substring(0, name.length() - 1) : name;
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
        OMTBlockEntry prefixBlockAsEntry = (OMTBlockEntry) PsiTreeUtil.findFirstParent(prefixBlock, parent -> parent instanceof OMTBlockEntry);
        setRootBlock(prefixBlockAsEntry, "prefixes");
    }

    public void setRootBlock(OMTImportBlock importBlock) {
        if (importBlock == null) {
            return;
        }
        OMTBlockEntry importBlockAsEntry = (OMTBlockEntry) PsiTreeUtil.findFirstParent(importBlock, parent -> parent instanceof OMTBlockEntry);
        setRootBlock(importBlockAsEntry, "import");
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
        quickFormat();
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
            case "prefixes":
                // prefixes is either after import or at the very top
                return getRootBlock("import").orElse(this.getRoot().getBlockEntryList().get(0));

            // import or undefined can be added at the top
            default:
            case "import":
                return this.getRoot().getFirstChild();
        }
    }

    private void updateExportMembers() {
        HashMap<String, OMTExportMember> exported = new HashMap<>();
        // commands
        Optional<OMTCommandsBlock> commands = getSpecificBlock("commands", OMTCommandsBlock.class);
        commands.ifPresent(omtCommandsBlock -> omtCommandsBlock.getDefineCommandStatementList().stream()
                .map(omtDefineCommandStatement -> new OMTExportMemberImpl(omtDefineCommandStatement, ExportMemberType.Command))
                .forEach(omtExportMember -> exported.put(omtExportMember.getName(), omtExportMember))
        );

        // queries
        Optional<OMTQueriesBlock> queries = getSpecificBlock("queries", OMTQueriesBlock.class);
        queries.ifPresent(omtQueriesBlock -> omtQueriesBlock.getDefineQueryStatementList().stream()
                .map(omtDefineQueryStatement -> new OMTExportMemberImpl(omtDefineQueryStatement, ExportMemberType.Query))
                .forEach(omtExportMember -> exported.put(omtExportMember.getName(), omtExportMember))
        );

        // modelItems
        Optional<OMTModelBlock> model = getSpecificBlock("model", OMTModelBlock.class);
        model.ifPresent(omtModelBlock -> omtModelBlock.getModelItemBlockList().stream()
                .map(omtModelItemBlock -> {
                    String modelItemType = omtModelItemBlock.getModelItemLabel().getModelItemTypeElement().getText();
                    switch (modelItemType.toLowerCase()) {
                        case "!activity":
                            return new OMTExportMemberImpl(omtModelItemBlock, ExportMemberType.Activity);
                        case "!procedure":
                            return new OMTExportMemberImpl(omtModelItemBlock, ExportMemberType.Procedure);
                        case "!standalonequery":
                            return new OMTExportMemberImpl(omtModelItemBlock, ExportMemberType.StandaloneQuery);
                        default:
                            return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(omtExportMember -> exported.put(omtExportMember.getName(), omtExportMember))
        );
        exportMembers = exported;
    }
}
