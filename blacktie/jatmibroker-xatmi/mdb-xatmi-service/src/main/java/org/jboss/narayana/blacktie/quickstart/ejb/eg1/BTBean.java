package org.jboss.narayana.blacktie.quickstart.ejb.eg1;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateless
public class BTBean {

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void txRequired() {
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public void txSupports() {
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void txMandatory() {
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void txNever() {
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void txRequiresNew() {
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void txNotSupported() {
    }
}
