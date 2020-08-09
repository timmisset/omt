//package com.misset.opp.omt.psi;
//
//import com.misset.opp.omt.domain.OMTModelItem;
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class OMTModelItemTest {
//
//    @Test
//    void parseSimpleType() {
//        String type = "Component";
//        OMTModelItem omtModelItem = new OMTModelItem(type, new ArrayList<>());
//        assertEquals("Component", omtModelItem.getType());
//    }
//
//    @Test
//    void parseComplexType() {
//        String type = "{\n" +
//                "    type: 'Binding',\n" +
//                "    yaml: { stringShortcut },\n" +
//                "}";
//        OMTModelItem omtModelItem = new OMTModelItem(type, new ArrayList<>());
//        assertEquals("Binding", omtModelItem.getType());
//    }
//    @Test
//    void parseComplexTypeOtherOrder() {
//        String type = "{\n" +
//                "    yaml: { stringShortcut },\n" +
//                "    type: 'Binding',\n" +
//                "}";
//        OMTModelItem omtModelItem = new OMTModelItem(type, new ArrayList<>());
//        assertEquals("Binding", omtModelItem.getType());
//    }
//    @Test
//    void parseComplexType_Unknown() {
//        String type = "{\n" +
//                "    yaml: { stringShortcut },\n" +
//                "    typo: 'Binding',\n" +
//                "}";
//        OMTModelItem omtModelItem = new OMTModelItem(type, new ArrayList<>());
//        assertEquals(null, omtModelItem.getType());
//    }
//}
