package com.misset.opp.omt.external.util.builtIn;

import com.google.gson.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.misset.opp.omt.psi.impl.OMTCallableImpl;
import com.misset.opp.omt.psi.impl.OMTParameterImpl;
import com.misset.opp.omt.psi.support.OMTParameter;
import org.commonmark.html.HtmlRenderer;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BuiltInUtil {

    private final static String COMMAND_NAME_PREFIX = "COMMAND";
    private final static String OPERATOR_NAME_PREFIX = "OPERATOR";

    public static BuiltInUtil SINGLETON = new BuiltInUtil();

    private HashMap<String, BuiltInMember> builtInMembers = new HashMap<>();

    public boolean isBuiltInOperator(String name) {
        return builtInMembers.containsKey(getIndexedName(name, BuiltInType.Operator));
    }

    public boolean isBuiltInCommand(String name) {
        return builtInMembers.containsKey(getIndexedName(name, BuiltInType.Command));
    }

    public BuiltInMember getBuiltInMember(String name, BuiltInType type) {
        return builtInMembers.get(getIndexedName(name, type));
    }

    public List<String> getBuiltInOperatorsAsSuggestions() {
        return builtInMembers.values().stream()
                .filter(OMTCallableImpl::isOperator)
                .map(OMTCallableImpl::asSuggestion)
                .collect(Collectors.toList());
    }

    public List<String> getBuiltInCommandsAsSuggestions() {
        return builtInMembers.values().stream()
                .filter(OMTCallableImpl::isCommand)
                .map(OMTCallableImpl::asSuggestion)
                .collect(Collectors.toList());
    }

    private String getIndexedName(String name, BuiltInType type) {
        name = name.startsWith("@") && type == BuiltInType.Command ? name.substring(1) : name;
        return (type == BuiltInType.Command ? COMMAND_NAME_PREFIX : OPERATOR_NAME_PREFIX) + name;
    }

    public JsonObject parseBuiltIn(String block, BuiltInType type) {
        Pattern regEx = Pattern.compile(String.format("(?<=export const builtin%s)([^=]*)=([^;]*)", type.name()));
        Matcher m = regEx.matcher(block);
        boolean found = m.find();
        if (found && m.groupCount() == 2) {
            JsonParser parser = new JsonParser();
            String blockToParse = m.group(2);
            blockToParse = blockToParse.replaceAll("\\/\\/.*", ""); // remove the comments
            blockToParse = blockToParse.replaceAll("factory.*", ""); // remove the factor property
            if (type == BuiltInType.Command) {
                Pattern linkRegEx = Pattern.compile("link:.*");
                Matcher linkM = linkRegEx.matcher(blockToParse);
                while (linkM.find()) {
                    String matchingPattern = linkM.group();
                    String replacementPattern = matchingPattern.replace("link: ", "link: '");
                    replacementPattern = replacementPattern.substring(0, replacementPattern.length() - 1) + "',";
                    blockToParse = blockToParse.replace(matchingPattern, replacementPattern);
                }
            }
            blockToParse = blockToParse.replaceAll("([,\\s]+})", "}"); // remove trailing commas
            return (JsonObject) parser.parse(blockToParse);
        }
        return null;
    }

    public void reset() {
        builtInMembers.clear();
    }

    public boolean isLoaded() {
        return !builtInMembers.isEmpty();
    }

    public void reloadBuiltInFromDocument(Document document, BuiltInType type, Project project) {
        JsonObject items = parseBuiltIn(document.getText(), type);
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        items.keySet().forEach(name -> {
            JsonObject operator = (JsonObject) items.get(name);
            JsonElement params = operator.get("params");
            List<OMTParameter> inputParameters = new ArrayList<>();
            if (params != null) {
                JsonArray parameters = (JsonArray) params;
                parameters.forEach(jsonElement ->
                        inputParameters.add(
                                new OMTParameterImpl((JsonPrimitive) jsonElement,
                                        String.format("$param%s", inputParameters.size()))
                        ));
            }

            JsonElement link = operator.get("link");
            JsonElement doc = operator.get("doc");
            List<String> localVariables = new ArrayList<>();
            if (link != null) {
                String linkVariables = link.getAsString();
                Pattern pattern = Pattern.compile("\\$[^:, ]+:");
                Matcher matcher = pattern.matcher(linkVariables);
                while (matcher.find()) {
                    String variableName = matcher.group();
                    variableName = variableName.endsWith(":") ? variableName.substring(0, variableName.length() - 1) : variableName;
                    localVariables.add(variableName);
                }
            }
            BuiltInMember builtInMember = new BuiltInMember(name, inputParameters, type, localVariables);

            if (!doc.isJsonNull()) {
                String docAsString = doc.getAsString();
                docAsString = docAsString.replace("com.", "").replace("ops.", "").replace(".doc", ".md");
                PsiFile[] filesByName = FilenameIndex.getFilesByName(project, docAsString, GlobalSearchScope.allScope(project));
                if (filesByName.length == 1) {
                    Document builtInMemberDoc = FileDocumentManager.getInstance().getDocument(filesByName[0].getVirtualFile());
                    String text = builtInMemberDoc.getText();

                    Node parse = parser.parse(text);
                    builtInMember.setHTMLDescription(renderer.render(parse));
                }
            }

            builtInMembers.put(getIndexedName(name, type), builtInMember);
        });
    }

}
