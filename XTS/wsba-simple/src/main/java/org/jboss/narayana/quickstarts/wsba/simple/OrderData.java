package org.jboss.narayana.quickstarts.wsba.simple;

import org.jboss.narayana.compensations.api.CompensationScoped;

import java.io.Serializable;

/**
 * @author paul.robinson@redhat.com 02/08/2013
 */
@CompensationScoped
public class OrderData implements Serializable {

    private String emailAddress;
    private String orderId;
    private String item;

    public String getEmailAddress() {

        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {

        this.emailAddress = emailAddress;
    }

    public String getOrderId() {

        return orderId;
    }

    public void setOrderId(String orderId) {

        this.orderId = orderId;
    }

    public String getItem() {

        return item;
    }

    public void setItem(String item) {

        this.item = item;
    }
}
