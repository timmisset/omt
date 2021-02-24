package com.misset.opp.ttl.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.named.OMTCurie;
import com.misset.opp.omt.psi.references.TTLReferenceElement;
import com.misset.opp.ttl.psi.TTLFile;
import com.misset.opp.ttl.psi.TTLIri;
import com.misset.opp.ttl.psi.TTLObject;
import com.misset.opp.ttl.psi.TTLSubject;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.references.TTLReferenceElement.getSubjectClassIri;
import static com.misset.opp.util.UtilManager.getProjectUtil;

public class TTLUtil {
    private final HashMap<String, ArrayList<FakePsiElement>> ttlSubjectReferences = new HashMap<>();
    private final HashMap<String, HashMap<String, ArrayList<FakePsiElement>>> ttlPredicateReferences = new HashMap<>();

    /**
     * Make references to the TTL ontology files in order to resolve them from OMTCurieElements
     * Since the TTL files are structured by the LNKD.tech plugin this needs to be installed and will be used
     */
    public void resetOntologyPsiReferences(Project project) {
        ttlSubjectReferences.clear();
        ttlPredicateReferences.clear();
        final Collection<VirtualFile> ttlFiles = FilenameIndex.getAllFilesByExt(project, "ttl");
        ttlFiles.stream()
                .filter(virtualFile -> !virtualFile.getPath().contains("target"))
                .map(
                        virtualFile -> {
                            PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
                            return file instanceof TTLFile ? (TTLFile) file : null;
                        })
                .filter(Objects::nonNull)
                .forEach(turtleFile -> {
                    PsiTreeUtil.findChildrenOfType(turtleFile, TTLSubject.class).stream()
                            .filter(turtleSubject ->
                                    turtleSubject.getIri() != null)
                            .forEach(turtleSubject -> {
                                String iri = turtleSubject.getIri().getResourceAsString();
                                ArrayList<FakePsiElement> fakePsiElements = ttlSubjectReferences.getOrDefault(iri, new ArrayList<>());
                                fakePsiElements.add(new TTLReferenceElement(turtleSubject));
                                ttlSubjectReferences.put(iri, fakePsiElements);
                            });

                    PsiTreeUtil.findChildrenOfType(turtleFile, TTLObject.class).stream()
                            .filter(turtleObject ->
                                    turtleObject.getIri() != null)
                            .forEach(turtleObject -> {
                                final String iri = turtleObject.getIri().getResourceAsString();
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
        validateModelLoaded(curie);
        String iri = curie.getAsResource().getURI();
        return getTTLReference(iri, subjectFilter);
    }

    public List<FakePsiElement> getTTLReference(TTLIri iri, List<Resource> subjectFilter) {
        validateModelLoaded(iri);
        return getTTLReference(iri.getResourceAsString(), subjectFilter);
    }

    public boolean hasSubject(TTLIri iri) {
        return iri != null && ttlSubjectReferences.containsKey(iri.getResourceAsString());
    }

    private void validateModelLoaded(PsiElement element) {
        if (getProjectUtil().getOntologyModel() == null) {
            getProjectUtil().loadOntologyModel(element.getProject(), true);
        }
    }

    private List<FakePsiElement> getTTLReference(String iri, List<Resource> subjectFilter) {
        if (getProjectUtil().getOntologyModel() == null) {
            return new ArrayList<>();
        }
        return ttlSubjectReferences.getOrDefault(iri,
                ttlPredicateReferences.getOrDefault(iri, new HashMap<>())
                        .keySet()
                        .stream()
                        .filter(subjectIri -> subjectFilter == null || subjectFilter.isEmpty() ||
                                subjectFilter.contains(getProjectUtil().getOntologyModel().createResource(subjectIri)))
                        .map(subjectIri -> ttlPredicateReferences.get(iri).get(subjectIri))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toCollection(ArrayList::new)));
    }

    public void renameSubject(TTLSubject from, TTLSubject to) {
        ttlSubjectReferences.put(to.getResourceAsString(), new ArrayList<>(
                Collections.singletonList(new TTLReferenceElement(to))));
        ttlSubjectReferences.remove(from.getResourceAsString());
    }
}
