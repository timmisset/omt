package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.misset.opp.omt.psi.named.OMTCurie;
import org.apache.jena.rdf.model.Resource;
import org.eclipse.collections.api.RichIterable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lnkd.editor.intellij.turtle.TurtleFile;
import tech.lnkd.editor.intellij.turtle.psi.TurtleSubject;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The curie reference resolves to the declaration of the curie prefix in either the prefixes: node or
 * a defined PREFIX statement when used in a script.
 * The CurieUtil will find the declaring statement of the prefix
 */
public class CurieReference extends PsiReferenceBase<OMTCurie> implements PsiPolyVariantReference {
    Resource resource;

    public CurieReference(@NotNull OMTCurie omtCurie, TextRange textRange) {
        super(omtCurie, textRange);
        resource = omtCurie.getAsResource();
    }

    // This reference uses the LNKD.tech Editor plugin
    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        final Collection<VirtualFile> ttlFiles = FilenameIndex.getAllFilesByExt(getElement().getProject(), "ttl");
        final List<TurtleFile> ttlPsiFiles = ttlFiles.stream().map(
                virtualFile -> {
                    PsiFile file = PsiManager.getInstance(myElement.getProject()).findFile(virtualFile);
                    return file instanceof TurtleFile ? (TurtleFile) file : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // look in all the available turtle files if any of the subjects resolve to the curie element
        for (TurtleFile ttlFile : ttlPsiFiles) {
            if (ttlFile.resources().containsKey(resource.toString())) {
                final RichIterable<TurtleSubject> turtleSubjects = ttlFile.resources().get(resource.toString());
                final TurtleSubject subject = turtleSubjects.getOnly();
                return new ResolveResult[]{new PsiElementResolveResult(subject)};
            }
        }
        return ResolveResult.EMPTY_ARRAY;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

}
