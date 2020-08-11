//package com.misset.opp.omt.domain.util;
//
//import com.google.gson.*;
//import com.intellij.openapi.editor.Document;
//import com.misset.opp.omt.domain.OMTCommand;
//import com.misset.opp.omt.domain.OMTOperator;
//import com.misset.opp.omt.psi.OMTParameter;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class BuiltInUtil {
//
//    public static JsonObject parseBuiltIn(String block, BuiltInType type) {
//        Pattern regEx = Pattern.compile(String.format("(?<=export const builtin%s)([^=]*)=([^;]*)", type.name()));
//        Matcher m = regEx.matcher(block);
//        boolean found = m.find();
//        if(found && m.groupCount() == 2) {
//            JsonParser parser = new JsonParser();
//            String blockToParse = m.group(2);
//            blockToParse = blockToParse.replaceAll("\\/\\/.*", ""); // remove the comments
//            blockToParse = blockToParse.replaceAll("factory.*", ""); // remove the factor property
//            if(type == BuiltInType.Commands) {
//                blockToParse = blockToParse.replaceAll("link.*", ""); // remove the link property
//            }
//            blockToParse = blockToParse.replaceAll("([,\\s]+})", "}"); // remove trailing commas
//            return (JsonObject)parser.parse(blockToParse);
//        }
//        return null;
//    }
//    public static <T> List<T> reloadBuiltInFromDocument(Document document, BuiltInType type) {
//        JsonObject items = BuiltInUtil.parseBuiltIn(document.getText(), type);
//
//        List<T> parsedItems = new ArrayList<>();
//        items.keySet().forEach(name -> {
//            JsonObject operator = (JsonObject) items.get(name);
//            JsonElement params = operator.get("params");
//            List<OMTParameter> literals = new ArrayList<>();
//            if(params != null) {
//                JsonArray parameters = (JsonArray) params;
//                parameters.forEach(jsonElement -> literals.add(new OMTParameter((JsonPrimitive) jsonElement)));
//            }
//
//            if (type == BuiltInType.Commands) {
//                parsedItems.add((T)new OMTCommand(name, literals, "Built-in Command"));
//            } else if (type == BuiltInType.Operators) {
//                parsedItems.add((T)new OMTOperator(name, literals, "Built-in Operator"));
//            }
//        });
//        return parsedItems;
//    }
//
//}
