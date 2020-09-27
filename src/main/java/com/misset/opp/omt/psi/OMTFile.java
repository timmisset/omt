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
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OMTFile extends PsiFileBase {
    public OMTFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, OMTLanguage.INSTANCE);
    }

    private static final ModelUtil modelUtil = ModelUtil.SINGLETON;
    private static final ImportUtil importUtil = ImportUtil.SINGLETON;

    @NotNull
    @Override
    public FileType getFileType() {
        return OMTFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "OMT File";
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
                omtImportBlock.getImportList().forEach(omtImport -> importedList.addAll(omtImport.getMemberList().getMemberList()))
        );
        return importedList;
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
                prefixHashMap.put(omtPrefix.getNamespacePrefix().getText(), omtPrefix)));
        return prefixHashMap;
    }

    public void updateMembers() {
        updateExportMembers();
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
