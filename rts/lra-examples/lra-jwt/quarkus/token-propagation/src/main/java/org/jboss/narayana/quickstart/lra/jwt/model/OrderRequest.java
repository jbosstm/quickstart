/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.quickstart.lra.jwt.model;

/**
 * The unit of work exchanged between the order, inventory and payment services.
 */
public class OrderRequest {

    private String orderId;
    private String itemId;
    private int quantity;
    private double amount;

    public OrderRequest() {
    }

    public OrderRequest(String orderId, String itemId, int quantity, double amount) {
        this.orderId = orderId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "OrderRequest{orderId='" + orderId + "', itemId='" + itemId
                + "', quantity=" + quantity + ", amount=" + amount + '}';
    }
}
