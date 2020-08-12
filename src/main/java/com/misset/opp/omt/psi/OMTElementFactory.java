package com.misset.opp.omt.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.OMTFileType;
import com.misset.opp.omt.psi.util.CurieUtil;
//import com.misset.opp.omt.domain.util.CurieUtil;

public class OMTElementFactory {
    private final static String defaultBlock = "prefixes:\n" +
            "\n";
    public static OMTPrefix createPrefix(String leftHand, String rightHand, Project project) {
        if(!leftHand.endsWith(":")) { leftHand += ":"; }
        if(rightHand != null) { rightHand = "<http://enter.your/iri/>"; }

        OMTFile file = createFile(project, String.format("prefixes:\n" +
                "    %s %s\n" +
                "\n" +
                "queries:", leftHand, rightHand));

        OMTPrefixBlock prefixBlock = CurieUtil.getPrefixBlock(file);
        return prefixBlock.getPrefixList().get(0);
    }

    /**
     * @param curie - entire curie => some:thing
     * @param project
     * @return
     */
    public static OMTCurieElement createCurieElement(Project project, String curie) {
        OMTFile file = createFile(project, String.format("something: |\n" +
                "\t%s", curie));
        return PsiTreeUtil.findChildOfType(file, OMTCurieElement.class);
    }

    /**
     * @param curie - entire constant curie => /some:thing
     * @param project
     * @return
     */
    public static OMTCurieConstantElement createCurieConstantElement(Project project, String curie) {
        OMTFile file = createFile(project, String.format("something: |\n" +
                "\t%s", curie));
        return PsiTreeUtil.findChildOfType(file, OMTCurieConstantElement.class);
    }

    public static OMTPrefixBlock createPrefixBlock(Project project) {
        OMTFile file = createFile(project, defaultBlock);

        return CurieUtil.getPrefixBlock(file);
    }

    private static OMTFile createFile(Project project, String text) {
        String name = "dummy.omt";
        return (OMTFile) PsiFileFactory.getInstance(project).createFileFromText(name, OMTFileType.INSTANCE, text);
    }
    public static OMTVariable createVariable(Project project, String name) {
        name = name.startsWith("$") ? name : String.format("$%s", name);
        return (OMTVariable)createFile(project, name).getFirstChild();
    }

    public static PsiElement createOperator(Project project, String name) {
        OMTFile file = createFile(project, String.format("queries: |\n" +
                "\n" +
                "            DEFINE QUERY %s() => ''; ", name));
        OMTDefineQueryStatement defineQueryStatement = PsiTreeUtil.findChildOfType(file, OMTDefineQueryStatement.class);
        return defineQueryStatement.getDefineName();

    }

    public static PsiElement createCommand(Project project, String name) {
        OMTFile file = createFile(project, String.format("commands: |\n" +
                "\n" +
                "            DEFINE COMMAND %s() => { RETURN ''} ", name));
        OMTDefineCommandStatement defineCommandStatement = PsiTreeUtil.findChildOfType(file, OMTDefineCommandStatement.class);
        return defineCommandStatement.getDefineName();

    }

    public static PsiElement createMember(Project project, String name) {
        OMTFile file = createFile(project, String.format("import:\n" +
                "    '@client/medewerker/src/utils/lidmaatschap.queries.omt':\n" +
                "        -   %s", name));
        return PsiTreeUtil.findChildOfType(file, OMTMember.class);
    }

    public static PsiElement createImportSource(Project project, String name) {
        OMTFile file = createFile(project, String.format("import:\n" +
                "    %s\n" +
                "        -   member", name));
        return PsiTreeUtil.findChildOfType(file, OMTImportSource.class);
    }

    public static PsiElement createNewLine(Project project) {
        return createFile(project, "\n").getFirstChild();
    }

    public static PsiElement createIdent(Project project) {
        return createFile(project, "\n    a").getChildren()[1];
    }



}
