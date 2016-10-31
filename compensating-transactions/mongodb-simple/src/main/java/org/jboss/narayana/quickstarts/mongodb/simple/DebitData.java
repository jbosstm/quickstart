/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
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
