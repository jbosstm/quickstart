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
public class DebitData implements Serializable {

    private String fromAccount;
    private Double amount;

    public DebitData() {}

    public void setFromAccount(String fromAccount) {

        this.fromAccount = fromAccount;
    }

    public void setAmount(Double amount) {

        this.amount = amount;
    }

    public String getFromAccount() {

        return fromAccount;
    }

    public Double getAmount() {

        return amount;
    }
}