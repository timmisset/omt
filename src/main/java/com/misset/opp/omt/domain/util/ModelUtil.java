//package com.misset.opp.omt.domain.util;
//
//import com.intellij.openapi.project.Project;
//import com.intellij.psi.PsiElement;
//import com.intellij.psi.util.PsiTreeUtil;
//import com.misset.opp.omt.domain.OMTModelItem;
//import com.misset.opp.omt.domain.OMTModelItemAttribute;
//import com.misset.opp.omt.psi.*;
//import com.misset.opp.omt.domain.OMTParameter;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//public class ModelUtil {
//
//    public static boolean annotateModelItems = true;
//    private static String modelDirectory = "/core/omt/model";
//    // Keeps track of all parsed model items, such as Activity, Procedure etc
//    // This will be used to determine which attributes are allowed
//    private static HashMap<String, OMTModelItem> modelItems = new HashMap<>();
//
//    // Keeps track of all parsed attributes, such as prefixes, graphs etc
//    // this will be used to determine what type of data is allowed. Strings, lists other attributes or modelItems
//    private static HashMap<String, OMTModelItemAttribute> modelItemAttributes = new HashMap<>();
//
//    // Keeps track of all processed content
//    // since we will be checking the content multiple times, it's quicker to keep it in memory since it's only a few files
//    private static List<String> modelFileContents = new ArrayList<>();
//
//    /**
//     * Returns the modelItem block that this element belongs to
//     * A modelItem is an element declared directly under model:
//     * @param element
//     * @return
//     */
//    public static Optional<OMTModelItemBlock> getModelItemBlock(PsiElement element) {
//        OMTModelItemBlock block = PsiTreeUtil.getTopmostParentOfType(element, OMTModelItemBlock.class);
//        return block != null ? Optional.of(block) : Optional.empty();
//    }
//
//
//    public static List<OMTDefineQueryStatement> getAllDefinedQueries(PsiElement containingElement) {
//        // from the document in the queries root:
//        List<OMTDefineQueryStatement> definedQueries = new ArrayList<>();
//        OMTQueriesBlock queriesBlock = PsiTreeUtil.findChildOfType(containingElement, OMTQueriesBlock.class);
//        if(queriesBlock != null) {
//            definedQueries.addAll(queriesBlock.getDefineQueryStatementList());
//        }
//        return definedQueries;
//    }
//
//    public static List<OMTParameter> getModelItemParameters(OMTModelItemBlock modelItemBlock) {
//        List<OMTParameter> parameters = new ArrayList<>();
//
//        if(modelItemBlock.getBlock() != null) {
//            Optional<OMTSequence> params = modelItemBlock.getBlock().getBlockEntryList().stream()
//                    .filter(omtBlockEntry -> omtBlockEntry.getPropertyLabel().getText().equals("params"))
//                    .map(OMTBlockEntry::getSequence)
//                    .findFirst();
//
//            if(params.isPresent()) {
//                parameters = params.get().getSequenceItemList().stream()
//                        .map(OMTSequenceItem::getSequenceItemValue)
//                        .map(OMTSequenceItemValue::getParameter)
//                        .filter(Objects::nonNull)
//                        .map(OMTParameter::new)
//                        .collect(Collectors.toList());
//            }
//        }
//        return parameters;
//    }
//
//    public static List<OMTModelItemBlock> getModelItemBlocksOfType(OMTModelBlock modelBlock, String typeOf) {
//        return getModelItemBlocksOfTypes(modelBlock, new String[] { typeOf });
//    }
//    /**
//     * Retrieves the modelItem blocks by their type cast !Procedure, !Component etc.
//     * @param modelBlock
//     * @param typesOf Activity, Component, Procedure etc. Without !
//     * @return
//     */
//    public static List<OMTModelItemBlock> getModelItemBlocksOfTypes(OMTModelBlock modelBlock, String[] typesOf) {
//        List<String> types = Arrays.asList(typesOf);
//        return modelBlock.getModelItemBlockList().stream().filter(modelItemBlock ->
//            types.contains(modelItemBlock
//                    .getModelItemLabel()
//                    .getModelItemTypeElement()
//                    .getText().substring(1)))
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Returns the String representation of the ModelItem type, dropping the !
//     * !Procedure => Procedure
//     * Can be used to obtain the corresponding ModelItem definition
//     * @param modelItemBlock
//     * @return
//     */
//    public static String getModelItemType(OMTModelItemBlock modelItemBlock) {
//        return modelItemBlock.getModelItemLabel().getModelItemTypeElement().getText().substring(1);
//    }
//    public static OMTModelItem getModelItem(OMTModelItemBlock modelItemBlock, Project project) {
//        OMTModelItem omtModelItem = getModelItems(project).get(getModelItemType(modelItemBlock));
//        if(modelItems.isEmpty()) {
//            // apparently we cannot retrieve the data, don't annotate any model items
//            // TODO: Show user warning that the model item files cannot be located
//            System.out.println("Annotation is disabled for model items");
//            annotateModelItems = false;
//        }
//        return omtModelItem;
//    }
//
//    // TODO: move this to a helper file, preferable external to make it configurable
//    public static String getModelItemAttributeSuggestion(OMTModelItem modelItem, String label) {
//        // some hardcoded suggestions of common mistakes. Let's see how well this holds up:
//        String wrongLabel = label.toLowerCase();
//        if(modelItem.getType().equals("Activity")) {
//            switch (wrongLabel) {
//                case "onrun":
//                case "run":
//                case "oninit":
//                case "init":
//                case "start":
//                case "onstart": // any casing issue
//                    return "onStart";
//
//                case "cancel": return "onCancel";
//                case "bindings": return "params";
//            }
//        }
//        if(modelItem.getType().equals("Component")) {
//            switch (wrongLabel) {
//                case "run":
//                case "onrun":
//                case "oninit": // any casing issue
//                case "init":
//                case "onstart":
//                case "start":
//                    return "onInit";
//                case "params": return "bindings";
//
//            }
//        }
//        return null;
//    }
//
//    public static boolean hasModelItemDefinition(String name) {
//        return modelItems.containsKey(name);
//    }
//    public static OMTModelItem getModelItemDefinition(String name) {
//        return modelItems.get(name);
//    }
//    public static boolean hasModelItemAttributeDefinition(String name) {
//        return modelItemAttributes.containsKey(name);
//    }
//    public static OMTModelItemAttribute getOrSetModelItemAttributeDefinition(OMTModelItemAttribute modelItemAttribute) {
//        if(!modelItemAttributes.containsKey(modelItemAttribute.getName())) {
//            modelItemAttributes.put(modelItemAttribute.getName(), modelItemAttribute);
//        }
//        return modelItemAttributes.get(modelItemAttribute.getName());
//    }
//
//    public static HashMap<String, OMTModelItem> getModelItems(Project project) {
//        if(modelItems == null || modelItems.isEmpty()) {
//            modelItems = new HashMap<>();
//            modelItemAttributes = new HashMap<>();
//            modelFileContents = new ArrayList<>();
//
//            String folder = String.format("%s%s", project.getBasePath(), "/core/omt/src/model");
//            List<File> modelFiles = getAllModelFiles(folder);
//            for(File file : modelFiles) {
//                try {
//                    String content = new String(Files.readAllBytes(file.toPath()));
//                    OMTModelItem omtModelItem = parseModelItem(content);
//                    if(omtModelItem != null) {
//                        // can be null for @Def containing files that are not really model files (like module.ts)
//                        modelItems.put(omtModelItem.getType(), omtModelItem);
//                    }
//                } catch (IOException e) {
//                    // TODO: this should get a better error resolver, but for now it's fine
//                    e.printStackTrace();
//                }
//            }
//
//            // validate the model:
//            for(Map.Entry<String, OMTModelItem> modelItem: modelItems.entrySet()) {
//                OMTModelItem omtModelItem = modelItem.getValue();
//                List<String> missingAttributes = omtModelItem.validate();
//                for(String attribute : missingAttributes) {
//                    // retrieve the missing attributes from the contents:
//                    String modelItemAttributeDef = getModelItemAttributeDef(attribute);
//                    OMTModelItemAttribute modelItemAttribute = new OMTModelItemAttribute(attribute, modelItemAttributeDef);
//                    modelItemAttributes.put(modelItemAttribute.getName(), modelItemAttribute);
//                }
//            }
//            // some of the attributes are missing because they are indirectly defined in the model
//            // therefore, we need to check the files again for these items:
//
//
//        }
//        return modelItems;
//    }
//
//    public static String getModelItemAttributeDef(String name) {
//
//        Pattern pattern = Pattern.compile(String.format("export const %s[^{]*((?s).*?(?=;))", name));
//        for(String content : modelFileContents) {
//            Matcher matcher = pattern.matcher(content);
//            if(matcher.find()) {
//                return matcher.group(1);
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Returns all the files in the filesystem that are considered part of the OMT model
//     * This is determined by searching inside the files for a @Def annotation on an exported class
//     * @param path
//     * @return
//     */
//    public static List<File> getAllModelFiles(String path) {
//        File directory = new File(path);
//        File[] files = directory.listFiles();
//        List<File> collectedFiles = new ArrayList<>();
//        for(File file : files) {
//            if(file.isDirectory()) {
//                collectedFiles.addAll(getAllModelFiles(file.getPath())); }
//            else {
//                if(isModelFile(file)) {
//                    collectedFiles.add(file);
//                }
//            }
//        }
//        return collectedFiles;
//    }
//
//    private static boolean isModelFile(File file) {
//        try {
//            String content = new String(Files.readAllBytes(file.toPath()));
//            Pattern pattern = Pattern.compile("@Def");
//            Matcher matcher = pattern.matcher(content);
//
//            if(isUsefulFile(file)) {
//                modelFileContents.add(content); // used later to search for missing items in other files
//            }
//            return matcher.find();
//        } catch (IOException e) {
//            // TODO: this should get a better error resolver, but for now it's fine
//            return false;
//        }
//    }
//    private static boolean isUsefulFile(File file) {
//        String path = file.getPath();
//        if(path.endsWith(".spec.ts")) { return false; } // ignore test files
//        if(path.endsWith("module.ts")) { return false; } // ignore the module file
//        if(path.endsWith("index.ts")) { return false; } // ignore the indexing files
//        return path.endsWith(".ts"); // ignore all non .ts files
//    }
//
//    /**
//     * Parse the contents of a model item file into an OMTModelItem
//     * @param content
//     * @return
//     */
//    public static OMTModelItem parseModelItem(String content) {
//        // first pattern will parse the OTM model into 2 blocks, one that contains the annotation
//        // the second that contains the exporting class
//        // i.e. @Def and export class BindingDef
//        Pattern fileParser = Pattern.compile("@Def\\s*\\(([^)]*)\\)\\s*(export(?s)(.)*)(export|class)");
//        Matcher matcher = fileParser.matcher(content);
//
//        if(!matcher.find()) { return null; } // something is wrong. Probably need to adjust the regEx to a change in the model markup
//        String def =  matcher.group(1);
//        String block =  matcher.group(2);
//
//        // the second pattern will extract all attributes that are registered with the model item
//        // it should be able to do this on the original content but to be safe let's do this after parsing
//        // the Def block
//        Pattern attributeParser = Pattern.compile("@Attribute([^;]*)");
//        matcher = attributeParser.matcher(block);
//
//        List<String> attributesToParse = new ArrayList<>();
//        while(matcher.find()) {
//            attributesToParse.add(matcher.group(1));
//        }
//        return new OMTModelItem(def, attributesToParse);
//    }
//
//    public static String getAnnotationValue(String input, String annotationKey) {
//        return getAnnotationField(input, String.format("%s:\\s*([^,]*)", annotationKey));
//    }
//    public static String getAnnotationBlock(String input, String annotationKey) {
//        return getAnnotationField(input, String.format("%s:\\s*([^}]*})", annotationKey));
//    }
//    private static String getAnnotationField(String input, String regEx) {
//        if(input == null || input.length() == 0) { return null; }
//        Pattern pattern = Pattern.compile(regEx);
//        Matcher matcher = pattern.matcher(input);
//        if(matcher.find()) {
//            return matcher.group(1).replaceAll("\'", "");
//        } else {
//            return null;
//        }
//    }
//    public static String getAnnotationValueOrSingle(String input, String annotationKey) {
//        if(input == null || input.length() == 0) { return null; }
//        if(input.startsWith("{") && input.endsWith("}")) {
//            return getAnnotationValue(input, annotationKey);
//        } else {
//            return input.replaceAll("\'", "");
//        }
//    }
//}