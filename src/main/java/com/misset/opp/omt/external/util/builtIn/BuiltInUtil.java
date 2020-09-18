package com.misset.opp.omt.external.util.builtIn;

import com.google.gson.*;
import com.intellij.openapi.editor.Document;
import com.misset.opp.omt.psi.impl.OMTCallableImpl;
import com.misset.opp.omt.psi.impl.OMTParameterImpl;
import com.misset.opp.omt.psi.support.OMTParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BuiltInUtil {

    private final static String COMMAND_NAME_PREFIX = "COMMAND";
    private final static String OPERATOR_NAME_PREFIX = "OPERATOR";

    private static HashMap<String, BuiltInMember> builtInMembers = new HashMap<>();

    public static boolean isBuiltInOperator(String name) {
        return builtInMembers.containsKey(getIndexedName(name, BuiltInType.Operator));
    }

    public static boolean isBuiltInCommand(String name) {
        return builtInMembers.containsKey(getIndexedName(name, BuiltInType.Command));
    }

    public static BuiltInMember getBuiltInMember(String name, BuiltInType type) {
        return builtInMembers.get(getIndexedName(name, type));
    }

    public static List<String> getBuiltInOperatorsAsSuggestions() {
        return builtInMembers.values().stream()
                .filter(OMTCallableImpl::isOperator)
                .map(OMTCallableImpl::asSuggestion)
                .collect(Collectors.toList());
    }

    public static List<String> getBuiltInCommandsAsSuggestions() {
        return builtInMembers.values().stream()
                .filter(OMTCallableImpl::isCommand)
                .map(OMTCallableImpl::asSuggestion)
                .collect(Collectors.toList());
    }

    private static String getIndexedName(String name, BuiltInType type) {
        name = name.startsWith("@") && type == BuiltInType.Command ? name.substring(1) : name;
        return (type == BuiltInType.Command ? COMMAND_NAME_PREFIX : OPERATOR_NAME_PREFIX) + name;
    }

    public static JsonObject parseBuiltIn(String block, BuiltInType type) {
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
//                blockToParse = blockToParse.replaceAll("link.*", ""); // remove the link property
            }
            blockToParse = blockToParse.replaceAll("([,\\s]+})", "}"); // remove trailing commas
            return (JsonObject) parser.parse(blockToParse);
        }
        return null;
    }

    public static void reset() {
        builtInMembers.clear();
    }

    public static boolean hasLoaded() {
        return !builtInMembers.isEmpty();
    }

    public static void reloadBuiltInFromDocument(Document document, BuiltInType type) {
        JsonObject items = BuiltInUtil.parseBuiltIn(document.getText(), type);
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
            builtInMembers.put(getIndexedName(name, type), new BuiltInMember(name, inputParameters, type, localVariables));
        });
    }

}
