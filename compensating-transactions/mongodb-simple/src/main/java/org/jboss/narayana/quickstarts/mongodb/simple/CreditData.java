package org.jboss.narayana.quickstarts.mongodb.simple;

import org.jboss.narayana.compensations.api.CompensationScoped;

import java.io.Serializable;

/**
 * This is a CompensationScoped POJO that is used to store data against the current running compensating transaction.
 *
 * This scope is also available to the compensation handlers.
 *
 * @author paul.robinson@redhat.com 09/01/2014
 */
@CompensationScoped
public class CreditData implements Serializable {

    private String toAccount;
    private Double amount;

    public CreditData() {}

    public void setToAccount(String toAccount) {

        this.toAccount = toAccount;
    }

    public void setAmount(Double amount) {

        this.amount = amount;
    }

    public String getToAccount() {

        return toAccount;
    }

    public Double getAmount() {

        return amount;
    }
}