package com.misset.opp.omt.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.misset.opp.omt.OMTFileType;
//import com.misset.opp.omt.domain.util.CurieUtil;

public class OMTElementFactory {
    private final static String defaultBlock = "prefixes:\n" +
            "\n";
//    public static OMTPrefix createPrefix(String leftHand, String rightHand, Project project) {
//        if(!leftHand.endsWith(":")) { leftHand += ":"; }
//        if(rightHand != null) { rightHand = "<http://enter.your/iri/>"; }
//
//        OMTFile file = createFile(project, String.format("prefixes:\n" +
//                "    %s %s\n" +
//                "\n" +
//                "queries:", leftHand, rightHand));
//
//        OMTPrefixBlock prefixBlock = CurieUtil.getPrefixBlock(file);
//        return prefixBlock.getPrefixList().get(0);
//    }

//    public static OMTPrefixBlock createPrefixBlock(Project project) {
//        OMTFile file = createFile(project, defaultBlock);
//
//        return CurieUtil.getPrefixBlock(file);
//    }

    private static OMTFile createFile(Project project, String text) {
        String name = "dummy.omt";
        return (OMTFile) PsiFileFactory.getInstance(project).createFileFromText(name, OMTFileType.INSTANCE, text);
    }
    public static OMTVariable createVariable(Project project, String name) {
        name = name.startsWith("$") ? name : String.format("$%s", name);
        return (OMTVariable)createFile(project, name).getFirstChild();
    }

    public static PsiElement createNewLine(Project project) {
        return createFile(project, "\n").getFirstChild();
    }
    public static PsiElement createIdent(Project project) {
        return createFile(project, "\n    a").getChildren()[1];
    }


}
