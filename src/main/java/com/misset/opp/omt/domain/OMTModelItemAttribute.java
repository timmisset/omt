//package com.misset.opp.omt.domain;
//
//import com.misset.opp.omt.domain.util.ModelUtil;
//
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class OMTModelItemAttribute {
//
//    private boolean required;
//    private String type;
//    private String name;
//    private String defaultValue;
//    private String structureDef;
//    private OMTModelItemAttribute elementDef;
//    private String additionalParameters;
//
//    /**
//     * Already parsed annotation block retrieved from separate imports
//     * Retrieves information from exporting class
//     * @param name
//     * @param exportingClass
//     */
//    public OMTModelItemAttribute(String name, String exportingClass) {
//        this.name = name;
//        this.type = ModelUtil.getAnnotationValue(exportingClass, "type");
//        this.additionalParameters = ModelUtil.getAnnotationValue(exportingClass, "variables");
//        String elementDef = ModelUtil.getAnnotationBlock(exportingClass, "elementDef");
//        if(elementDef != null) { this.elementDef = new OMTModelItemAttribute(exportingClass); }
//    }
//
//    /**
//     * Default annotated attributes part of model items, will get the information from the @Attribute annotation
//     * @param attribute
//     */
//    public OMTModelItemAttribute(String attribute) {
//        // TODO: in some cases the attributes are assigned using the shorthand of Object.assign instead of type
//        // for now, this workaround but should be reconsidered
//        attribute = attribute.replace("...", "type: ");
//
//        Pattern annotationPattern = Pattern.compile("[^{]([^\\)]*)\\)"); // retrieves the annotation part of the attribute
//        Matcher matcher = annotationPattern.matcher(attribute);
//        if(!matcher.find()) { return; }
//        String annotation = matcher.group(1);
//
//        type = ModelUtil.getAnnotationValueOrSingle(annotation, "type");
//        if(type.equals("AttributeType.Structure")) {
//            structureDef = ModelUtil.getAnnotationValue(annotation, "structureDef");
//        }
//        else if(type.equals("AttributeType.Mapping")) {
//            String element = ModelUtil.getAnnotationBlock(annotation, "elementDef");
//            if(element != null) { elementDef = new OMTModelItemAttribute(element); }
//        }
//        required = "true".equals(ModelUtil.getAnnotationValue(annotation, "required"));
//
//        // secondly, the name (and optional default value) of the attribute
//        Pattern nameValuePattern = Pattern.compile("\\)\\s*([^!?:= ]*)[^=]*=?\\s*([^;]*)"); // get the name and potentially the default value
//        matcher = nameValuePattern.matcher(attribute);
//        if(matcher.find()) {
//            this.name = matcher.group(1);
//            this.defaultValue = matcher.groupCount() >= 2 && matcher.group(2).length() > 0 ? matcher.group(2) : null;
//        }
//    }
//
//    public String getType() { return type; }
//    public boolean isRequired() { return required; }
//    public String getName() { return name; }
//    public String getDefaultValue() { return defaultValue; }
//    public boolean hasStructureDef() { return structureDef != null; }
//    public boolean hasElementDef() { return elementDef != null; }
//}
