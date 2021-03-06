package com.misset.opp.omt.completion;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTPrefix;
import org.apache.jena.rdf.model.Resource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.misset.opp.util.UtilManager.getCurieUtil;
import static com.misset.opp.util.UtilManager.getProjectUtil;
import static com.misset.opp.util.UtilManager.getRDFModelUtil;

public abstract class RDFCompletion extends OMTCompletion {
    protected void setResolvedElementsForClasses(PsiElement element, boolean addPathStart) {
        getProjectUtil().getRDFModelUtil().getAllClasses().stream().filter(resource -> resource.getURI() != null).forEach(
                resource -> setCurieSuggestion(element, resource, false, CLASSES_PRIORITY, addPathStart)
        );
        Arrays.asList("string", "integer", "boolean", "date").forEach(
                type -> addPriorityElement("string", 1)
        );
    }

    protected void setResolvedElementsForComparableTypes(PsiElement element, List<Resource> resources) {
        if (!resources.isEmpty()) {
            getRDFModelUtil().getComparableOptions(resources).forEach(
                    resource -> setCurieSuggestion(element, resource, false, EQUATION_PRIORITY, true)
            );
        }
    }

    protected void setCurieSuggestion(PsiElement elementAt, Resource resource, boolean reverse, int priority) {
        setCurieSuggestion(elementAt, resource, reverse, priority, false);
    }

    protected void setCurieSuggestion(PsiElement elementAt, Resource resource, boolean reverse, int priority, boolean addPathStart) {
        OMTFile omtFile = (OMTFile) elementAt.getContainingFile();
        String curieElement = omtFile.resourceToCurie(resource);
        String title = curieElement;
        AtomicBoolean registerPrefix = new AtomicBoolean(false);
        if (curieElement.equals(resource.toString())) {
            curieElement = getPrefixSuggestion(resource);
            title = resource.toString();
            registerPrefix.set(true);
        }
        List<Resource> resolvesTo;
        if (reverse) {
            title = "^" + title;
            curieElement = "^" + curieElement;
            resolvesTo = getRDFModelUtil().getPredicateSubjects(resource, false);
        } else {
            resolvesTo = getRDFModelUtil().getPredicateObjects(resource, false);
        }
        if (addPathStart) {
            curieElement = "/" + curieElement;
        }

        addPriorityElement(curieElement, priority, title, (context, item) ->
                        // if the iri is not registered in the page, do it
                        ApplicationManager.getApplication().runWriteAction(() -> {
                            if (registerPrefix.get()) {
                                getCurieUtil().addPrefixToBlock(context.getFile(),
                                        item.getLookupString().split(":")[0].substring(reverse || addPathStart ? 1 : 0),
                                        resource.getNameSpace());
                            }
                        }),
                null,
                !resolvesTo.isEmpty() && !getRDFModelUtil().isTypePredicate(resource) ? omtFile.resourceToCurie(resolvesTo.get(0)) : null);
    }

    private String getPrefixSuggestion(Resource resource) {
        if (resource.getNameSpace() == null) {
            return resource.getURI();
        }
        List<OMTPrefix> knownPrefixes = getProjectUtil().getKnownPrefixes(resource.getNameSpace());
        if (!knownPrefixes.isEmpty()) {
            return knownPrefixes.get(0).getNamespacePrefix().getName() + ":" + resource.getLocalName();
        }
        return resource.getURI();
    }
}
