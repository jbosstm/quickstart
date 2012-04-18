package org.jboss.narayana.blacktie.quickstart.ejb.eg1;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateless
public class BTBean {

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void txMandatory() {
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void txNever() {
    }
}
