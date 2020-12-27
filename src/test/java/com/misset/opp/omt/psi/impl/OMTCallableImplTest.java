package com.misset.opp.omt.psi.impl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.named.OMTCall;
import com.misset.opp.omt.psi.support.BuiltInType;
import com.misset.opp.omt.psi.support.ExportMemberType;
import com.misset.opp.omt.psi.support.OMTCallable;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import java.util.HashMap;

import static com.misset.opp.omt.psi.util.UtilManager.*;
import static org.mockito.Mockito.mock;

class OMTCallableImplTest extends OMTTestSuite {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("OMTCallableImplTest");
        super.setUp();
        setBuiltinAndModel();
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void hasFlags() {
        final OMTBuiltInMember contains = getBuiltinUtil().getBuiltInMember("CONTAINS", BuiltInType.Operator);
        final OMTBuiltInMember first = getBuiltinUtil().getBuiltInMember("FIRST", BuiltInType.Operator);
        assertTrue(contains.hasFlags());
        assertFalse(first.hasFlags());
    }

    @Test
    void getFlags() {
        final OMTBuiltInMember contains = getBuiltinUtil().getBuiltInMember("CONTAINS", BuiltInType.Operator);
        assertContainsElements(contains.getFlags(), "ignoreCase");
    }

    @Test
    void isOperator() {
        final OMTBuiltInMember first = getBuiltinUtil().getBuiltInMember("FIRST", BuiltInType.Operator);
        final OMTBuiltInMember log = getBuiltinUtil().getBuiltInMember("LOG", BuiltInType.Command);
        assertTrue(first.isOperator());
        assertFalse(log.isOperator());
    }

    @Test
    void isCommand() {
        final OMTBuiltInMember first = getBuiltinUtil().getBuiltInMember("FIRST", BuiltInType.Operator);
        final OMTBuiltInMember log = getBuiltinUtil().getBuiltInMember("LOG", BuiltInType.Command);
        assertFalse(first.isCommand());
        assertTrue(log.isCommand());
    }

    @Test
    void getMinExpected() {
        final OMTBuiltInMember first = getBuiltinUtil().getBuiltInMember("FIRST", BuiltInType.Operator);
        final OMTBuiltInMember log = getBuiltinUtil().getBuiltInMember("LOG", BuiltInType.Command);
        assertEquals(0, first.getMinExpected());
        assertEquals(1, log.getMinExpected());
    }

    @Test
    void getMaxExpected() {
        final OMTBuiltInMember first = getBuiltinUtil().getBuiltInMember("FIRST", BuiltInType.Operator);
        final OMTBuiltInMember log = getBuiltinUtil().getBuiltInMember("LOG", BuiltInType.Command);
        assertEquals(1, first.getMaxExpected());
        assertEquals(-1, log.getMaxExpected());
    }

    @Test
    void htmlDescription() {
        final OMTCallableImpl callable = mock(OMTCallableImpl.class, InvocationOnMock::callRealMethod);
        callable.setHTMLDescription("test");
        assertEquals("test", callable.htmlDescription());

        final OMTBuiltInMember first = getBuiltinUtil().getBuiltInMember("FIRST", BuiltInType.Operator);
        assertEquals("<b>FIRST</b><br>Type: Operator<br><br>Params:<br>$param0 (optionalBoolean)", first.htmlDescription().trim());
    }

    @Test
    void shortDescription() {
        final OMTBuiltInMember first = getBuiltinUtil().getBuiltInMember("FIRST", BuiltInType.Operator);
        assertEquals("Operator: FIRST", first.shortDescription());
    }

    @Test
    void asSuggestion() {
        final OMTBuiltInMember newTransientGraph = getBuiltinUtil().getBuiltInMember("NEW_TRANSIENT_GRAPH", BuiltInType.Command);
        final OMTBuiltInMember newGraph = getBuiltinUtil().getBuiltInMember("NEW_GRAPH", BuiltInType.Command);
        final OMTBuiltInMember first = getBuiltinUtil().getBuiltInMember("FIRST", BuiltInType.Operator);
        final OMTBuiltInMember currentDate = getBuiltinUtil().getBuiltInMember("CURRENT_DATE", BuiltInType.Operator);
        assertEquals("@NEW_TRANSIENT_GRAPH()", newTransientGraph.getAsSuggestion());
        assertEquals("@NEW_GRAPH($param0)", newGraph.getAsSuggestion());
        assertEquals("FIRST($param0)", first.getAsSuggestion());
        assertEquals("CURRENT_DATE", currentDate.getAsSuggestion());
    }

