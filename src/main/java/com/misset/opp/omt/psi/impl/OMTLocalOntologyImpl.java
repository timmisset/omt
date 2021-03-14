package com.misset.opp.omt.psi.impl;

import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTGenericBlock;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.OMTScalarValue;
import com.misset.opp.omt.psi.OMTSequence;
import com.misset.opp.omt.psi.OMTSequenceItem;
import com.misset.opp.omt.psi.util.ModelUtil;
import com.misset.opp.omt.util.RDFModelUtil;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.misset.opp.util.UtilManager.getModelUtil;
import static com.misset.opp.util.UtilManager.getRDFModelUtil;

/**
 * A specified ModelItem block of the type !Ontology that is used to generate additional
 * RDF model information in the transaction that runs it via the LOAD_ONTOLOGY command
 * usually via a Procedure with it's own transaction
 */
public class OMTLocalOntologyImpl extends OMTModelItemBlockSupImpl {
    public OMTLocalOntologyImpl(OMTModelItemBlock modelItemBlock) {
        super(modelItemBlock.getNode());
    }

    @NotNull
    public String getPrefix() {
        final Optional<OMTBlockEntry> prefix = getModelUtil().getModelItemBlockEntry(getBlock(), "prefix");
        if (prefix.isPresent()) {
            return getModelUtil().getEntryBlockValue(prefix.get());
        }
        return "";
    }

    @NotNull
    public List<Resource> getClasses() {
        final Optional<OMTBlockEntry> classes = getModelUtil().getModelItemBlockEntry(getBlock(), "classes");
        if (classes.isEmpty()) return Collections.emptyList();

        final OMTGenericBlock classesBlock = (OMTGenericBlock) classes.get();
        final OMTSequence classesBlockSequence = classesBlock.getSequence();
        if (classesBlockSequence == null) {
            return Collections.emptyList();
        }

        // use the omtFile to parse the curie into a full iri
        final OMTFile omtFile = (OMTFile) getContainingFile();
        final String prefix = getPrefix();

        return classesBlockSequence.getSequenceItemList().stream()
                .map(OMTSequenceItem::getScalarValue)
                .filter(Objects::nonNull)
                .map(OMTScalarValue::getIndentedBlock)
                .filter(Objects::nonNull)
                .map(omtBlock -> getModelUtil().getEntryBlockEntry(omtBlock, "id").orElse(null))
                .filter(Objects::nonNull)
                .map(omtBlockEntry -> omtFile.curieToIri(String.format("%s:%s", prefix, getModelUtil().getEntryBlockValue(omtBlockEntry))))
                .map(iri -> getRDFModelUtil().getResource(iri))
                .collect(Collectors.toList());
    }

    private List<OMTBlock> getClassBlocks() {
        final Optional<OMTBlockEntry> classes = getModelUtil().getModelItemBlockEntry(getBlock(), "classes");
        if (classes.isEmpty()) return Collections.emptyList();

        final OMTGenericBlock classesBlock = (OMTGenericBlock) classes.get();
        final OMTSequence classesBlockSequence = classesBlock.getSequence();
        if (classesBlockSequence == null) {
            return Collections.emptyList();
        }

        return classesBlockSequence.getSequenceItemList().stream()
                .map(OMTSequenceItem::getScalarValue)
                .filter(Objects::nonNull)
                .map(OMTScalarValue::getIndentedBlock)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @NotNull
    public List<Statement> getStatements() {
        List<Statement> statements = new ArrayList<>();
        final OMTFile omtFile = (OMTFile) getContainingFile();
        final ModelUtil modelUtil = getModelUtil();
        final String prefix = getPrefix();

        getClassBlocks().forEach(
                classBlock -> {
                    final OMTBlockEntry idEntry = modelUtil.getEntryBlockEntry(classBlock, "id").orElse(null);
                    final OMTBlockEntry propertiesEntry = modelUtil.getEntryBlockEntry(classBlock, "properties").orElse(null);

                    String classIri = omtFile.curieToIri(String.format("%s:%s", prefix, modelUtil.getEntryBlockValue(idEntry)));
                    statements.add(getRDFModelUtil().getClassStatement(classIri));

                    if (propertiesEntry.getBlock() != null &&
                            !propertiesEntry.getBlock().getBlockEntryList().isEmpty()) {
                        statements.addAll(
                                propertiesEntry
                                        .getBlock()
                                        .getBlockEntryList()
                                        .stream()
                                        .map(blockEntry -> this.getPropertyAsStatement((OMTGenericBlock) blockEntry, classIri, prefix, omtFile))
                                        .collect(Collectors.toList())
                        );
                    }
                    propertiesEntry.getBlock();

                }
        );
        return statements;
    }

    /**
     * Provide the OMTFile for performance. Every call to getContainingFile traverses the entire PsiTree
     *
     * @param blockEntry
     * @param file
     * @return
     */
    private Statement getPropertyAsStatement(OMTGenericBlock blockEntry, String classIri, String prefix, OMTFile file) {
        final ModelUtil modelUtil = getModelUtil();
        final RDFModelUtil rdfModelUtil = getRDFModelUtil();
        String predicate = file.curieToIri(String.format("%s:%s", prefix, blockEntry.getName()));
        if (blockEntry.getScalar() != null) {
            // scalar value, the value in this case equals the property type
            final String objectType = blockEntry.getScalar().getText();
            if (rdfModelUtil.isKnownPrimitiveType(objectType)) {
//                return StatementImpl(
//                        rdfModelUtil.getResource(classIri),
//                        rdfModelUtil.getResource(predicate),
//                        rdfModelUtil.getPrimitiveTypeAsResource(objectType));
            }
        }
        return null; //new StatementImpl();
    }

}
