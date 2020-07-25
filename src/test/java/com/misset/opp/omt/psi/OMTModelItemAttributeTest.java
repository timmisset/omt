package com.misset.opp.omt.psi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OMTModelItemAttributeTest {

    @Test
    void title() {
        String attribute = "(AttributeType.InterpolatedString)\n" +
                "    title?: QueryNodeRef;";

        OMTModelItemAttribute modelItemAttribute = new OMTModelItemAttribute(attribute);

        assertEquals("title", modelItemAttribute.getName());
        assertEquals("AttributeType.InterpolatedString", modelItemAttribute.getType());
        assertFalse(modelItemAttribute.isRequired());
        assertNull(modelItemAttribute.getDefaultValue());
        assertFalse(modelItemAttribute.hasStructureDef());
        assertFalse(modelItemAttribute.hasElementDef());
    }

    @Test
    void autonomous() {
        String attribute = "(AttributeType.Boolean)\n" +
                "    autonomous = false;";
        OMTModelItemAttribute modelItemAttribute = new OMTModelItemAttribute(attribute);

        assertEquals("autonomous", modelItemAttribute.getName());
        assertEquals("AttributeType.Boolean", modelItemAttribute.getType());
        assertFalse(modelItemAttribute.isRequired());
        assertEquals("false", modelItemAttribute.getDefaultValue());
        assertFalse(modelItemAttribute.hasStructureDef());
        assertFalse(modelItemAttribute.hasElementDef());
    }

    @Test
    void graphs() {
        String attribute = "({  \n" +
                "        type: AttributeType.Structure,\n" +
                "        structureDef: GraphSelectionDef,\n" +
                "    })\n" +
                "    graphs?: GraphSelectionDef;";
        OMTModelItemAttribute modelItemAttribute = new OMTModelItemAttribute(attribute);

        assertEquals("graphs", modelItemAttribute.getName());
        assertEquals("AttributeType.Structure", modelItemAttribute.getType());
        assertFalse(modelItemAttribute.isRequired());
        assertTrue(modelItemAttribute.hasStructureDef());
        assertFalse(modelItemAttribute.hasElementDef());
    }
    @Test
    void queryWatchers() {
        String attribute = "(queryWatchersDef)\n" +
                "    watchers?: QueryWatcherDef[];";
        OMTModelItemAttribute modelItemAttribute = new OMTModelItemAttribute(attribute);

        assertEquals("watchers", modelItemAttribute.getName());
        assertEquals("queryWatchersDef", modelItemAttribute.getType());
        assertFalse(modelItemAttribute.isRequired());
        assertFalse(modelItemAttribute.hasStructureDef());
        assertFalse(modelItemAttribute.hasElementDef());
    }
    @Test
    void payload() {
        String attribute = "(payloadAttributeDef)\n" +
                "    payload: PayloadDef = {};";
        OMTModelItemAttribute modelItemAttribute = new OMTModelItemAttribute(attribute);

        assertEquals("payload", modelItemAttribute.getName());
        assertEquals("payloadAttributeDef", modelItemAttribute.getType());
        assertEquals("{}", modelItemAttribute.getDefaultValue());
        assertFalse(modelItemAttribute.isRequired());
        assertFalse(modelItemAttribute.hasStructureDef());
        assertFalse(modelItemAttribute.hasElementDef());
    }
    @Test
    void prefixes() {
        String attribute = "(AttributeType.Prefixes)\n" +
                "    prefixes?: PrefixesRef;";
        OMTModelItemAttribute modelItemAttribute = new OMTModelItemAttribute(attribute);

        assertEquals("prefixes", modelItemAttribute.getName());
        assertEquals("AttributeType.Prefixes", modelItemAttribute.getType());
        assertFalse(modelItemAttribute.isRequired());
        assertFalse(modelItemAttribute.hasStructureDef());
        assertFalse(modelItemAttribute.hasElementDef());
    }
    @Test
    void actions() {
        String attribute = "({\n" +
                "        type: AttributeType.Mapping,\n" +
                "        elementDef: {\n" +
                "            type: AttributeType.Structure,\n" +
                "            structureDef: ActionDef,\n" +
                "        },\n" +
                "    })\n" +
                "    actions: Record<string, ActionDef> = {};";
        OMTModelItemAttribute modelItemAttribute = new OMTModelItemAttribute(attribute);

        assertEquals("actions", modelItemAttribute.getName());
        assertEquals("AttributeType.Mapping", modelItemAttribute.getType());
        assertFalse(modelItemAttribute.isRequired());
        assertFalse(modelItemAttribute.hasStructureDef());
        assertTrue(modelItemAttribute.hasElementDef());
    }
}