    @Test
    void getReturnType() {
        final OMTBuiltInMember first = getBuiltinUtil().getBuiltInMember("FIRST", BuiltInType.Operator);
        final OMTBuiltInMember exists = getBuiltinUtil().getBuiltInMember("EXISTS", BuiltInType.Operator);
        assertEquals(first.getReturnType().get(0), getRDFModelUtil().getResource("http://www.w3.org/2001/XMLSchema#any"));
        assertEquals(exists.getReturnType().get(0), getRDFModelUtil().getResource("http://www.w3.org/2001/XMLSchema#boolean"));
    }

    @Test
    void returnsAny() {
        final OMTBuiltInMember first = getBuiltinUtil().getBuiltInMember("FIRST", BuiltInType.Operator);
        final OMTBuiltInMember exists = getBuiltinUtil().getBuiltInMember("EXISTS", BuiltInType.Operator);
        assertTrue(first.returnsAny());
        assertFalse(exists.returnsAny());
    }

    @Test
    void getCallableType() {
        String content = "model:\n" +
                "    activiteit: !Activity\n" +
                "\n";
        final PsiFile psiFile = myFixture.configureByText("test.omt", content);
        ApplicationManager.getApplication().runReadAction(() -> {
            final OMTModelItemBlock modelItemBlock = PsiTreeUtil.findChildOfType(psiFile, OMTModelItemBlock.class);
            assertEquals("Activity", new OMTExportMemberImpl(modelItemBlock, ExportMemberType.Activity).getCallableType());
        });
    }

    @Test
    void getCallArgumentTypesFromModelItem() {
        String content = "prefixes:\n" +
                "        ont:     <http://ontologie#>\n" +
                "\n" +
                "model:\n" +
                "    activiteit: !Activity\n" +
                "        onStart: |\n" +
                "            @activiteit2('test', 'test');\n" +
                "    activiteit2: !Activity\n" +
                "        params:\n" +
                "            -   $inputParam1 (ont:ClassA)\n" +
                "            -   $inputParam2 (string)\n" +
                "\n";

        OMTCall call = getCallFromContent(content);
        ApplicationManager.getApplication().runReadAction(() -> {
            final OMTCallable callable = getMemberUtil().getCallable(call);
            final HashMap<String, Resource> callArgumentTypes = callable.getCallArgumentTypes();
            assertTrue(callArgumentTypes.containsKey("$inputParam1"));
            assertTrue(callArgumentTypes.containsKey("$inputParam2"));
            assertEquals(callArgumentTypes.get("$inputParam1"), getRDFModelUtil().getResource("http://ontologie#ClassA"));
            assertEquals(callArgumentTypes.get("$inputParam2"), getRDFModelUtil().getResource("http://www.w3.org/2001/XMLSchema#string"));
        });
    }

    @Test
    void getCallArgumentTypesFromDefined() {
        String content = "prefixes:\n" +
                "        ont:     <http://ontologie#>\n" +
                "\n" +
                "model:\n" +
                "    activiteit: !Activity\n" +
                "        queries: |\n" +
                "            /**\n" +
                "            * @param $inputParam1 (ont:ClassA)\n" +
                "            */\n" +
                "            DEFINE QUERY myQuery($inputParam1) => $inputParam1;\n" +
                "        onStart: |\n" +
                "            myQuery('test');\n" +
                "\n";

        OMTCall call = getCallFromContent(content);
        ApplicationManager.getApplication().runReadAction(() -> {
            final OMTCallable callable = getMemberUtil().getCallable(call);
            final HashMap<String, Resource> callArgumentTypes = callable.getCallArgumentTypes();
            assertTrue(callArgumentTypes.containsKey("$inputParam1"));
            assertEquals(callArgumentTypes.get("$inputParam1"), getRDFModelUtil().getResource("http://ontologie#ClassA"));
        });
    }

    private OMTCall getCallFromContent(String content) {
        final PsiFile psiFile = myFixture.configureByText("test.omt", content);
        return ApplicationManager.getApplication().runReadAction((Computable<OMTCall>) () -> PsiTreeUtil.findChildOfType(psiFile, OMTCall.class));
    }
}
