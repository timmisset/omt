package com.misset.opp.omt.external.util.builtIn;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.psi.impl.OMTCallableImpl;
import com.misset.opp.omt.psi.impl.OMTParameterImpl;
import com.misset.opp.omt.psi.support.OMTParameter;
import com.misset.opp.omt.psi.util.ProjectUtil;
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

    public BuiltInMember getBuiltInMember(String name, BuiltInType type) {
        return builtInMembers.get(getIndexedName(name, type));
    }

    public List<String> getBuiltInOperatorsAsSuggestions() {
        return builtInMembers.values().stream()
                .filter(OMTCallableImpl::isOperator)
                .map(OMTCallableImpl::getAsSuggestion)
                .collect(Collectors.toList());
    }

    public List<String> getBuiltInCommandsAsSuggestions() {
        return builtInMembers.values().stream()
                .filter(OMTCallableImpl::isCommand)
                .map(OMTCallableImpl::getAsSuggestion)
                .collect(Collectors.toList());
    }

    public List<String> getBuiltInSuggestions(BuiltInType type) {
        return type == BuiltInType.Command ? getBuiltInCommandsAsSuggestions() : getBuiltInOperatorsAsSuggestions();
    }

    public boolean isCommand(BuiltInType type) {
        return type == BuiltInType.Command || type == BuiltInType.HttpCommands || type == BuiltInType.ParseJsonCommand;
    }

    private String getIndexedName(String name, BuiltInType type) {
        name = name.startsWith("@") && isCommand(type) ? name.substring(1) : name;
        return (isCommand(type) ? COMMAND_NAME_PREFIX : OPERATOR_NAME_PREFIX) + name;
    }

    private JsonObject parseHttpCommands(String block) {
        Pattern regEx = Pattern.compile("(?<=return )([\\s\\S]*)};");
        Matcher m = regEx.matcher(block);
        boolean found = m.find();
        if (found && m.groupCount() == 1) {
            String group = m.group(1) + "}";
            return parseBlock(group, BuiltInType.Command);
        }
        return null;
    }

    private JsonObject parseJsonCommand(String block) {
        Pattern regEx = Pattern.compile("(?<=export const jsonParseCommandDefinition)([^=]*)=([^;]*)");
        Matcher m = regEx.matcher(block);
        boolean found = m.find();
        if (found && m.groupCount() == 2) {
            String group = m.group(2);
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("JSON_PARSE", parseBlock(group, BuiltInType.Command));
            return jsonObject;
        }
        return null;
    }

    private JsonObject parseBuiltIn(String block, BuiltInType type) {
        Pattern regEx = Pattern.compile(String.format("(?<=export const builtin%s)([^=]*)=([^;]*)", type.name()));
        Matcher m = regEx.matcher(block);
        boolean found = m.find();
        if (found && m.groupCount() == 2) {
            return parseBlock(m.group(2), type);
        }
        return null;
    }

    private JsonObject parseBlock(String blockToParse, BuiltInType type) {
        JsonParser parser = new JsonParser();
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

    public void reset() {
        builtInMembers.clear();
    }

    public boolean isLoaded() {
        return !builtInMembers.isEmpty();
    }

    private String mapFlag(String constName, String documentContent) {
        Pattern regEx = Pattern.compile(String.format("const %s = '(.*)';", constName));
        Matcher matcher = regEx.matcher(documentContent);
        if (!matcher.find()) {
            return constName;
        }
        return matcher.group(1);
    }

    public void reloadBuiltInFromDocument(Document document,
                                          BuiltInType type,
                                          Project project,
                                          ProjectUtil projectUtil) {
        JsonObject items;
        switch (type) {
            case HttpCommands:
                items = parseHttpCommands(document.getText());
                break;
            case ParseJsonCommand:
                items = parseJsonCommand(document.getText());
                break;
            default:
                items = parseBuiltIn(document.getText(), type);
                break;
        }
        if (items == null) {
            return;
        }

        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        HashMap<String, String> flagNames = new HashMap<>();

        items.keySet().forEach(name -> {
            JsonObject operator = (JsonObject) items.get(name);
            JsonElement params = operator.get("params");
            List<OMTParameter> inputParameters = new ArrayList<>();
            if (params != null) {
                JsonArray parameters = (JsonArray) params;
                parameters.forEach(jsonElement ->
                        inputParameters.add(
                                new OMTParameterImpl(jsonElement,
                                        String.format("$param%s", inputParameters.size()))
                        ));
            }

            JsonElement link = operator.get("link");
            JsonElement doc = operator.get("doc");
            JsonElement flags = operator.get("flags");
            JsonElement returns = operator.get("returns");
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
            List<String> flagsAsList = new ArrayList<>();
            if (flags != null) {
                flags.getAsJsonArray().forEach(flag -> {
                    if (flag != null && !flag.isJsonNull()) {
                        String flagName = flag.getAsJsonArray().get(0).getAsString();
                        if (!flagNames.containsKey(flagName)) {
                            flagNames.put(flagName, mapFlag(flagName, document.getText()));
                        }
                        flagsAsList.add(flagNames.get(flagName));
                    }
                });
            }
            String dataType = "any";
            if (returns != null) {
                switch (returns.getAsString()) {
                    case "ResultType.Boolean":
                        dataType = "boolean";
                        break;
                    case "ResultType.String":
                        dataType = "string";
                        break;
                    case "ResultType.DateLike":
                        dataType = "dateTime";
                        break;
                    case "ResultType.Number":
                        dataType = "decimal";
                        break;
                    default:
                        dataType = "any";
                }
            }

            BuiltInMember builtInMember = new BuiltInMember(name, inputParameters, type, localVariables, flagsAsList, dataType);

            if (doc != null && !doc.isJsonNull()) {
                String docAsString = doc.getAsString();
                switch (docAsString) {
                    case "HttpCall.docGet":
                        docAsString = "HttpCallCommandGet.md";
                        break;
                    case "HttpCall.docPost":
                        docAsString = "HttpCallCommandPost.md";
                        break;
                    case "HttpCall.docPut":
                        docAsString = "HttpCallCommandPut.md";
                        break;
                    default:
                        docAsString = docAsString.replace("com.", "").replace("ops.", "").replace(".doc", ".md");
                        break;
                }

                PsiFile[] filesByName = projectUtil.getFilesByName(project, docAsString);

                if (filesByName.length == 1) {
                    Document builtInMemberDoc = projectUtil.getDocument(filesByName[0].getVirtualFile());
                    String text = builtInMemberDoc.getText();

                    Node parse = parser.parse(text);
                    builtInMember.setHTMLDescription(renderer.render(parse));
                }
            }

            builtInMembers.put(getIndexedName(name, type), builtInMember);
        });
    }
}
