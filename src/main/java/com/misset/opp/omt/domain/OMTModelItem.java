//package com.misset.opp.omt.domain;
//
//import com.misset.opp.omt.domain.util.ModelUtil;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//public class OMTModelItem {
//
//    private String type;
//    // stored as key and type
//    private HashMap<String, String> attributes = new HashMap<>();
//
//    public OMTModelItem(String def, List<String> attributes) {
//        parseDef(def);
//        attributes.forEach(this::parseAttribute);
//    }
//    private void parseDef(String def) {
//        // parse the Def block into a type
//        // input can be (TYPE) or ({ type: TYPE, ...other Properties })
//        type = ModelUtil.getAnnotationValueOrSingle(def, "type");
//    }
//    private void parseAttribute(String attribute) {
//        OMTModelItemAttribute modelItemAttribute = new OMTModelItemAttribute(attribute);
//        attributes.put(modelItemAttribute.getName(),
//                ModelUtil.getOrSetModelItemAttributeDefinition(modelItemAttribute).getType());
//    }
//    public String getType() { return type; }
//
//    public int numberOfAttributes() { return attributes.size(); }
//    public boolean hasAttribute(String name) { return attributes.containsKey(name); }
//    public String getAttributeType(String name) { return attributes.get(name); }
//
//    /**
//     * Will return a list with missing attribute definitions
//     * used to autocomplete the model by looking into the model files for specific definitions
//     * @return
//     */
//    public List<String> validate() {
//        List<String> missingAttributes = new ArrayList<>();
//        for(String modelItemAttributeType : attributes.values()) {
//            if(!modelItemAttributeType.startsWith("AttributeType.") &&
//                    !modelItemAttributeType.startsWith("[") && // TODO: fix voor array types
//                    !ModelUtil.hasModelItemDefinition(modelItemAttributeType) &&
//                    !ModelUtil.hasModelItemAttributeDefinition(modelItemAttributeType)) {
//                missingAttributes.add(modelItemAttributeType);
//            }
//        }
//        return missingAttributes;
//    }
//}
