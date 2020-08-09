package com.misset.opp.omt.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.OMTFileType;
import com.misset.opp.omt.OMTLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
                .map(specificBlock -> (T)specificBlock.getFirstChild())
                .findFirst();
    }

    public List<OMTMember> getImportedMembers() {
        List<OMTMember> importedList = new ArrayList<>();
        Optional<OMTImportBlock> optionalOMTImportBlock = getRootBlock("import");
        optionalOMTImportBlock.ifPresent(omtImportBlock ->
                omtImportBlock.getImportList().forEach(omtImport -> {
                    omtImport.getMemberList().getMemberList().forEach(member ->
                            importedList.add(member)
                    );
                })
        );
        return importedList;
    }
}
