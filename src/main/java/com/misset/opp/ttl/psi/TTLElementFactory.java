package com.misset.opp.ttl.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.ttl.TTLFileType;

public class TTLElementFactory {
    public static PsiElement fromString(String text, Class<? extends PsiElement> getClass, Project project) {
        PsiElement rootElement = createFile(project, text);
        return PsiTreeUtil.findChildOfType(rootElement, getClass);
    }

    private static TTLFile createFile(Project project, String text) {
        String name = "dummy.ttl";
        return (TTLFile) PsiFileFactory.getInstance(project).createFileFromText(name, TTLFileType.INSTANCE, text);
    }

    public static TTLSubject getSubject(Project project, String prefix, String localName) {
        return (TTLSubject) fromString(String.format("%s:%s\n" +
                "    a:Predicate an:Object\n" +
                "    .", prefix, localName), TTLSubject.class, project);
    }

    public static TTLObject getObject(Project project, String prefix, String localName) {
        return (TTLObject) fromString(String.format("a:Subject\n" +
                "    a:Predicate %s:%s\n" +
                "    .", prefix, localName), TTLObject.class, project);
    }
}
