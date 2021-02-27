package com.misset.opp.omt.psi.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.misset.opp.omt.OMTTestSuite;

public class ReferenceTest extends OMTTestSuite {

    private Class<? extends PsiElement> elementAtCaretClass;

    /**
     * @param elementAtCaretClass Will get the parent (or self) of class for the element at caret position
     * @throws Exception
     */
    protected void setUp(Class<? extends PsiElement> elementAtCaretClass) throws Exception {
        super.setUp();
        this.elementAtCaretClass = elementAtCaretClass;
    }

    protected void assertHasReference(String content) {
        assertReference(content, true);
    }

    protected void assertHasNoReference(String content) {

        assertReference(content, false);
    }

    private void assertReference(String content, boolean expectToHaveReference) {
        getElementAtCaret(content, element ->
                assertEquals(expectToHaveReference, element.getReference() != null && element.getReference().resolve() != null), elementAtCaretClass, true);
    }

    protected void assertHasUsages(String content, int usages) {
        assertHasUsages(getFileName(), content, usages);
    }

    protected void assertHasUsages(String filename, String content, int usages) {
        getElementAtCaret(filename, content, element ->
                withProgress(() -> assertEquals(usages,
                        ReferencesSearch
                                .search(element)
                                .findAll()
                                .size())), elementAtCaretClass, false);
    }

}
