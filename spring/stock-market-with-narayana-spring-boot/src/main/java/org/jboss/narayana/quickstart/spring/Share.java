package org.jboss.narayana.quickstart.spring;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Entity
public class Share {

    /**
     * Unique share identifier.
     */
    @Id
    private String symbol;

    /**
     * Amount of shares available for purchase.
     */
    private int amount;

    /**
     * Current share price.
     */
    private int price;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("Share{symbol='%s', amount=%d, price=%d}", symbol, amount, price);
    }

}