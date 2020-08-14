package com.misset.opp.omt.psi.named;

import com.intellij.psi.PsiNameIdentifierOwner;
import com.misset.opp.omt.psi.support.OMTExportMember;

/**
 * The named element version of Members, which are operators, commands or the calls to them, this is used for the reference provider
 */
public interface OMTMemberNamedElement extends PsiNameIdentifierOwner {
    /**
     * Will parse this member named element to an exported member, which can be used for signature validation
     * expected parameters etc
     *
     * @return
     */
    public OMTExportMember asExportedMember();
}
