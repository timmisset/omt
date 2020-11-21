package com.misset.opp.omt.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiParserFacade;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.OMTFileType;
import com.misset.opp.omt.psi.util.CurieUtil;

public class OMTElementFactory {
    private static final CurieUtil curieUtil = CurieUtil.SINGLETON;

    public static OMTVariable createVariable(Project project, String name) {
        name = name.startsWith("$") ? name.substring(1) : name;
        OMTFile file = createFile(project, String.format("model:\n" +
                "    Template: !Activity\n" +
                "        variables:\n" +
                "            -   $%s\n", name));
        return PsiTreeUtil.findChildOfType(file, OMTVariable.class);
    }

    public static OMTModelItemLabel createModelItemLabelPropertyLabel(Project project, String name, String type) {
        name = name.endsWith(":") ? name.substring(0, name.length() - 1) : name;
        type = type.startsWith("!") ? type.substring(1) : type;
        OMTFile file = createFile(project, String.format("model:\n" +
                "    %s: !%s\n" +
                "        variables:\n" +
                "            -   $variable\n", name, type));
        return PsiTreeUtil.findChildOfType(file, OMTModelItemLabel.class);
    }

    public static OMTMember createMember(Project project, String name) {
        String format = String.format("import:\n" +
                "    '@client/medewerker/src/utils/lidmaatschap.queries.omt':\n" +
                "        -   %s\n", name);
        OMTFile file = createFile(project, format);
        PsiElement firstChild = file.getFirstChild();
        return PsiTreeUtil.findChildOfType(firstChild, OMTMember.class);
    }

    /**
     * @param curie   - entire curie => some:thing
     * @param project
     * @return
     */
    public static OMTNamespacePrefix createNamespacePrefix(Project project, String curie) {
        OMTFile file = createFile(project, String.format("prefixes:\n" +
                "    %s:    <http://ontologie.alfabet.nl/alfabet#>\n" +
                "", curie));
        return PsiTreeUtil.findChildOfType(file, OMTNamespacePrefix.class);
    }

    private static OMTFile createFile(Project project, String text) {
        String name = "dummy.omt";
        return (OMTFile) PsiFileFactory.getInstance(project).createFileFromText(name, OMTFileType.INSTANCE, text);
    }

    public static OMTDefineName createOperator(Project project, String name) {
        OMTFile file = createFile(project, String.format("queries: |\n" +
                "\n" +
                "            DEFINE QUERY %s() => ''; \n" +
                "\n", name));
        OMTDefineQueryStatement defineQueryStatement = PsiTreeUtil.findChildOfType(file, OMTDefineQueryStatement.class);
        return defineQueryStatement.getDefineName();
    }

    public static OMTOperatorCall createOperatorCall(Project project, String name, String flagSignature, String signature) {
        OMTFile file = createFile(project, String.format("model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        onStart: |\n" +
                "            %s%s%s;", name, flagSignature, signature));
        return PsiTreeUtil.findChildOfType(file, OMTOperatorCall.class);
    }

    public static OMTDefineName createCommand(Project project, String name) {
        OMTFile file = createFile(project, String.format("commands: |\n" +
                "\n" +
                "            DEFINE COMMAND %s() => { RETURN ''} \n" +
                "\n", name));
        OMTDefineCommandStatement defineCommandStatement = PsiTreeUtil.findChildOfType(file, OMTDefineCommandStatement.class);
        return defineCommandStatement.getDefineName();

    }

    public static OMTCommandCall createCommandCall(Project project, String name, String flagSignature, String signature) {
        OMTFile file = createFile(project, String.format("model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        onStart: |\n" +
                "            @%s%s%s;", name, flagSignature, signature));
        return PsiTreeUtil.findChildOfType(file, OMTCommandCall.class);
    }


    public static PsiElement createImportSource(Project project, String name) {
        OMTFile file = createFile(project, String.format("import:\n" +
                "    %s\n" +
                "        -   member\n" +
                "\n", name));
        return PsiTreeUtil.findChildOfType(file, OMTImportSource.class);
    }

    public static PsiElement createNewLine(Project project) {
        return createFile(project, "\n").getFirstChild();
    }

    public static OMTBlock addEntryToBlock(Project project, OMTBlock block, String propertyLabel) {
        return addEntryToBlock(project, block, propertyLabel, "DUMMYVALUE");
    }

    public static OMTBlock addEntryToBlock(Project project, OMTBlock block, String propertyLabel, String propertyValue) {
        String blockText = String.format("%s: %s", propertyLabel, propertyValue);
        OMTFile file = createFile(project, blockText);
        PsiElement blockEntry = PsiTreeUtil.findChildOfType(file, OMTBlockEntry.class);
        if (blockEntry == null) {
            return block;
        }
        blockEntry.add(getWhiteSpaceElement(project, 1, "\n"));

//        if (block.getDedentToken() != null) {
//            block.addBefore(blockEntry, block.getDedentToken());
//        } else {
        block.add(blockEntry);
//        }

        return block;
    }

    public static PsiElement getWhiteSpaceElement(Project project, int length, String character) {
        return PsiParserFacade.SERVICE.getInstance(project).createWhiteSpaceFromText(getWhiteSpace(length, character));
    }

    public static String getWhiteSpace(int length, String character) {
        return new String(new char[length]).replace("\0", character);
    }

    public static String getIndentSpace(int indents) {
        return getIndentSpace(indents, 0);
    }

    public static String getIndentSpace(int indents, int offset) {
        int length = indents * 4 - offset;
        return new String(new char[length]).replace("\0", " ");
    }

    public static PsiElement fromString(String text, Class<? extends PsiElement> getClass, Project project) {
        PsiElement rootElement = createFile(project, text);
        return PsiTreeUtil.findChildOfType(rootElement, getClass);
    }

    public static void validateFile(String text, Project project) {
        PsiElement rootElement = createFile(project, text);
    }
}
