package com.misset.opp.omt.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.misset.opp.omt.OMTFileType;
import com.misset.opp.omt.OMTLanguage;
import com.misset.opp.omt.psi.impl.OMTExportMemberImpl;
import com.misset.opp.omt.psi.support.ExportMemberType;
import com.misset.opp.omt.psi.support.OMTExportMember;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class OMTFile extends PsiFileBase {
    public OMTFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, OMTLanguage.INSTANCE);
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

    public <T> Optional<T> getRootBlock(String name) {
        OMTBlock rootBlock = (OMTBlock) getFirstChild();
        return rootBlock.getSpecificBlockList().stream()
                .filter(specificBlock -> specificBlock.getFirstChild().getFirstChild().getText().startsWith(name))
                .map(specificBlock -> (T) specificBlock.getFirstChild())
                .findFirst();
    }

    private HashMap<String, OMTExportMember> exportMembers = new HashMap<>();

    public List<OMTMember> getImportedMembers() {
        List<OMTMember> importedList = new ArrayList<>();
        Optional<OMTImportBlock> optionalOMTImportBlock = getRootBlock("import");
        optionalOMTImportBlock.ifPresent(omtImportBlock ->
                omtImportBlock.getImportList().forEach(omtImport -> importedList.addAll(omtImport.getMemberList().getMemberList()))
        );
        return importedList;
    }

    /**
     * Root queries and commands are available as well as OMT model items: Activity, Procedure and StandAloneQuery
     * They are available as OMTExportMembers which can resolve to the PsiElement that defines them but have additional
     * resolve functions such as the type, number of expected parameters etc
     *
     * @return
     */
    public HashMap<String, OMTExportMember> getExportedMembers() {
        if (exportMembers.isEmpty()) {
            updateExportMembers();
        }
        return exportMembers;
    }

    public void updateMembers() {
        updateExportMembers();
    }

    private void updateExportMembers() {
        HashMap<String, OMTExportMember> exported = new HashMap<>();
        // commands
        Optional<OMTCommandsBlock> commands = getRootBlock("commands");
        commands.ifPresent(omtCommandsBlock -> omtCommandsBlock.getDefineCommandStatementList().stream()
                .map(omtDefineCommandStatement -> new OMTExportMemberImpl(omtDefineCommandStatement, ExportMemberType.Command))
                .forEach(omtExportMember -> exported.put(omtExportMember.getName(), omtExportMember))
        );

        // queries
        Optional<OMTQueriesBlock> queries = getRootBlock("queries");
        queries.ifPresent(omtQueriesBlock -> omtQueriesBlock.getDefineQueryStatementList().stream()
                .map(omtDefineQueryStatement -> new OMTExportMemberImpl(omtDefineQueryStatement, ExportMemberType.Query))
                .forEach(omtExportMember -> exported.put(omtExportMember.getName(), omtExportMember))
        );

        // modelItems
        Optional<OMTModelBlock> model = getRootBlock("model");
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
                }).forEach(omtExportMember -> exported.put(omtExportMember.getName(), omtExportMember))
        );
        exportMembers = exported;
    }
}
