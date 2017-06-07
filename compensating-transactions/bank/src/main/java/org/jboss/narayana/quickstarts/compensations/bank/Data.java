package org.jboss.narayana.quickstarts.compensations.bank;

import org.jboss.narayana.compensations.api.CompensationScoped;

import java.io.Serializable;

@CompensationScoped
public class Data implements Serializable {

    private String account;
    private double amount;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
